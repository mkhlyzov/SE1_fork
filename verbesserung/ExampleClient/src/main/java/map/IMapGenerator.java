package map;

import messagesbase.messagesfromclient.PlayerHalfMap;

public interface IMapGenerator {
    public PlayerHalfMap generate(String playerId);
}
