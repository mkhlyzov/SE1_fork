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
        if(gameHelper.isVisited(node))
           return getSymbolForNodeVisited(node, gameHelper);
        ETerrain terrain = node.getTerrain();
        EPlayerPositionState position = node.getPlayerPositionState();
        EFortState fortState = node.getFortState();
        ETreasureState hasTreasure = node.getTreasureState();

        // Spielerzustand hat höchste Priorität
        switch (position) {
            case MyPlayerPosition:
                return "🧍"; // Eigener Spieler
            case EnemyPlayerPosition:
                return "🤺"; // Gegner
            case BothPlayerPosition:
                return "⚔️"; // Beide auf dem Feld
        }

        // // Burganzeige (wird nicht überschrieben durch Terrain)
        if (fortState == EFortState.MyFortPresent) return "🏰";
        if (fortState == EFortState.EnemyFortPresent) return "🏯";

        // // Schatzanzeige (anders je nach Sammlung)
        
        if(hasTreasure == ETreasureState.MyTreasureIsPresent) return "💰"; // Sichtbarer Schatz


        // Terrainanzeige
        return switch (terrain) {
            case Grass -> "\uD83D\uDFE9";
            case Water -> "\uD83D\uDFE6";
            case Mountain -> "\uD83D\uDFEB";
        };

    }

    private String getSymbolForNodeVisited(FullMapNode node, GameHelper gameHelper) {
        ETerrain terrain = node.getTerrain();
        EPlayerPositionState position = node.getPlayerPositionState();
        EFortState fortState = node.getFortState();
        ETreasureState hasTreasure = node.getTreasureState();

        // Spielerzustand hat höchste Priorität
        switch (position) {
            case MyPlayerPosition:
                return "🧍"; // Eigener Spieler
            case EnemyPlayerPosition:
                return "🤺"; // Gegner
            case BothPlayerPosition:
                return "⚔️"; // Beide auf dem Feld
        }

        // // Burganzeige (wird nicht überschrieben durch Terrain)
        if (fortState == EFortState.MyFortPresent) return "🏰";
        if (fortState == EFortState.EnemyFortPresent) return "🏯";

        // // Schatzanzeige (anders je nach Sammlung)
        
        //if(hasTreasure == ETreasureState.MyTreasureIsPresent) return "🟡"; // Sichtbarer Schatz
        if(gameHelper.goldWasHere(node))
        {
            if(node.getTreasureState() == ETreasureState.NoOrUnknownTreasureState)
            {
                return "\uD83D\uDC7B";
            }
            else
            {
                return "\uD83D\uDCB0";
            }
        }

        // Terrainanzeige
        return switch (terrain) {
            case Grass -> "🟢";     
            case Water -> "\uD83D\uDFE6";     
            case Mountain -> "🟤"; 
        };
    }

    /**
     * Gibt das Spielende in der Konsole aus.
     */
    public void printGameResult(boolean won) {
        System.out.println(won ? "🏆 Du hast gewonnen!" : "💀 Du hast verloren.");
    }

    /**
     * Gibt technische Validierungsfehler auf System.err aus.
     */
    public void logValidationError(String message, String className, String methodName) {
        System.err.println("[FEHLER] " + message);
        System.err.println("Verursacht durch: " + methodName + " in " + className);
    }
}
