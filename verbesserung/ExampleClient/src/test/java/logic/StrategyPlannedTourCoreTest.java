package logic;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

class StrategyPlannedTourCoreTest{

    private final int NUM_TEST_REPEATS = 100;

    @Disabled
    @Test
    void CharacterMovesTowardsMountains() {
    
        for(int i = 0; i < NUM_TEST_REPEATS; i++)
        {    
            Random r= new Random();
            int maxX = 5 + r.nextInt(16);
            int maxY = 5 + r.nextInt(16);
            assertTrue(maxX >= 5 && maxX <= 20);
            assertTrue(maxY >= 5 && maxY <= 20);
            

            int mountainX = 1 + r.nextInt(maxX - 2);
            int mountainY = 1 + r.nextInt(maxY - 2);

            int playerX = mountainX;
            int playerY = mountainY;

            

            if (r.nextBoolean()) {
                do{
                    playerY = r.nextInt(maxY);
                }while(playerY == mountainY);
            }
            else
            {
                do {
                    playerX = r.nextInt(maxX);
                }while(playerX == mountainX);
            }

          

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
                            x, y
                        );
                        if(terrain == ETerrain.Mountain)
                        {
                            mountainNode = node;
                        }
                        nodes.add(node);
                }
            }
            assertTrue(mountainNode != null);
            
            FullMap map = new FullMap(nodes);

            long grassNeighbours =
                nodes.stream()
                    .filter(n ->
                        Math.abs(n.getX() - mountainX) <= 1 &&
                        Math.abs(n.getY() - mountainY) <= 1 &&
                        !(n.getX() == mountainX && n.getY() == mountainY)
                    )
                    .filter(n -> n.getTerrain() == ETerrain.Grass)
                    .count();

            assertTrue(grassNeighbours == 8);
            
            GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));

            GameState gameState = new GameState(
                map,
                Set.of(
                    new PlayerState(
                        "Test",
                        "Player",
                        "u123456",
                        EPlayerGameState.MustWait,
                        new UniquePlayerIdentifier("player1"),
                        false
                    )
                ),
                "ABC"
            );

            helper.update(gameState);


            StrategyNearestNeighbour strategy = new StrategyNearestNeighbour();
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
    }



    @Test
    void BestTourReachesMountainAtOptimalStep() {

        for (int i = 0; i < NUM_TEST_REPEATS; i++) {

            Random r = new Random();
            int maxX = 5 + r.nextInt(16);
            int maxY = 5 + r.nextInt(16);

            int mountainX = r.nextInt(maxX);
            int mountainY = r.nextInt(maxY);

            int playerX, playerY;

            do {
                playerX = r.nextInt(maxX);
                playerY = r.nextInt(maxY);
            } while (playerX == mountainX && playerY == mountainY);

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
                        x, y
                    );

                    if (terrain == ETerrain.Mountain) mountainNode = node;
                    if (playerState == EPlayerPositionState.MyPlayerPosition) playerNode = node;

                    nodes.add(node);
                }
            }

            assertTrue(mountainNode != null);
            assertTrue(playerNode != null);

            FullMap map = new FullMap(nodes);
            GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));

            GameState gameState = new GameState(
                map,
                Set.of(new PlayerState(
                    "Test","Player","u123456",
                    EPlayerGameState.MustWait,
                    new UniquePlayerIdentifier("player1"),
                    false
                )),
                "ABC"
            );
            helper.update(gameState);

            StrategyNearestNeighbour strategy = new StrategyNearestNeighbour();

            strategy.calculateNextMove(helper);
            // === получаем тур целей ===
            Queue<FullMapNode> tour = strategy.get_plannedTour();
            // тур должен содержать гору как цель
            assertTrue(tour.contains(mountainNode));

            int distance = Math.abs(playerX - mountainX) + Math.abs(playerY - mountainY);

            assertTrue(tour.peek() == playerNode);
            List l = new ArrayList<>(tour);
            assertTrue(l.get(distance) == mountainNode);
        }
    }


    
}