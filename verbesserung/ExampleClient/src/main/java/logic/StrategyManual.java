package logic;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.PlayerMove;

public class StrategyManual implements IStrategy{
    
    @Override
    public PlayerMove calculateNextMove(GameHelper gameHelper) {
        UniquePlayerIdentifier playerId = gameHelper.getPlayerId();

        try {
            System.out.println("Введите ход (w=Up, s=Down, a=Left, d=Right): ");
            int ch = System.in.read();

            // очищаем остаток буфера до перевода строки
            while (System.in.available() > 0) {
                System.in.read();
            }

            return switch (ch) {
                case 'w' -> PlayerMove.of(playerId, EMove.Up);
                case 's' -> PlayerMove.of(playerId, EMove.Down);
                case 'a' -> PlayerMove.of(playerId, EMove.Left);
                case 'd' -> PlayerMove.of(playerId, EMove.Right);
                default  -> {
                    System.out.println("Неверный ввод, двигаюсь вправо по умолчанию.");
                    yield PlayerMove.of(playerId, EMove.Right);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return PlayerMove.of(playerId, EMove.Right);
        }
    }


}
