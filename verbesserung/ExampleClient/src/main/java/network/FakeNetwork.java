package network;

import engine.FakeEngine;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;

public class FakeNetwork implements INetwork{
    private FakeEngine fakeEngine = new FakeEngine();
    // private UniquePlayerIdentifier playerid;
    @Override
    public void registerPlayer(String studentUAccount) {
        fakeEngine.createPlayer();
    }    
    @Override
    public void sendHalfMap(PlayerHalfMap halfMap){
       fakeEngine.generateMap(halfMap);   
    }
    @Override
    public GameState getGameState() {
        return fakeEngine.getState();
    }
    @Override
    public void sendMove(PlayerMove move) {
        try {
            fakeEngine.applyMove(move);
            System.out.println("✅ Zug erfolgreich gesendet!");
        }
        catch(IllegalArgumentException | IllegalStateException e){
            System.err.println("❌ Fehler beim Senden des Zuges: " + e.getMessage());
        }    
    }
    @Override
    public UniquePlayerIdentifier getPlayerId() {
        return fakeEngine.getPlayerId();
    }
}
