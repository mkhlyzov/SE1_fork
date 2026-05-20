package logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import testutils.TestLogger;

class StrategyPlannedTourTest {

    private final int NUM_TEST_REPEATS = 100;
    private static final Logger LOGGER = TestLogger.getLogger();

    @RepeatedTest(100)
    public void CharacterMovesTowardsMountains() {

        Random r = new Random();
        int maxX = r.nextBoolean() ? 10 : 20;
        int maxY = 100 / maxX;
        assertTrue(maxX >= 5 && maxX <= 20);
        assertTrue(maxY >= 5 && maxY <= 20);

        int playerX, playerY, mountainX, mountainY;

        int shiftX = (r.nextBoolean() && (maxX == 20)) ? 10 : 0;
        int shiftY = (r.nextBoolean() && (maxY == 10)) ? 5 : 0;

        mountainX = 1 + r.nextInt(10 - 2) + shiftX;
        mountainY = 1 + r.nextInt(5 - 2) + shiftY;

        do {
            if (r.nextBoolean()) {
                playerX = r.nextInt(10) + shiftX;
                playerY = mountainY;
            } else {
                playerX = mountainX;
                playerY = r.nextInt(5) + shiftY;
            }
        } while (playerX == mountainX && playerY == mountainY);

        assertTrue(playerX == mountainX || playerY == mountainY);
        assertTrue(playerX >= 0 && playerX < maxX);
        assertTrue(playerY >= 0 && playerY < maxY);

        List<FullMapNode> nodes = new ArrayList<>();
        FullMapNode mountainNode = null;

        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {

                ETerrain terrain = ETerrain.Grass;
                EPlayerPositionState playerState = EPlayerPositionState.NoPlayerPresent;

                if (x == mountainX && y == mountainY) {
                    terrain = ETerrain.Mountain;
                }

                if (x == playerX && y == playerY) {
                    playerState = EPlayerPositionState.MyPlayerPosition;
                }

                FullMapNode node = new FullMapNode(
                        terrain,
                        playerState,
                        ETreasureState.NoOrUnknownTreasureState,
                        EFortState.NoOrUnknownFortState,
                        x, y);
                if (terrain == ETerrain.Mountain) {
                    mountainNode = node;
                }
                nodes.add(node);
            }
        }
        assertTrue(mountainNode != null);

        FullMap map = new FullMap(nodes);

        long grassNeighbours = nodes.stream()
                .filter(n -> Math.abs(n.getX() - mountainX) <= 1 &&
                        Math.abs(n.getY() - mountainY) <= 1 &&
                        !(n.getX() == mountainX && n.getY() == mountainY))
                .filter(n -> n.getTerrain() == ETerrain.Grass)
                .count();

        assertTrue(grassNeighbours == 8);

        GameHelper helper = Utils.generateGameHelper(map);

        IStrategy strategy = new StrategyPlannedTour();
        PlayerMove move = strategy.calculateNextMove(helper);

        int newX = playerX;
        int newY = playerY;

        switch (move.getMove()) {
            case Up -> newY--;
            case Down -> newY++;
            case Left -> newX--;
            case Right -> newX++;
        }

        assertTrue(newX >= 0 && newX < maxX);
        assertTrue(newY >= 0 && newY < maxY);

        int oldDistance = Math.abs(playerX - mountainX) + Math.abs(playerY - mountainY);

        int newDistance = Math.abs(newX - mountainX) + Math.abs(newY - mountainY);

