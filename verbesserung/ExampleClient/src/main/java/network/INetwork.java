package network;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;

public interface INetwork {
    public void registerPlayer(String studentId);
    public void sendHalfMap(PlayerHalfMap halfMap);
    public void sendMove(PlayerMove move);
    public GameState getGameState();
    public UniquePlayerIdentifier getPlayerId();
}
