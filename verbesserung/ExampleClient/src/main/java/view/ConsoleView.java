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

public class ConsoleView {

    private GameState currentGameState;
    private final Set<String> visitedFields = new HashSet<>();
    private boolean lastHadTreasure = false;
    private String rememberGoldPosition = null;
    private final String playerId;

    public ConsoleView(String playerId)
    {
       this.playerId = playerId;
    }
    public void update(GameState gameState){
        currentGameState = gameState;
        FullMap map = gameState.getMap();
        boolean hasTreasureNow = gameState.getPlayers().stream().filter(p->p.getUniquePlayerID().equals(playerId)).findFirst().map(PlayerState::hasCollectedTreasure).orElse(false);
        int maxX = map.stream().mapToInt(FullMapNode::getX).max().orElse(0);
        int maxY = map.stream().mapToInt(FullMapNode::getY).max().orElse(0);
        for (FullMapNode node : map.getMapNodes()) {
            int x = node.getX();
            int y = node.getY();
            String key = x + "," + y;
            if(node.getTreasureState() == ETreasureState.MyTreasureIsPresent){
                rememberGoldPosition = key;
            }
            if(node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition || node.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition)
            {
                visitedFields.add(key);

                if(hasTreasureNow && !lastHadTreasure){
                    rememberGoldPosition = key;
                }
                if(node.getTerrain() == ETerrain.Mountain)
                {
                    int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1},{1,1},{-1,1},{-1,-1},{1,-1}}; 
                    for (int[] dir : dirs) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
            
                        if (nx >= 0 && ny >= 0 && nx <= maxX && ny <= maxY) {
                            String neighbourkey = nx + "," + ny;
                            visitedFields.add(neighbourkey);
                        }
                    }
                }
            }
        }
        lastHadTreasure = hasTreasureNow;
    }

    public void render() {
        FullMap map = currentGameState.getMap();
        if (map == null || map.isEmpty()) {
            System.out.println("❗ Karte ist leer oder nicht geladen.");
            return;
        }

        // Dynamische Größe bestimmen
        int maxX = map.stream().mapToInt(FullMapNode::getX).max().orElse(0);
        int maxY = map.stream().mapToInt(FullMapNode::getY).max().orElse(0);

        // 2D-Matrix vorbereiten
        String[][] grid = new String[maxY + 1][maxX + 1];
        for (FullMapNode node : map.getMapNodes()) {
            int x = node.getX();
            int y = node.getY();
            grid[y][x] = getSymbolForNode(node);
        }

        System.out.println("📜 Aktuelle Spielkarte:");
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                System.out.print(grid[y][x] != null ? grid[y][x] : "⬜");
            }
            System.out.println(); // Zeilenumbruch
        }
    }
  
    private String getSymbolForNode(FullMapNode node) {
        int x = node.getX();
        int y = node.getY();
        String n = x + "," + y;
        if(visitedFields.contains(n))
           return getSymbolForNodeVisited(node);
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

    private String getSymbolForNodeVisited(FullMapNode node) {
        ETerrain terrain = node.getTerrain();
        EPlayerPositionState position = node.getPlayerPositionState();
        EFortState fortState = node.getFortState();
        ETreasureState hasTreasure = node.getTreasureState();
        int x = node.getX();
        int y = node.getY();
        String n = x + "," + y;

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
        if(rememberGoldPosition != null && rememberGoldPosition.equals(n))
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

    private boolean hasTreasure(GameState gameState, UniquePlayerIdentifier playerId) {
        return gameState.getPlayers().stream()
                        .filter(p->p.getUniquePlayerID().equals(playerId.getUniquePlayerID()))
                        .findFirst()
                        .map(PlayerState::hasCollectedTreasure)
                        .orElse(false);
    }

}
