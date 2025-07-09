package view;

import java.util.HashSet;
import java.util.Set;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

import logic.GameHelper;

public class ConsoleView {
    public ConsoleView() {}

    public void render(GameHelper gameHelper) {
        FullMap map = gameHelper.getMap();
        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();

        // 2D-Matrix vorbereiten
        String[][] grid = new String[maxY + 1][maxX + 1];
        for (FullMapNode node : map.getMapNodes()) {
            int x = node.getX();
            int y = node.getY();
            grid[y][x] = getSymbolForNode(node, gameHelper);
        }

        System.out.println("📜 Aktuelle Spielkarte:");
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                System.out.print(grid[y][x] != null ? grid[y][x] : "⬜");
            }
            System.out.println(); // Zeilenumbruch
        }
    }
  
    private String getSymbolForNode(FullMapNode node, GameHelper gameHelper) {
        // Spielerzustand hat höchste Priorität
        EPlayerPositionState position = node.getPlayerPositionState();
        switch (position) {
            case EPlayerPositionState.MyPlayerPosition:
                return "🧍"; // Eigener Spieler
            case EPlayerPositionState.EnemyPlayerPosition:
                return "🤺"; // Gegner
            case EPlayerPositionState.BothPlayerPosition:
                return "⚔️"; // Beide auf dem Feld
        }

        // // Burganzeige (wird nicht überschrieben durch Terrain)
        EFortState fortState = node.getFortState();
        switch (fortState) {
            case EFortState.MyFortPresent:
                return "🏰"; // eigene Burg
            case EFortState.EnemyFortPresent:
                return "🏯"; // gegnerische Burg
        }

        // // Schatzanzeige (anders je nach Sammlung)
        if (gameHelper.goldWasHere(node)) {
            ETreasureState hasTreasure = node.getTreasureState();
            switch(hasTreasure) {
                case ETreasureState.MyTreasureIsPresent:
                    return "💰"; // Sichtbarer Schatz
                case ETreasureState.NoOrUnknownTreasureState:
                    return "🟡"; // Sichtbarer Schatz
            }
        }

        // Terrainanzeige
        ETerrain terrain = node.getTerrain();
        if(gameHelper.isVisited(node)) {
            return switch (terrain) {
                case ETerrain.Grass -> "🟢";     
                case ETerrain.Water -> "\uD83D\uDFE6";     
                case ETerrain.Mountain -> "🟤"; 
            };
        } else {
            return switch (terrain) {
                case ETerrain.Grass -> "\uD83D\uDFE9";
                case ETerrain.Water -> "\uD83D\uDFE6";
                case ETerrain.Mountain -> "\uD83D\uDFEB";
            };  
        }
    }

    /**
     * Gibt das Spielende in der Konsole aus.
     */
    public void printGameResult(boolean won) {
        System.out.println(won ? "🏆 Du hast gewonnen!" : "💀 Du hast verloren.");
    }
}
