package logic;

import java.util.Set;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class Utils {
  public static GameHelper generateGameHelper(FullMap map) {
    GameHelper helper = new GameHelper(new UniquePlayerIdentifier("player1"));

    GameState gameState =
        new GameState(
            map,
            Set.of(
                new PlayerState(
                    "Test",
                    "Player",
                    "u123456",
                    EPlayerGameState.MustWait,
                    new UniquePlayerIdentifier("player1"),
                    false)),
            "ABC");

    helper.update(gameState);

    return helper;
  }
}
