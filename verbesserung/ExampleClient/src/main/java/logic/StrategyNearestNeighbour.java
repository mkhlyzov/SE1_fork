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
import java.util.Queue;
import java.util.Set;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

public class StrategyNearestNeighbour implements IStrategy {

    private Queue<FullMapNode> plannedTour = new LinkedList<>();
    private int myXmin, myXmax, myYmin, myYmax;
    private int enemyXmin, enemyXmax, enemyYmin, enemyYmax;
    private boolean isInitialized = false;
    // private EMove lastMove = null;
    // private int lastX = -1, lastY = -1;
    
    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        initialize(gameHelper);

        FullMap map = gameHelper.getMap();
        FullMapNode myPosition = gameHelper.getMyPosition();
        UniquePlayerIdentifier playerId = gameHelper.getPlayerId();
        boolean hasTreasure = gameHelper.hasTreasure();
        boolean enemyFortVisible = map.getMapNodes().stream().anyMatch(n -> n.getFortState() == EFortState.EnemyFortPresent);
        FullMapNode peek = plannedTour.peek();
        
        if (myPosition == null) {
            System.out.println("myPosition is null. sending EMove.Right");
            return PlayerMove.of(playerId, EMove.Right);
        }
        
        System.out.println(myPosition);
        System.out.println(myXmin + " " + myXmax + " " + myYmin + " " + myYmax);
        

        // check for certain EVENTS and reconstruct route if needed
        if(hasTreasure  && peek != null && insideMine(peek) || (enemyFortVisible && ( peek == null || peek.getFortState() != EFortState.EnemyFortPresent)))
        {
            plannedTour.clear();
        }
        // если тура нет — строим новый
        gameHelper.playerRecentlyMoved();
        gameHelper.playerRecentlyMoved();
        if (gameHelper.playerRecentlyMoved()){
            System.out.println("playerRecentlyMoved event triggered");
            plannedTour.clear();
        } else {
            System.out.println("playerRecentlyMoved event NOT triggered");
        }
        if (plannedTour.isEmpty()) {
            List<FullMapNode> goals = collectGoals(map, gameHelper, hasTreasure);
            plannedTour = new LinkedList<>(bestNearestNeighbourTour(map, myPosition, goals,25));
        }

        // if (plannedTour.isEmpty()) {
        //     List<FullMapNode> goals = collectGoals(map, gameHelper, hasTreasure);
        //     FullMapNode nearestNow = bfsNearest(myPosition, new HashSet<>(goals), map);
        //     if (nearestNow != null) {
        //         plannedTour.add(nearestNow);
        //     }
        // }

        if (plannedTour.isEmpty()) {
            return PlayerMove.of(playerId, EMove.Right); // fallback
        }

        // if (lastMove != null && myPosition.getX() == lastX && myPosition.getY() == lastY) {
        //     return PlayerMove.of(playerId, lastMove);
        // }

        System.out.print("planned tour: ");
        for(FullMapNode t: plannedTour){
            System.out.print("(" + t.getX() + ", " + t.getY() + ") ");
        }
        System.out.println();


        // Двигаемся к следующей цели
        FullMapNode goal = plannedTour.peek(); // goal = (19, 0)
        Pathfinder pathfinder = new Pathfinder(map);
        // pathfinder.findPath( (18, 0), (19, 0) );
        // path = [19,0)]
        List<FullMapNode> path = pathfinder.findPath(myPosition, goal);

        if (path.isEmpty()) {
            plannedTour.poll(); // цель недостижима
            return PlayerMove.of(playerId, EMove.Right);
        }
        

        FullMapNode next = path.get(0); // next = (19, 0)
        // FullMapNode next = path.size() > 1 ? path.get(1) : goal;
        // if (myPosition.getX() == goal.getX() && myPosition.getY() == goal.getY()) {
        //     plannedTour.poll();
        // }
        // почему на изменение координаты потребовалось 4 шага а не 2
        // первый шаг - движение вправо
        // второй шаг - вниз
        // третий шаг - вниз. на этом шаге координата меняется
        
        

