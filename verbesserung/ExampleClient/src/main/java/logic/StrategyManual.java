package logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.PlayerMove;

public class StrategyManual implements IStrategy {
    private static final Logger LOGGER = Logger.getLogger(StrategyManual.class.getName());

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
                default -> {
                    LOGGER.warning("Неверный ввод, двигаюсь вправо по умолчанию.");
                    yield PlayerMove.of(playerId, EMove.Right);
                }
            };
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Ошибка при чтении ручного ввода.", e);
            return PlayerMove.of(playerId, EMove.Right);
        }
    }

}
