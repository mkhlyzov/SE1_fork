package engine;

import logic.IStrategy;
import logic.StrategyAlwaysClosest;
import logic.StrategyNearestNeighbour;
import map.ClientMap;
import messagesbase.messagesfromclient.PlayerHalfMap;

public class GameSimulator {
    public static void main(String[] args) {
        System.out.println("Game simulation started...");

        FakeEngine engine = new FakeEngine();
        
        String playerId_1 = "player_1";
        String playerId_2 = "player_2";

        IStrategy strategy_1 = new StrategyNearestNeighbour();
        IStrategy strategy_2 = new StrategyAlwaysClosest();

        ClientMap mapGenerator_1 = new ClientMap(playerId_1);
        PlayerHalfMap halfMapData_1 = mapGenerator_1.generate();
        engine.registerPlayer(playerId_1, halfMapData_1);
        
        ClientMap mapGenerator_2 = new ClientMap(playerId_2);
        PlayerHalfMap halfMapData_2 = mapGenerator_2.generate();
        engine.registerPlayer(playerId_2, halfMapData_2);



        // Add game logic and interactions here
        System.out.println("Game simulation ended.");
    }
}