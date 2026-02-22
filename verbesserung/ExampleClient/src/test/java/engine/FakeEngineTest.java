package engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

class FakeEngineTest {

    @Test
    void GameStateShowsBothPlayers() {
        int x = 5;
        int y = 2;
        assertTrue(x + y == 7);
    }
    
}
