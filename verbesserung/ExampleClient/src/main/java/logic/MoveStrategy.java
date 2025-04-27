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
            return PlayerMove.of(playerId, EMove.Right);
        }

        boolean playerHasTreasure = hasTreasure(map);

        FullMapNode goal;

        if (playerHasTreasure) {
            goal = findEnemyFort(map);
            if (goal != null) {
                System.out.println("🎯 Ziel: Gegnerische Burg finden!");
            } else {
                System.out.println("❓ Keine gegnerische Burg sichtbar – bleibe stehen.");
                return stayClose(myPosition, playerId, map);
            }
        } else {
            goal = findTreasure(map);
            if (goal != null) {
                System.out.println("🎯 Ziel: Schatz gefunden bei " + goal.getX() + "," + goal.getY());
            } else {
                System.out.println("🛑 Kein Schatz sichtbar – bleibe stehen.");
                return stayClose(myPosition, playerId,map);
            }
        }

        Pathfinder pathfinder = new Pathfinder(map);
        List<FullMapNode> path = pathfinder.findPath(myPosition.getX(), myPosition.getY(), goal.getX(), goal.getY());

        if (path.isEmpty()) {
            System.out.println("⚠️ Kein Pfad gefunden – bleibe in Nähe.");
            return stayClose(myPosition, playerId, map);
        }

        FullMapNode next = path.get(0);

        if (next.getTerrain() == ETerrain.Water) {
            System.out.println("🚫 Achtung! Wasser voraus – bleibe lieber stehen.");
            return stayClose(myPosition, playerId, map);
        }

        EMove move = calculateMove(myPosition, next);

        if (!isMoveValid(myPosition.getX(), myPosition.getY(), move)) {
            System.out.println("🚫 Bewegung ungültig – bleibe stehen.");
            return stayClose(myPosition, playerId, map);
        }

        return PlayerMove.of(playerId, move);
    }

    private boolean hasTreasure(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition &&
                node.getTreasureState() == ETreasureState.MyTreasureIsPresent) {
                return true;
            }
        }
        return false;
    }

    private FullMapNode findTreasure(FullMap map) {
        return map.getMapNodes().stream()
                .filter(node -> node.getTreasureState() == ETreasureState.MyTreasureIsPresent)
                .filter(node -> node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain)
                .findFirst().orElse(null);
    }

    private FullMapNode findEnemyFort(FullMap map) {
        return map.getMapNodes().stream()
                .filter(node -> node.getFortState() == EFortState.EnemyFortPresent)
                .filter(node -> node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain)
                .findFirst().orElse(null);
    }

    private FullMapNode getMyPosition(FullMap map) {
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition) {
                return node;
            }
        }
        return null;
    }

    private PlayerMove stayClose(FullMapNode pos, UniquePlayerIdentifier playerId, FullMap map) {
        int x = pos.getX();
        int y = pos.getY();

        if (isSafeMove(map, x + 1, y)) return PlayerMove.of(playerId, EMove.Right);
        if (isSafeMove(map, x, y + 1)) return PlayerMove.of(playerId, EMove.Down);
        if (isSafeMove(map, x - 1, y)) return PlayerMove.of(playerId, EMove.Left);
        if (isSafeMove(map, x, y - 1)) return PlayerMove.of(playerId, EMove.Up);

        return PlayerMove.of(playerId, EMove.Right); // Notlösung
    }

    private EMove calculateMove(FullMapNode from, FullMapNode to) {
        if (to.getX() > from.getX()) return EMove.Right;
        if (to.getX() < from.getX()) return EMove.Left;
        if (to.getY() > from.getY()) return EMove.Down;
        if (to.getY() < from.getY()) return EMove.Up;
        return EMove.Right; // Fallback
    }

    private boolean isSafeMove(FullMap map, int x, int y) {
        if (x < 0 || x >= 20 || y < 0 || y >= 10) {
            return false;
        }
        for (FullMapNode node : map.getMapNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return node.getTerrain() == ETerrain.Grass || node.getTerrain() == ETerrain.Mountain;
            }
        }
        return false;
    }

    private boolean isMoveValid(int x, int y, EMove move) {
        switch (move) {
            case Right: return x < 19;
            case Left: return x > 0;
            case Down: return y < 9;
            case Up: return y > 0;
            default: return false;
        }
    }
}
