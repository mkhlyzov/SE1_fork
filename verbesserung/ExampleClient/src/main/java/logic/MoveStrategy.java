package logic;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.*;

 //import java.io.FileWriter;
 //import java.io.IOException;
 //import java.io.PrintWriter;
 //import java.time.LocalDateTime;
import java.util.*;
import messagesbase.messagesfromclient.ETerrain;

import logic.GameHelper;

public class MoveStrategy {

    private int myXmin, myXmax, myYmin, myYmax;
    private int enemyXmin, enemyXmax, enemyYmin, enemyYmax;
    private boolean isInitialized = false;
   
   
    private void initialize(GameHelper gameHelper) {
        FullMap map = gameHelper.getMap();
        FullMapNode myPosition = gameHelper.getMyPosition();
        if (!isInitialized) {
            int maxX = gameHelper.getMaxX();  // 9 or 19
            int maxY = gameHelper.getMaxY();  // 9 or 4
            int playerX = myPosition.getX();
            int playerY = myPosition.getY();

            if (maxX == 9 && maxY == 9) {
                // 🟥 Quadrat map (10x10)
                myXmin = 0; myXmax = 10;
                myYmin = 0; myYmax = 5;

                if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                    // all good!
                    enemyXmin = 0; enemyXmax = 10;
                    enemyYmin = 5; enemyYmax = 10;
                } else {
                    // it was enemy side
                    myXmin = 0; myXmax = 10;
                    myYmin = 5; myYmax = 10;

                    enemyXmin = 0; enemyXmax = 10;
                    enemyYmin = 0; enemyYmax = 5;
                }
            } else if (maxX == 19 && maxY == 4) {
                // 🟦 Rechteck map (20x5)
                myXmin = 0; myXmax = 10;
                myYmin = 0; myYmax = 5;

                if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                    // all good!
                    enemyXmin = 10; enemyXmax = 20;
                    enemyYmin = 0; enemyYmax = 5;
                } else {
                    // it was enemy side
                    myXmin = 10; myXmax = 20;
                    myYmin = 0; myYmax = 5;

                    enemyXmin = 0; enemyXmax = 10;
                    enemyYmin = 0; enemyYmax = 5;
                }
            } else {
                System.err.println("❌ Unbekanntes Kartenformat (" + (maxX + 1) + " x " + (maxY + 1) + ")");
            }
            isInitialized = true;
        }
    }

    
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        initialize(gameHelper);
        FullMap map = gameHelper.getMap();
        FullMapNode myPosition = gameHelper.getMyPosition();
        assert myPosition != null;
        boolean playerHasTreasure = gameHelper.hasTreasure();

        FullMapNode goal;

        if (playerHasTreasure) {
            goal = findEnemyFort(map);
            if (goal == null) {
                System.out.println("🔍 Suche Gegnerburg (nur auf feindlicher Seite)");
                goal = findClosestUndiscoveredNode(
                    gameHelper,
                    enemyXmin, enemyYmin, enemyXmax, enemyYmax
                );
            } else {
                System.out.println("Coordinates of  Fort: " + goal.getX() + ", " + goal.getY());
            }
        } else {
            goal = findTreasure(map);
            if (goal == null) {
                System.out.println("🔍 Suche Schatz (egal wo)");
                goal = findClosestUndiscoveredNode(
                    gameHelper,
                    myXmin, myYmin, myXmax, myYmax
                );
            } else {
                System.out.println("Coordinates of  Treasure: " + goal.getX() + ", " + goal.getY());
            }
        }

        UniquePlayerIdentifier playerId = gameHelper.getPlayerId();
        if (goal == null || goal == myPosition) {
            System.out.println("❌ Kein Ziel gefunden – bleibe stehen.");
            return stayClose(myPosition, playerId, map);
        }

        Pathfinder pathfinder = new Pathfinder(map);  
        List<FullMapNode> trajectory = pathfinder.findPath(myPosition, goal);

        FullMapNode next = trajectory.get(0);        
        EMove move = calculateMove(myPosition, next);
        return PlayerMove.of(playerId, move);
    }

    private FullMapNode findTreasure(FullMap map) {
        return map.getMapNodes().stream()
                .filter(n -> n.getTreasureState() == ETreasureState.MyTreasureIsPresent)
                .findFirst().orElse(null);
    }

    private FullMapNode findEnemyFort(FullMap map) {
        return map.getMapNodes().stream()
                .filter(n -> n.getFortState() == EFortState.EnemyFortPresent)
                .findFirst().orElse(null);
    }

    private PlayerMove stayClose(FullMapNode pos, UniquePlayerIdentifier playerId, FullMap map) {
        int x = pos.getX();
        int y = pos.getY();
        if (isSafeMove(map, x + 1, y)) return PlayerMove.of(playerId, EMove.Right);
        if (isSafeMove(map, x, y + 1)) return PlayerMove.of(playerId, EMove.Down);
        if (isSafeMove(map, x - 1, y)) return PlayerMove.of(playerId, EMove.Left);
        if (isSafeMove(map, x, y - 1)) return PlayerMove.of(playerId, EMove.Up);
        return PlayerMove.of(playerId, EMove.Right);
    }

    private boolean isSafeMove(FullMap map, int x, int y) {
        return map.getMapNodes().stream()
                .anyMatch(n -> n.getX() == x && n.getY() == y &&
                               n.getTerrain() != ETerrain.Water);
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        if (to.getX() > from.getX()) return EMove.Right;
        if (to.getX() < from.getX()) return EMove.Left;
        if (to.getY() > from.getY()) return EMove.Down;
        if (to.getY() < from.getY()) return EMove.Up;
        return EMove.Right;
    }

    // Find nearest undiscovered node
    private FullMapNode findClosestUndiscoveredNode(
        GameHelper gameHelper,
        int x1, int y1, int x2, int y2
    ) {
        FullMap map = gameHelper.getMap();
        FullMapNode start = gameHelper.getMyPosition();
        if (start == null) return null;

        Map<String, FullMapNode> nodeMap = new HashMap<>();
        for (FullMapNode node : map.getMapNodes()) {
            nodeMap.put(key(node.getX(), node.getY()), node);
        }

        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();

        Queue<FullMapNode> queue = new LinkedList<>();
        Set<String> visitedInSearch = new HashSet<>();

        queue.add(start);
        visitedInSearch.add(key(start.getX(), start.getY()));

        while (!queue.isEmpty()) {
            FullMapNode current = queue.poll();
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : dirs) {
                int nx = current.getX() + d[0];
                int ny = current.getY() + d[1];
                String neighborKey = key(nx, ny);

                if (nx < 0 || ny < 0 || nx > maxX || ny > maxY) continue;
                if (visitedInSearch.contains(neighborKey)) continue;

                FullMapNode neighbor = nodeMap.get(neighborKey);
                if (neighbor == null || neighbor.getTerrain() == ETerrain.Water) continue;
                visitedInSearch.add(neighborKey);
                queue.add(neighbor);

                if(neighbor.getTerrain() == ETerrain.Mountain) continue;

                if (!gameHelper.isVisited(neighbor)) {
                    if (nx >= x1 && nx < x2 && ny >= y1 && ny < y2)
                        return neighbor;
                }
            }
        }

        return null;
    }

    // === Helpers ===
    private String key(int x, int y) {
        return x + "," + y;
    }
    
    private FullMapNode getNodeAt(FullMap map, int x, int y) {
        return map.getMapNodes().stream()
            .filter(n -> n.getX() == x && n.getY() == y)
            .findFirst().orElse(null);
    }

}

