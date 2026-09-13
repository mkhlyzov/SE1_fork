package network;

import java.util.Set;
import java.util.logging.Logger;

import engine.FakeEngine;
import logic.GameHelper;
import logic.IStrategy;
import map.ClientMap;
import map.IMapGenerator;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class OfflineNetwork extends AbstractDelayedNtwork implements INetwork {
  private FakeEngine engine = new FakeEngine();
  private boolean mapReady = false;
  
  private UniquePlayerIdentifier playerId;
  
  private UniquePlayerIdentifier enemyId;
  private final IStrategy enemyStrategy;
  GameHelper enemyhelper;
  private Thread enemyWorker = null;

  public OfflineNetwork(IStrategy enemyStrategy) {
    this.LOGGER = Logger.getLogger("");
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
    IMapGenerator mapGenerator_2 = new ClientMap();
    PlayerHalfMap halfMapData_2 = mapGenerator_2.generate(enemyId.getUniquePlayerID());
    engine.registerPlayer(enemyId.getUniquePlayerID(), halfMapData_2);
    enemyhelper = new GameHelper(enemyId);
    mapReady = true;
  }

  @Override
  public void sendMove(PlayerMove move) {
    assert enemyWorker == null || !enemyWorker.isAlive();

    engine.applyMove(move);

    if (engine.isFinished()) {
      return;
    }

    enemyWorker = new Thread(() -> {
      GameState enemyState = engine.getState(enemyId.getUniquePlayerID());

      enemyhelper.update(enemyState);

      PlayerMove enemyMove = enemyStrategy.calculateNextMove(enemyhelper);

      engine.applyMove(enemyMove);
    });

    enemyWorker.start();
  }

  @Override
  public GameState getGameState() {
    delayForPolling();

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
