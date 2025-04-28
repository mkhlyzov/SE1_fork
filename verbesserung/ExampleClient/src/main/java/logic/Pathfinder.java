package logic;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import java.util.Comparator;

import java.util.*;

public class Pathfinder {

    private final Map<String, FullMapNode> graph = new HashMap<>();

    public Pathfinder(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            String key = key(node.getX(), node.getY());
            graph.put(key, node);
        }
    }

    private String key(int x, int y) {
        return x + "," + y;
    }

    private List<FullMapNode> getNeighbors(FullMapNode node) {
        List<FullMapNode> neighbors = new ArrayList<>();
        int[][] directions = {{0,1}, {1,0}, {-1,0}, {0,-1}};
        for (int[] dir : directions) {
            int nx = node.getX() + dir[0];
            int ny = node.getY() + dir[1];
            String k = key(nx, ny);
            if (graph.containsKey(k)) {
                FullMapNode neighbor = graph.get(k);
                if (isWalkable(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    private boolean isWalkable(FullMapNode node) {
        ETerrain terrain = node.getTerrain();
        return terrain == ETerrain.Grass || terrain == ETerrain.Mountain;
    }

    private int getTerrainCost(FullMapNode node) {
        ETerrain terrain = node.getTerrain();
        switch (terrain) {
            case Grass:
                return 1;
            case Mountain:
                return 8;
            case Water:
                return 9999; // sollte nie genommen werden
            default:
                return 10;
        }
    }

    public List<FullMapNode> findPath(int startX, int startY, int goalX, int goalY) {
        String startKey = key(startX, startY);
        String goalKey = key(goalX, goalY);

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.priority));
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, Integer> costSoFar = new HashMap<>();

        frontier.add(new Node(startKey, 0));
        cameFrom.put(startKey, null);
        costSoFar.put(startKey, 0);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            if (current.key.equals(goalKey)) {
                break;
            }

            FullMapNode currentNode = graph.get(current.key);
            for (FullMapNode neighbor : getNeighbors(currentNode)) {
                if (neighbor.getTerrain() == ETerrain.Water) {
                    continue; // 🚫 Never go into water!
                }
                String neighborKey = key(neighbor.getX(), neighbor.getY());
                int newCost = costSoFar.get(current.key) + getTerrainCost(neighbor);

                if (!costSoFar.containsKey(neighborKey) || newCost < costSoFar.get(neighborKey)) {
                    costSoFar.put(neighborKey, newCost);
                    int heuristic = Math.abs(goalX - neighbor.getX()) + Math.abs(goalY - neighbor.getY());
                    int priority = newCost + heuristic;
                    frontier.add(new Node(neighborKey, priority));
                    cameFrom.put(neighborKey, current.key);
                }
            }
        }

        // Pfad zurückverfolgen
        List<FullMapNode> path = new ArrayList<>();
        String currentKey = goalKey;

        while (!currentKey.equals(startKey)) {
            if (!cameFrom.containsKey(currentKey)) {
                return new ArrayList<>(); // Kein Pfad gefunden
            }
            path.add(graph.get(currentKey));
            currentKey = cameFrom.get(currentKey);
        }

        Collections.reverse(path);
        return path;
    }

    private static class Node {
        String key;
        int priority;

        Node(String key, int priority) {
            this.key = key;
            this.priority = priority;
        }
    }
}