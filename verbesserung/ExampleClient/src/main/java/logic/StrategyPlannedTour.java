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

    private List<Integer> exploration_progress = new ArrayList();
    

   
   
    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        Set<FullMapNode> goals = collectGoals(gameHelper);
        FullMap map = gameHelper.getMap();
        FullMapNode pPos = gameHelper.getMyPosition();
        List<FullMapNode> tour = findBestTour(map,pPos,goals,25);
        return null;
    }


    

    public List<FullMapNode> findBestTour(FullMap map,FullMapNode pPos, Set<FullMapNode> goals, int noiseRepeats)
    {
        assert !goals.isEmpty();
        List <FullMapNode> bestTour = null; 
        double bestCost = Double.MAX_VALUE;

        for (int restart = 0; restart < noiseRepeats; restart++)
        {
            List<FullMapNode> remaining = new ArrayList<>(goals);
            List<FullMapNode> tour = new ArrayList<>();
            FullMapNode currentPos = pPos;

        }    
        return  bestTour;
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

    
   

    FullMapNode closestByBFS(FullMapNode start, Set<FullMapNode> goals, GameHelper gameHelper)
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


    

    


    List<FullMapNode> continiousPathBFS(FullMapNode start, FullMapNode finish, GameHelper gameHelper, Set<FullMapNode> goals) 
    {
            
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
        int fromCost = (from.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        int toCost = (to.getTerrain() == ETerrain.Mountain) ? 2 : 1;
        return fromCost + toCost;
    }


    



}