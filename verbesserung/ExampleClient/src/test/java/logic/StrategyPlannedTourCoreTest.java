package logic;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    
    @Test
    void CharacterMovesTowardsMountains() {
        Random r= new Random();
        int maxX = 5 + r.nextInt(16);
        int maxY = 5 + r.nextInt(16);
        assertTrue(maxX >= 5 && maxX <= 20);
        assertTrue(maxY >= 5 && maxY <= 20);
          

        int mountainX = 1 + r.nextInt(maxX - 2);
        int mountainY = 1 + r.nextInt(maxY - 2);

        int playerX = mountainX + 2;
        int playerY = mountainY;

        if (playerX >= maxX) {
            playerX = mountainX - 2;
        }

        assertTrue(playerX == mountainX || playerY == mountainY);


        List<FullMapNode> nodes = new ArrayList<>();

        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                
                ETerrain terrain = ETerrain.Grass;
                EPlayerPositionState playerState =
                EPlayerPositionState.NoPlayerPresent;

                if (x == mountainX && y == mountainY) {
                    terrain = ETerrain.Mountain;
                }

                if (x == playerX && y == playerY) {
                    playerState = EPlayerPositionState.MyPlayerPosition;
                }
                
                nodes.add(new FullMapNode(
                        terrain,
                        playerState,
                        ETreasureState.NoOrUnknownTreasureState,
                        EFortState.NoOrUnknownFortState,
                        x, y
                    ));
            }
        }
        FullMap map = new FullMap(nodes);
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

        StrategyNearestNeighbour strategy = new StrategyNearestNeighbour();
        PlayerMove move = strategy.calculateNextMove(helper);

        FullMapNode start = helper.getMyPosition();
        FullMapNode mountain = nodes.stream()
            .filter(n -> n.getTerrain() == ETerrain.Mountain)
            .findFirst()
            .orElseThrow();

        int newX = start.getX();
        int newY = start.getY();

        switch (move.getMove()) {
            case Up -> newY--;
            case Down -> newY++;
            case Left -> newX--;
            case Right -> newX++;
        }

        int oldDistance =
            Math.abs(start.getX() - mountainX) +
            Math.abs(start.getY() - mountainY);

        int newDistance =
            Math.abs(newX - mountainX) +
            Math.abs(newY - mountainY);

        assertTrue(newDistance <= oldDistance);
    }
}