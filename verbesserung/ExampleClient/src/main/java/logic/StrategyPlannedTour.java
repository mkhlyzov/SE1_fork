package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import util.RandomManager;

public class StrategyPlannedTour implements IStrategy {

    private static final Logger LOGGER = Logger.getLogger(StrategyPlannedTour.class.getName());
    private List<FullMapNode> plannedTour = new LinkedList<>();

    public StrategyPlannedTour() {
    }

    public List<FullMapNode> get_plannedTour() {
        return plannedTour;
    }

    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        if (gameHelper.playerRecentlyMoved()) {

            Set<FullMapNode> goals = collectGoals(gameHelper);
            updateBestTour(gameHelper, goals, 25);
        }

        EMove move = calculateMove(plannedTour.get(0), plannedTour.get(1));

        LOGGER.fine("Selected move: " + move);

        return PlayerMove.of(
                gameHelper.getPlayerId(),
                move);
    }

    public void updateBestTour(GameHelper gameHelper, Set<FullMapNode> goals, int noiseRepeats) {
        assert !goals.isEmpty();
        List<FullMapNode> bestTour = null;
        double bestScore = -1.0;

        for (int restart = 0; restart < noiseRepeats; restart++) {
            for (FullMapNode anchor : goals) {
                List<FullMapNode> tour = generateFullTourThroughAnchor(gameHelper, anchor, goals);
                double score = computeTourScore_v1(tour, goals, 0.97);
                if (score > bestScore) {
                    bestScore = score;
                    bestTour = tour;
                }
            }
        }

        plannedTour = bestTour;

        LOGGER.fine("Best tour: " + plannedTour.stream()
                .map(n -> "(" + n.getX() + "," + n.getY() + "," + n.getTerrain() + ")")
                .collect(Collectors.joining(" ")));

        LOGGER.fine("Best tour score: " + bestScore);
    }

    /**
     * Skips mountain goals that are not useful for exploration.
     *
     * A mountain goal is considered useful only if it can reveal at least one
     * remaining grass goal within distance sqrt(2). If the mountain is not useful,
     * it is removed from the remaining goals and the next closest goal is selected.
     * 
     * Container goalsRemaining will be modified inplace in case mountain was
     * rejected
     * and next was recalculated.
     * 
     * @param next           current candidate goal
     * @param current        current position from which the next goal is chosen
     * @param goalsRemaining mutable set of goals that still need to be covered
     * @param gameHelper     helper used to search for the closest next goal
     * @return the original goal if it is useful, the next useful goal, or null if
     *         none exists
     */
    private FullMapNode skipUselessMountainGoals(
            FullMapNode next,
            FullMapNode current,
            Set<FullMapNode> goalsRemaining,
            GameHelper gameHelper) {
        while (next != null && next.getTerrain() == ETerrain.Mountain) {
            boolean useful = false;

            for (FullMapNode g : goalsRemaining) {
                int dx = g.getX() - next.getX();
                int dy = g.getY() - next.getY();

                if (g.getTerrain() == ETerrain.Grass && (dx * dx + dy * dy <= 2)) {
                    useful = true;
                    break;
                }
            }

            if (useful) {
                break;
            }

            goalsRemaining.remove(next);
            next = closestByBFS(current, goalsRemaining, gameHelper);
        }

        return next;
    }

    /**
     * Removes already covered goals after a path segment has been added to the
     * tour.
     *
     * Every node directly visited by the path is removed from goalsRemaining.
     * Additionally, when the path visits a mountain node, nearby grass goals
     * within observation distance are also removed, because they are considered
     * observed from that mountain.
     *
     * Container goalsRemaining will be modified inplace.
     * 
     * @param path           path segment that was just added to the tour
     * @param goalsRemaining mutable set of goals that still need to be covered
     */
    private void updateGoalsRemainingAfterPath(
            List<FullMapNode> path,
            Set<FullMapNode> goalsRemaining) {
        for (FullMapNode n : path) {
            goalsRemaining.remove(n);

            if (n.getTerrain() == ETerrain.Mountain) {
                for (FullMapNode g : new HashSet<>(goalsRemaining)) {
                    int dx = g.getX() - n.getX();
                    int dy = g.getY() - n.getY();

                    if (g.getTerrain() == ETerrain.Grass && ((dx * dx + dy * dy) <= 2)) {
                        goalsRemaining.remove(g);
                    }
                }
            }
        }
    }

    /**
     * Builds a continuous tour starting from the player's current position and
     * forcing the tour to first try the given anchor goal.
     *
     * The method repeatedly chooses the next closest remaining goal, builds a
     * path to it, removes all goals covered by that path, and appends the path
     * to the resulting tour.
     *
     * Mountain goals that do not help reveal nearby grass goals are skipped.
     *
     * @param gameHelper helper that provides the current player position and map
     *                   neighbours
     * @param anchor     first goal candidate to try in the generated tour
     * @param goals      all goal nodes that should be covered by the tour
     * @return continuous list of map nodes representing the generated tour
     */
    List<FullMapNode> generateFullTourThroughAnchor(
            GameHelper gameHelper,
            FullMapNode anchor,
            Set<FullMapNode> goals) {
        Set<FullMapNode> goalsRemaining = new HashSet<>(goals);
        FullMapNode pPos = gameHelper.getMyPosition();

        List<FullMapNode> tour = new ArrayList<>();
        tour.add(pPos);
        FullMapNode current = pPos;
        FullMapNode next = anchor;

        while (!goalsRemaining.isEmpty()) {

            next = skipUselessMountainGoals(next, current, goalsRemaining, gameHelper);

            if (next == null) {
                break;
            }

            List<FullMapNode> path = continiousPathBFS(current, next, gameHelper, goalsRemaining);

            updateGoalsRemainingAfterPath(path, goalsRemaining);
            path.remove(0);
            tour.addAll(path);

            current = next;
            next = closestByBFS(current, goalsRemaining, gameHelper);
        }

        return tour;
    }

    public double computeTourScore_v1(List<FullMapNode> tour, Set<FullMapNode> goals, double gamma) {
        /*
         * Assumptions:
         * 1. First element is player position
         * 2. Tour is continuous
         * 3. Tour covers goals
         */

        if (tour == null || tour.size() < 2)
            return 0.0;

        Set<FullMapNode> visited = new HashSet<>();
        visited.add(tour.get(0)); // старт уже посещён

        double score = 0.0;
        int cumulativeCost = 0;

        for (int i = 1; i < tour.size(); i++) {
            FullMapNode from = tour.get(i - 1);
            FullMapNode to = tour.get(i);

            // добавляем стоимость перехода
            cumulativeCost += terrainTransitionCost(from, to);

            // reward = 1 если клетка новая
            if (goals.contains(to) && !visited.contains(to) && to.getTerrain() == ETerrain.Grass) {
                score += Math.pow(gamma, cumulativeCost);
                visited.add(to);
            }

            if (to.getTerrain() == ETerrain.Mountain) {
                for (FullMapNode neighbour : goals) {
                    if (neighbour.equals(to))
                        continue;

                    int dx = neighbour.getX() - to.getX();
                    int dy = neighbour.getY() - to.getY();

                    if (dx * dx + dy * dy > 2)
                        continue;

                    int dxAbs = Math.abs(dx);
                    int dyAbs = Math.abs(dy);

                    int steps = dxAbs + dyAbs;

                    int extraSteps = 3 + 2 * (steps - 1);

                    if (!visited.contains(neighbour) && neighbour.getTerrain() == ETerrain.Grass) {
                        score += Math.pow(gamma, cumulativeCost + extraSteps);
                        visited.add(neighbour);
                    }
                }
            }
        }

        return score;
    }

    public double computeTourScore_v2(List<FullMapNode> tour, Set<FullMapNode> goals, GameHelper gameHelper,
            double gamma) {
        /*
         * Assumptions:
         * 1. First element is player position
         * 2. Tour is continuous
         * 3. Tour covers goals
         */

        if (tour == null || tour.size() < 2)
            return 0.0;

        Set<FullMapNode> visited = new HashSet<>();
        visited.add(tour.get(0)); // старт уже посещён

        double score = 0.0;
        int cumulativeCost = 0;

        for (int i = 1; i < tour.size(); i++) {
            FullMapNode from = tour.get(i - 1);
            FullMapNode to = tour.get(i);

            // добавляем стоимость перехода
            cumulativeCost += terrainTransitionCost(from, to);

            // reward = 1 если клетка новая
            if (goals.contains(to) && !visited.contains(to) && to.getTerrain() == ETerrain.Grass) {
                score += Math.pow(gamma, cumulativeCost);
                visited.add(to);
            }

            if (to.getTerrain() == ETerrain.Mountain) {
                for (FullMapNode neighbour : goals) {
                    if (neighbour.equals(to))
                        continue;

                    int dx = neighbour.getX() - to.getX();
                    int dy = neighbour.getY() - to.getY();

                    if (dx * dx + dy * dy > 2)
                        continue;

                    if (!visited.contains(neighbour) && neighbour.getTerrain() == ETerrain.Grass) {
                        List<FullMapNode> path = continiousPathBFS(to, neighbour, gameHelper, goals);

                        int extraSteps = 0;

                        for (int j = 1; j < path.size(); j++)
                            extraSteps += terrainTransitionCost(path.get(j - 1), path.get(j));

                        score += Math.pow(gamma, cumulativeCost + extraSteps);
                        visited.add(neighbour);
                    }
                }
            }
        }

        return score;
    }

    private Set<FullMapNode> getMountainGoals(GameHelper gameHelper, boolean enemySide) {
        return gameHelper.getMap().getMapNodes().stream()
                .filter(n -> !gameHelper.isVisited(n))
                .filter(n -> n.getTerrain() == ETerrain.Mountain)
                .filter(n -> enemySide ? gameHelper.insideEnemy(n) : gameHelper.insideMine(n))
                .collect(Collectors.toSet());
    }

    private Set<FullMapNode> getGrassGoals(GameHelper gameHelper, boolean enemySide) {
        return gameHelper.getMap().getMapNodes().stream()
                .filter(n -> !gameHelper.isObserved(n))
                .filter(n -> n.getTerrain() == ETerrain.Grass)
                .filter(n -> enemySide ? gameHelper.insideEnemy(n) : gameHelper.insideMine(n))
                .collect(Collectors.toSet());
    }

    private Set<FullMapNode> getExpectedFortNodes(GameHelper gameHelper) {
        Point enemyPos = gameHelper.getFirstTrueEnemyPosition();
        Set<FullMapNode> result = new HashSet<>();
        if (enemyPos == null) {
            return result;
        }
        FullMapNode enemyNode = gameHelper.getMap().getMapNodes().stream()
                .filter(n -> n.getX() == enemyPos.x && n.getY() == enemyPos.y)
                .findFirst()
                .orElse(null);

        if (enemyNode == null) {
            return result;
        }
        result = getNodesInRadius(enemyNode, 8, gameHelper).stream()
                .filter(n -> !gameHelper.isObserved(n))
                .filter(n -> gameHelper.insideEnemy(n))
                .collect(Collectors.toSet());
        return result;
    }

    private Set<FullMapNode> collectGoals(GameHelper gameHelper) {
        /*
         * In order to find gold and enemy castle agent has to explore the map.
         * Function 'colletGoals' chooses which map nodes need to be explored and
         * returns a list of them with a purpose of later building a complete
         * tour over these map nodes.
         * 
         * map,hasTreasure,enemySide Coordinates
         */
        // List<FullMapNode> goals = new ArrayList<>();
        // return goals;
        // Set<FullMapNode> goals = new HashSet<>();
        Set<FullMapNode> goals = new LinkedHashSet<>();
        boolean hasTreasure = gameHelper.hasTreasure();
        FullMap map = gameHelper.getMap();

        if (hasTreasure) {
            // искать замок на чужой стороне
            map.getMapNodes().stream()
                    .filter(n -> n.getFortState() == EFortState.EnemyFortPresent)
                    .forEach(goals::add);
            // fallback: исследуем чужую половину
            if (goals.isEmpty()) {
                goals.addAll(getExpectedFortNodes(gameHelper));
            }

            if (goals.isEmpty()) {
                goals.addAll(getMountainGoals(gameHelper, true));
                goals.addAll(getGrassGoals(gameHelper, true));
            }
        } else {
            map.getMapNodes().stream()
                    .filter(n -> n.getTreasureState() == ETreasureState.MyTreasureIsPresent)
                    .forEach(goals::add);

            if (goals.isEmpty()) {
                goals.addAll(getMountainGoals(gameHelper, false));
                goals.addAll(getGrassGoals(gameHelper, false));
            }
        }

        LOGGER.fine(() -> "Goals collected: " + goals.stream()
                .map(g -> "(" + g.getX() + ", " + g.getY() + ") ")
                .collect(Collectors.joining(" ")));
        return goals;
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        if (to.getX() > from.getX())
            return EMove.Right;
        else if (to.getX() < from.getX())
            return EMove.Left;
        else if (to.getY() > from.getY())
            return EMove.Down;
        else
            return EMove.Up;
    }

    public FullMapNode closestByBFS(FullMapNode start, Set<FullMapNode> goals, GameHelper gameHelper) {
        class PQItem {
            final FullMapNode node;
            final int cost;

            PQItem(FullMapNode n, int c) {
                node = n;
                cost = c;
            }
        }
        PriorityQueue<PQItem> pq = new PriorityQueue<>(Comparator.comparingDouble(it -> it.cost));

        Map<FullMapNode, Integer> bestCost = new HashMap<>();
        pq.add(new PQItem(start, 0));
        bestCost.put(start, 0);

        while (!pq.isEmpty()) {
            PQItem cur = pq.poll();
            if (goals.contains(cur.node)) {
                return cur.node;
            }

            List<FullMapNode> nbs = gameHelper.getNeighbours4(cur.node);
            // Collections.shuffle(nbs,RandomManager.getRandom());
            Collections.shuffle(nbs, RandomManager.getRandom());

            for (FullMapNode nb : nbs) {
                if (!isPassable(nb))
                    continue;

                int newCost = cur.cost + terrainTransitionCost(cur.node, nb);

                int oldCost = bestCost.getOrDefault(nb, Integer.MAX_VALUE);

                if (newCost < oldCost) {
                    bestCost.put(nb, newCost);
                    pq.add(new PQItem(nb, newCost));
                }
            }
        }
        return null;
    }

    private boolean isPassable(FullMapNode node) {
        return node.getTerrain() != ETerrain.Water;
    }

    public List<FullMapNode> continiousPathBFS(
            FullMapNode start,
            FullMapNode finish,
            GameHelper gameHelper,
            Set<FullMapNode> goals) {
        // --- small helper class for the priority queue ---
        class PQItem {
            final FullMapNode node;
            final double cost;

            PQItem(FullMapNode n, double c) {
                node = n;
                cost = c;
            }
        }

        PriorityQueue<PQItem> pq = new PriorityQueue<>(Comparator.comparingDouble((it -> it.cost)));

        Map<FullMapNode, FullMapNode> parent = new HashMap<>();
        Map<FullMapNode, Double> bestCost = new HashMap<>();

        pq.add(new PQItem(start, 0.0));
        parent.put(start, null);
        bestCost.put(start, 0.0);

        while (!pq.isEmpty()) {
            PQItem cur = pq.poll();

            if (cur.node.equals(finish))
                break;

            List<FullMapNode> nbs = gameHelper.getNeighbours4(cur.node);
            // Collections.shuffle(nbs);
            Collections.shuffle(nbs, RandomManager.getRandom());

            for (FullMapNode nb : nbs) {
                if (!isPassable(nb))
                    continue;

                double reward = goals.contains(nb) ? -(1. / (goals.size() * 2)) : 0.0;
                int stepCost = terrainTransitionCost(cur.node, nb);
                double newCost = cur.cost + (double) stepCost + reward; // + noise

                double oldCost = bestCost.getOrDefault(nb, Double.MAX_VALUE);

                if (newCost < oldCost) {
                    bestCost.put(nb, newCost);
                    parent.put(nb, cur.node);
                    pq.add(new PQItem(nb, newCost));
                }
            }
        }
        if (!parent.containsKey(finish)) {
            return List.of();
        }

        LinkedList<FullMapNode> path = new LinkedList<>();
        FullMapNode walk = finish;

        while (walk != null) {
            path.addFirst(walk);
            walk = parent.get(walk);
        }

        return path;
    }

    /**
     * Returns all map nodes that can be reached from the given center node
     * with a total movement cost less than or equal to the given radius.
     *
     * Movement is calculated using 4-neighbour movement only.
     * The cost of moving from one node to another is the sum of both terrain costs:
     * Grass = 1, Mountain = 2.
     *
     * @param center the start node of the radius search
     * @param radius the maximum allowed movement cost
     * @return a set of all reachable nodes inside the given radius, including
     *         center
     */
    public Set<FullMapNode> getNodesInRadius(FullMapNode center, int radius, GameHelper gameHelper) {
        class PQItem {
            final FullMapNode node;
            final int cost;

            PQItem(FullMapNode node, int cost) {
                this.node = node;
                this.cost = cost;
            }
        }

        Set<FullMapNode> result = new HashSet<>();
        Map<FullMapNode, Integer> bestCost = new HashMap<>();

        PriorityQueue<PQItem> pq = new PriorityQueue<>(Comparator.comparingInt(it -> it.cost));

        pq.add(new PQItem(center, 0));
        bestCost.put(center, 0);

        while (!pq.isEmpty()) {
            PQItem cur = pq.poll();

            if (cur.cost > bestCost.get(cur.node)) {
                continue;
            }

            result.add(cur.node);

            for (FullMapNode nb : gameHelper.getNeighbours4(cur.node)) {
                int newCost = cur.cost + terrainTransitionCost(cur.node, nb);

                if (newCost <= radius &&
                        newCost < bestCost.getOrDefault(nb, Integer.MAX_VALUE)) {

                    bestCost.put(nb, newCost);
                    pq.add(new PQItem(nb, newCost));
                }
            }
        }

        return result;
    }

    /**
     * Calculates the movement cost between two directly adjacent nodes.
     *
     * The transition cost is the sum of terrain costs of both nodes:
     * Grass = 1, Mountain = 2.
     *
     * @param from the source node
     * @param to   the directly adjacent target node
     * @return the movement cost between the two nodes
     */
    private int terrainTransitionCost(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        int fromCost = (from.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        int toCost = (to.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        return fromCost + toCost;
    }
}
