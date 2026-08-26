package engine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

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
import util.RandomManager;

public class FakeEngine {

    private static final Logger LOGGER = Logger.getLogger(FakeEngine.class.getName());
    private ETerrain[][] terrainGrid;
    private int WIDTH;
    private int HEIGHT;
    private static final int HIDE_POSITION_NUM_TURNS = 8;
    private Map<String, PlayerData> players = new HashMap<>();

    private static class PlayerData {
        UniquePlayerIdentifier playerId;
        PlayerHalfMap halfMapData;

        Point position;
        Point fortPosition;
        Point treasurePosition;
        boolean treasureCollected = false;
        boolean treasureObserved = false;
        boolean enemyFortObserved = false;
        EPlayerGameState state = EPlayerGameState.MustWait;
        List<PlayerMove> moveBuffer = new ArrayList<>();
        int numMovesApplied = 0;
    }

    public FakeEngine() {
    }

    public Boolean isFinished() {
        for (PlayerData pd : players.values()) {
            if (pd.state == EPlayerGameState.Won) {
                return true;
            }
        }
        return false;
    }

    public Boolean playerHasWon(String playerId) {
        PlayerData pd = players.get(playerId);
        PlayerData pd_enemy = players.values().stream().filter(p -> !p.playerId.getUniquePlayerID().equals(playerId))
                .findFirst().orElse(null);

        if (pd.state == EPlayerGameState.Won
                || pd_enemy.state == EPlayerGameState.Lost) {
            return true;
        } else if (pd_enemy.state == EPlayerGameState.Won
                || pd.state == EPlayerGameState.Lost) {
            return false;
        } else {
            return null;
        }
    }

    public void registerPlayer(
            String playerId,
            PlayerHalfMap halfMapData) {
        PlayerData data = new PlayerData();
        data.playerId = new UniquePlayerIdentifier(playerId);
        data.halfMapData = halfMapData;
        players.put(playerId, data);

        if (players.size() == 2) {
            generateFullMap();
            for (PlayerData pd : players.values()) {
                pd.state = EPlayerGameState.MustAct;
            }
        }
    }

    private void generateFullMap() {
        FullMap fullMap = null;
        for (int i = 0; i < 20; i++) {

            fullMap = generateFullMap_helper();

            if (mapIsConnected(fullMap)) {
                createTerrainArray(fullMap);
                return;
            }
        }
        createTerrainArray(fullMap);
        // throw new IllegalStateException("Could not generate connected map after 20
        // attempts");
    }

    private FullMap generateFullMap_helper() {
        assert players.size() == 2 : "Exactly 2 players should be registered to generate the full map.";

        List<PlayerData> shuffledPlayers = new ArrayList<>(players.values());
        Collections.shuffle(shuffledPlayers, RandomManager.getRandom());

        PlayerData shiftedPlayer = shuffledPlayers.get(0);
        PlayerData otherPlayer = shuffledPlayers.get(1);

        PlayerHalfMap shiftedHalfMap = shiftCoordinates(shiftedPlayer.halfMapData);
        PlayerHalfMap otherHalfMap = otherPlayer.halfMapData;

        for (PlayerData pd : players.values()) {
            PlayerHalfMap currentHalfMap = pd.halfMapData;

            if (pd == shiftedPlayer) {
                currentHalfMap = shiftedHalfMap;
            }

            pd.treasurePosition = addTreasure(currentHalfMap);
            if (pd.treasurePosition == null) {
                pd.state = EPlayerGameState.Lost;
                LOGGER.severe("Could not place Treasure on the map");
            }

            PlayerHalfMapNode fortNode = findFort(currentHalfMap);
            pd.fortPosition = new Point(fortNode.getX(), fortNode.getY());
            pd.position = new Point(fortNode.getX(), fortNode.getY());
        }

        return combineHalfMaps(shiftedHalfMap, otherHalfMap);
    }

    private PlayerHalfMap normalizeFortCount(PlayerHalfMap half) {
        List<PlayerHalfMapNode> forts = half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .toList();
        List<PlayerHalfMapNode> nodes = new ArrayList<>(half.getMapNodes());

        Random r = RandomManager.getRandom();
        PlayerHalfMapNode keep = forts.get(r.nextInt(forts.size()));

        for (int i = 0; i < nodes.size(); i++) {
            PlayerHalfMapNode n = nodes.get(i);
            if (n.isFortPresent() && !(n.getX() == keep.getX() && n.getY() == keep.getY())) {
                nodes.set(i, new PlayerHalfMapNode(
                        n.getX(), n.getY(), false, n.getTerrain()));
            }
        }

        return new PlayerHalfMap(half.getUniquePlayerID(), nodes);
    }