        assertTrue(newDistance < oldDistance);
    }

    // @Test
    @RepeatedTest(100)
    public void BestTourReachesMountainAtOptimalStep() {
        Random r = new Random();
        // int maxX = 5 + r.nextInt(16);
        // int maxY = 5 + r.nextInt(16);
        int maxX = r.nextBoolean() ? 10 : 20;
        int maxY = 100 / maxX;

        int playerX, playerY, mountainX, mountainY;

        mountainX = 1 + r.nextInt(10 - 2);
        mountainY = 1 + r.nextInt(5 - 2);

        do {
            playerX = r.nextInt(10);
            playerY = r.nextInt(5);
        } while (playerX == mountainX && playerY == mountainY);

        if (r.nextBoolean()) {
            if (maxX == 20) {
                mountainX += 10;
                playerX += 10;
            } else {
                mountainY += 5;
                playerY += 5;
            }
        }

        // === карта ===
        List<FullMapNode> nodes = new ArrayList<>();
        FullMapNode mountainNode = null;
        FullMapNode playerNode = null;

        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                ETerrain terrain = ETerrain.Grass;
                EPlayerPositionState playerState = EPlayerPositionState.NoPlayerPresent;

                if (x == mountainX && y == mountainY) {
                    terrain = ETerrain.Mountain;
                }
                if (x == playerX && y == playerY) {
                    playerState = EPlayerPositionState.MyPlayerPosition;
                }

                FullMapNode node = new FullMapNode(
                        terrain,
                        playerState,
                        ETreasureState.NoOrUnknownTreasureState,
                        EFortState.NoOrUnknownFortState,
                        x, y);

                if (terrain == ETerrain.Mountain)
                    mountainNode = node;
                if (playerState == EPlayerPositionState.MyPlayerPosition)
                    playerNode = node;

                nodes.add(node);
            }
        }

        assertTrue(mountainNode != null);
        assertTrue(playerNode != null);

        FullMap map = new FullMap(nodes);
        GameHelper helper = Utils.generateGameHelper(map);

        StrategyPlannedTour strategy = new StrategyPlannedTour();

        strategy.calculateNextMove(helper);
        // === получаем тур целей ===
        List<FullMapNode> tour = strategy.get_plannedTour();
        // тур должен содержать гору как цель
        assertTrue(tour.contains(mountainNode));

        int distance = Math.abs(playerX - mountainX) + Math.abs(playerY - mountainY);

        assertTrue(tour.get(0) == playerNode);

        assertTrue(tour.get(distance) == mountainNode);

    }

    @Test
    public void ignoreMountain_v1() {
        int NUM_SUCCESS = 0;
        for (int i = 0; i < NUM_TEST_REPEATS; i++) {

            Random r = new Random();
            // int maxX = 5 + r.nextInt(16);
            // int maxY = 5 + r.nextInt(16);
            int maxX = r.nextBoolean() ? 10 : 20;
            int maxY = 100 / maxX;

            int playerX, playerY, mountainX, mountainY;

            mountainX = 0;
            mountainY = 0;

            do {
                playerX = r.nextInt(10);
                playerY = r.nextInt(5);
            } while (playerX == mountainX && playerY == mountainY);

            if (r.nextBoolean()) {
                if (maxX == 20) {
                    mountainX += 10;
                    playerX += 10;
                } else {
                    mountainY += 5;
                    playerY += 5;
                }
            }

            // === карта ===
            List<FullMapNode> nodes = new ArrayList<>();
            FullMapNode mountainNode = null;
            FullMapNode playerNode = null;

            for (int x = 0; x < maxX; x++) {
                for (int y = 0; y < maxY; y++) {
                    ETerrain terrain = ETerrain.Grass;
                    EPlayerPositionState playerState = EPlayerPositionState.NoPlayerPresent;

                    if (x == mountainX && y == mountainY) {
                        terrain = ETerrain.Mountain;
                    }
                    if (x == playerX && y == playerY) {
                        playerState = EPlayerPositionState.MyPlayerPosition;
                    }

                    FullMapNode node = new FullMapNode(
                            terrain,
                            playerState,
                            ETreasureState.NoOrUnknownTreasureState,
                            EFortState.NoOrUnknownFortState,
                            x, y);

                    if (terrain == ETerrain.Mountain)
                        mountainNode = node;
                    if (playerState == EPlayerPositionState.MyPlayerPosition)
                        playerNode = node;

                    nodes.add(node);
                }
            }

            assertTrue(mountainNode != null);
            assertTrue(playerNode != null);

            FullMap map = new FullMap(nodes);
            GameHelper helper = Utils.generateGameHelper(map);

            StrategyPlannedTour strategy = new StrategyPlannedTour();

            strategy.calculateNextMove(helper);
            // === получаем тур целей ===
            List<FullMapNode> tour = strategy.get_plannedTour();
            LOGGER.fine("Tour: " + tour.stream()
                    .map(n -> "(" + n.getX() + "," + n.getY() + "," + n.getTerrain() + ")")
                    .collect(Collectors.joining(" ")));
            // тур должен содержать гору как цель
            // assertFalse(tour.contains(mountainNode));
            if (!tour.contains(mountainNode)) {
                NUM_SUCCESS++;
            }

        }
        LOGGER.fine("NUM_SUCCESS = " + NUM_SUCCESS);
        assertTrue((double) NUM_SUCCESS / NUM_TEST_REPEATS > 0.90);

    }

    @Test
    public void ignoreMountain_v2() {

        FullMapNode A = new FullMapNode(ETerrain.Mountain, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 0, 0);
        FullMapNode B = new FullMapNode(ETerrain.Grass, EPlayerPositionState.MyPlayerPosition,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 0, 1);
        FullMapNode C = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 1, 0);
        FullMapNode D = new FullMapNode(ETerrain.Grass, EPlayerPositionState.NoPlayerPresent,
                ETreasureState.NoOrUnknownTreasureState, EFortState.NoOrUnknownFortState, 1, 1);
        StrategyPlannedTour strategy = new StrategyPlannedTour();
        FullMap map = new FullMap(List.of(A, B, C, D));
        GameHelper helper = Utils.generateGameHelper(map);
        Set<FullMapNode> goals = Set.of(A, C);

        Set<List<FullMapNode>> alternatives = new HashSet<>();

        for (FullMapNode anchor : goals) {
            List<FullMapNode> tour = strategy.generateFullTourThroughAnchor(helper, anchor, goals);

            assertNotNull(tour);
            alternatives.add(tour);

        }

        strategy.updateBestTour(helper, goals, 25);
        List<FullMapNode> bestTour = strategy.get_plannedTour();

        assertNotNull(bestTour);

        LOGGER.fine("Количество альтернативных туров: " + alternatives.size());

        for (List<FullMapNode> tour : alternatives) {
            double score = strategy.computeTourScore_v1(tour, goals, 0.97);
            LOGGER.fine("Alternative tour: " + tour.stream()
                    .map(n -> "(" + n.getX() + "," + n.getY() + "," + n.getTerrain() + ")")
                    .collect(Collectors.joining(" ")));
            LOGGER.fine("Score: " + score);
        }

        double bestScore = strategy.computeTourScore_v1(bestTour, goals, 0.97);

        String msg = ("Best tour: " + bestTour.stream()
                .map(n -> "(" + n.getX() + "," + n.getY() + "," + n.getTerrain() + ")")
                .collect(Collectors.joining(" ")));
        LOGGER.fine(msg);
        LOGGER.fine("Best score: " + bestScore);
    }

}
