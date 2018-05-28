package colorgameserver;

public class Lobby {
    private String redPlayer;
    private String bluePlayer;
    private boolean redReady;
    private boolean blueReady;
    
    public Lobby() {
        redPlayer = null;
        bluePlayer = null;
        redReady = false;
        blueReady = false;
    }
    
    public void setRed(String s){
        redPlayer = s;
    }
    
    public void setBlue(String s){
        bluePlayer = s;
    }
    
    public String getRed(){
        return redPlayer;
    }
    
    public String getBlue(){
        return bluePlayer;
    }
    
    public void setRedReady(boolean b){
        redReady = b;
    }
    
    public void setBlueReady(boolean b){
        blueReady = b;
    }
    
    public boolean getRedReady(){
        return redReady;
    }
    
    public boolean getBlueReady(){
        return blueReady;
    }
    
    public void redExited(){
        redPlayer = null;
        redReady = false;
    }
    
    public void blueExited(){
        bluePlayer = null;
        blueReady = false;
    }
}
