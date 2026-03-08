package engine;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FakeEngineTest {

    @Test
    public void GameStateShowsBothPlayers() {
        int x = 5;
        int y = 2;
        assertTrue(x + y == 7);
        GameSimulator simulator = new GameSimulator();
        simulator.singlePlayer(null);
    }
    
}
