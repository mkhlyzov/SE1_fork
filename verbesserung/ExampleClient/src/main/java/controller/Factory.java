package controller;

import logic.IStrategy;
import logic.StrategyAlwaysClosest;
import logic.StrategyManual;
import logic.StrategyNearestNeighbour;
import logic.StrategyPlannedTour;
import model.GameSettings;
import model.StrategyType;
import network.ClientNetwork;
import network.INetwork;
import network.OfflineNetwork;

public class Factory {

    public static IStrategy buildPlayeStrategy(GameSettings settings) {
        return buildIStrategy(settings.getPlayerStrategy());
    }

    public static INetwork buildNetwork(GameSettings settings) {
        return switch (settings.getGameMode()) {

            case ONLINE -> new ClientNetwork(settings.getServerURL(), settings.getGameId());

            case OFFLINE -> new OfflineNetwork(buildIStrategy(settings.getEnemyStrategy()));
        };
    }

    private static IStrategy buildIStrategy(StrategyType strategy) {
        return switch (strategy) {

            case PLANNED_TOUR -> new StrategyPlannedTour();

            case NEAREST_NEIGHBOUR -> new StrategyNearestNeighbour();

            case ALWAYS_CLOSEST -> new StrategyAlwaysClosest();

            case MANUAL -> new StrategyManual();
        };
    }
}
