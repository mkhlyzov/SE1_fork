package clientcore;

import java.util.Set;
import java.util.logging.Logger;

import logic.GameHelper;
import logic.IStrategy;
import logic.StrategyPlannedTour;
import map.ClientMap;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import network.ClientNetwork;
import network.INetwork;
import view.ConsoleView;

public class ClientMain {

    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private INetwork net;
    private IStrategy strategy;

    public ClientMain(INetwork network) {
        this.net = network;
        // this.strategy = new StrategyNearestNeighbour();
        this.strategy = new StrategyPlannedTour();
        // this.strategy = new StrategyAlwaysClosest();
        // this.strategy = new StrategyManual();

    }

    public void startGame(String studentId) {
        // ✅ Registrierung
        net.registerPlayer(studentId);

        if (net.getPlayerId() == null) {
            LOGGER.severe("Registrierung fehlgeschlagen, Spiel kann nicht gestartet werden.");
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
                        LOGGER.info("Spieler-ID: " + myPlayerId + "; Aktueller Status vom Server: " + status);

                        if (status.equals("MustAct")) {
                            LOGGER.warning("Ich bin schon in der Move-Phase!");
                            canSendMap = true; // trotzdem senden, falls noch nicht gesendet
                        } else if (status.equals("Won") || status.equals("Lost")) {
                            LOGGER.info("Spiel wurde bereits beendet mit Status: " + status);
                            return;
                        }
                        break;
                    }
                }
            }

            if (canSendMap && !mapSent) {
                LOGGER.info("Sende HalfMap jetzt an den Server...");
                PlayerHalfMap halfMap = mapGen.generate();
                net.sendHalfMap(halfMap);
                LOGGER.fine("HalfMap wurde an sendHalfMap() übergeben.");
                mapSent = true;
            }

            LOGGER.fine("Warte auf meinen Zug zum Senden der HalfMap...");
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

            if (state != null) {
                for (PlayerState ps : state.getPlayers()) {
                    String myPlayerId = net.getPlayerId().getUniquePlayerID();
                    if (ps.getUniquePlayerID().equals(myPlayerId)) {
                        switch (ps.getState()) {
                            case MustAct -> myTurnToMove = true;
                            // case MustWait -> myTurnToMove = false;
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
            // System.out.println("The value of variable myTurnTomove = " + myTurnToMove);
            if (myTurnToMove) {
                gameHelper.update(state);
                view.render(gameHelper); // 🗺️ Konsolenkarte ausgeben
                long t0 = System.nanoTime();
                PlayerMove move = strategy.calculateNextMove(gameHelper);
                long t1 = System.nanoTime();
                double dt1_0 = (t1 - t0) / 1000000;
                LOGGER.fine("Execution time of function calculateNextMove takes in ms = " + dt1_0);
                try {
                    net.sendMove(move);
                    long t2 = System.nanoTime();
                    double dt2_1 = (t2 - t1) / 1000000;
                    LOGGER.fine("Execution time of function sendMove takes in ms = " + dt2_1);
                } catch (Exception e) {
                    long t2 = System.nanoTime();
                    double dt2_1 = (t2 - t1) / 1000000;
                    LOGGER.fine("Execution time of function sendMove takes in ms = " + dt2_1);
                    throw e;
                }
            } else {
                LOGGER.fine("Warte auf meinen Zug...");
            }

        }
    }

    public static void main(String[] args) {
        INetwork network;
        String studentId = "kostarievd00"; // 🧑‍🎓 Deinen u:account hier einsetzen

        if (args.length < 3) {
            LOGGER.severe("Missing arguments. Required: [mode] [serverURL] [gameId]");
            return;
        } else {
            String gamemode = args[0];
            String serverURL = args[1];
            String gameId = args[2];
            network = new ClientNetwork(serverURL, gameId);
        }
        ClientMain main = new ClientMain(network);
        main.startGame(studentId);
    }
}
