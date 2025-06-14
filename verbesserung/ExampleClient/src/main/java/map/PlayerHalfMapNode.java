package map;
import messagesbase.messagesfromclient.ETerrain;


public class PlayerHalfMapNode {
    private int x;
    private int y;
    private ETerrain setType;
    private boolean fortPresent;
   

    public PlayerHalfMapNode(int x, int y, ETerrain setType, boolean fortPresent) {
        this.x = x;
        this.y = y;
        this.setType = setType;
        this.fortPresent = fortPresent;
    }

    // === Getter ===
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ETerrain getSetType() {
        return setType;
    }

    public boolean isFortPresent() {
        return fortPresent;
    }

    // === Setter (optional) ===
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setSetType(ETerrain setType) {
        this.setType = setType;
    }

    public void setFortPresent(boolean fortPresent) {
        this.fortPresent = fortPresent;
    }
    
}