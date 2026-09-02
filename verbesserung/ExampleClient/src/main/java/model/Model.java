package model;

import logic.GameHelper;

public class Model {
  private GameSettings settings;
  private GameHelper gameHelper;

  private volatile boolean pause = false;
  private volatile boolean step = false;
  private volatile boolean abort = false;
  private volatile boolean gameFinished = false;

  private Boolean gameResult;

  public GameSettings getSettings() {
    return settings;
  }

  public void setSettings(GameSettings settings) {
    this.settings = settings;
  }

  public GameHelper getGameHelper() {
    return gameHelper;
  }

  public void setGameHelper(GameHelper gameHelper) {
    this.gameHelper = gameHelper;
  }

  public boolean isPause() {
    return pause;
  }

  public void setPause(boolean pause) {
    this.pause = pause;
  }

  public boolean isStep() {
    return step;
  }

  public void setStep(boolean step) {
    this.step = step;
  }

  public boolean isAbort() {
    return abort;
  }

  public void setAbort(boolean abort) {
    this.abort = abort;
  }

  public boolean isGameFinished() {
    return gameFinished;
  }

  public void setGameFinished(boolean gameFinished) {
    this.gameFinished = gameFinished;
  }

  public Boolean getGameResult() {
    return gameResult;
  }

  public void setGameResult(Boolean gameResult) {
    this.gameResult = gameResult;
  }
}
