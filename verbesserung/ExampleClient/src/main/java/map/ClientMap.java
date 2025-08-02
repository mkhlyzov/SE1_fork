package map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class ClientMap {

    private final List<PlayerHalfMapNode> myNodes = new ArrayList<>();
    private final String playerId;

    public ClientMap(String playerId) {
        this.playerId = playerId;
    }

    public PlayerHalfMap generate() {
        List<PlayerHalfMapNode> nodes = new ArrayList<>();
        Random rand = new Random();

        int width = 10;
        int height = 5;

        int grassCount = 0;
        int waterCount = 0;
        int mountainCount = 0;
        int waterTop = 0, waterBottom = 0, waterLeft = 0, waterRight = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ETerrain terrain;

                boolean isEdgeTop = y == 0;
                boolean isEdgeBottom = y == height - 1;
                boolean isEdgeLeft = x == 0;
                boolean isEdgeRight = x == width - 1;

                ETerrain proposedTerrain = randomTerrain(rand);

                if (proposedTerrain == ETerrain.Water && ((isEdgeTop && waterTop >= 2) ||
                        (isEdgeBottom && waterBottom >= 2) || (isEdgeLeft && waterLeft >= 2) || (isEdgeRight && waterRight >= 2))) {
                    terrain = chooseNonWater(rand);
                } else {
                    terrain = proposedTerrain;
                }

                if (terrain == ETerrain.Water) {
                    waterCount++;
                    if (isEdgeTop) waterTop++;
                    if (isEdgeBottom) waterBottom++;
                    if (isEdgeLeft) waterLeft++;
                    if (isEdgeRight) waterRight++;
                }
                if (terrain == ETerrain.Grass) grassCount++;
                if (terrain == ETerrain.Mountain) mountainCount++;

                PlayerHalfMapNode node = new PlayerHalfMapNode(x, y, false, terrain);
                nodes.add(node);
            }
        }

        if (grassCount < 30 || waterCount < 10 || mountainCount < 5) {
            // System.out.println("⚠️ Bedingungen nicht erfüllt – Map wird neu generiert...");
            return generate();
        }

        // 🏰 Place the fort near the center (x 3-6, y 1-3)
        PlayerHalfMapNode fortNode = null;
        int countfort = 0;
        for (int i = 0; i < 1000 && countfort < 6; ++i){
            int idx = rand.nextInt(nodes.size());
            PlayerHalfMapNode node = nodes.get(idx);
            if (node.getTerrain() == ETerrain.Grass && node.getX() >= 3 && node.getX() <= 6 && node.getY() >= 1 && node.getY() <= 3 && !node.isFortPresent()) {
                fortNode = new PlayerHalfMapNode(node.getX(), node.getY(), true, ETerrain.Grass);
                nodes.set(idx, fortNode);
                countfort++;
                System.out.println("Coordinates of Fort " + node.getX() + node.getY());
            }
        }
        if(countfort < 6) return generate();

        // 🌟 Place the treasure close to the fort (within 2-3 moves)
        for (PlayerHalfMapNode node : nodes) {
            int dist = Math.abs(node.getX() - fortNode.getX()) + Math.abs(node.getY() - fortNode.getY());
            if (dist >= 2 && dist <= 4 && node.getTerrain() == ETerrain.Grass) {
                // Artificially treat it later as containing the treasure
                // Server will recognize based on halfmap-combination
                // So it's enough that bot finds it near the start
                break; // No real treasure placement needed in HalfMap phase
            }
        }

        if (!isMapConnected(nodes)) {
            // System.out.println("🔁 Ungültige Map – wird neu generiert...");
            return generate();
        }

        myNodes.clear();
        myNodes.addAll(nodes);

        return new PlayerHalfMap(playerId, nodes);
    }

    private ETerrain randomTerrain(Random rand) {
        int r = rand.nextInt(100);
        if (r < 80) return ETerrain.Grass;
        if (r < 90) return ETerrain.Mountain;
        return ETerrain.Water;
    }

    private ETerrain chooseNonWater(Random rand) {
        return rand.nextBoolean() ? ETerrain.Grass : ETerrain.Mountain;
    }

    private boolean isMapConnected(List<PlayerHalfMapNode> mapNodes) {
        int width = 10;
        int height = 5;

        boolean[][] visited = new boolean[height][width];
        List<PlayerHalfMapNode> walkables = new ArrayList<>();

        for (PlayerHalfMapNode node : mapNodes) {
            if (node.getTerrain() != ETerrain.Water) {
                walkables.add(node);
            }
        }

        if (walkables.isEmpty()) return false;

        Queue<PlayerHalfMapNode> queue = new LinkedList<>();
        PlayerHalfMapNode start = walkables.get(0);
        queue.add(start);
        visited[start.getY()][start.getX()] = true;

        int connectedCount = 1;

        while (!queue.isEmpty()) {
            PlayerHalfMapNode current = queue.poll();
            int x = current.getX();
            int y = current.getY();

            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !visited[ny][nx]) {
                    for (PlayerHalfMapNode neighbor : walkables) {
                        if (neighbor.getX() == nx && neighbor.getY() == ny) {
                            visited[ny][nx] = true;
                            queue.add(neighbor);
                            connectedCount++;
                            break;
                        }
                    }
                }
            }
        }

        return connectedCount == walkables.size();
    }

    public List<PlayerHalfMapNode> getMyNodes() {
        return myNodes;
    }
}

