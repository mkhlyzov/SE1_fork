package map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import util.RandomManager;

public class ClientMap implements IMapGenerator {
  private static final Logger LOGGER = Logger.getLogger(ClientMap.class.getName());
  private final int FORTCOUNT = 1;
  private final int height;
  private final int width;

  private final MapValidator validator;

  public ClientMap() {
    this.height = 5;
    this.width = 10;

    this.validator = new MapValidator();
  }

  public ClientMap(int height, int width) {
    this.height = height;
    this.width = width;

    this.validator = new MapValidator();
  }

  public PlayerHalfMap generate_old(String playerId) {
    List<PlayerHalfMapNode> nodes = new ArrayList<>();
    Random rand = RandomManager.getRandom();

    int total = width * height;
    int mingrassCount = (int) Math.floor(total * 0.48);
    int minwaterCount = (int) Math.floor(total * 0.14);
    int minmountainCount = (int) Math.floor(total * 0.10);
    while (true) {
      nodes.clear();
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

          if (proposedTerrain == ETerrain.Water
              && ((isEdgeTop && waterTop >= 2)
                  || (isEdgeBottom && waterBottom >= 2)
                  || (isEdgeLeft && waterLeft >= 2)
                  || (isEdgeRight && waterRight >= 2))) {
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

          // PlayerHalfMapNode node = new PlayerHalfMapNode(x, y, false, terrain);
          nodes.add(new PlayerHalfMapNode(x, y, false, terrain));
        }
      }

      if (grassCount < mingrassCount
          || waterCount < minwaterCount
          || mountainCount < minmountainCount) {
        // System.out.println("⚠️ Bedingungen nicht erfüllt – Map wird neu
        // generiert...");
        continue;
      }

      // 🏰 Place the fort near the center (x 3-6, y 1-3)
      int countfort = 0;
      for (int i = 0; i < 1000 && countfort < FORTCOUNT; ++i) {
        int idx = rand.nextInt(nodes.size());
        PlayerHalfMapNode node = nodes.get(idx);
        if (node.getTerrain() != ETerrain.Grass) continue;
        // if (node.getX() >= 3 && node.getX() <= 6 && node.getY() >= 1 && node.getY()
        // <= 3 && !node.isFortPresent())
        // continue;
        if (node.isFortPresent()) continue;
        PlayerHalfMapNode fortNode =
            new PlayerHalfMapNode(node.getX(), node.getY(), true, ETerrain.Grass);
        nodes.set(idx, fortNode);
        countfort++;
        LOGGER.fine("Coordinates of Fort " + node.getX() + ", " + node.getY());
      }
      if (countfort < FORTCOUNT) continue;

      if (!validator.isMapConnected(nodes)) {
        // System.out.println("🔁 Ungültige Map – wird neu generiert...");
        continue;
      }
      break;
    }

    return new PlayerHalfMap(playerId, nodes);
  }

  @Override
  public PlayerHalfMap generate(String playerId) {
    List<PlayerHalfMapNode> nodes = new ArrayList<>();
    Random rand = RandomManager.getRandom();

    int total = width * height;
    int mingrassCount = (int) Math.floor(total * 0.48);
    int minwaterCount = (int) Math.floor(total * 0.14);
    int minmountainCount = (int) Math.floor(total * 0.10);
    while (true) {
      nodes.clear();
      int grassCount = 0;
      int waterCount = 0;
      int mountainCount = 0;
      // int waterTop = 0, waterBottom = 0, waterLeft = 0, waterRight = 0;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {

          ETerrain terrain = randomTerrain(rand);

          if (terrain == ETerrain.Water) waterCount++;
          if (terrain == ETerrain.Grass) grassCount++;
          if (terrain == ETerrain.Mountain) mountainCount++;

          // PlayerHalfMapNode node = new PlayerHalfMapNode(x, y, false, terrain);
          nodes.add(new PlayerHalfMapNode(x, y, false, terrain));
        }
      }

      if (grassCount < mingrassCount
          || waterCount < minwaterCount
          || mountainCount < minmountainCount) {
        // System.out.println("⚠️ Bedingungen nicht erfüllt – Map wird neu
        // generiert...");
        continue;
      }

      if (!validator.validateEdgesSimple(nodes)) {
        continue;
      }

      // 🏰 Place the fort near the center (x 3-6, y 1-3)
      int countfort = 0;
      for (int i = 0; i < 1000 && countfort < FORTCOUNT; ++i) {
        int idx = rand.nextInt(nodes.size());
        PlayerHalfMapNode node = nodes.get(idx);
        if (node.getTerrain() != ETerrain.Grass) continue;
        // if (node.getX() >= 3 && node.getX() <= 6 && node.getY() >= 1 && node.getY()
        // <= 3 && !node.isFortPresent())
        // continue;
        if (node.isFortPresent()) continue;
        PlayerHalfMapNode fortNode =
            new PlayerHalfMapNode(node.getX(), node.getY(), true, ETerrain.Grass);
        nodes.set(idx, fortNode);
        countfort++;
        LOGGER.fine("Coordinates of Fort " + node.getX() + ", " + node.getY());
      }
      if (countfort < FORTCOUNT) continue;

      if (!validator.isMapConnected(nodes)) {
        // System.out.println("🔁 Ungültige Map – wird neu generiert...");
        continue;
      }
      break;
    }

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

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }
}

// Extended map borders ( Change Request ): The client has visited medieval
// fortresses and therefore desires stronger defensive measures. Therefore, the
// old rule regarding water at the map half edges is replaced by:
// At least 40% of the fields on each edge of a map half must be accessible.
// At least 20% of the fields on each edge of a map half must be inaccessible.
// The client providing the second half of the map takes the other client's half
// into account as follows:
// At least 40% of the fields on each edge must allow a successful switch from
// the new second half of the map to the previous first half.
