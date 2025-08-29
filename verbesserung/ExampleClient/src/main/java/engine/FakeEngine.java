package engine;



import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import map.ClientMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class FakeEngine {

    private UniquePlayerIdentifier playerid;

    private ETerrain[][] terrainGrid;
    private int WIDTH;
    private int HEIGHT; 

    private Point playerPos;
    private Point treasurePos;
    private Point fortPos;
    private Point enemyFortPos;
    private boolean treasureWasCollected = false;
    private boolean treasureWasObserved = false;
    private boolean enemyFortWasObserved = false;

    private List<PlayerMove> movesBuffer = new ArrayList<>();
    private EPlayerGameState gameState = EPlayerGameState.MustAct;

    public FakeEngine(){}

    public void createPlayer() {
        this.playerid = new UniquePlayerIdentifier("FakePlayer-1");
    }

    public void generateMap(PlayerHalfMap halfMapData){
        ClientMap map = new ClientMap("FakePlayer-2");
        PlayerHalfMap half2 = map.generate();   
        half2 = normalizeFortCount(half2);

        halfMapData = normalizeFortCount(halfMapData);
        
        if (new Random().nextBoolean()) {
            halfMapData = shiftCoordinates(halfMapData);
        } else {
            half2 = shiftCoordinates(half2);
        }
        
        treasurePos = addTreasureNearFort(halfMapData);
        if (treasurePos == null) {
            System.err.println("Could not place Treasuere on the map");
        }

        PlayerHalfMapNode fort = halfMapData.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);      
        fortPos = new Point(fort.getX(), fort.getY());
        playerPos = new Point(fort.getX(),fort.getY());
        
        PlayerHalfMapNode enemyFort = half2.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);
        enemyFortPos = new Point(enemyFort.getX(), enemyFort.getY());
        FullMap fullMap = combineHalfMaps(halfMapData, half2);
        createTerrainArray(fullMap);
    }

    private PlayerHalfMap normalizeFortCount(PlayerHalfMap half) {
        List<PlayerHalfMapNode> forts = half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .toList();
        List<PlayerHalfMapNode> nodes = new ArrayList<>(half.getMapNodes());
        
        Random r = new Random();
        PlayerHalfMapNode keep = forts.get(r.nextInt(forts.size()));
        
        for (int i = 0; i < nodes.size(); i++) {
            PlayerHalfMapNode n = nodes.get(i);
            if (n.isFortPresent() && !(n.getX() == keep.getX() && n.getY() == keep.getY())) {
                nodes.set(i, new PlayerHalfMapNode(
                    n.getX(), n.getY(), false, n.getTerrain()
                ));
            }
        }

        return new PlayerHalfMap(half.getUniquePlayerID(),nodes);
    }

    private PlayerHalfMap shiftCoordinates(PlayerHalfMap halfMapData){
        boolean makeSquare = new Random().nextBoolean();
        List<PlayerHalfMapNode> newNodes = new ArrayList<>();
        if (makeSquare) {
            int maxY = halfMapData.getMapNodes().stream().mapToInt(n->n.getY()).max().orElse(0);
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                    node.getX(),
                    node.getY() + maxY + 1,
                    node.isFortPresent(),
                    node.getTerrain()
                ));
            }
        } else {
            int maxX = halfMapData.getMapNodes().stream().mapToInt(n->n.getX()).max().orElse(0);
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                    node.getX() + maxX + 1,
                    node.getY(),
                    node.isFortPresent(),
                    node.getTerrain()
                ));
            }
        }
        return new PlayerHalfMap(halfMapData.getUniquePlayerID(),newNodes);
    }

    private Point addTreasureNearFort(PlayerHalfMap half) {
        PlayerHalfMapNode fortNode = half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);

        if (fortNode == null) return null;

        Random r = new Random();
        List<PlayerHalfMapNode> candidates = half.getMapNodes().stream()
                .filter(n -> n.getTerrain() == ETerrain.Grass)
                // .filter(n -> Math.abs(n.getX() - fort.getX()) + Math.abs(n.getY() - fort.getY()) <= 3)
                // .filter(n -> !(n.getX() == fort.getX() && n.getY() == fort.getY()))
                .filter(n->!n.isFortPresent())
                .toList();

        if (candidates.isEmpty()) return null;

        PlayerHalfMapNode gold = candidates.get(r.nextInt(candidates.size()));
        return new Point(gold.getX(), gold.getY());
    }

    private FullMap combineHalfMaps(PlayerHalfMap half1, PlayerHalfMap half2) {
        List<PlayerHalfMapNode> combinedNodes = new ArrayList<>();
        combinedNodes.addAll(half1.getMapNodes());
        combinedNodes.addAll(half2.getMapNodes());
        
        List<FullMapNode> fullMapNodes = new ArrayList<>();

        for (PlayerHalfMapNode node : combinedNodes) {
            
            fullMapNodes.add(new FullMapNode(
                    node.getTerrain(),
                    EPlayerPositionState.NoPlayerPresent,
                    ETreasureState.NoOrUnknownTreasureState,
                    EFortState.NoOrUnknownFortState,
                    node.getX(),
                    node.getY()
            ));
        }

        return new FullMap(fullMapNodes);
    }

    private void createTerrainArray(FullMap fullMap) {
        WIDTH = fullMap.getMapNodes().stream()
        .mapToInt(FullMapNode::getX)
        .max()
        .orElse(0) + 1;
        //x_Coordinates: 0,1,2,3,4,5,6,7,8,9
        // WIDTH = 10
        HEIGHT = fullMap.getMapNodes().stream()
        .mapToInt(FullMapNode::getY)
        .max()
        .orElse(0) + 1;
        //y_Coordinates: 0,1,2,3,4,5,6,7,8,9
        // HEIGHT = 10
        terrainGrid = new ETerrain[WIDTH][HEIGHT]; 
        for (FullMapNode node : fullMap.getMapNodes()) {
            terrainGrid[node.getX()][node.getY()] = node.getTerrain();
        }
    }
    
    public void applyMove(PlayerMove move){
        int dx = 0, dy = 0;

        switch(move.getMove()){
            case Up -> dy = -1;
            case Down -> dy = 1;
            case Left -> dx = -1;
            case Right -> dx = 1;
        }
        Point current = playerPos;
        Point newPos = new Point(current.x + dx, current.y + dy);
       
        resetBufferIfDirectionChanged(move);
        movesBuffer.add(move);

        int required = stepCost(current, newPos);
        if(movesBuffer.size() >= required)
        {
            movesBuffer.clear();
            playerPos = newPos;
            
            updateObjectivesVisibility(playerPos);

            if(playerPos.equals(treasurePos)) {
                treasureWasCollected = true;
            }
        }

        if (!inBounds(playerPos) || isWater(playerPos)) 
        { 
            gameState = EPlayerGameState.Lost;
        }
        
        if(treasureWasCollected && playerPos.equals(enemyFortPos))
        {
            gameState = EPlayerGameState.Won;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sleep unterbrochen: " + e.getMessage());
        }
    }
    
    private void updateObjectivesVisibility(Point pos) {
        ETerrain currentTerrain = getTerrain(pos.x, pos.y);
        if (currentTerrain == ETerrain.Mountain) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Point neighbour = new Point(pos.x + dx, pos.y + dy);
                    if (!inBounds(neighbour)) continue;
                    if(treasurePos.equals(neighbour)){
                        treasureWasObserved = true;
                    }
                    if(enemyFortPos.equals(neighbour)){
                        enemyFortWasObserved = true;
                    }
                }
            }
        } else {
            if(enemyFortPos.equals(pos)) {
                enemyFortWasObserved = true;
            }
            if(treasurePos.equals(pos)) {
                treasureWasObserved = true;
            }
        }
    }

    private void resetBufferIfDirectionChanged(PlayerMove current) {
        if (!movesBuffer.isEmpty()) {
            PlayerMove last = movesBuffer.get(movesBuffer.size() - 1);
            if (last.getMove() != current.getMove()) {
                movesBuffer.clear();              
            }
        }
    }
    
    public GameState getState() {
        PlayerState myPlayer = new PlayerState(
            "Fake", "Player", "fake_user",
            gameState,
            playerid,
            treasureWasCollected
        );
        Set <PlayerState> players = Set.of(myPlayer);

        if (terrainGrid == null) 
            return new GameState(players,"ABC");

        // При возврате карты – обновляем состояние золота
        List <FullMapNode> updatedNodes = new ArrayList<>();
        for(int x = 0; x < WIDTH; x++)
        {
            for(int y = 0; y < HEIGHT; y++)
            {
                ETerrain terrain = terrainGrid[x][y];
                Point p = new Point(x,y);
                // boolean isVisible = visibleCells.contains(p);
                ETreasureState treasureState = (treasurePos.equals(p) && !treasureWasCollected && treasureWasObserved) ? ETreasureState.MyTreasureIsPresent : ETreasureState.NoOrUnknownTreasureState;
                EPlayerPositionState positionState = (playerPos.equals(p)) ? EPlayerPositionState.MyPlayerPosition : EPlayerPositionState.NoPlayerPresent;

                // EFortState fortState = (fortPos.equals(p)) ? EFortState.MyFortPresent : EFortState.NoOrUnknownFortState;

                EFortState fortState = fortPos.equals(p) ? EFortState.MyFortPresent : ((enemyFortPos.equals(p) && enemyFortWasObserved) ? EFortState.EnemyFortPresent : EFortState.NoOrUnknownFortState);

                updatedNodes.add(new FullMapNode(terrain,positionState,treasureState,fortState,x,y));
            }
        }
       

        FullMap map = new FullMap(updatedNodes);

        return new GameState(map,players,"ABC");
    }

    public UniquePlayerIdentifier getPlayerId() {
        return playerid;
    }
       
    private ETerrain getTerrain(int x, int y) {
        return terrainGrid[x][y];           // terrain[x][y]
    }
    
    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < WIDTH && p.y >= 0 && p.y < HEIGHT;
    }
    private boolean isWater(Point p) {
        return getTerrain(p.x, p.y) == ETerrain.Water;
    }

    private int enterCost(ETerrain t) {
        return switch (t) {
            case ETerrain.Grass -> 0;
            case ETerrain.Mountain -> 2;
            case ETerrain.Water -> 0;
        };
    }
    private int leaveCost(ETerrain t) {
        return switch (t) {
            case ETerrain.Grass -> 1;
            case ETerrain.Mountain -> 2;
            case ETerrain.Water -> 9999;
        };
    }
    private int stepCost(Point from, Point to) {
        return leaveCost(getTerrain(from.x, from.y)) + enterCost(getTerrain(to.x, to.y));
    }
}
