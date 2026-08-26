package network;

import java.util.Set;

import engine.FakeEngine;
import logic.GameHelper;
import logic.IStrategy;
import map.ClientMap;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class OfflineNetwork implements INetwork {

    private FakeEngine engine = new FakeEngine();
    private UniquePlayerIdentifier playerId;
    private UniquePlayerIdentifier enemyId;
    private final IStrategy enemyStrategy;
    private boolean mapReady = false;
    GameHelper enemyhelper;

    public OfflineNetwork(IStrategy enemyStrategy) {
        this.enemyStrategy = enemyStrategy;
    }

    @Override
    public void registerPlayer(String studentId) {
        playerId = new UniquePlayerIdentifier("player_1");
        enemyId = new UniquePlayerIdentifier("player_2");
    }

    @Override
    public void sendHalfMap(PlayerHalfMap halfMap) {

        engine.registerPlayer(playerId.getUniquePlayerID(), halfMap);
        ClientMap mapGenerator_2 = new ClientMap(enemyId.getUniquePlayerID());
        PlayerHalfMap halfMapData_2 = mapGenerator_2.generate();
        engine.registerPlayer(enemyId.getUniquePlayerID(), halfMapData_2);
        enemyhelper = new GameHelper(enemyId);
        mapReady = true;
    }

    @Override
    public void sendMove(PlayerMove move) {
        engine.applyMove(move);
        if (engine.isFinished()) {
            return;
        }
        GameState enemyState = engine.getState(enemyId.getUniquePlayerID());
        enemyhelper.update(enemyState);
        PlayerMove enemyMove = enemyStrategy.calculateNextMove(enemyhelper);
        engine.applyMove(enemyMove);
    }

    @Override
    public GameState getGameState() {
        if (!mapReady) {
            PlayerState myPlayer = new PlayerState(
                    "Fake",
                    "Player",
                    playerId.getUniquePlayerID(),
                    EPlayerGameState.MustAct,
                    playerId,
                    false);
            return new GameState(Set.of(myPlayer), "ABC");
        }
        return engine.getState(playerId.getUniquePlayerID());
    }

    @Override
    public UniquePlayerIdentifier getPlayerId() {
        return playerId;
    }
}
