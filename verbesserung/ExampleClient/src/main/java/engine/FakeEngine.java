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
    // private FullMap currentFullMap; // хранит итоговую карту
    private ETerrain[][] terrainGrid;
    private int WIDTH;
    private int HEIGHT; 
    private Point treasurePos;      // позиция золота
    private Point playerPos;
    private Point fortPos;
    private EPlayerGameState gameState = EPlayerGameState.MustAct;
    private List<PlayerMove> movesBuffer = new ArrayList<>();
    //
    private boolean treasureWasCollected = false;
    private boolean treasureWasObserved = false;

    public FakeEngine(){

    }

    public void generateMap(PlayerHalfMap halfMapData){
        ClientMap map = new ClientMap("FakePlayer-2");
        PlayerHalfMap half2 = map.generate();   
        half2 = normalizeFortCount(half2);
        half2 = shiftCoordinates(half2);

        halfMapData = normalizeFortCount(halfMapData);
        // halfMapData = shiftCoordinates(halfMapData);
        treasurePos = addTreasureNearFort(halfMapData);

        PlayerHalfMapNode fort = halfMapData.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);       
        playerPos = new Point(fort.getX(),fort.getY());
        

        FullMap fullMap = combineHalfMaps(halfMapData, half2);

        // currentFullMap = convertToFullMap(halfMapData);
        
        // System.out.println("FakeEngine: Single map generated with " + currentFullMap.getMapNodes().size() + " nodes.");
        createTerrainArray(fullMap);
    }
    
    public void createPlayer()
    {
        this.playerid = new UniquePlayerIdentifier("FakePlayer-1");
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
            playerPos = newPos;
            if(playerPos.equals(treasurePos))
            {
                treasureWasCollected = true;
            }
            clearBufferWhenFull(required);
        }

        if (!inBounds(playerPos) || isWater(playerPos)) 
        { 
            gameState = EPlayerGameState.Lost;
        }
        
        if(treasureWasCollected)
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

    
    

    public UniquePlayerIdentifier getPlayerId(){
        return playerid;
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

                EFortState fortState = (fortPos.equals(p)) ? EFortState.MyFortPresent : EFortState.NoOrUnknownFortState;

                updatedNodes.add(new FullMapNode(terrain,positionState,treasureState,fortState,x,y));
            }
        }
       

        FullMap map = new FullMap(updatedNodes);

        return new GameState(map,players,"ABC");
    }

   
    
    //  private FullMap convertToFullMap(PlayerHalfMap half) {
    //     List<FullMapNode> nodes = new ArrayList<>();

    //     for (PlayerHalfMapNode node : half.getMapNodes()) {
    //         nodes.add(new FullMapNode(
    //                 node.getTerrain(),
    //                 EPlayerPositionState.NoPlayerPresent,
    //                 (treasurePos != null && node.getX() == treasurePos.x && node.getY() == treasurePos.y)
    //                         ? ETreasureState.MyTreasureIsPresent
    //                         : ETreasureState.NoOrUnknownTreasureState,
    //                 node.isFortPresent() ? EFortState.MyFortPresent : EFortState.NoOrUnknownFortState,
    //                 node.getX(),
    //                 node.getY()
    //         ));
    //     }

    //     return new FullMap(nodes);
    // }

    private void createTerrainArray(FullMap fullMap)
    {
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
    
    private PlayerHalfMap normalizeFortCount(PlayerHalfMap half) {
        List<PlayerHalfMapNode> forts = half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .toList();
        List<PlayerHalfMapNode> nodes = new ArrayList<>(half.getMapNodes());
        
            Random r = new Random();
            PlayerHalfMapNode keep = forts.get(r.nextInt(forts.size()));
            fortPos = new Point(keep.getX(),keep.getY());
            // List<PlayerHalfMapNode> nodes = new ArrayList<>(half.getMapNodes());

            for (int i = 0; i < nodes.size(); i++) {
                PlayerHalfMapNode n = nodes.get(i);
                if (n.isFortPresent() &&
                    !(n.getX() == keep.getX() && n.getY() == keep.getY())) {
                    nodes.set(i, new PlayerHalfMapNode(n.getX(), n.getY(), false, n.getTerrain()));
                }
            }

        return new PlayerHalfMap(half.getUniquePlayerID(),nodes);
    }

    
    private Point addTreasureNearFort(PlayerHalfMap half) {
        PlayerHalfMapNode fort = half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);

        if (fort == null) return null;

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


    // private void updateVisibility(Point pos)
    // {
    //     visibleCells.add(pos);
    //     FullMapNode current = currentFullMap.getMapNodes().stream()
    //     .filter(n -> n.getX() == pos.x && n.getY() == pos.y)
    //     .findFirst()
    //     .orElse(null);

    //     if(current != null && current.getTerrain() == ETerrain.Mountain){
    //         for(int dx = -1; dx <= 1;dx++){
    //             for(int dy = -1; dy <= 1;dy++)
    //             {
    //                 Point p = new Point(pos.x + dx, pos.y + dy);
    //                 currentFullMap.getMapNodes().stream()
    //                 .filter(n->n.getX() == p.x && n.getY() == p.y && n.getTerrain() != ETerrain.Water)
    //                 .findFirst()
    //                 .ifPresent(n->visibleCells.add(p));
    //             } 
    //         }
    //     }
    // }
    
    private PlayerHalfMap shiftCoordinates(PlayerHalfMap halfMapData){
        boolean makeSquare = new Random().nextBoolean();
        List<PlayerHalfMapNode> newNodes = new ArrayList<>();
        if (makeSquare) {
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                    node.getX(),
                    node.getY() + 5,
                    node.isFortPresent(),
                    node.getTerrain()
                ));
            }
        }
        else{
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                    node.getX() + 10,
                    node.getY(),
                    node.isFortPresent(),
                    node.getTerrain()
                ));
            }
        }
        return new PlayerHalfMap(halfMapData.getUniquePlayerID(),newNodes);
    }


    private FullMap combineHalfMaps(PlayerHalfMap half1, PlayerHalfMap half2) {
        // List<PlayerHalfMapNode> combined = new ArrayList<>(half1.getMapNodes());
        // for(PlayerHalfMapNode node: half2.getMapNodes()) {
        //     combined.add(new PlayerHalfMapNode(
        //         node.getX(),
        //         node.getY(),
        //         node.isFortPresent(),
        //         node.getTerrain()
        //     ));
        // }
        // // return new FullMap(combined);
        // return new PlayerHalfMap(half1.getUniquePlayerID(), combined);

        List<PlayerHalfMapNode> combinedNodes = new ArrayList<>();
        combinedNodes.addAll(half1.getMapNodes());
        combinedNodes.addAll(half2.getMapNodes());
        
        
        List<FullMapNode> fullMapNodes = new ArrayList<>();

        for (PlayerHalfMapNode node : combinedNodes) {
            // EPlayerPositionState playerState = (node.getX() == playerPos.x && node.getY() == playerPos.y)
            //         ? EPlayerPositionState.MyPlayerPosition
            //         : EPlayerPositionState.NoPlayerPresent;
            // EPlayerPositionState playerState = (playerPos.equals(p)) 
            //     ? EPlayerPositionState.MyPlayerPosition 
            //     : EPlayerPositionState.NoPlayerPresent;

            // ETreasureState treasureState = (node.getX() == treasurePos.x && node.getY() == treasurePos.y && !treasureWasCollected)
            //         ? ETreasureState.MyTreasureIsPresent
            //         : ETreasureState.NoOrUnknownTreasureState;

            // EFortState fortState = (node.getX() == fortPos.x && node.getY() == fortPos.y)
            //         ? EFortState.MyFortPresent
            //         : EFortState.NoOrUnknownFortState;

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

       
    private ETerrain getTerrain(int x, int y) {
        return terrainGrid[x][y];           // terrain[x][y]
    }

    
    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < WIDTH && p.y >= 0 && p.y < HEIGHT;
    }
    private boolean isWater(Point p) {
        return getTerrain(p.x, p.y) == ETerrain.Water;
    }

    
    private int enterCost(ETerrain t) {           // Grass=1, Mountain=2
        return (t == ETerrain.Mountain) ? 2 : 0;
    }
    private int leaveCost(ETerrain t) {           // Grass=1, Mountain=2
        return (t == ETerrain.Mountain) ? 2 : 1;
    }
    private int stepCost(Point from, Point to) {
        return leaveCost(getTerrain(from.x, from.y)) + enterCost(getTerrain(to.x, to.y));
    }

    
    private void resetBufferIfDirectionChanged(PlayerMove current) {
        if (!movesBuffer.isEmpty()) {
            PlayerMove last = movesBuffer.get(movesBuffer.size() - 1);
            if (last.getMove() != current.getMove()) {
                movesBuffer.clear();              
            }
        }
    }
    private void clearBufferWhenFull(int required) {
        if (movesBuffer.size() >= required) {
            movesBuffer.clear();                  // clear after a completed step
        }
    }

}
