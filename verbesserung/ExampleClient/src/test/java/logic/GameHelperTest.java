package logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import engine.FakeEngine;
import map.ClientMap;
import map.IMapGenerator;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import util.RandomManager;
import view.ConsoleView;

public class GameHelperTest {

  private final int NUM_ROUNDS_HIDDEN = 8;

  @Test
  void mustWaitDoesNotChangePlayerPositionHistory() {

    UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player1");

    GameHelper helper = new GameHelper(playerId, true);

    FullMapNode position_0_0 = buildPPos(0, 0);

    PlayerState playerStateMustAct = buildPlayerState(EPlayerGameState.MustAct);

    GameState state1 = new GameState(new FullMap(List.of(position_0_0)), Set.of(playerStateMustAct), "state1");

    helper.update(state1);

    FullMapNode position_1_0 = buildPPos(1, 0);

    PlayerState playerMustWait = buildPlayerState(EPlayerGameState.MustWait);

    GameState state2 = new GameState(new FullMap(List.of(position_1_0)), Set.of(playerMustWait), "state2");

    helper.update(state2);

    GameState state3 = new GameState(new FullMap(List.of(position_1_0)), Set.of(playerStateMustAct), "state3");

    helper.update(state3);

    assertTrue(helper.playerRecentlyMoved());
  }

  @Test
  void multipleMustWaitDoNotChangePlayerPositionHistory() {

    UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player1");

    GameHelper helper = new GameHelper(playerId, true);

    FullMapNode position_0_0 = buildPPos(0, 0);

    PlayerState playerStateMustAct = buildPlayerState(EPlayerGameState.MustAct);
    PlayerState playerStateMustWait = buildPlayerState(EPlayerGameState.MustWait);

    GameState state1 = new GameState(new FullMap(List.of(position_0_0)), Set.of(playerStateMustAct), "state1");

    helper.update(state1);

    FullMapNode position_1_0 = buildPPos(1, 0);

    GameState state2 = new GameState(new FullMap(List.of(position_1_0)), Set.of(playerStateMustAct), "state2");

    helper.update(state2);

    GameState state3 = new GameState(new FullMap(List.of(position_1_0)), Set.of(playerStateMustWait), "state3");

    helper.update(state3);

    GameState state4 = new GameState(new FullMap(List.of(position_1_0)), Set.of(playerStateMustWait), "state4");

    helper.update(state4);

    assertEquals(2, helper.getPlayerPosHistory().size());
    assertEquals(new Point(0, 0), helper.getPlayerPosHistory().get(0));
    assertEquals(new Point(1, 0), helper.getPlayerPosHistory().get(1));
  }

  @Test
  void sameGameStateIsNotProcessedTwice() {

    UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player1");

    GameHelper helper = new GameHelper(playerId, true);

    FullMapNode position_0_0 = buildPPos(0, 0);

    PlayerState playerStateMustAct = buildPlayerState(EPlayerGameState.MustAct);

    GameState state = new GameState(new FullMap(List.of(position_0_0)), Set.of(playerStateMustAct), "state1");

    helper.update(state);
    helper.update(state);

    assertEquals(1, helper.getPlayerPosHistory().size());
  }

  @Test
  void differentGameStatesAreProcessed() {

    UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player1");

    GameHelper helper = new GameHelper(playerId, true);

    FullMapNode position_0_0 = buildPPos(0, 0);

    PlayerState playerStateMustAct = buildPlayerState(EPlayerGameState.MustAct);

    GameState state1 = new GameState(new FullMap(List.of(position_0_0)), Set.of(playerStateMustAct), "state1");

    GameState state2 = new GameState(new FullMap(List.of(position_0_0)), Set.of(playerStateMustAct), "state2");

    helper.update(state1);
    helper.update(state2);

    assertEquals(2, helper.getPlayerPosHistory().size());
  }

