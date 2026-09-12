package map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class MapValidator {
    private int getMaxX(List<PlayerHalfMapNode> mapNodes) {
        int maxX = 0;
        for (PlayerHalfMapNode node : mapNodes) {
            maxX = Math.max(maxX, node.getX());
        }
        return maxX;
    }

    private int getMaxY(List<PlayerHalfMapNode> mapNodes) {
        int maxY = 0;
        for (PlayerHalfMapNode node : mapNodes) {
            maxY = Math.max(maxY, node.getY());
        }
        return maxY;
    }

    public boolean isMapConnected(List<PlayerHalfMapNode> mapNodes) {
        int width = getMaxX(mapNodes) + 1;
        int height = getMaxY(mapNodes) + 1;

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

  public boolean validateEdgesSimple(List<PlayerHalfMapNode> nodes) {
    int width = getMaxX(nodes) + 1;
    int height = getMaxY(nodes) + 1;

    if (!checkline(nodes, 0, true, width)) return false;
    if (!checkline(nodes, height - 1, true, width)) return false;

    if (!checkline(nodes, 0, false, height)) return false;
    if (!checkline(nodes, width - 1, false, height)) return false;
    return true;
  }

  private boolean checkline(
      List<PlayerHalfMapNode> nodes, int fixed, boolean horizontal, int length) {
    int accessible = 0;
    int water = 0;
    for (PlayerHalfMapNode n : nodes) {
      boolean onLine = horizontal ? n.getY() == fixed : n.getX() == fixed;
      if (!onLine) continue;

      if (n.getTerrain() == ETerrain.Water) water++;
      else accessible++;
    }
    int minAccessible = (int) Math.ceil(length * 0.40);
    int minWater = (int) Math.ceil(length * 0.20);

    return accessible >= minAccessible && water >= minWater;
  }
}
