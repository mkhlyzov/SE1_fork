package network;

import engine.FakeEngine;
import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;

public class FakeNetwork implements INetwork{
    private FakeEngine fakeEngine = new FakeEngine();
    private long lastPollTime = 0;
    private int GAMESTATE_REQUEST_DELAY = 0;

    public FakeNetwork(int GAMESTATE_REQUEST_DELAY)
    {
        this.GAMESTATE_REQUEST_DELAY = GAMESTATE_REQUEST_DELAY;
    }
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
        delayForPolling();
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

    public void  delayForPolling(){
        long now = System.currentTimeMillis();
    
        if (lastPollTime == 0) {
            lastPollTime = now;
            return; 
        }
    
        long elapsed = now - lastPollTime;
        long sleepTime = GAMESTATE_REQUEST_DELAY - elapsed;
    
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Sleep unterbrochen: " + e.getMessage());
            }
        }

        lastPollTime = System.currentTimeMillis();
    }
}
