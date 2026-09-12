package controller;

import java.util.logging.Logger;

import logic.GameHelper;
import logic.IStrategy;
import map.IMapGenerator;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;
import model.GameSettings;
import model.Model;
import network.INetwork;
import view.IView;

public class GameController {
  private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
  private Model model;
  private IView view;

  private INetwork network;
  private IStrategy strategy;

  private final Object monitor = new Object();
  private Thread gameWorkerThread = null;

  public GameController(Model model, IView view) {
    this.model = model;
    this.view = view;
  }

  public void startSession() {
    startNewGame();
  }

  public void startNewGame() {
    if (gameWorkerThread != null && gameWorkerThread.isAlive()) {
      abortGame();
      gameWorkerThread.interrupt(); // is it good or bad???
    }

    gameWorkerThread = new Thread(this::_startNewGame);
    gameWorkerThread.start();
  }
  
  private void _startNewGame() {
    applySettings();

    registerPlayer();
    sendHalfMap();
    startGameLoop();
  }



  private void registerPlayer() {
    network.registerPlayer(model.getSettings().getStudentId());

    assert network.getPlayerId() != null;
    LOGGER.info("Player registered successfully with id: " + network.getPlayerId().getUniquePlayerID());

    model.setGameHelper(new GameHelper(network.getPlayerId()));
  }

  private void sendHalfMap() {
    waitPermissionForSendingHalfmap();
    LOGGER.info("Permission for sending HalfMap acquired.");

    if (model.isGameFinished()) {
      finishGame();
      return;
    }

    PlayerHalfMap halfMap = createHalfMap();
    network.sendHalfMap(halfMap);
    LOGGER.fine("HalfMap wurde gesendet.");
  }

  private PlayerHalfMap createHalfMap() {
    String myPlayerId = network.getPlayerId().getUniquePlayerID();
    IMapGenerator mapGen = Factory.buildMapGenerator(model.getSettings());
    return mapGen.generate(myPlayerId);
  }

  private void waitPermissionForSendingHalfmap() {
    boolean canSendMap = false;

    while (!canSendMap && !model.isGameFinished()) {

      GameState state = network.getGameState();

      if (state == null) {
        LOGGER.warning(
            "GameState ist null. " + "Warte weiter auf Erlaubnis zum Senden der HalfMap.");
        continue;
      }

      PlayerState myPlayer = GameHelper.getPlayerState(state, network.getPlayerId());

      if (myPlayer == null) {
        LOGGER.warning(
            "Eigener Spieler wurde im GameState " + state.getGameStateId() + " nicht gefunden.");
        continue;
      }

      canSendMap = canSendHalfMap(myPlayer);
    }
  }

  private boolean canSendHalfMap(PlayerState player) {

    LOGGER.info("Aktueller Status vom Server: " + player.getState());

    switch (player.getState()) {
      case MustAct -> {
        LOGGER.info("Erlaubnis zum Senden " + "der HalfMap erhalten.");
        return true;
      }

      case Won -> {
        LOGGER.info("Spiel wurde bereits gewonnen.");
        model.setGameResult(true);
        model.setGameFinished(true);
        return false;
      }

      case Lost -> {
        LOGGER.info("Spiel wurde bereits verloren.");
        model.setGameResult(false);
        model.setGameFinished(true);
        return false;
      }

      default -> {
        LOGGER.fine("Warte auf Erlaubnis zum " + "Senden der HalfMap...");
        return false;
      }
    }
  }

  private void startGameLoop() {
    while (!model.isGameFinished() && !model.isAbort()) {

      waitIfPaused();

      if (model.isAbort()) {
        break;
      }

      playOneTurn();
    }
  }

  private void waitIfPaused() {

    synchronized (monitor) {
      while (model.isPause() && !model.isStep() && !model.isAbort()) {

        try {
          monitor.wait();

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private void playOneTurn() {

    GameState state = network.getGameState();

    assert state != null;

    GameHelper gameHelper = model.getGameHelper();
    gameHelper.update(state);
    view.render(gameHelper);

    PlayerState myPlayerState = GameHelper.getPlayerState(state, network.getPlayerId());

    assert myPlayerState != null;

    switch(myPlayerState.getState()) {
      case EPlayerGameState.MustAct -> {
        PlayerMove move = strategy.calculateNextMove(gameHelper);
        network.sendMove(move);
        model.setStep(false);
      }
      case EPlayerGameState.Won, EPlayerGameState.Lost -> {
        finishGame();
      }
      case EPlayerGameState.MustWait -> {
        LOGGER.fine("Waiting for my turn...");
      }
    }
  }

  private void finishGame() {
    model.setGameFinished(true);

    GameHelper gameHelper = model.getGameHelper();
    GameState state = gameHelper.getGameState();
    boolean won = GameHelper.getPlayerState(state, network.getPlayerId()).getState() == EPlayerGameState.Won;
    view.printGameResult(won);
  }
  
  public void pauseGame() {
    model.setPause(true);
  }

  public void resumeGame() {
    synchronized (monitor) {
      model.setPause(false);
      monitor.notifyAll();
    }
  }

  public void stepGame() {
    synchronized (monitor) {
      model.setStep(true);
      monitor.notifyAll();
    }
  }

  public void abortGame() {
    synchronized (monitor) {
      model.setAbort(true);
      monitor.notifyAll();
    }
  }

  /*
   * updateSettings
   *
   * Stores the new game settings. The settings are saved for the next game and
   * don not
   * affect the currently running game.
   */
  public void updateSettings(GameSettings settings) {
    model.setSettings(settings);
  }

  /*
   * applySettings
   *
   * Applies the stored game settings when a new game is started.
   * Greates the player strategy and network configuration based
   * on the settings currently stored in the model.
   * This model should be called at the start of each new game.
   */
  public void applySettings() {
    strategy = Factory.buildPlayeStrategy(model.getSettings());
    network = Factory.buildNetwork(model.getSettings());

    model.setGameFinished(false);
    model.setAbort(false);
  }
}
