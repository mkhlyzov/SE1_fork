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

    private int myXmin;
    private int myXmax;
    private int myYmin;
    private int myYmax;
    private int enemyXmin;
    private int enemyXmax;
    private int enemyYmin;
    private int enemyYmax;
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
        initializeMapCoordinates();
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



    private void initializeMapCoordinates() {
        if(isInitialized)
            return;
        
        FullMap map = this.getMap();
        FullMapNode myPosition = this.getMyPosition();
        int maxX = this.getMaxX();  // 9 or 19
        int maxY = this.getMaxY();  // 9 or 4
        int playerX = myPosition.getX();
        int playerY = myPosition.getY();

        if (maxX == 9 && maxY == 9) {
            // 10x10 — split horizontally
            myXmin = 0; myXmax = 10;
            myYmin = 0; myYmax = 5;

            if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 5; enemyYmax = 10;
            } else {
                myXmin = 0; myXmax = 10;
                myYmin = 5; myYmax = 10;

                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 0; enemyYmax = 5;
            }
        } else if (maxX == 19 && maxY == 4) {
            // 20x5 — split vertically
            myXmin = 0; myXmax = 10;
            myYmin = 0; myYmax = 5;

            if (myXmin <= playerX && playerX < myXmax && myYmin <= playerY && playerY < myYmax) {
                enemyXmin = 10; enemyXmax = 20;
                enemyYmin = 0; enemyYmax = 5;
            } else {
                myXmin = 10; myXmax = 20;
                myYmin = 0; myYmax = 5;

                enemyXmin = 0; enemyXmax = 10;
                enemyYmin = 0; enemyYmax = 5;
            }
        } else {
            System.err.println("Unknown map format (" + (maxX + 1) + " x " + (maxY + 1) + ")");
        }
        isInitialized = true;
        
    }

    public boolean insideMine(FullMapNode n) {
        return n.getX() >= myXmin && n.getX() < myXmax && n.getY() >= myYmin && n.getY() < myYmax;
    }

    public boolean insideEnemy(FullMapNode n) {
        return n.getX() >= enemyXmin && n.getX() < enemyXmax && n.getY() >= enemyYmin && n.getY() < enemyYmax;
    }

    public List<FullMapNode> getNeighbours4(FullMapNode node) {
        List<FullMapNode> neighbours = new ArrayList<>();
        for(FullMapNode n: this.getMap().getMapNodes())
        {
            int dx = n.getX() - node.getX();
            int dy = n.getY() - node.getY();
            if((dx*dx + dy*dy) == 1 && n.getTerrain() != ETerrain.Water)
            {
                neighbours.add(n);
            }
        }
        return neighbours;
    }

    public List<FullMapNode> getNeighbours8(FullMapNode node) {
        List<FullMapNode> neighbours = new ArrayList<>();
        for(FullMapNode n: this.getMap().getMapNodes())
        {
            int dx = n.getX() - node.getX();
            int dy = n.getY() - node.getY();
            if((dx*dx + dy*dy) <= 2 && n.getTerrain() != ETerrain.Water)
            {
                neighbours.add(n);
            }
        }
        return neighbours;
    }
}