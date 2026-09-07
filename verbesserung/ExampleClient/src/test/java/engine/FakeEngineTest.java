package engine;

import org.junit.jupiter.api.Test;

import main.Main;
import util.RandomManager;

public class FakeEngineTest {

  @Test
  public void GameStateShowsBothPlayers() {
    RandomManager.setSeed(1774785431801L);
    GameSimulator simulator = new GameSimulator();
    simulator.multiPlayer(null);
  }

  @Test
  public void RunGame() {
    Main.main(new String[0]);
  }
}
