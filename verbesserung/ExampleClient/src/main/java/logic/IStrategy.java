package logic;
import messagesbase.messagesfromclient.PlayerMove;

public interface IStrategy {
    public PlayerMove calculateNextMove(GameHelper gameHelper);
}