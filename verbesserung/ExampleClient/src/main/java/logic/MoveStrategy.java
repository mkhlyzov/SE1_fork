package logic;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.*;
import java.util.Comparator;
import java.util.List;

public class MoveStrategy {

    private int explorationMoves = 0;
    private final int MAX_EXPLORATION_MOVES = 20;
    private int localMoves = 0;

    public PlayerMove calculateNextMove(GameState gameState, UniquePlayerIdentifier playerId) {
        FullMap map = gameState.getMap();
        FullMapNode myPosition = getMyPosition(map);
    
        if (myPosition == null) {
            System.err.println("⚠️ Spielerposition nicht gefunden.");
            return PlayerMove.of(playerId, EMove.Right); // Notfallbewegung
        }
    
        boolean playerHasTreasure = hasTreasure(map);
        FullMapNode goal;
    
        if (playerHasTreasure) {
            goal = findMyFort(map);
            System.out.println("🎯 Ziel: Fort erreichen");
        } else {
            goal = findTreasure(map);
            if (goal == null) {
                //goal = findUnvisited(map); // Erkunde die Karte weiter
                if(explorationMoves >= MAX_EXPLORATION_MOVES)
                {
                   return stayAroundStart(map, myPosition, playerId);
                }
                goal = findClosestUnvisited(map,myPosition);
                if (goal != null) {
                    explorationMoves++;
                    System.out.println("🔍 Schatz nicht sichtbar – erkunde: " + goal.getX() + "," + goal.getY());
                } else {
                    System.out.println("🛑 Keine sinnvolle Erkundung mehr möglich");
                    return tryFallbackMove(map, myPosition, playerId);
                }
            } else {
                System.out.println("🎯 Ziel: Schatz holen bei: " + goal.getX() + "," + goal.getY());
            }
        }
    
        // Wenn Ziel ungültig oder im Wasser ist
        if (goal == null || goal.getTerrain() == ETerrain.Water) {
            System.out.println("⚠️ Ziel ist nicht erreichbar – Fallback-Bewegung.");
            return tryFallbackMove(map, myPosition, playerId);
        }
    
        // Pfad berechnen
        Pathfinder pathfinder = new Pathfinder(map);
        List<FullMapNode> path = pathfinder.findPath(myPosition.getX(), myPosition.getY(), goal.getX(), goal.getY());
    
        if (path.isEmpty()) {
            System.out.println("⚠️ Kein Pfad zum Ziel – Fallback-Bewegung.");
            return tryFallbackMove(map, myPosition, playerId);
        }
    
        FullMapNode next = path.get(0);
        if (next.getTerrain() == ETerrain.Water) {
            System.out.println("💧 Nächster Schritt ist Wasser – Fallback.");
            return tryFallbackMove(map, myPosition, playerId);
        }
    
        int dx = next.getX() - myPosition.getX();
        int dy = next.getY() - myPosition.getY();
        
        if (!isMovePossible(map, next.getX(), next.getY())) {
            System.out.println("🚫 Bewegung würde aus Karte hinaus führen. Fallback.");
            return tryFallbackMove(map, myPosition, playerId);
        }

        EMove move;
        if (dx == 1) move = EMove.Right;
        else if (dx == -1) move = EMove.Left;
        else if (dy == 1) move = EMove.Down;
        else if (dy == -1) move = EMove.Up;
        else move = EMove.Right; // bleibt auf dem gleichen Feld → Fehler vermeiden
    
        System.out.println("➡️ Bewegung: " + move + " zu " + next.getX() + "," + next.getY());
        if (!isMoveValid(myPosition.getX(), myPosition.getY(), move)) {
            System.out.println("🚫 Bewegung würde aus der Karte rausgehen! Fallback Bewegung!");
            return tryFallbackMove(map, myPosition, playerId);
        }
        return PlayerMove.of(playerId, move);
    }
    

    private PlayerMove tryFallbackMove(FullMap map, FullMapNode pos, UniquePlayerIdentifier playerId) {
        int x = pos.getX();
        int y = pos.getY();
    
        if (isMovePossible(map, x + 1, y)) return PlayerMove.of(playerId, EMove.Right);
        if (isMovePossible(map, x, y + 1)) return PlayerMove.of(playerId, EMove.Down);
        if (isMovePossible(map, x - 1, y)) return PlayerMove.of(playerId, EMove.Left);
        if (isMovePossible(map, x, y - 1)) return PlayerMove.of(playerId, EMove.Up);
    
        System.out.println("❗ Kein sicherer Fallback – bleibe stehen (DummyMove nach oben)");
        return PlayerMove.of(playerId, EMove.Up);
    }
    
    