    private PlayerHalfMap shiftCoordinates(PlayerHalfMap halfMapData) {
        boolean makeSquare = RandomManager.getRandom().nextBoolean();
        List<PlayerHalfMapNode> newNodes = new ArrayList<>();
        if (makeSquare) {
            int maxY = halfMapData.getMapNodes().stream().mapToInt(n -> n.getY()).max().orElse(0);
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                        node.getX(),
                        node.getY() + maxY + 1,
                        node.isFortPresent(),
                        node.getTerrain()));
            }
        } else {
            int maxX = halfMapData.getMapNodes().stream().mapToInt(n -> n.getX()).max().orElse(0);
            for (PlayerHalfMapNode node : halfMapData.getMapNodes()) {
                newNodes.add(new PlayerHalfMapNode(
                        node.getX() + maxX + 1,
                        node.getY(),
                        node.isFortPresent(),
                        node.getTerrain()));
            }
        }
        return new PlayerHalfMap(halfMapData.getUniquePlayerID(), newNodes);
    }

    private Point addTreasure(PlayerHalfMap half) {
        PlayerHalfMapNode fort = findFort(half);
        if (fort == null)
            return null;
        List<PlayerHalfMapNode> candidates = collectTreasureCandidates(half, fort);
        PlayerHalfMapNode chosen = RandomManager.chooseRandom(candidates);
        return chosen == null ? null : new Point(chosen.getX(), chosen.getY());
    }

    private PlayerHalfMapNode findFort(PlayerHalfMap half) {
        return half.getMapNodes().stream()
                .filter(PlayerHalfMapNode::isFortPresent)
                .findFirst()
                .orElse(null);
    }

    private List<PlayerHalfMapNode> collectTreasureCandidates(PlayerHalfMap half, PlayerHalfMapNode fort) {
        return half.getMapNodes().stream()
                .filter(n -> n.getTerrain() == ETerrain.Grass)
                .filter(n -> !n.isFortPresent())
                .toList();
    }

    private Boolean mapIsConnected(FullMap fullMap) {

        if (fullMap == null || fullMap.getMapNodes() == null || fullMap.getMapNodes().isEmpty()) {
            return false;
        }

        List<FullMapNode> walkable = fullMap.getMapNodes().stream()
                .filter(n -> n.getTerrain() != ETerrain.Water)
                .toList();

        if (walkable.isEmpty()) {
            return false;
        }

        Set<FullMapNode> visited = new HashSet<>();
        Queue<FullMapNode> queue = new LinkedList<>();

        FullMapNode start = walkable.get(0);
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            FullMapNode current = queue.poll();

            for (FullMapNode node : walkable) {
                int dx = node.getX() - current.getX();
                int dy = node.getY() - current.getY();

                if ((dx * dx + dy * dy) == 1 && !visited.contains(node)) {
                    visited.add(node);
                    queue.add(node);
                }
            }
        }

        return visited.size() == walkable.size();
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
                    node.getY()));
        }

        return new FullMap(fullMapNodes);
    }

    private void createTerrainArray(FullMap fullMap) {
        WIDTH = fullMap.getMapNodes().stream()
                .mapToInt(FullMapNode::getX)
                .max()
                .orElse(0) + 1;
        // x_Coordinates: 0,1,2,3,4,5,6,7,8,9
        // WIDTH = 10
        HEIGHT = fullMap.getMapNodes().stream()
                .mapToInt(FullMapNode::getY)
                .max()
                .orElse(0) + 1;
        // y_Coordinates: 0,1,2,3,4,5,6,7,8,9
        // HEIGHT = 10
        terrainGrid = new ETerrain[WIDTH][HEIGHT];
        for (FullMapNode node : fullMap.getMapNodes()) {
            terrainGrid[node.getX()][node.getY()] = node.getTerrain();
        }

        // state verification
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++)
                assert terrainGrid[i][j] != null;
        }
    }

    public void applyMove(PlayerMove move) {
        PlayerData pd = players.get(move.getUniquePlayerID());
        PlayerData pd_enemy = players.values().stream()
                .filter(p -> !p.playerId.getUniquePlayerID().equals(move.getUniquePlayerID())).findFirst().orElse(null);

        int dx = 0, dy = 0;
        switch (move.getMove()) {
            case Up -> dy = -1;
            case Down -> dy = 1;
            case Left -> dx = -1;
            case Right -> dx = 1;
        }
        Point currentPos = pd.position;
        Point newPos = new Point(currentPos.x + dx, currentPos.y + dy);

        resetBufferIfDirectionChanged(move);
        pd.moveBuffer.add(move);
        pd.numMovesApplied++;

        int stepsNeededToMove = stepCost(currentPos, newPos);
        if (pd.moveBuffer.size() >= stepsNeededToMove) {
            pd.moveBuffer.clear();
            pd.position = newPos;

            updateObjectivesVisibility(move.getUniquePlayerID());
        }

        if (!inBounds(pd.position) || isWater(pd.position)) {
            pd.state = EPlayerGameState.Lost;
            pd_enemy.state = EPlayerGameState.Won;
        }
        if (pd.position.equals(pd.treasurePosition)) {
            pd.treasureCollected = true;
        }
        if (pd.treasureCollected && pd.position.equals(pd_enemy.fortPosition)) {
            pd.state = EPlayerGameState.Won;
            pd_enemy.state = EPlayerGameState.Lost;
        }

        // try {
        // Thread.sleep(100);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // System.err.println("Sleep unterbrochen: " + e.getMessage());
        // }
    }

    private void updateObjectivesVisibility(String playerId) {
        PlayerData pd = players.get(playerId);
        PlayerData pd_enemy = players.values().stream().filter(p -> !p.playerId.getUniquePlayerID().equals(playerId))
                .findFirst().orElse(null);
        Point pos = pd.position;

        ETerrain currentTerrain = getTerrain(pos.x, pos.y);
        if (currentTerrain == ETerrain.Mountain) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Point neighbour = new Point(pos.x + dx, pos.y + dy);
                    if (!inBounds(neighbour))
                        continue;
                    if (pd.treasurePosition.equals(neighbour)) {
                        pd.treasureObserved = true;
                    }
                    if (pd_enemy.fortPosition.equals(neighbour)) {
                        pd.enemyFortObserved = true;
                    }
                }
            }
        } else {
            if (pd_enemy.fortPosition.equals(pos)) {
                pd.enemyFortObserved = true;
            }
            if (pd.treasurePosition.equals(pos)) {
                pd.treasureCollected = true;
            }
        }
    }

    private void resetBufferIfDirectionChanged(PlayerMove current) {
        PlayerData pd = players.get(current.getUniquePlayerID());
        List<PlayerMove> movesBuffer = pd.moveBuffer; // ???

        if (!movesBuffer.isEmpty()) {
            PlayerMove last = movesBuffer.get(movesBuffer.size() - 1);
            if (last.getMove() != current.getMove()) {
                movesBuffer.clear();
            }
        }
    }

    public GameState getState(String playerId) {
        PlayerData pd = players.get(playerId);
        PlayerData pd_enemy = players.values().stream().filter(p -> !p.playerId.getUniquePlayerID().equals(playerId))
                .findFirst().orElse(null);

        PlayerState myPlayer = new PlayerState(
                "Fake", "Player", playerId,
                pd.state,
                pd.playerId,
                pd.treasureCollected);
        PlayerState enemyPlayer = new PlayerState(
                "Fake", "Player", pd_enemy.playerId.getUniquePlayerID(),
                pd_enemy.state,
                pd_enemy.playerId,
                pd_enemy.treasureCollected);

        Set<PlayerState> players_set = Set.of(myPlayer, enemyPlayer);

        if (terrainGrid == null)
            return new GameState(players_set, "ABC");

        Point treasurePos = pd.treasurePosition;
        Boolean treasureWasCollected = pd.treasureCollected;
        Boolean treasureWasObserved = pd.treasureObserved;

        Point playerPos = pd.position;
        Point fortPos = pd.fortPosition;
        Point enemyFortPos = pd_enemy.fortPosition;
        Boolean enemyFortWasObserved = pd.enemyFortObserved;
        boolean hideEnemy = pd_enemy.numMovesApplied < HIDE_POSITION_NUM_TURNS;
        Point enemyPos = pd_enemy.position;
        if (hideEnemy) {
            Random r = RandomManager.getRandom();
            enemyPos = new Point(r.nextInt(WIDTH), r.nextInt(HEIGHT));
        }

        List<FullMapNode> mapNodes = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Point p = new Point(x, y);

                ETerrain terrain = terrainGrid[x][y];
                ETreasureState treasureState = (treasurePos.equals(p) && !treasureWasCollected && treasureWasObserved)
                        ? ETreasureState.MyTreasureIsPresent
                        : ETreasureState.NoOrUnknownTreasureState;

                EPlayerPositionState positionState;
                if (pd.position.equals(p)) {
                    if (enemyPos.equals(p)) {
                        positionState = EPlayerPositionState.BothPlayerPosition;
                    } else {
                        positionState = EPlayerPositionState.MyPlayerPosition;
                    }
                } else if (enemyPos.equals(p)) {
                    positionState = EPlayerPositionState.EnemyPlayerPosition;
                } else {
                    positionState = EPlayerPositionState.NoPlayerPresent;
                }

                EFortState fortState = fortPos.equals(p) ? EFortState.MyFortPresent
                        : ((enemyFortPos.equals(p) && enemyFortWasObserved) ? EFortState.EnemyFortPresent
                                : EFortState.NoOrUnknownFortState);

                mapNodes.add(new FullMapNode(
                        terrain, positionState, treasureState, fortState, x, y));
            }
        }

        FullMap map = new FullMap(mapNodes);
        return new GameState(map, players_set, "ABC");
    }

    private ETerrain getTerrain(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            return null;
        return terrainGrid[x][y]; // terrain[x][y]
    }

    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < WIDTH && p.y >= 0 && p.y < HEIGHT;
    }

    private boolean isWater(Point p) {
        return getTerrain(p.x, p.y) == ETerrain.Water;
    }

    private int enterCost(ETerrain t) {
        if (t == null)
            return 1;
        return switch (t) {
            case ETerrain.Grass -> 1;
            case ETerrain.Mountain -> 2;
            case ETerrain.Water -> 1;
        };
    }

    private int leaveCost(ETerrain t) {
        if (t == null)
            return 1;
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