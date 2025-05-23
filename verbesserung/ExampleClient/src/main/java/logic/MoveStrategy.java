package logic;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.*;

 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.time.LocalDateTime;
import java.util.*;
import messagesbase.messagesfromclient.ETerrain;

public class MoveStrategy {

     private final Set<String> visitedFields = new HashSet<>();
     private int turnCounter = 0;
     //private boolean treasureSeen = false;
     //private boolean treasureCollected = false;
    //  private boolean enemyFortSeen = false;

    private int myXmin, myXmax, myYmin, myYmax;
    private int enemyXmin, enemyXmax, enemyYmin, enemyYmax;
    private boolean isInitialized = false;
    private boolean treasureLastTurn = false;

   
   
    private void initialize(FullMap map, FullMapNode myPosition) {
        if (!isInitialized) {
            int maxX = getMaxX(map);  // 9 or 19
            int maxY = getMaxY(map);  // 9 or 4
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
            PseudoVisitEnemySide();
        }
    }

    private void PseudoVisitMySide() {
        visitedFields.clear();
        for (int x = myXmin; x < myXmax; x++) {
            for (int y = myYmin; y < myYmax; y++) {
                visitedFields.add(key(x, y));
            }
        }
    }

    private void PseudoVisitEnemySide() {
        visitedFields.clear();
        for (int x = enemyXmin; x < enemyXmax; x++) {
            for (int y = enemyYmin; y < enemyYmax; y++) {
                visitedFields.add(key(x, y));
            }
        }
    }
    
    public PlayerMove calculateNextMove(GameState gameState, UniquePlayerIdentifier playerId) {
        FullMap map = gameState.getMap();
        FullMapNode myPosition = getMyPosition(map);
        initialize(map,myPosition);
        if (myPosition == null) {
            System.err.println("⚠️ Spielerposition nicht gefunden.");
            return PlayerMove.of(playerId, EMove.Right);
        }
        drawMap(map);
        visitedFields.add(key(myPosition.getX(), myPosition.getY()));

        boolean playerHasTreasure = hasTreasure(gameState,playerId);
        FullMapNode goal;

        // 💰 Проверка: если золото исчезло, значит оно было поднято
        if (playerHasTreasure && !treasureLastTurn) {
            System.out.println("💰 Schatz wurde eingesammelt → markiere eigene Seite");
            PseudoVisitMySide();
        }
        treasureLastTurn = playerHasTreasure;

        if (playerHasTreasure) {
            goal = findEnemyFort(map);
            if (goal == null) {
                System.out.println("🔍 Suche Gegnerburg (nur auf feindlicher Seite)");
                goal = findClosestUndiscoveredNode(map, true);
            }
            else
            {
                System.out.println("Coordinates of  Fort: " + goal.getX() + ", " + goal.getY());
            }
        } else {
            goal = findTreasure(map);
            if (goal == null) {
                System.out.println("🔍 Suche Schatz (egal wo)");
                goal = findClosestUndiscoveredNode(map, false);
            }

            else
            {
                System.out.println("Coordinates of  Treasure: " + goal.getX() + ", " + goal.getY());
            }
        }

        if (goal == null) {
            System.out.println("❌ Kein Ziel gefunden – bleibe stehen.");
            return stayClose(myPosition, playerId, map);
        }

        Pathfinder pathfinder = new Pathfinder(map);  
        List<FullMapNode> path = pathfinder.findPath(myPosition.getX(), myPosition.getY(), goal.getX(), goal.getY());

        if (path.isEmpty()) {
            System.out.println("⚠️ Kein Pfad gefunden – bleibe stehen.");
            return stayClose(myPosition, playerId, map);
        }

        FullMapNode next = path.get(0);
        if (next.getTerrain() == ETerrain.Water) {
            System.out.println("🚫 Wasser – bleibe lieber stehen.");
            return stayClose(myPosition, playerId, map);
        }
        
        EMove move = calculateMove(myPosition, next);
        return PlayerMove.of(playerId, move);
    }

