package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;


public class GameHelper {
    private GameState currentGameState;
    private final Set<String> visitedFields = new HashSet<>();
    private final UniquePlayerIdentifier playerId;
    private boolean lastHadTreasure = false;
    private String rememberGoldPosition = null;

    private boolean isInitialized = false;

    private List<Point> playerPosHistory = new ArrayList<>();
    

    public GameHelper(UniquePlayerIdentifier playerId) {
        this.playerId = playerId;
    }

    public UniquePlayerIdentifier getPlayerId() {
        return playerId;
    }

    public FullMap getMap() {
        return currentGameState.getMap();
    }

    public int getMaxX() {
        FullMap map = currentGameState.getMap();
        return map.stream().mapToInt(FullMapNode::getX).max().orElse(0);
    }

    public int getMaxY() {
        FullMap map = currentGameState.getMap();
        return map.stream().mapToInt(FullMapNode::getY).max().orElse(0);
    }

    private String key(FullMapNode node) {
        return node.getX() + "," + node.getY();
    }
    
    public boolean isVisited(FullMapNode node) {
        return visitedFields.contains(key(node));
    }

    public FullMapNode getMyPosition() {
        FullMap map = currentGameState.getMap();
        return map.getMapNodes().stream()
                .filter(n -> n.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition || n.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition)
                .findFirst().orElse(null);
    }

    public boolean goldWasHere(FullMapNode node) {
        return rememberGoldPosition != null && key(node).equals(rememberGoldPosition);
    }

    public boolean hasTreasure() {
        return currentGameState.getPlayers().stream()
            .filter(p->p.getUniquePlayerID().equals(playerId.getUniquePlayerID()))
            .findFirst()
            .map(PlayerState::hasCollectedTreasure)
            .orElse(false);
    }

    public boolean playerRecentlyMoved() {
        
        // System.out.println("Size of Array: " + playerPosHistory.size());

        int size = playerPosHistory.size();
        if (size < 2) {
            return true;
        }
        
        Point previous = playerPosHistory.get(size - 2);
        Point current = playerPosHistory.get(size -  1);

        return !current.equals(previous);
    }

    public void update(GameState gameState) {
        currentGameState = gameState;
        Point currentPlayerPos = new Point(getMyPosition().getX(),getMyPosition().getY());
        playerPosHistory.add(currentPlayerPos);
        FullMap map = gameState.getMap();
        boolean hasTreasureNow = hasTreasure();
        int maxX = getMaxX();
        int maxY = getMaxY();

        for (FullMapNode node : map.getMapNodes()) {
            String key = key(node);
            if(node.getTreasureState() == ETreasureState.MyTreasureIsPresent){
                rememberGoldPosition = key;
            }
            if(node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition || node.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition)
            {
                visitedFields.add(key);
                if(hasTreasureNow && !lastHadTreasure){
                    rememberGoldPosition = key;
                }
                if(node.getTerrain() == ETerrain.Mountain)
                {
                    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1},{1,1},{-1,1},{-1,-1},{1,-1}}; 
                    for (int[] dir : dirs) {
                        int nx = node.getX() + dir[0];
                        int ny = node.getY() + dir[1];
            
                        if (nx >= 0 && ny >= 0 && nx <= maxX && ny <= maxY) {
                            String neighbourkey = nx + "," + ny;
                            visitedFields.add(neighbourkey);
                        }
                    }
                }
            }
        }
        lastHadTreasure = hasTreasureNow;
    }
}