  @RepeatedTest(100)
  void FirstTrueEnemyPosition_v1() {

    FakeEngine engine = new FakeEngine();

    String playerId_1 = "player_1";
    String playerId_2 = "player_2";

    List<Supplier<IStrategy>> strategies = List.of(
        StrategyAlwaysClosest::new,
        // StrategyNearestNeighbour::new,
        StrategyPlannedTour::new);

    IStrategy strategy_1 = RandomManager.chooseRandom(strategies).get();
    IStrategy strategy_2 = RandomManager.chooseRandom(strategies).get();

    IMapGenerator map_1 = new ClientMap();
    PlayerHalfMap halfMapData_1 = map_1.generate("player1");
    engine.registerPlayer(playerId_1, halfMapData_1);

    IMapGenerator map_2 = new ClientMap();
    PlayerHalfMap halfMapData_2 = map_2.generate("player2");
    engine.registerPlayer(playerId_2, halfMapData_2);

    GameHelper helper_1 = new GameHelper(new UniquePlayerIdentifier(playerId_1));

    GameHelper helper_2 = new GameHelper(new UniquePlayerIdentifier(playerId_2));

    Point pos_2_predicted_by_1 = null;

    for (int i = 0; i < NUM_ROUNDS_HIDDEN && !engine.isFinished(); i++) {

      GameState state_1 = engine.getState(playerId_1);
      helper_1.update(state_1);

      pos_2_predicted_by_1 = helper_1._getFirstTrueEnemyPosition_v1();
      assertNull(pos_2_predicted_by_1);

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);
      engine.applyMove(move_1);

      GameState state_2 = engine.getState(playerId_2);
      helper_2.update(state_2);

      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);
      engine.applyMove(move_2);
    }
    helper_1.update(engine.getState(playerId_1));
    helper_2.update(engine.getState(playerId_2));

    pos_2_predicted_by_1 = helper_1._getFirstTrueEnemyPosition_v1();

    FullMapNode pos_2_real = helper_2.getMyPosition();

    assertNotNull(pos_2_predicted_by_1);

    assertTrue(
        pos_2_real.getX() == pos_2_predicted_by_1.x && pos_2_real.getY() == pos_2_predicted_by_1.y);
  }

  @RepeatedTest(100)
  void FirstTrueEnemyPosition_v2() {

    FakeEngine engine = new FakeEngine();

    String playerId_1 = "player_1";
    String playerId_2 = "player_2";

    List<Supplier<IStrategy>> strategies = List.of(
        StrategyAlwaysClosest::new,
        // StrategyNearestNeighbour::new,
        StrategyPlannedTour::new);

    IStrategy strategy_1 = RandomManager.chooseRandom(strategies).get();
    IStrategy strategy_2 = RandomManager.chooseRandom(strategies).get();

    IMapGenerator map_1 = new ClientMap();
    PlayerHalfMap halfMapData_1 = map_1.generate("player1");
    engine.registerPlayer(playerId_1, halfMapData_1);

    IMapGenerator map_2 = new ClientMap();
    PlayerHalfMap halfMapData_2 = map_2.generate("player2");
    engine.registerPlayer(playerId_2, halfMapData_2);

    GameHelper helper_1 = new GameHelper(new UniquePlayerIdentifier(playerId_1));
    GameHelper helper_2 = new GameHelper(new UniquePlayerIdentifier(playerId_2));

    int turnCount = 0;

    for (; turnCount < NUM_ROUNDS_HIDDEN && !engine.isFinished(); turnCount++) {

      GameState state_1 = engine.getState(playerId_1);
      helper_1.update(state_1);

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);

      engine.applyMove(move_1);

      GameState state_2 = engine.getState(playerId_2);

      helper_2.update(state_2);

      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);

      engine.applyMove(move_2);
    }

    helper_1.update(engine.getState(playerId_1));
    helper_2.update(engine.getState(playerId_2));

    FullMapNode pos_1_real = helper_1.getMyPosition();

    FullMapNode pos_2_real = helper_2.getMyPosition();

    for (; turnCount < NUM_ROUNDS_HIDDEN + 10 && !engine.isFinished(); turnCount++) {

      GameState state_1 = engine.getState(playerId_1);
      helper_1.update(state_1);

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);
      engine.applyMove(move_1);

      if (engine.isFinished()) {
        break;
      }
      GameState state_2 = engine.getState(playerId_2);

      helper_2.update(state_2);

      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);

      engine.applyMove(move_2);

    }
    helper_1.update(engine.getState(playerId_1));
    helper_2.update(engine.getState(playerId_2));

    if (turnCount < NUM_ROUNDS_HIDDEN + 10) {
      return;
    }

    Point pos_1_predicted_by_2 = helper_2._getFirstTrueEnemyPosition_v2();
    Point pos_2_predicted_by_1 = helper_1._getFirstTrueEnemyPosition_v2();
    assertNotNull(pos_1_predicted_by_2);
    assertNotNull(pos_2_predicted_by_1);

    if (pos_1_real.getX() != pos_1_predicted_by_2.x || pos_1_real.getY() != pos_1_predicted_by_2.y) {

      System.out.println(
          "P1 real after 8 = "
              + pos_1_real.getX() + "," + pos_1_real.getY()
              + " | predicted by P2 = "

              + pos_1_predicted_by_2.x + "," + pos_1_predicted_by_2.y);
      helper_2.printEnemyPos();
    }
    if (pos_2_real.getX() != pos_2_predicted_by_1.x || pos_2_real.getY() != pos_2_predicted_by_1.y) {
      System.out.println(
          "P2 real after 8 = "
              + pos_2_real.getX() + "," + pos_2_real.getY()
              + " | predicted by P1 = "
              + pos_2_predicted_by_1.x + "," + pos_2_predicted_by_1.y);
      helper_1.printEnemyPos();
    }
    assertTrue(
        pos_1_real.getX() == pos_1_predicted_by_2.x
            && pos_1_real.getY() == pos_1_predicted_by_2.y);
    assertTrue(
        pos_2_real.getX() == pos_2_predicted_by_1.x
            && pos_2_real.getY() == pos_2_predicted_by_1.y);
  }

  @RepeatedTest(100)
  public void FirstTrueEnemyPosition_v1_wihtBonusRounds() {

    FakeEngine engine = new FakeEngine();

    String playerId_1 = "player_1";
    String playerId_2 = "player_2";

    IStrategy strategy_1 = new StrategyPlannedTour();
    IStrategy strategy_2 = new StrategyAlwaysClosest();

    IMapGenerator mapGenerator_1 = new ClientMap();
    PlayerHalfMap halfMapData_1 = mapGenerator_1.generate(playerId_1);
    engine.registerPlayer(playerId_1, halfMapData_1);

    IMapGenerator mapGenerator_2 = new ClientMap();
    PlayerHalfMap halfMapData_2 = mapGenerator_2.generate(playerId_2);
    engine.registerPlayer(playerId_2, halfMapData_2);

    GameHelper helper_1 = new GameHelper(new UniquePlayerIdentifier(playerId_1));

    GameHelper helper_2 = new GameHelper(new UniquePlayerIdentifier(playerId_2));

    ConsoleView view = new ConsoleView();

    for (int i = 0; i != NUM_ROUNDS_HIDDEN && !engine.isFinished(); i++) {

      GameState state_1 = engine.getState(playerId_1);

      helper_1.update(state_1);
      view.render(helper_1);

      if (i < NUM_ROUNDS_HIDDEN) {
        Point Pos2_expected = helper_1._getFirstTrueEnemyPosition_v1();
        assert (Pos2_expected == null);
      }

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);
      engine.applyMove(move_1);

      GameState state_2 = engine.getState(playerId_2);
      helper_2.update(state_2);
      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);
      engine.applyMove(move_2);
    }

    helper_1.update(engine.getState(playerId_1));
    helper_2.update(engine.getState(playerId_2));

    FullMapNode Pos2 = helper_2.getMyPosition();

    // LOGGER.fine("Player2: " + Pos2.getX() + ", " + Pos2.getY());

    for (int i = 0;; i++) {

      Point Pos2_expected = helper_1._getFirstTrueEnemyPosition_v1();

      assertTrue(Pos2.getX() == Pos2_expected.x && Pos2.getY() == Pos2_expected.y);

      if (engine.isFinished() || i == 6) {
        break;
      }

      PlayerMove move_1 = strategy_1.calculateNextMove(helper_1);
      engine.applyMove(move_1);

      GameState state_2 = engine.getState(playerId_2);
      helper_2.update(state_2);
      PlayerMove move_2 = strategy_2.calculateNextMove(helper_2);
      engine.applyMove(move_2);

      GameState state_1 = engine.getState(playerId_1);
      helper_1.update(state_1);
      view.render(helper_1);
    }
  }
  
  /*
   * TestEnemyPositionTrackCorrectlyUnderMustWait
   *
   * Все что нам важно, КАКОЙ геймстейт получает первый игрок
   *
   * ================ turn 0
   * gamestate((0,0), map=1x100, enemyPos=random1, mustWait)
   * update
   *
   * gamestate((0,0), map=1x100, enemyPos=random2, mustWait)
   * update
   *
   * gamestate((0,0), map=1x100, enemyPos=random3, mustAct)
   * update
   * ================ 1 turn passed
   * gamestate((0,0), map=1x100, enemyPos=random4, mustWait)
   * update
   *
   * gamestate((0,0), map=1x100, enemyPos=random5, mustWait)
   * update
   *
   * gamestate((0,0), map=1x100, enemyPos=random6, mustWait)
   * update
   *
   * gamestate((0,0), map=1x100, enemyPos=random7, mustAct)
   * update
   * ================ 2 turn passed
   * gamestate((0,0), map=1x100, enemyPos=random8, mustAct)
   * update
   * ================ 3 turn passed
   * ...
   * ================ 8 turn passed
   * gamestate((0,0), map=1x100, enemyPos=random, mustWait)
   * gamestate((0,0), map=1x100, enemyPos=random, mustWait)
   * gamestate((0,0), map=1x100, enemyPos=true=(1,99), mustAct)
   * ================ 9 turn passed
   * gamestate((0,0), map=1x100, enemyPos=true=(0,99), mustWait)
   * gamestate((0,0), map=1x100, enemyPos=true=(0,99), mustWait)
   * gamestate((0,0), map=1x100, enemyPos=true=(0,98), mustAct)
   * ================ 10 turn passed
   * gamestate((0,0), map=1x100, enemyPos=true=(0,98), mustWait)
   * gamestate((0,0), map=1x100, enemyPos=true=(0,97), mustAct)
   *
   * ================ BREAK
   * posExpected = gamehelper.get_first_true_enemy_position()
   * posTrue = (0,99)
   *
   * assert(posExpected == posTrue)
   */

  @Test
  void TestEnemyPositionTrackCorrectlyUnderMustWait() {

    class Factory {

      static GameState buildState(EPlayerGameState player1State, int enemyY, String stateId) {
        List<FullMapNode> nodes = new ArrayList<>();

        int x = 0;

        for (int y = 0; y < 100; y++) {

          EPlayerPositionState position = EPlayerPositionState.NoPlayerPresent;

          // Player 1 всегда находится в (0,0)
          if (x == 0 && y == 0) {
            position = EPlayerPositionState.MyPlayerPosition;
          }

          // Enemy position
          if (y == enemyY) {
            position = EPlayerPositionState.EnemyPlayerPosition;
          }

          nodes.add(
              new FullMapNode(
                  ETerrain.Grass,
                  position,
                  ETreasureState.NoOrUnknownTreasureState,
                  EFortState.NoOrUnknownFortState,
                  x,
                  y));
        }

        PlayerState player1 = new PlayerState(
            "Player", "One", "u1", player1State, new UniquePlayerIdentifier("player_1"), false);

        PlayerState player2 = new PlayerState(
            "Player",
            "Two",
            "u2",
            player1State == EPlayerGameState.MustAct
                ? EPlayerGameState.MustWait
                : EPlayerGameState.MustAct,
            new UniquePlayerIdentifier("player_2"),
            false);

        return new GameState(new FullMap(nodes), Set.of(player1, player2), stateId);
      }
    }

    GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player_1"), true);

    // ================= TURN 0
    helper.update(Factory.buildState(EPlayerGameState.MustWait, 10, "state1"));
    helper.update(Factory.buildState(EPlayerGameState.MustWait, 20, "state2"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 30, "state3"));

    // ================= 1 TURN PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 40, "state4"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 50, "state5"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 60, "state6"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 70, "state7"));

    // ================= 2 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 80, "state8"));

    // ================= 3 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 31, "state9"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 32, "state10"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 81, "state11"));

    // ================= 4 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 33, "state12"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 34, "state13"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 35, "state14"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 82, "state15"));

    // ================= 5 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 36, "state16"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 83, "state17"));
    // ================= 6 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 37, "state18"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 38, "state19"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 84, "state20"));

    // ================= 7 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 85, "state21"));

    // ================= 8 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 15, "state22"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 25, "state23"));

    // Первая настоящая позиция врага
    helper.update(Factory.buildState(EPlayerGameState.MustAct, 99, "state24"));

    // ================= 9 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 99, "state25"));

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 99, "state26"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 98, "state27"));

    // ================= 10 TURNS PASSED

    helper.update(Factory.buildState(EPlayerGameState.MustWait, 98, "state28"));

    helper.update(Factory.buildState(EPlayerGameState.MustAct, 97, "state29"));

    // ================= BREAK

    Point posExpected = helper._getFirstTrueEnemyPosition_v1();

    Point posTrue = new Point(0, 99);

    assertEquals(posTrue, posExpected);
  }

  private FullMapNode buildPPos(int x, int y) {
    return new FullMapNode(
        ETerrain.Grass,
        EPlayerPositionState.MyPlayerPosition,
        ETreasureState.NoOrUnknownTreasureState,
        EFortState.NoOrUnknownFortState,
        x,
        y);
  }

  private PlayerState buildPlayerState(EPlayerGameState state) {

    UniquePlayerIdentifier playerId = new UniquePlayerIdentifier("player1");
    return new PlayerState("Test", "Player", "u123456", state, playerId, false);
  }
}
