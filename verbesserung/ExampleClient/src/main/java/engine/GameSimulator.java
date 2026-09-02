package engine;

import java.util.logging.Level;
import java.util.logging.Logger;
import logic.GameHelper;
import logic.IStrategy;
import logic.StrategyAlwaysClosest;
import logic.StrategyPlannedTour;
import map.ClientMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;
import view.ConsoleView;
import view.SwingView;

/*
TODO:
1) Add information about who Lost/Won
2) implement MustWait state logic
 */

public class GameSimulator {

  private static final Logger LOGGER = Logger.getLogger(GameSimulator.class.getName());

  public static void main(String[] args) {
    LOGGER.info("Game simulation started...");
    // singlePlayer(args);
    multiPlayer(args);

    LOGGER.info("Game simulation ended.");
  }

  public static void singlePlayer(String[] args) {
    FakeEngine engine = new FakeEngine();

    String playerId_1 = "player_1";
    String playerId_2 = "player_2";

    // IStrategy strategy_1 = new StrategyNearestNeighbour();
    // IStrategy strategy_1 = new StrategyAlwaysClosest();
    IStrategy strategy_1 = new StrategyPlannedTour();

    ClientMap mapGenerator_1 = new ClientMap(playerId_1);
    PlayerHalfMap halfMapData_1 = mapGenerator_1.generate();
    engine.registerPlayer(playerId_1, halfMapData_1);

    ClientMap mapGenerator_2 = new ClientMap(playerId_2);
    PlayerHalfMap halfMapData_2 = mapGenerator_2.generate();
    engine.registerPlayer(playerId_2, halfMapData_2);

    GameHelper helper = new GameHelper(new UniquePlayerIdentifier(playerId_1));
    ConsoleView view = new ConsoleView();

    while (true) {
      GameState state = engine.getState(playerId_1);

      helper.update(state);
      view.render(helper);

      if (engine.isFinished()) {
        break;
      }

      PlayerMove move = strategy_1.calculateNextMove(helper);
      engine.applyMove(move);
    }
  }

  public static void multiPlayer(String[] args) {
    FakeEngine engine = new FakeEngine();

    String playerId_1 = "player_1";
    String playerId_2 = "player_2";

    // IStrategy strategy_1 = new StrategyAlwaysClosest();
    // IStrategy strategy_1 = new StrategyNearestNeighbour();
    IStrategy strategy_1 = new StrategyPlannedTour();
    IStrategy strategy_2 = new StrategyAlwaysClosest();
    Logger.getLogger(strategy_1.getClass().getName()).setLevel(Level.FINE);
    Logger.getLogger(strategy_2.getClass().getName()).setLevel(Level.OFF);
    ClientMap mapGenerator_1 = new ClientMap(playerId_1);
    PlayerHalfMap halfMapData_1 = mapGenerator_1.generate();
    engine.registerPlayer(playerId_1, halfMapData_1);

    ClientMap mapGenerator_2 = new ClientMap(playerId_2);
    PlayerHalfMap halfMapData_2 = mapGenerator_2.generate();
    engine.registerPlayer(playerId_2, halfMapData_2);

    GameHelper helper_1 = new GameHelper(new UniquePlayerIdentifier(playerId_1));
    GameHelper helper_2 = new GameHelper(new UniquePlayerIdentifier(playerId_2));
    // ConsoleView view = new ConsoleView();
    SwingView view = new SwingView();

    while (true) {
      GameState state_1 = engine.getState(playerId_1);
      GameState state_2 = engine.getState(playerId_2);

      helper_1.update(state_1);
      helper_2.update(state_2);
      view.render(helper_1);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
      if (engine.isFinished()) {
        break;
      }

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);
      engine.applyMove(move_1);
      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);
      engine.applyMove(move_2);
    }

    if (engine.playerHasWon(playerId_1)) {
      LOGGER.info("Player 1 WON!");
      view.printGameResult(true);

    } else if (engine.playerHasWon(playerId_2)) {
      LOGGER.info("Player 2 WON!");
      view.printGameResult(false);
    } else {
      LOGGER.warning("Winner is NULL.");
    }
  }
}
