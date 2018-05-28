package colorgameclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class ColorGameGateway implements colorgame.ColorGameConstants {
    private PrintWriter outputToServer;
    private BufferedReader inputFromServer;
    private static Lock lock = new ReentrantLock();

    // Establish the connection to the server.
    public ColorGameGateway() {
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8000);

            // Create an output stream to send data to the server
            outputToServer = new PrintWriter(socket.getOutputStream());

            // Create an input stream to read data from the server
            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException ex) {
            //Platform.runLater(() -> showAlert("Exception in gateway constructor: " + ex.toString() + "\n",true));
            ex.printStackTrace();
        }
    }
    
    //returns true if room is full
    public Boolean sendHandle(String handle){
        lock.lock();
        outputToServer.println(SEND_HANDLE);
        outputToServer.println(handle);
        outputToServer.flush();
        try{
            int result = Integer.parseInt(inputFromServer.readLine());
            switch(result){
                case ROOM_FULL:
                    return true;
                case ENTER_OK:
                    return false;
            }
        }catch(IOException ex){
            //Platform.runLater(() -> showAlert("Exception in sendHandle: " + ex.toString() + "\n",true));
            ex.printStackTrace();
        }finally{
            lock.unlock();
        }
        return null;
    }
    
    public void ready(){
        outputToServer.println(IS_READY);
        outputToServer.flush();
    }
    
    public void sendKey(int n){
        outputToServer.println(SEND_KEY);
        outputToServer.println(n);
        outputToServer.flush();
    }
    
    public int[] getLoc(){
        lock.lock();
        outputToServer.println(GET_LOCATION);
        outputToServer.flush();
        int[] locInfo = new int[8];
        try{
            for(int i=0;i<8;++i)
                locInfo[i] = Integer.parseInt(inputFromServer.readLine());
        }catch(IOException ex){
            ex.printStackTrace();
            locInfo = null;
        }finally{
            lock.unlock();
        }
        return locInfo;
    }
    
    public String[] getHandles(){
        lock.lock();
        outputToServer.println(GET_HANDLES);
        outputToServer.flush();
        String[] results = new String[2];
        try{
            results[0] = inputFromServer.readLine();
            results[1] = inputFromServer.readLine();
        }catch(IOException ex){
            ex.printStackTrace();
            results = null;
        }finally{
            lock.unlock();
        }
        return results;
    }
    
    public void reset(){
        outputToServer.println(RESET);
        outputToServer.flush();
    }
    
    /*try{
            
        }catch(IOException ex){
            Platform.runLater(() -> showAlert("Exception in sendHandle: " + ex.toString() + "\n",true));*/
    
    public List<String> updateLobby(){
        lock.lock();
        outputToServer.println(GET_LOBBY_INFO);
        outputToServer.flush();
        List<String> players = new ArrayList<>();
        try{
            String red = inputFromServer.readLine();
            String blue = inputFromServer.readLine();
            if(red.equals(""))players.add(null);
            else players.add(red);
            if(blue.equals(""))players.add(null);
            else players.add(blue);
            players.add(inputFromServer.readLine());
            players.add(inputFromServer.readLine());
        }catch(IOException ex){
            //System.out.println("Exception in sendHandle: " + ex.toString() + "\n");
            ex.printStackTrace();
            players = null;
        }finally{
            lock.unlock();
        }
        return players;
    }
}