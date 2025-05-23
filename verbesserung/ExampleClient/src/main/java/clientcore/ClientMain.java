package clientcore;

import java.util.Set;

import logic.MoveStrategy;
import map.ClientMap;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import network.clientNetwork;

public class ClientMain {

    public clientNetwork net;
    public ClientMap mapGen;

    public void startGame(String studentId) {
        // ✅ Registrierung
        net.registerPlayer(studentId);

        if (net.getPlayerId() == null) {
            System.err.println("❌ Registrierung fehlgeschlagen, Spiel kann nicht gestartet werden.");
            return;
        }

        String myPlayerId = net.getPlayerId().getUniquePlayerID();
        mapGen = new ClientMap(myPlayerId);

        boolean mapSent = false;

        // 🔄 Warten auf Erlaubnis zur HalfMap-Übertragung oder Move-Phase
        while (true) {
            GameState state = net.getGameState();
            boolean canSendMap = false;

            if (state != null) {
                Set<PlayerState> players = state.getPlayers();
                for (PlayerState ps : players) {
                    if (ps.getUniquePlayerID().equals(myPlayerId)) {
                        String status = ps.getState().name();
                        System.out.println("🧍 Spieler-ID: " + myPlayerId);
                        System.out.println("📡 Aktueller Status vom Server: " + status);

                        if (status.equals("MustProvideMap")) {
                            canSendMap = true;
                        } else if (status.equals("MustAct")) {
                            System.out.println("⚠️ Ich bin schon in der Move-Phase!");
                            canSendMap = true; // trotzdem senden, falls noch nicht gesendet
                        } else if (status.equals("Won") || status.equals("Lost")) {
                            // Kein Fehler ausgeben, falls das Spiel beendet wurde
                            System.out.println("🏁 Spiel wurde bereits beendet mit Status: " + status);
                            return;
                        }
                        break;
                    }
                }
            }

            if (canSendMap && !mapSent) {
                System.out.println("📤 Sende HalfMap jetzt an den Server...");
                PlayerHalfMap halfMap = mapGen.generate();
                net.sendHalfMap(halfMap);
                System.out.println("📨 HalfMap wurde an sendHalfMap() übergeben.");
                mapSent = true;
            }

            

            System.out.println("⏳ Warte auf meinen Zug zum Senden der HalfMap...");
            try {
                Thread.sleep(400); // Sicherstellen, dass mindestens 400ms warten
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (mapSent) {
                break;
            }
        }

        // 🔁 Danach: Move-Phase starten
        startMovePhase(myPlayerId);
    }

    public void startMovePhase(String myPlayerId) {
        MoveStrategy strategy = new MoveStrategy();

        while (true) {
            GameState state = net.getGameState();
            boolean myTurnToMove = false;

            if (state != null) {
                for (PlayerState ps : state.getPlayers()) {
                    if (ps.getUniquePlayerID().equals(myPlayerId)) {
                        if (ps.getState().name().equals("MustAct")) {
                            myTurnToMove = true;
                        } else if (ps.getState().name().equals("Won") || ps.getState().name().equals("Lost")) {
                            // Kein Fehler ausgeben, falls das Spiel beendet wurde
                            System.out.println("🏁 Spiel beendet: " + ps.getState().name());
                            return;
                        }
                        break;
                    }
                }
            }

            if (myTurnToMove) {
                PlayerMove move = strategy.calculateNextMove(state, net.getPlayerId());
                net.sendMove(move);
            } else {
                System.out.println("⏳ Warte auf meinen Zug zum Bewegen...");
            }

            try {
                Thread.sleep(400); // Sicherstellen, dass mindestens 400ms warten
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("❗ Missing arguments. Required: [mode] [serverURL] [gameId]");
            return;
        }

        String serverURL = args[1];
        String gameId = args[2];
        String studentId = "kostarievd00"; // 🧑‍🎓 Deinen u:account hier einsetzen

        ClientMain main = new ClientMain();
        main.net = new clientNetwork(serverURL, gameId);
        main.startGame(studentId);
    }
}