    private boolean isSafe(FullMap map, int x, int y) {
        if (x < 0 || x >= 20 || y < 0 || y >= 10) {
            return false; // ❌ outside map boundaries
        }

        for (FullMapNode node : map.getMapNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain;
            }
        }
        return false; // Unsafe if not on Grass or Mountain terrain
    }

    private FullMapNode getMyPosition(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition) {
                return node; // Return the player's current position on the map
            }
        }
        return null; // If no player position is found
    }

    private boolean hasTreasure(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition &&
                node.getTreasureState() == ETreasureState.MyTreasureIsPresent) {
                return true; // Player has the treasure
            }
        }
        return false; // No treasure
    }

    private FullMapNode findTreasure(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getTreasureState() == ETreasureState.MyTreasureIsPresent &&
                (node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain)) {
                return node;
            }
        }
        return null; // Kein Schatz gefunden oder liegt im Wasser
    }
    

    private FullMapNode findMyFort(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getFortState() == EFortState.MyFortPresent &&
                (node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain)) {
                return node; // Return the location of the fort if it's on Grass or Mountain
            }
        }
        return null; // No fort found
    }
    
    private FullMapNode findClosestUnvisited(FullMap map, FullMapNode current) {
        Pathfinder pathfinder = new Pathfinder(map); // <-- Hier Pathfinder initialisieren!
    
        return map.getMapNodes().stream()
            .filter(node -> (node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain)
                            && node.getPlayerPositionState() == EPlayerPositionState.NoPlayerPresent
                            && node.getTreasureState() == ETreasureState.NoOrUnknownTreasureState)
            .min(Comparator.comparingInt(node -> {
                List<FullMapNode> path = pathfinder.findPath(current.getX(), current.getY(), node.getX(), node.getY());
                return path.isEmpty() ? Integer.MAX_VALUE : path.size();
            }))
            .orElse(null);
    }
    
 
    // private FullMapNode findUnvisited(FullMap map) {
    //     return findClosestUnvisited(map,current);
    // }


    private PlayerMove stayAroundStart(FullMap map, FullMapNode pos, UniquePlayerIdentifier playerId) {
        localMoves++;
        if (localMoves >= 3) {
            if (isMovePossible(map, pos.getX() + 1, pos.getY())) {
                return PlayerMove.of(playerId, EMove.Right);
            }
            if (isMovePossible(map, pos.getX(), pos.getY() + 1)) {
                return PlayerMove.of(playerId, EMove.Down);
            }
            if (isMovePossible(map, pos.getX() - 1, pos.getY())) {
                return PlayerMove.of(playerId, EMove.Left);
            }
            if (isMovePossible(map, pos.getX(), pos.getY() - 1)) {
                return PlayerMove.of(playerId, EMove.Up);
            }
            return tryFallbackMove(map, pos, playerId);
        }
    
    
        if (isMovePossible(map, pos.getX() + 1, pos.getY())) return PlayerMove.of(playerId, EMove.Right);
        if (isMovePossible(map, pos.getX(), pos.getY() + 1)) return PlayerMove.of(playerId, EMove.Down);
        if (isMovePossible(map, pos.getX() - 1, pos.getY())) return PlayerMove.of(playerId, EMove.Left);
        if (isMovePossible(map, pos.getX(), pos.getY() - 1)) return PlayerMove.of(playerId, EMove.Up);
    
        return tryFallbackMove(map, pos, playerId);
    }
    

    private boolean isMovePossible(FullMap map, int x, int y) {
        if (x < 0 || x >= 20 || y < 0 || y >= 10) {
            return false;
        }
        return isSafe(map, x, y);
    }
    

    private boolean isMoveValid(int x, int y, EMove move) {
        switch (move) {
            case Right:
                return x < 19;
            case Left:
                return x > 0;
            case Down:
                return y < 9;
            case Up:
                return y > 0;
            default:
                return false;
        }
    }
    
  
}
