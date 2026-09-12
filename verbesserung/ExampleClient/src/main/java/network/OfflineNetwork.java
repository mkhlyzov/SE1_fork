package network;

import java.util.Set;
import java.util.logging.Level;
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

public class OfflineNetwork implements INetwork {
  private static final int GAMESTATE_REQUEST_DELAY = 500;
  private static final Logger LOGGER = Logger.getLogger("");

  private FakeEngine engine = new FakeEngine();
  private boolean mapReady = false;
  private long lastPollTime = 0;
  
  private UniquePlayerIdentifier playerId;
  
  private UniquePlayerIdentifier enemyId;
  private final IStrategy enemyStrategy;
  GameHelper enemyhelper;
  private Thread enemyWorker = null;

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

  private void delayForPolling() {
    long now = System.currentTimeMillis();

    if (lastPollTime == 0) {
      lastPollTime = now;
      return;
    }

    long elapsed = now - lastPollTime;
    long sleepTime = GAMESTATE_REQUEST_DELAY - elapsed;

    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.log(Level.WARNING, "Sleep unterbrochen.", e);
      }
    }

    lastPollTime = System.currentTimeMillis();
  }
}
