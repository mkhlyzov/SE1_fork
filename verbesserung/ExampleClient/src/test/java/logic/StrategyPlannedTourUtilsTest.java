package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

class StrategyPlannedTourUtilsTest {

    private final int NUM_TEST_REPEATS = 100;

    @Test
    void continiousPathBFS_findsSimplePath() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        FullMapNode a = node(0, 0, ETerrain.Grass);
        FullMapNode b = node(1, 0, ETerrain.Grass);
        FullMapNode c = node(2, 0, ETerrain.Grass);

        GameHelper helper = mock(GameHelper.class);
        when(helper.getNeighbours4(a)).thenReturn(new ArrayList<>(List.of(b)));

        when(helper.getNeighbours4(b)).thenReturn(new ArrayList<>(List.of(a, c)));

        when(helper.getNeighbours4(c)).thenReturn(new ArrayList<>(List.of(b)));


        List<FullMapNode> path = strategy.continiousPathBFS(a,c, helper, Set.of());
        assertNotNull(path);
        assert(path.size() == 3);
        assertEquals(a, path.get(0));
        assertEquals(b,path.get(1));
        assertEquals(c, path.get(2));
    }
   
    @Test
    void continiousPathBFS_doesNotUseWater() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        FullMapNode a = node(0, 0, ETerrain.Grass);
        FullMapNode w = node(1, 0, ETerrain.Water);
        FullMapNode c = node(2, 0, ETerrain.Grass);

        GameHelper helper = mock(GameHelper.class);
        when(helper.getNeighbours4(a)).thenReturn(List.of(w));
        when(helper.getNeighbours4(w)).thenReturn(List.of(a, c));
        when(helper.getNeighbours4(c)).thenReturn(List.of(w));

        List<FullMapNode> path = strategy.continiousPathBFS(a, c, helper, Set.of());

        assertTrue(
            path == null || path.isEmpty(),
            "No valid path should exist because water blocks the way"
        );
        assertTrue(
            path.stream().noneMatch(n -> n.getTerrain() == ETerrain.Water),
            "Path must not contain water tiles"
        );
    }

    @Test
    void continiousPathBFS_doesNotUseWater2() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,0);
        FullMapNode b = node(1, 0, ETerrain.Grass);
        FullMapNode w = node(2, 0, ETerrain.Water);
        FullMapNode c = node(3, 0, ETerrain.Grass);
        FullMapNode d = node(4, 0, ETerrain.Grass);
        List<FullMapNode> nodes = List.of(a,b,w,c,d);
        Set<PlayerState> players = Set.of(new PlayerState("Test","Player","u123456",EPlayerGameState.MustWait,new UniquePlayerIdentifier("player1"),false));
        FullMap map = new FullMap(nodes);
        GameState gamestate = new GameState(map,players,"ABC");
        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gamestate);
        // GameHelper helper = mock(GameHelper.class);
        // when(helper.getNeighbours4(a)).thenReturn(new ArrayList<>(List.of(b)));
        // when(helper.getNeighbours4(b)).thenReturn(new ArrayList<>(List.of(a, w)));
        // when(helper.getNeighbours4(w)).thenReturn(new ArrayList<>(List.of(b,c)));
        // when(helper.getNeighbours4(c)).thenReturn(new ArrayList(List.of(w,d)));
        // when(helper.getNeighbours4(d)).thenReturn(new ArrayList(List.of(c)));


        List<FullMapNode> path = strategy.continiousPathBFS(a, d, helper, Set.of());

        assertTrue(
            path == null || path.isEmpty(),
            "No valid path should exist because water blocks the way"
        );
        assertTrue(
            path.stream().noneMatch(n -> n.getTerrain() == ETerrain.Water),
            "Path must not contain water tiles"
        );
    }


    @Test
    void continiousPathBFS_randomlyChoosesBetweenAlternativePaths() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,0);
        FullMapNode b = node(0, 1, ETerrain.Grass);
        FullMapNode c = node(1, 0, ETerrain.Grass);
        FullMapNode d = node(1, 1, ETerrain.Grass);
        List<FullMapNode> nodes = List.of(a,b,c,d);
        Set<PlayerState> players = Set.of(new PlayerState("Test","Player","u123456",EPlayerGameState.MustWait,new UniquePlayerIdentifier("player1"),false));
        FullMap map = new FullMap(nodes);
        GameState gamestate = new GameState(map,players,"ABC");
        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gamestate);

        boolean sawPathViaB = false;
        boolean sawPathViaC = false;
        for (int i = 0; i < NUM_TEST_REPEATS; i++) {
            List<FullMapNode> path =
                strategy.continiousPathBFS(a, d, helper, Set.of());

            assertNotNull(path);
            assertEquals(a, path.get(0));
            assertEquals(d, path.get(path.size() - 1));

            if (path.contains(b)) sawPathViaB = true;
            if (path.contains(c)) sawPathViaC = true;

            if (sawPathViaB && sawPathViaC) {
                break;
            }
        }

    }

    @Test
    void continiousPathBFS_prefersUnvisitedPathWhenAlternativeExists() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,0);
        FullMapNode b = node(0, 1, ETerrain.Grass);
        FullMapNode c = node(1, 0, ETerrain.Grass);
        FullMapNode d = node(1, 1, ETerrain.Grass);
        List<FullMapNode> nodes = List.of(a,b,c,d);
        Set<PlayerState> players = Set.of(new PlayerState("Test","Player","u123456",EPlayerGameState.MustWait,new UniquePlayerIdentifier("player1"),false));
        FullMap map = new FullMap(nodes);
        GameState gamestate = new GameState(map,players,"ABC");
        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gamestate);

        for (int i = 0; i < NUM_TEST_REPEATS; i++) {

            List<FullMapNode> path = strategy.continiousPathBFS(a, d, helper, Set.of(a,c,d));

            assertNotNull(path);
            assertEquals(a, path.get(0));
            assertEquals(d, path.get(path.size() - 1));

            // разведанный путь НИКОГДА не должен выбираться
            assertFalse(path.contains(b));

            // неразведанный путь ВСЕГДА должен выбираться
            assertTrue(path.contains(c));
        }
    }
    // ===== helper method =====

    @Test
    void continiousPathBFS_choosesLongerButCheaperPath() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        // A
        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,1);

        // верхний (дешёвый) путь
        FullMapNode g1 = node(0, 0, ETerrain.Grass);
        FullMapNode g2 = node(1, 0, ETerrain.Grass);
        FullMapNode g3 = node(2, 0, ETerrain.Grass);
        FullMapNode g4 = node(3, 0, ETerrain.Grass);
        FullMapNode g5 = node(4, 0, ETerrain.Grass);
        FullMapNode g6 = node(5, 0, ETerrain.Grass);
        FullMapNode g7 = node(6, 0, ETerrain.Grass);
        // нижний (дорогой) путь
        FullMapNode m1 = node(1, 1, ETerrain.Mountain);
        FullMapNode m2 = node(2, 1, ETerrain.Mountain);
        FullMapNode m3 = node(3, 1, ETerrain.Mountain);
        FullMapNode m4 = node(4, 1, ETerrain.Mountain);
        FullMapNode m5 = node(5, 1, ETerrain.Mountain);

        // B
        FullMapNode b = node(6, 1, ETerrain.Grass);

        List<FullMapNode> nodes = List.of(
            g1, g2, g3, g4, g5,g6,g7,
            a,m1, m2, m3, m4, m5,b
        );

        Set<PlayerState> players = Set.of(
            new PlayerState(
                "Test","Player","u123456",
                EPlayerGameState.MustWait,
                new UniquePlayerIdentifier("player1"),
                false
            )
        );

        FullMap map = new FullMap(nodes);
        GameState gameState = new GameState(map, players, "ABC");

        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gameState);

        List<FullMapNode> path =
            strategy.continiousPathBFS(a, b, helper, Set.of());

        assertNotNull(path);
        assertEquals(a, path.get(0));
        assertEquals(b, path.get(path.size() - 1));

        // маршрут НЕ должен идти через горы
        assertFalse(path.contains(m1));
        assertFalse(path.contains(m2));
        assertFalse(path.contains(m3));
        assertFalse(path.contains(m4));
        assertFalse(path.contains(m5));

        // маршрут должен идти по траве
        assertTrue(path.contains(g1));
        assertTrue(path.contains(g2));
        assertTrue(path.contains(g3));
        assertTrue(path.contains(g4));
        assertTrue(path.contains(g5));
        assertTrue(path.contains(g6));
        assertTrue(path.contains(g7));
    }

    @Test
    void continiousPathBFS_choosesLongerButCheaperPath_ignoresMountainGoals() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        // A
        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,1);

        // верхний (дешёвый) путь
        FullMapNode g1 = node(0, 0, ETerrain.Grass);
        FullMapNode g2 = node(1, 0, ETerrain.Grass);
        FullMapNode g3 = node(2, 0, ETerrain.Grass);
        FullMapNode g4 = node(3, 0, ETerrain.Grass);
        FullMapNode g5 = node(4, 0, ETerrain.Grass);
        FullMapNode g6 = node(5, 0, ETerrain.Grass);
        FullMapNode g7 = node(6, 0, ETerrain.Grass);
        // нижний (дорогой) путь
        FullMapNode m1 = node(1, 1, ETerrain.Mountain);
        FullMapNode m2 = node(2, 1, ETerrain.Mountain);
        FullMapNode m3 = node(3, 1, ETerrain.Mountain);
        FullMapNode m4 = node(4, 1, ETerrain.Mountain);
        FullMapNode m5 = node(5, 1, ETerrain.Mountain);

        // B
        FullMapNode b = node(6, 1, ETerrain.Grass);

        List<FullMapNode> nodes = List.of(
            g1, g2, g3, g4, g5,g6,g7,
            a,m1, m2, m3, m4, m5,b
        );

        Set<PlayerState> players = Set.of(
            new PlayerState(
                "Test","Player","u123456",
                EPlayerGameState.MustWait,
                new UniquePlayerIdentifier("player1"),
                false
            )
        );

        FullMap map = new FullMap(nodes);
        GameState gameState = new GameState(map, players, "ABC");

        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gameState);

        List<FullMapNode> path =
            strategy.continiousPathBFS(a, b, helper, Set.of(m1,m2,m3,m4,m5));

        assertNotNull(path);
        assertEquals(a, path.get(0));
        assertEquals(b, path.get(path.size() - 1));

        // маршрут НЕ должен идти через горы
        assertFalse(path.contains(m1));
        assertFalse(path.contains(m2));
        assertFalse(path.contains(m3));
        assertFalse(path.contains(m4));
        assertFalse(path.contains(m5));

        // маршрут должен идти по траве
        assertTrue(path.contains(g1));
        assertTrue(path.contains(g2));
        assertTrue(path.contains(g3));
        assertTrue(path.contains(g4));
        assertTrue(path.contains(g5));
        assertTrue(path.contains(g6));
        assertTrue(path.contains(g7));
    }

    @Test
    void findClosestBFS_ignoresVisitedGoals() {
        StrategyPlannedTour strategy = new StrategyPlannedTour();

        // A
        FullMapNode a = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,ETreasureState.NoOrUnknownTreasureState,EFortState.NoOrUnknownFortState,0,0);
        FullMapNode b = node(1,0,ETerrain.Grass);
        FullMapNode c = node(2, 0, ETerrain.Grass);
        FullMapNode d = node(3, 0, ETerrain.Grass);
        

        List<FullMapNode> nodes = List.of(
            a,b,c,d
        );

        Set<PlayerState> players = Set.of(
            new PlayerState(
                "Test","Player","u123456",
                EPlayerGameState.MustWait,
                new UniquePlayerIdentifier("player1"),
                false
            )
        );

        FullMap map = new FullMap(nodes);
        GameState gameState = new GameState(map, players, "ABC");

        GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));
        helper.update(gameState);

        Set<FullMapNode> goals = Set.of(c,d);

        FullMapNode result =
            strategy.closestByBFS(a, goals, helper);

        // должна быть выбрана неразведанная цель
        assertEquals(c, result);

        
    }


    



    private FullMapNode node(int x, int y, ETerrain terrain) {
        return new FullMapNode(
            terrain,
            EPlayerPositionState.NoPlayerPresent,
            ETreasureState.NoOrUnknownTreasureState,
            EFortState.NoOrUnknownFortState,
            x,
            y
        );
    }
}
