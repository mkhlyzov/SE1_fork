package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

public class StrategyPlannedTour implements IStrategy {

    private List<FullMapNode> plannedTour = new LinkedList<>();

    
    public List<FullMapNode> get_plannedTour(){
        return plannedTour;
    }
   
    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        Set<FullMapNode> goals = collectGoals(gameHelper);
        updateBestTour(gameHelper, goals, 25);
        
        return PlayerMove.of(
            gameHelper.getPlayerId(), 
            calculateMove(plannedTour.get(0), plannedTour.get(1))
        );
    }  

    public void updateBestTour(GameHelper gameHelper,Set<FullMapNode> goals, int noiseRepeats)
    {
        assert !goals.isEmpty();
        List <FullMapNode> bestTour = null; 
        double bestScore = -1.0;

        for (int restart = 0; restart < noiseRepeats; restart++) {
            for (FullMapNode anchor: goals) {
                List<FullMapNode> tour = generateFullTourThroughAnchor(gameHelper, anchor, goals);
                double score = computeTourScore_v1(tour, goals, 0.97);
                if (score > bestScore) {
                    bestScore = score;
                    bestTour = tour;
                }
            }
        }

        plannedTour = bestTour;
    }

    List<FullMapNode> generateFullTourThroughAnchor(
        GameHelper gameHelper,
        FullMapNode anchor,
        Set<FullMapNode> goals
    ) {
        Set<FullMapNode> goalsRemaining = new HashSet<>(goals);
        FullMapNode pPos = gameHelper.getMyPosition();

        List<FullMapNode> tour = new ArrayList<>();
        tour.add(pPos);
        FullMapNode current = pPos;
        FullMapNode next = anchor;

        while (!goalsRemaining.isEmpty()) {
            List<FullMapNode> path = continiousPathBFS(current, next, gameHelper, goalsRemaining);
            
            // unpdate goals visibility assuming ALL nodes are Grass.
            // Mountains logic is NOT implemented yet
            for (FullMapNode n : path) {
                if (goalsRemaining.contains(n)) {
                    goalsRemaining.remove(n);
                }
            }
            path.remove(0);
            tour.addAll(path);
            
            current = next;
            next = closestByBFS(current, goalsRemaining, gameHelper);
        }

        return tour;
    }

    public double computeTourScore_v1(List<FullMapNode> tour,Set<FullMapNode> goals,double gamma)
    {
        /*
        Assumptions:
        1. First element is player position
        2. Tour is continuous
        3. Tour covers goals
        */

        if (tour == null || tour.size() < 2)
            return 0.0;

       

        Set<FullMapNode> visited = new HashSet<>();
        visited.add(tour.get(0)); // старт уже посещён

        double score = 0.0;
        int cumulativeCost = 0;

        for (int i = 1; i < tour.size(); i++)
        {
            FullMapNode from = tour.get(i - 1);
            FullMapNode to   = tour.get(i);

            // добавляем стоимость перехода
            cumulativeCost += terrainTransitionCost(from, to);

            // reward = 1 если клетка новая
            if (goals.contains(to) && !visited.contains(to) && to.getTerrain() != ETerrain.Mountain)
            {
                score += Math.pow(gamma, cumulativeCost);
                visited.add(to);
            }

            if(to.getTerrain() == ETerrain.Mountain)
            {
                for(FullMapNode neighbour: goals)
                {
                    if(neighbour.equals(to)) continue;

                    int dx = neighbour.getX() - to.getX(); 
                    int dy = neighbour.getY() - to.getY();
                   
                    if(dx * dx + dy * dy > 2) continue;

                    int dxAbs = Math.abs(dx);
                    int dyAbs = Math.abs(dy);

                    int steps = dxAbs + dyAbs;

                    int extraSteps = 3 + 2 * (steps - 1);
                   
                    score += Math.pow(gamma, cumulativeCost + extraSteps);
                    visited.add(neighbour);
                }    
            }    
        }

        return score;
    }

    public double computeTourScore_v2(List<FullMapNode> tour,Set<FullMapNode> goals, GameHelper gameHelper,double gamma)
    {
        /*
        Assumptions:
        1. First element is player position
        2. Tour is continuous
        3. Tour covers goals
        */

        if (tour == null || tour.size() < 2)
            return 0.0;

        

        Set<FullMapNode> visited = new HashSet<>();
        visited.add(tour.get(0)); // старт уже посещён

        double score = 0.0;
        int cumulativeCost = 0;

        for (int i = 1; i < tour.size(); i++)
        {
            FullMapNode from = tour.get(i - 1);
            FullMapNode to   = tour.get(i);

            // добавляем стоимость перехода
            cumulativeCost += terrainTransitionCost(from, to);

            // reward = 1 если клетка новая
            if (goals.contains(to) && !visited.contains(to) && to.getTerrain() != ETerrain.Mountain)
            {
                score += Math.pow(gamma, cumulativeCost);
                visited.add(to);
            }

            if(to.getTerrain() == ETerrain.Mountain)
            {
                for(FullMapNode neighbour: goals)
                {
                    if(neighbour.equals(to)) continue;

                    int dx = neighbour.getX() - to.getX(); 
                    int dy = neighbour.getY() - to.getY();

                    if(dx * dx + dy * dy > 2) continue;

                    if(!visited.contains(neighbour))
                    {
                        List<FullMapNode> path = continiousPathBFS(to, neighbour, gameHelper, goals);

                        int extraSteps = 0;

                        for(int j = 1; j < path.size(); j++)
                            extraSteps += terrainTransitionCost(path.get(j-1), path.get(j));

                        score += Math.pow(gamma, cumulativeCost + extraSteps);
                        visited.add(neighbour);
                    }
                }
            }
        }

        return score;
    }

    private Set<FullMapNode> collectGoals(GameHelper gameHelper){
        /*  
            In order to find gold and enemy castle agent has to explore the map.
            Function 'colletGoals' chooses which map nodes need to be explored and
            returns a list of them with a purpose of later building a complete
            tour over these map nodes.

            map,hasTreasure,enemySide Coordinates
        */
        // List<FullMapNode> goals = new ArrayList<>();
        // return goals;
        Set<FullMapNode> goals = new HashSet<>();
        boolean hasTreasure = gameHelper.hasTreasure();
        FullMap map = gameHelper.getMap();

       if (hasTreasure) {
            // искать замок на чужой стороне
            map.getMapNodes().stream()
                .filter(n -> n.getFortState() == EFortState.EnemyFortPresent)
                .forEach(goals::add);
            // fallback: исследуем чужую половину
            if (goals.isEmpty()) {
                for (FullMapNode n : map.getMapNodes()) {
                    if (!gameHelper.isVisited(n) &&
                        n.getTerrain() != ETerrain.Water && n.getTerrain() != ETerrain.Mountain &&
                        gameHelper.insideEnemy(n)
                    ) {
                        goals.add(n);
                    }
                }
            }
        } else {
            map.getMapNodes().stream()
                .filter(n -> n.getTreasureState() == ETreasureState.MyTreasureIsPresent)
                .forEach(goals::add);
            if (goals.isEmpty()) {
                for (FullMapNode n : map.getMapNodes()) {
                    if (!gameHelper.isVisited(n) &&
                        n.getTerrain() != ETerrain.Water && n.getTerrain() != ETerrain.Mountain &&
                        gameHelper.insideMine(n)
                    ) {
                        goals.add(n);
                    }
                }
            }
            
        }
        System.out.print("Goals collected: ");
        for (FullMapNode g: goals) {
            System.out.print("(" + g.getX() + ", " + g.getY() + ") ");
        }
        System.out.println();
        return goals;
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        if (to.getX() > from.getX()) return EMove.Right;
        else if (to.getX() < from.getX()) return EMove.Left;
        else if (to.getY() > from.getY()) return EMove.Down;
        else return EMove.Up;
    }

    public FullMapNode closestByBFS(FullMapNode start, Set<FullMapNode> goals, GameHelper gameHelper)
    {
        class PQItem
        {
            final FullMapNode node;
            final int cost;

            PQItem(FullMapNode n, int c) {
                node = n;
                cost = c;
            }  
        }
        PriorityQueue<PQItem> pq = new PriorityQueue<>(Comparator.comparingDouble(it -> it.cost));
        Map <FullMapNode,Integer> bestCost = new HashMap<>();
        pq.add(new PQItem(start, 0));
        bestCost.put(start,0);

        while(!pq.isEmpty())
        {
            PQItem cur = pq.poll();
            if(goals.contains(cur.node) && !gameHelper.isVisited(cur.node))
            {
                return cur.node;
            }
            
            List <FullMapNode> nbs = gameHelper.getNeighbours4(cur.node);
            Collections.shuffle(nbs);

            for(FullMapNode nb: nbs)
            {
                if (!isPassable(nb)) continue;

                
                int newCost = cur.cost + terrainTransitionCost(cur.node, nb);

                int oldCost = bestCost.getOrDefault(nb,Integer.MAX_VALUE);

                if(newCost < oldCost)
                {
                    bestCost.put(nb,newCost);
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
        Set<FullMapNode> goals
    ) {
        // --- small helper class for the priority queue ---
        class PQItem {
            final FullMapNode node;
            final double cost;
            PQItem(FullMapNode n, double c) { node = n; cost = c; }
        }
        
        PriorityQueue<PQItem> pq = new PriorityQueue<>(Comparator.comparingDouble((it -> it.cost)));
        Map<FullMapNode,FullMapNode> parent = new HashMap<>();
        Map<FullMapNode, Double> bestCost = new HashMap<>();

        pq.add(new PQItem(start,0.0));
        parent.put(start,null);
        bestCost.put(start, 0.0);

        while(!pq.isEmpty())
        {   
            PQItem cur = pq.poll();

            if (cur.node.equals(finish)) break;

            List <FullMapNode> nbs = gameHelper.getNeighbours4(cur.node);
            Collections.shuffle(nbs);

            for(FullMapNode nb: nbs)
            {
                if (!isPassable(nb)) continue;

                double reward = goals.contains(nb)? -(1./(goals.size()*2)): 0.0;
                int stepCost = terrainTransitionCost(cur.node, nb);
                double newCost = cur.cost + (double)stepCost + reward; // + noise

                double oldCost = bestCost.getOrDefault(nb, Double.MAX_VALUE);

                if (newCost < oldCost) {
                    bestCost.put(nb, newCost);
                    parent.put(nb, cur.node);
                    pq.add(new PQItem(nb, newCost));
                }
            }
        }
        if(!parent.containsKey(finish)){
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

    private int terrainTransitionCost(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        int fromCost = (from.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        int toCost = (to.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        return fromCost + toCost;
    }
}