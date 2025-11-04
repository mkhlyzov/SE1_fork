package logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

public class StrategyPlannedTour implements IStrategy {

    private List<Integer> exploration_progress = new ArrayList();
    private int myXmin;
    private int myXmax;
    private int myYmin;
    private int myYmax;
    private int enemyXmin;
    private int enemyXmax;
    private int enemyYmin;
    private int enemyYmax;
    private boolean isInitialized = false;

    private boolean insideMine(FullMapNode n) {
        return n.getX() >= myXmin && n.getX() < myXmax && n.getY() >= myYmin && n.getY() < myYmax;
    }

    private boolean insideEnemy(FullMapNode n) {
        return n.getX() >= enemyXmin && n.getX() < enemyXmax && n.getY() >= enemyYmin && n.getY() < enemyYmax;
    }

    private void initialize(GameHelper gameHelper) {
        FullMap map = gameHelper.getMap();
        FullMapNode myPosition = gameHelper.getMyPosition();
        if (!isInitialized) {
            int maxX = gameHelper.getMaxX();  // 9 or 19
            int maxY = gameHelper.getMaxY();  // 9 or 4
            int playerX = myPosition.getX();
            int playerY = myPosition.getY();

        if (maxX == 9 && maxY == 9) {
            // 10x10 — split horizontally
            myXmin = 0; myXmax = 10;
            myYmin = 0; myYmax = 5;

            if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 5; enemyYmax = 10;
            } else {
                myXmin = 0; myXmax = 10;
                myYmin = 5; myYmax = 10;

                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 0; enemyYmax = 5;
            }
        } else if (maxX == 19 && maxY == 4) {
            // 20x5 — split vertically
            myXmin = 0; myXmax = 10;
            myYmin = 0; myYmax = 5;

            if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                enemyXmin = 10; enemyXmax = 20;
                enemyYmin = 0; enemyYmax = 5;
            } else {
                myXmin = 10; myXmax = 20;
                myYmin = 0; myYmax = 5;

                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 0; enemyYmax = 5;
            }
        } else {
            System.err.println("Unknown map format (" + (maxX + 1) + " x " + (maxY + 1) + ")");
        }
        isInitialized = true;
        }
    }

    
    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        initialize(gameHelper);

        
        FullMapNode myPos = gameHelper.getMyPosition();
        if (myPos == null) {
            System.out.println("⚠️ My position is null, fallback move → RIGHT");
            return PlayerMove.of(gameHelper.getPlayerId(), EMove.Right);
        }

        
        List<FullMapNode> goals = collectGoals(gameHelper);
        if (goals.isEmpty()) {
            System.out.println("ℹ️ No goals found — random fallback move → RIGHT");
            return PlayerMove.of(gameHelper.getPlayerId(), EMove.Right);
        }

        
        FullMapNode nearestGoal = closestByBFS(myPos, goals, gameHelper);

        
        if (nearestGoal == null) {
            nearestGoal = findNearestByManhattan(myPos, goals);
            if (nearestGoal == null) {
                System.out.println("⚠️ No reachable goal found — fallback → RIGHT");
                return PlayerMove.of(gameHelper.getPlayerId(), EMove.Right);
            }
        }

        
        List<FullMapNode> path = continiousPath(myPos, nearestGoal, gameHelper);
        if (path == null || path.isEmpty()) {
            System.out.println("⚠️ Path to goal not found — fallback → RIGHT");
            return PlayerMove.of(gameHelper.getPlayerId(), EMove.Right);
        }

        
        FullMapNode nextStep = path.get(0);
        EMove move = calculateMove(myPos, nextStep);

        System.out.printf("➡️ Moving from (%d,%d) to (%d,%d) [%s]%n",
            myPos.getX(), myPos.getY(), nextStep.getX(), nextStep.getY(), move);

        
        return PlayerMove.of(gameHelper.getPlayerId(), move);
    }


    

    


    private List<FullMapNode> collectGoals(GameHelper gameHelper){
        /*  
            In order to find gold and enemy castle agent has to explore the map.
            Function 'colletGoals' chooses which map nodes need to be explored and
            returns a list of them with a purpose of later building a complete
            tour over these map nodes.

            map,hasTreasure,enemySide Coordinates
        */
        // List<FullMapNode> goals = new ArrayList<>();
        // return goals;
        List<FullMapNode> goals = new ArrayList<>();
        List<FullMapNode> mapNodes = new ArrayList<>(gameHelper.getMap().getMapNodes());

        for (FullMapNode node : mapNodes) {
            boolean notVisited = !gameHelper.isVisited(node);

            // My side exploration: grass or mountain, not yet visited
            if (insideMine(node) && notVisited) {
                if (node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain) {
                    goals.add(node);
                }
            }

            // Enemy side exploration
            if (insideEnemy(node) && notVisited) {
                if (node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain) 
                {
                    goals.add(node);
                }
            
            }
        }

        return goals;
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) { 
        int dx = to.getX() - from.getX(); 
        int dy = to.getY() - from.getY(); 
        if (dx == 1 && dy == 0) return EMove.Right; 
        if (dx == -1 && dy == 0) return EMove.Left; 
        if (dx == 0 && dy == 1) return EMove.Down; 
        if (dx == 0 && dy == -1) return EMove.Up; 
        
        return EMove.Right; // fallback }
    }
    

    
    private FullMapNode closestByBFS(FullMapNode start, List<FullMapNode> goals, GameHelper gameHelper) {
        List<FullMapNode> mapNodes = new ArrayList<>(gameHelper.getMap().getMapNodes());
        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();

        // Use a queue for BFS
        List<FullMapNode> queue = new ArrayList<>();
        List<FullMapNode> visited = new ArrayList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            FullMapNode current = queue.remove(0);

            // If this node is a goal → return immediately
            if (goals.contains(current)) {
                return current;
            }

            // Explore 4 neighbours
            for (FullMapNode neighbor : getNeighbours(current, mapNodes, maxX, maxY)) {
                if (!visited.contains(neighbor) && isPassable(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // If no goal reachable
        return null;
    }


    private boolean isPassable(FullMapNode node) {
        ETerrain t = node.getTerrain();
        return (t == ETerrain.Grass || t == ETerrain.Mountain);
    }


    private List<FullMapNode> getNeighbours(FullMapNode node, List<FullMapNode> mapNodes, int maxX, int maxY) {
        List<FullMapNode> neighbours = new ArrayList<>();
        int x = node.getX();
        int y = node.getY();

        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 0 && ny >= 0 && nx <= maxX && ny <= maxY) {
                for (FullMapNode n : mapNodes) {
                    if (n.getX() == nx && n.getY() == ny) {
                        neighbours.add(n);
                        break;
                    }
                }
            }
        }
        return neighbours;
    }


    
    private List<FullMapNode> continiousPath(FullMapNode start, FullMapNode goal, GameHelper gameHelper) {
        List<FullMapNode> mapNodes = new ArrayList<>(gameHelper.getMap().getMapNodes());
        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();

        // queue & visited keyed by "x,y" to avoid relying on equals/hashCode
        ArrayDeque<FullMapNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>(); // childKey -> parentKey
        Map<String, FullMapNode> byKey = new HashMap<>();

        // utility to make keys
        Function<FullMapNode, String> keyOf = n -> n.getX() + "," + n.getY();

        // index nodes by key for quick lookup during backtracking
        for (FullMapNode n : mapNodes) {
            byKey.put(keyOf.apply(n), n);
        }

        // init BFS
        String startKey = keyOf.apply(start);
        String goalKey  = keyOf.apply(goal);
        queue.add(start);
        visited.add(startKey);
        parent.put(startKey, null);

        // BFS loop
        boolean found = false;
        while (!queue.isEmpty()) {
            FullMapNode current = queue.poll();
            String cKey = keyOf.apply(current);

            if (cKey.equals(goalKey)) {
                found = true;
                break;
            }

            for (FullMapNode nb : getNeighbours(current, mapNodes, maxX, maxY)) {
                if (!isPassable(nb)) continue;
                String nKey = keyOf.apply(nb);
                if (visited.contains(nKey)) continue;

                visited.add(nKey);
                parent.put(nKey, cKey);
                queue.add(nb);
            }
        }

        // No path
        if (!found) return new ArrayList<>();

        
        LinkedList<FullMapNode> path = new LinkedList<>();
        String walk = goalKey;
        while (walk != null) {
            FullMapNode node = byKey.get(walk);
            path.addFirst(node);
            walk = parent.get(walk);
        }

        if (!path.isEmpty()) {
            path.removeFirst();
        }

        return path;
    }


    private FullMapNode findNearestByManhattan(FullMapNode from, List<FullMapNode> goals) {
        FullMapNode best = null;
        int bestDist = Integer.MAX_VALUE;
        for (FullMapNode g : goals) {
            int d = Math.abs(g.getX() - from.getX()) + Math.abs(g.getY() - from.getY());
            if (d < bestDist) {
                bestDist = d;
                best = g;
            }
        }
        return best;
    }


}