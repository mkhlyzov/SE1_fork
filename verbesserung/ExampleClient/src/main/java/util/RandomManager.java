package util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class RandomManager {

    private static final Logger LOGGER = Logger.getLogger(RandomManager.class.getName());
    private static Random random;
    private static Long currentSeed;
    private static boolean printSeed = true;

    static {
        randomizeSeed();
    }

    public static void randomizeSeed() {
        currentSeed = System.currentTimeMillis();
        random = new Random(currentSeed);
        if (printSeed) {
            LOGGER.info("[RandomManager] New random seed: " + currentSeed);
        }
    }

    public static void setSeed(long seed) {
        currentSeed = seed;
        random = new Random(seed);
        if (printSeed) {
            LOGGER.info("[RandomManager] Seed fixed to: " + seed);
        }
    }

    public static Random getRandom() {

        if (random == null) {
            randomizeSeed();
        }
        return random;
    }

    public static <T> T chooseRandom(List<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        List<T> sortedItems = new ArrayList<>(items);
        sortedItems.sort(Comparator.comparingInt(Object::hashCode));
        return sortedItems.get(getRandom().nextInt(items.size()));
    }
}