        // EMove move = calculateMove(myPosition, next);
        // lastMove = move;
        // lastX = myPosition.getX();
        // lastY = myPosition.getY();
        // return PlayerMove.of(playerId, move);

        System.out.println("Selected move: " + calculateMove(myPosition, next));

        return PlayerMove.of(playerId, calculateMove(myPosition, next));
    }

    /** Инициализация сторон карты */
    private void initialize(GameHelper gameHelper) {
        if (isInitialized) return;

        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();
        FullMapNode myPos = gameHelper.getMyPosition();

        if (maxX == 9 && maxY == 9) {
            // квадрат 10x10
            myXmin = 0; myXmax = 10;
            if (myPos.getY() < 5) {
                myYmin = 0; myYmax = 5;
                enemyYmin = 5; enemyYmax = 10;
            } else {
                myYmin = 5; myYmax = 10;
                enemyYmin = 0; enemyYmax = 5;
            }
            enemyXmin = 0; enemyXmax = 10;
        } else if (maxX == 19 && maxY == 4) {
            // прямоугольник 20x5
            myYmin = 0; myYmax = 5;
            if (myPos.getX() < 10) {
                myXmin = 0; myXmax = 10;
                enemyXmin = 10; enemyXmax = 20;
            } else {
                myXmin = 10; myXmax = 20;
                enemyXmin = 0; enemyXmax = 10;
            }
            enemyYmin = 0; enemyYmax = 5;
        }
        isInitialized = true;
    }

    /** Цели зависят от состояния игрока */
    private List<FullMapNode> collectGoals(FullMap map, GameHelper helper, boolean hasTreasure) {
        List<FullMapNode> goals = new ArrayList<>();

        if (hasTreasure) {
            // искать замок на чужой стороне
            map.getMapNodes().stream()
                .filter(n -> n.getFortState() == EFortState.EnemyFortPresent)
                .forEach(goals::add);
            // fallback: исследуем чужую половину
            if (goals.isEmpty()) {
                for (FullMapNode n : map.getMapNodes()) {
                    if (!helper.isVisited(n) &&
                        n.getTerrain() != ETerrain.Water && n.getTerrain() != ETerrain.Mountain &&
                        insideEnemy(n)
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
                    if (!helper.isVisited(n) &&
                        n.getTerrain() != ETerrain.Water && n.getTerrain() != ETerrain.Mountain &&
                        insideMine(n)
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

    /** NN-тур, выбираем лучший старт */
    private List<FullMapNode> bestNearestNeighbourTour(FullMap map, FullMapNode start, List<FullMapNode> goals, int noiseRepeats) {
        if (goals.isEmpty()) return Collections.emptyList();
        Pathfinder pathfinder = new Pathfinder(map);

        int bestCost = Integer.MAX_VALUE;
        List<FullMapNode> bestTour = new ArrayList<>();

        for(int i = 0; i < noiseRepeats;i++)
        {
            for (FullMapNode g : goals) {
                List<FullMapNode> tour = nearestNeighbourTour(map, g, new HashSet<>(goals));
                int cost = computeTourCost(pathfinder, start, tour);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestTour = tour;
                }
            }
        }    
        System.out.print("Best Tour: ");
        for(FullMapNode t: bestTour){
            System.out.print("(" + t.getX() + ", " + t.getY() + ") ");
        }
        System.out.println();
        return bestTour;
    }

    /** NN-тур от конкретного старта */
    private List<FullMapNode> nearestNeighbourTour(FullMap map, FullMapNode start, Set<FullMapNode> unvisited) {
        List<FullMapNode> tour = new ArrayList<>();
        FullMapNode current = start;
        Pathfinder pathfinder = new Pathfinder(map);

        tour.add(start);
        unvisited.remove(start);
        while (!unvisited.isEmpty()) {
            FullMapNode nearest = bfsNearest(current, unvisited, map);
            if (nearest == null) break;
            tour.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }
        return tour;
    }

    
    
    
    
    /** BFS для поиска ближайшего кандидата */
    private FullMapNode bfsNearest(FullMapNode start, Set<FullMapNode> goals, FullMap map) {
       
        class PQItem {
            final FullMapNode node;
            final double cost;
            PQItem(FullMapNode n, double c) { node = n; cost = c; }
        }

        PriorityQueue<PQItem> pq =
            new PriorityQueue<>(Comparator.comparingDouble((it -> it.cost)));
        Map<String, Double> best = new HashMap<>();

        String sk = key(start);
        best.put(sk, 0.);
        pq.add(new PQItem(start, 0));

        while (!pq.isEmpty()) {
            PQItem cur = pq.poll();
            String ck = key(cur.node);


            
            if (goals.contains(cur.node)) return cur.node;

            for (FullMapNode nb : getNeighbors(cur.node, map)) {
                double noise = (Math.random() - 0.5) * 0.01; // -0.005 ... 0.005 // 0
                double newCost = cur.cost + stepCost(cur.node, nb) + noise;
                String nk = key(nb);
                if (newCost < best.getOrDefault(nk, Double.MAX_VALUE)) {
                    best.put(nk,newCost);
                    pq.add(new PQItem(nb, newCost));
                }
            }
        }
        return null; 
    } 


    /** Подсчёт стоимости тура */
    private int computeTourCost(Pathfinder pathfinder, FullMapNode start, List<FullMapNode> tour) {
        int cost = 0;
        FullMapNode cur = start;
        for (FullMapNode goal : tour) {
            List<FullMapNode> path = pathfinder.findPath(cur, goal);
            if (path.isEmpty()) return Integer.MAX_VALUE;
            for (int i = 0; i < path.size(); i++) {
                FullMapNode from = (i == 0) ? cur : path.get(i - 1);
                FullMapNode to = path.get(i);
                cost += stepCost(from, to);
            }
            cur = goal;
        }
        return cost;
    }
    
    private int stepCost(FullMapNode from, FullMapNode to) {
        return leaveCost(from.getTerrain()) + enterCost(to.getTerrain());
    }
    
    private int enterCost(ETerrain t) {
        return switch (t) {
            case Grass -> 1;
            case Mountain -> 2;
            case Water -> 9999;
        };
    }
    
    private int leaveCost(ETerrain t) {
        return switch (t) {
            case Grass -> 1;
            case Mountain -> 2;
            case Water -> 9999;
        };
    }

    private List<FullMapNode> getNeighbors(FullMapNode node, FullMap map) {
        List<FullMapNode> res = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = node.getX()+d[0], ny = node.getY()+d[1];
            map.getMapNodes().stream()
                .filter(n -> n.getX()==nx && n.getY()==ny && n.getTerrain()!=ETerrain.Water)
                .findFirst().ifPresent(res::add);
        }
        return res;
    }

    private boolean insideMine(FullMapNode n) {
        return n.getX()>=myXmin && n.getX()<myXmax && n.getY()>=myYmin && n.getY()<myYmax;
    }

    private boolean insideEnemy(FullMapNode n) {
        return n.getX()>=enemyXmin && n.getX()<enemyXmax && n.getY()>=enemyYmin && n.getY()<enemyYmax;
    }

    private String key(FullMapNode n) {
        return n.getX()+","+n.getY();
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        assert dx * dx + dy * dy == 1;

        if (to.getX() > from.getX()) return EMove.Right;
        if (to.getX() < from.getX()) return EMove.Left;
        if (to.getY() > from.getY()) return EMove.Down;
        if (to.getY() < from.getY()) return EMove.Up;
        return EMove.Right;
    }
}
