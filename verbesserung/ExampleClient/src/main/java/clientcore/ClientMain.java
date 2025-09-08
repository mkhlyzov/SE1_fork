package clientcore;

import java.util.Set;

import logic.GameHelper;
import logic.IStrategy;
import logic.StrategyNearestNeighbour;
import map.ClientMap;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import network.ClientNetwork;
import network.FakeNetwork;
import network.INetwork;
import view.ConsoleView;

public class ClientMain {
    private INetwork net;
    private IStrategy strategy;

    public ClientMain(INetwork network) {
        this.net = network;
        this.strategy = new StrategyNearestNeighbour();
        // StrategyAlwaysClosest
        // StrategyTravelingSalesman
        // StrategyBruteForce
    }

    public void startGame(String studentId) {
        // ✅ Registrierung
        net.registerPlayer(studentId);

        if (net.getPlayerId() == null) {
            System.err.println("❌ Registrierung fehlgeschlagen, Spiel kann nicht gestartet werden.");
            return;
        }

        String myPlayerId = net.getPlayerId().getUniquePlayerID();

        ClientMap mapGen = new ClientMap(myPlayerId);
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

                        if (status.equals("MustAct")) {
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
            

            if (mapSent) {
                break;
            }
        }

        // 🔁 Danach: Move-Phase starten
        startMovePhase();
    }

    public void startMovePhase() {
        ConsoleView view = new ConsoleView();
        GameHelper gameHelper = new GameHelper(net.getPlayerId());
    
        while (true) {
            GameState state = net.getGameState();
            boolean myTurnToMove = false;
            gameHelper.update(state);
            view.render(gameHelper);  // 🗺️ Konsolenkarte ausgeben

            if (state != null) {
                for (PlayerState ps : state.getPlayers()) {
                    String myPlayerId = net.getPlayerId().getUniquePlayerID();
                    if (ps.getUniquePlayerID().equals(myPlayerId)) {
                        switch (ps.getState()) {
                            case MustAct -> myTurnToMove = true;
                            //case MustWait -> myTurnToMove = false;
                            case Won -> {
                                view.printGameResult(true);
                                return;
                            }
                            case Lost -> {
                                view.printGameResult(false);
                                return;
                            }
                        }
                        break;
                    }
                }
            }
            //System.out.println("The value of variable myTurnTomove = " + myTurnToMove);
            if (myTurnToMove) {
                PlayerMove move = strategy.calculateNextMove(gameHelper);
                net.sendMove(move);
            } else {
                System.out.println("⏳ Warte auf meinen Zug...");
            }
    
           
        }
    }
    

    public static void main(String[] args) {
        INetwork network;
        String studentId = "kostarievd00"; // 🧑‍🎓 Deinen u:account hier einsetzen


        if (args.length < 3) {
            System.err.println("❗ Missing arguments. Required: [mode] [serverURL] [gameId]");
            // return;
            if (args.length > 0) {
                network = new FakeNetwork(Integer.parseInt(args[0]));
            } else {
                network = new FakeNetwork(0);
            }
        }
        else{
            String gamemode = args[0];
            String serverURL = args[1];
            String gameId = args[2];
            network = new ClientNetwork(serverURL,gameId);
        }
        ClientMain main = new ClientMain(network);
        main.startGame(studentId);
    }
}