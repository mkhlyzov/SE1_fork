package model;

public class GameSettings {
  private String studentId;
  private EGameMode gameMode;
  private StrategyType playerStrategy;
  private StrategyType enemyStrategy;
  private String serverURL;
  private String gameId;
  private int delay;
  private Long fixedSeed;

  public GameSettings(
      String studentId,
      EGameMode gameMode,
      StrategyType playerStrategy,
      StrategyType enemyStrategy,
      String serverURL,
      String gameId,
      int delay,
      Long fixedSeed) {
    this.studentId = studentId;
    this.gameMode = gameMode;
    this.playerStrategy = playerStrategy;
    this.enemyStrategy = enemyStrategy;
    this.serverURL = serverURL;
    this.gameId = gameId;
    this.delay = delay;
    this.fixedSeed = fixedSeed;
  }

  public static GameSettings getDefaultSettings() {
    GameSettings defaults = new GameSettings(
        "Fake1",
        EGameMode.OFFLINE,
        // StrategyType.PLANNED_TOUR,
        // StrategyType.ALWAYS_CLOSEST,
        StrategyType.PLANNED_TOUR,
        StrategyType.NEAREST_NEIGHBOUR,
        null,
        null,
        100,
        null);
    return defaults;
  }

  public String getStudentId() {
    return studentId;
  }

  public StrategyType getPlayerStrategy() {
    return playerStrategy;
  }

  public StrategyType getEnemyStrategy() {
    return enemyStrategy;
  }

  public String getServerURL() {
    return serverURL;
  }

  public String getGameId() {
    return gameId;
  }

  public EGameMode getGameMode() {
    return gameMode;
  }

  public int getDelay() {
    return delay;
  }
}
