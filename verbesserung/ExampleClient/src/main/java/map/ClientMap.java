package map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class ClientMap {
    private final String playerId;

    public ClientMap(String playerId) {
        this.playerId = playerId;
    }

    public PlayerHalfMap generate() {
        List<PlayerHalfMapNode> nodes = new ArrayList<>();
        Random rand = new Random();

        int width = 10;
        int height = 5;
        int total = width * height;

        
        int grassCount = (int)Math.floor(total * 0.48);
        int waterCount = (int)Math.floor(total * 0.14);
        int mountainCount = (int)Math.floor(total * 0.10);
        int castleCount = (int)Math.floor(total * 0.12);

        
        int used = grassCount + waterCount + mountainCount + castleCount;
        int leftover = total - used;
        grassCount += leftover + castleCount; 

        while (true) {
            nodes.clear();

            
            List<ETerrain> terrains = new ArrayList<>();
            for (int i = 0; i < grassCount; i++) terrains.add(ETerrain.Grass);
            for (int i = 0; i < waterCount; i++) terrains.add(ETerrain.Water);
            for (int i = 0; i < mountainCount; i++) terrains.add(ETerrain.Mountain);

            
            Collections.shuffle(terrains, rand);

           
            int idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    nodes.add(new PlayerHalfMapNode(x, y, false, terrains.get(idx++)));
                }
            }

            
            int placed = 0;
            for (PlayerHalfMapNode n : nodes) {
                if (n.getTerrain() == ETerrain.Grass && placed < castleCount) {
                    nodes.set(nodes.indexOf(n),
                        new PlayerHalfMapNode(n.getX(), n.getY(), true, ETerrain.Grass));
                    placed++;
                }
            }
            if (placed < castleCount) continue;

            if (!isMapConnected(nodes)) continue;

            break;

        }

        return new PlayerHalfMap(playerId, nodes); 
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
}