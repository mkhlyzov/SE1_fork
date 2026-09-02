package logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import org.junit.jupiter.api.Test;

public class StrategyPlannedTourScoreTest {
  @Test
  public void TestComputeTourCost_v1() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G, B, C);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    double score = strategy.computeTourScore_v1(tour, goals, 0.99);
    double expected = 5.54168;
    assertEquals(score, expected, 1e-5);
  }

  @Test
  public void TestComputeTourCost_v2() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G, B, C);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    FullMap map = new FullMap(List.of(A, B, C, D, E, F, G, H));
    GameHelper helper = Utils.generateGameHelper(map);
    double score = strategy.computeTourScore_v2(tour, goals, helper, 0.99);
    double expected = 5.54168;
    assertEquals(score, expected, 1e-5);
  }

  @Test
  public void TestComputeTourCostOnlyGoals_v1() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    double score = strategy.computeTourScore_v1(tour, goals, 0.99);
    double expected = 3.75092;
    assertEquals(score, expected, 1e-5);
  }

  @Test
  public void TestComputeTourCostOnlyGoals_v2() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    FullMap map = new FullMap(List.of(A, B, C, D, E, F, G, H));
    GameHelper helper = Utils.generateGameHelper(map);
    double score = strategy.computeTourScore_v2(tour, goals, helper, 0.99);
    double expected = 3.75092;
    assertEquals(score, expected, 1e-5);
  }

  @Test
  public void TestComputeTourCostMountains_v1() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Mountain,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    double score = strategy.computeTourScore_v1(tour, goals, 0.99);
    double expected = 2.78696;
    assertEquals(expected, score, 1e-5);
  }

  @Test
  public void TestComputeTourCostMountains_v2() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Mountain,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(D, E, F, G);
    List<FullMapNode> tour = List.of(H, G, F, E, F, B, C, D);
    FullMap map = new FullMap(List.of(A, B, C, D, E, F, G, H));
    GameHelper helper = Utils.generateGameHelper(map);
    double score = strategy.computeTourScore_v2(tour, goals, helper, 0.99);
    double expected = 2.78696;
    assertEquals(expected, score, 1e-5);
  }

  @Test
  public void TestComputeTourScoreHard() {
    FullMapNode A =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            0);
    FullMapNode B =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            0);
    FullMapNode C =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            0);
    FullMapNode D =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            0);
    FullMapNode E =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            1);
    FullMapNode F =
        new FullMapNode(
            ETerrain.Water,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            1);
    FullMapNode G =
        new FullMapNode(
            ETerrain.Mountain,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            1);
    FullMapNode H =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            1);
    FullMapNode I =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            0,
            2);
    FullMapNode J =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            1,
            2);
    FullMapNode K =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            2,
            2);
    FullMapNode L =
        new FullMapNode(
            ETerrain.Grass,
            EPlayerPositionState.MyPlayerPosition,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            3,
            2);
    StrategyPlannedTour strategy = new StrategyPlannedTour();
    Set<FullMapNode> goals = Set.of(A, B, D, E, G, H, I, J, K);
    List<FullMapNode> tour = List.of(L, H, G, K, J, I);
    FullMap map = new FullMap(List.of(A, B, C, D, E, F, G, H, I, J, K, L));
    GameHelper helper = Utils.generateGameHelper(map);
    double score_099 = strategy.computeTourScore_v2(tour, goals, helper, 0.99);
    double score_097 = strategy.computeTourScore_v2(tour, goals, helper, 0.97);
    double expected_099 = 5.43250;
    double expected_097 = 4.47128;
    assertEquals(expected_099, score_099, 1e-5);
    assertEquals(expected_097, score_097, 1e-5);
  }
}
