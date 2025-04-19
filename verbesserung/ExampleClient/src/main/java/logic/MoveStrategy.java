package logic;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.*;

import java.util.List;

public class MoveStrategy {

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
                goal = findUnvisited(map); // Erkunde die Karte weiter
                if (goal != null) {
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
    
        EMove move;
        if (dx == 1) move = EMove.Right;
        else if (dx == -1) move = EMove.Left;
        else if (dy == 1) move = EMove.Down;
        else if (dy == -1) move = EMove.Up;
        else move = EMove.Right; // bleibt auf dem gleichen Feld → Fehler vermeiden
    
        System.out.println("➡️ Bewegung: " + move + " zu " + next.getX() + "," + next.getY());
        return PlayerMove.of(playerId, move);
    }
    

    private PlayerMove tryFallbackMove(FullMap map, FullMapNode pos, UniquePlayerIdentifier playerId) {
        if (isSafe(map, pos.getX() + 1, pos.getY())) return PlayerMove.of(playerId, EMove.Right);
        if (isSafe(map, pos.getX(), pos.getY() + 1)) return PlayerMove.of(playerId, EMove.Down);
        if (isSafe(map, pos.getX() - 1, pos.getY())) return PlayerMove.of(playerId, EMove.Left);
        if (isSafe(map, pos.getX(), pos.getY() - 1)) return PlayerMove.of(playerId, EMove.Up);

        System.out.println("❗ Kein sicherer Fallback – bleibe stehen (gehe nach rechts als Notlösung)");
        return PlayerMove.of(playerId, EMove.Right); // Fallback to right if no safe move is found
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

    private FullMapNode findUnvisited(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if ((node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain) &&
                node.getPlayerPositionState() == EPlayerPositionState.NoPlayerPresent &&
                node.getTreasureState() == ETreasureState.NoOrUnknownTreasureState) {
                return node;
            }
        }
        return null;
    }
    
}
