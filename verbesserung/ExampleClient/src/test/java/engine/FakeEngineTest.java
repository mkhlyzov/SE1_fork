package engine;

import client.main.MainClient;
import org.junit.jupiter.api.Test;
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
    MainClient.main(new String[0]);
  }
}
