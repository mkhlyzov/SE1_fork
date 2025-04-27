package map;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

import java.util.*;

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
    
        // Track the number of grass and water tiles
        int grassCount = 0;
        int waterCount = 0;
        int mountainCount = 0;
        int waterTop = 0, waterBottom = 0, waterLeft = 0, waterRight = 0;
    
        // Generate the map
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ETerrain terrain;
    
                boolean isEdgeTop = y == 0;
                boolean isEdgeBottom = y == height - 1;
                boolean isEdgeLeft = x == 0;
                boolean isEdgeRight = x == width - 1;
    
                // Apply edge water restrictions
                ETerrain proposedTerrain = randomTerrain(rand);

               // Verhindere zu viel Wasser an den Rändern
               if (proposedTerrain == ETerrain.Water && ((isEdgeTop && waterTop >= 2) ||
               (isEdgeBottom && waterBottom >= 2) || (isEdgeLeft && waterLeft >= 2) || (isEdgeRight && waterRight >= 2))) {
                terrain = chooseNonWater(rand); // Ersatz wenn Grenze erreicht
               } 
               else {
                   terrain = proposedTerrain;
               }

              // Jetzt korrekt zählen
              if (terrain == ETerrain.Water) {
                    waterCount++;
              if (isEdgeTop) waterTop++;
              
              if (isEdgeBottom) waterBottom++;
              
              if (isEdgeLeft) waterLeft++;
              
              if (isEdgeRight) waterRight++;
               }

              if (terrain == ETerrain.Grass) {
                    grassCount++;  // Increment the grass count
              }
              if (terrain == ETerrain.Mountain) {
                    mountainCount++;  // Increment the grass count
              }
               PlayerHalfMapNode node = new PlayerHalfMapNode(x, y, false, terrain);
               nodes.add(node);
            }
        }
    
        // Ensure that we have at least 24 grass fields
        if (grassCount < 24) {
            System.out.println("⚠️ Nicht genug Grasfelder – Map wird neu generiert...");
            return generate(); // Recursively regenerate if not enough grass
        }

        // Ensure that we have at least 7 water fields
        if (waterCount < 7) {
            System.out.println("⚠️ Nicht genug Wasserfelder – Map wird neu generiert...");
            return generate(); // Recursively regenerate if not enough water
        }

        if (mountainCount < 5) {
            System.out.println("⚠️ Nicht genug Bergfelder – Map wird neu generiert...");
            return generate(); // Neu generieren, wenn zu wenige Mountains
        }
    
        // 🏰 Place the fort (on grass only)
        while (true) {
            int idx = rand.nextInt(nodes.size());
            PlayerHalfMapNode node = nodes.get(idx);
            if (node.getTerrain() == ETerrain.Grass) {
                nodes.set(idx, new PlayerHalfMapNode(node.getX(), node.getY(), true, node.getTerrain()));
                break;
            }
        }
    
        // Ensure that the map is connected
        if (!isMapConnected(nodes)) {
            System.out.println("🔁 Ungültige Map – wird neu generiert...");
            return generate(); // Recursively regenerate if the map is not connected
        }
    
        myNodes.clear();
        myNodes.addAll(nodes);
    
        return new PlayerHalfMap(playerId, nodes);
    }

    // Random terrain generator
    private ETerrain randomTerrain(Random rand) {
        int r = rand.nextInt(100);
        if (r < 80) return ETerrain.Grass;
        if (r < 90) return ETerrain.Mountain;
        return ETerrain.Water;
    }

    // Choose a terrain that is not water
    private ETerrain chooseNonWater(Random rand) {
        return rand.nextBoolean() ? ETerrain.Grass : ETerrain.Mountain;
    }

    // Check if the map is connected (i.e., all walkable areas are connected)
    private boolean isMapConnected(List<PlayerHalfMapNode> mapNodes) {
        int width = 10;
        int height = 5;

        boolean[][] visited = new boolean[height][width];
        List<PlayerHalfMapNode> walkables = new ArrayList<>();

        // Add all non-water nodes to walkables list
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

        // Perform BFS to check connectivity
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

    // Get the nodes of the generated map
    public List<PlayerHalfMapNode> getMyNodes() {
        return myNodes;
    }
}




