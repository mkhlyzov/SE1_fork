package engine;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerMove;

public class FakeEngine {
    private UniquePlayerIdentifier playerid;
    public FakeEngine(){
    }
    public void generateMap(PlayerHalfMap halfMapData){
       
    }
    public void createPlayer()
    {
        this.playerid = new UniquePlayerIdentifier("FAKE- ");
    }

    public void applyMove(PlayerMove move){

    }

    public UniquePlayerIdentifier getPlayerId(){
        return playerid;
    }
}