    private boolean hasTreasure(GameState gameState, UniquePlayerIdentifier playerId) {
        return gameState.getPlayers().stream()
                        .filter(p->p.getUniquePlayerID().equals(playerId.getUniquePlayerID()))
                        .findFirst()
                        .map(PlayerState::hasCollectedTreasure)
                        .orElse(false);
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

    // private FullMapNode findMyFort(FullMap map)
    // {
    //     return map.getMapNodes().stream()
    //               .filter(n -> n.getFortState() == EFortState.MyFortPresent)
    //               .findFirst().orElse(null);
    // }

    private FullMapNode getMyPosition(FullMap map) {
        return map.getMapNodes().stream()
                .filter(n -> n.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition)
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
        if (to.getX() > from.getX()) return EMove.Right;
        if (to.getX() < from.getX()) return EMove.Left;
        if (to.getY() > from.getY()) return EMove.Down;
        if (to.getY() < from.getY()) return EMove.Up;
        return EMove.Right;
    }

    // 🔍 Find nearest undiscovered node (optionally on enemy side only)
    private FullMapNode findClosestUndiscoveredNode(FullMap map, boolean mustBeOnEnemySide) {
        FullMapNode start = getMyPosition(map);
        if (start == null) return null;

        Map<String, FullMapNode> nodeMap = new HashMap<>();
        for (FullMapNode node : map.getMapNodes()) {
            nodeMap.put(key(node.getX(), node.getY()), node);
        }

        int maxX = getMaxX(map);
        int maxY = getMaxY(map);

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
                if (neighbor == null || neighbor.getTerrain() == ETerrain.Water || neighbor.getTerrain() == ETerrain.Mountain) continue;

                visitedInSearch.add(neighborKey);
                queue.add(neighbor);

                if (!visitedFields.contains(neighborKey)) {
                    if (!mustBeOnEnemySide || (nx >= enemyXmin && nx < enemyXmax && ny >= enemyYmin && ny < enemyYmax)) {
                        return neighbor;
                    }
                }
            }
        }

        return null;
    }

    // === Helpers ===
    private String key(int x, int y) {
        return x + "," + y;
    }

    private int getMaxX(FullMap map) {
        return map.getMapNodes().stream().mapToInt(FullMapNode::getX).max().orElse(0);
    }

    private int getMaxY(FullMap map) {
        return map.getMapNodes().stream().mapToInt(FullMapNode::getY).max().orElse(0);
    }
    
    

    
    
    
    private void drawMap(FullMap map) {
        turnCounter++;
        try (PrintWriter writer = new PrintWriter(new java.io.FileOutputStream("map_log.txt", true))) {
            writer.println("===== Turn #" + turnCounter + " =====");
            writer.println("🕒 " + LocalDateTime.now());
            writer.println();

             boolean treasureVisibleThisTurn = false;
             boolean fortVisibleThisTurn = false;

            for (int y = 0; y < 10; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < 20; x++) {
                    FullMapNode node = getNodeAt(map, x, y);
                    if (node != null) {
                        if (node.getTreasureState() == ETreasureState.MyTreasureIsPresent) treasureVisibleThisTurn = true;
                        if (node.getFortState() == EFortState.EnemyFortPresent) fortVisibleThisTurn = true;
                    }
                    String symbol = getSymbol(node);
                    if(!visitedFields.contains(key(x,y)))
                    {
                        symbol = symbol.toLowerCase();
                    }
                    row.append(symbol);
                }
                writer.println(row);
            }

            writer.println();
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Schreiben der Karte: " + e.getMessage());
        }
    }

    private FullMapNode getNodeAt(FullMap map, int x, int y) {
        return map.getMapNodes().stream()
            .filter(n -> n.getX() == x && n.getY() == y)
            .findFirst().orElse(null);
    }

    private String getSymbol(FullMapNode node) {
        if (node == null) return ".";

        String symbol = switch (node.getTerrain()) {
            case Water -> "W";
            case Grass -> "G";
            case Mountain -> "M";
        };

        if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition) symbol = "P";
        else if (node.getPlayerPositionState() == EPlayerPositionState.EnemyPlayerPosition) symbol = "E";
        else if (node.getFortState() == EFortState.MyFortPresent) symbol = "F";
        else if (node.getFortState() == EFortState.EnemyFortPresent) symbol = "X";
        else if (node.getTreasureState() == ETreasureState.MyTreasureIsPresent) symbol = "T";

        return symbol;
    }

}

