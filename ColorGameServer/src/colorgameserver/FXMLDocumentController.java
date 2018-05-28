/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colorgameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 *
 * @author Nguyen Anh Kiet 1
 */
public class FXMLDocumentController implements Initializable,colorgame.ColorGameConstants {
    private Integer players = 0;
    private Lobby lobby;
    private int[] loc;
    private int[] pixels;
    
    @FXML
    private TextArea textArea;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lobby = new Lobby();
        loc = new int[]{RADIUS,SPEED,100,100,RADIUS,SPEED,400,400};
        //radius,speed,X,Y of red ball, radius,speed,X,Y of blue ball
        
        pixels = new int[]{0,0,0,0};
        
        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
        
                while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();
    
                // Increment clientNo
                players++;
          
                Platform.runLater( () -> {
                    // Display the client number
                    textArea.appendText("Player " + players + " requested to join at " + new Date() + '\n');
                });
          
                // Create and start a new thread for the connection
                new Thread(new HandleAPlayer(socket,textArea,lobby,loc,pixels)).start();
                }   
            }
            catch(IOException ex) {
                System.err.println(ex);
            }
        }).start();
    }    
}

class HandleAPlayer implements Runnable,colorgame.ColorGameConstants{
    private Socket socket;
    private TextArea textArea;
    private Lobby lobby;
    private boolean full=false;
    private boolean isRed=false,isBlue=false;
    private int[] loc;
    private int[] pixels;
    private boolean one=false,two=false,three=false,four=false,six=false,seven=false,eight=false,nine=false;
    
    public HandleAPlayer(Socket socket,TextArea textArea,Lobby lobby,int[] loc,int[] pixels){
        this.socket = socket;
        this.textArea = textArea;
        this.lobby = lobby;
        this.loc = loc;
        this.pixels = pixels;
    }
    
    public void run(){
        try{
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());
            while(!full){
                int request = Integer.parseInt(inputFromClient.readLine());
                switch(request) {
                    case SEND_HANDLE: {
                        String handle = inputFromClient.readLine();
                        if(lobby.getRed()==null){
                            lobby.setRed(handle);
                            isRed=true;
                            outputToClient.println(ENTER_OK);
                        }
                        else if(lobby.getBlue()==null){
                            lobby.setBlue(handle);
                            isBlue=true;
                            outputToClient.println(ENTER_OK);
                        }
                        else if(lobby.getRed()!=null && lobby.getBlue()!=null){
                            outputToClient.println(ROOM_FULL);
                            full=true;
                        }
                        outputToClient.flush();
                        break;
                    }
                    case GET_LOBBY_INFO: {
                        if(lobby.getRed()==null)outputToClient.println("");
                        else outputToClient.println(lobby.getRed());
                        if(lobby.getBlue()==null)outputToClient.println("");
                        else outputToClient.println(lobby.getBlue());
                        outputToClient.println(lobby.getRedReady());
                        outputToClient.println(lobby.getBlueReady());
                        outputToClient.flush();
                        break;
                    }
                    case IS_READY: {
                        if(isRed)lobby.setRedReady(true);
                        else if(isBlue)lobby.setBlueReady(true);
                        break;
                    }
                    case SEND_KEY: {
                        int key=Integer.parseInt(inputFromClient.readLine());
                        switch(key){
                            case 1:
                                one=true;two=three=four=six=seven=eight=nine=false;
                                break;
                            case 2:
                                two=true;one=three=four=six=seven=eight=nine=false;
                                break;
                            case 3:
                                three=true;two=one=four=six=seven=eight=nine=false;
                                break;
                            case 4:
                                four=true;two=three=one=six=seven=eight=nine=false;
                                break;
                            case 6:
                                six=true;two=three=four=one=seven=eight=nine=false;
                                break;
                            case 7:
                                seven=true;two=three=four=six=one=eight=nine=false;
                                break;
                            case 8:
                                eight=true;two=three=four=six=seven=one=nine=false;
                                break;
                            case 9:
                                nine=true;two=three=four=six=seven=eight=one=false;
                                break;
                            
                        }
                        break;
                    }
                    case GET_LOCATION: {
                        if(one){
                            if(isRed){
                                loc[2]=(loc[2]+500-(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))+1)%500;
                                loc[3]=(loc[3]+(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))-1)%500;
                            }
                            else if(isBlue){
                                loc[6]=(loc[6]+500-(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))+1)%500;
                                loc[7]=(loc[7]+(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))-1)%500;
                            }
                        }
                        else if(two){
                            if(isRed)loc[3]=(loc[3]+loc[1])%500;
                            else if(isBlue)loc[7]=(loc[7]+loc[5])%500;
                        }
                        else if(three){
                            if(isRed){
                                loc[2]=(loc[2]+(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))-1)%500;
                                loc[3]=(loc[3]+(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))-1)%500;
                            }
                            else if(isBlue){
                                loc[6]=(loc[6]+(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))-1)%500;
                                loc[7]=(loc[7]+(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))-1)%500;
                            }
                        }
                        else if(four){
                            if(isRed)loc[2]=(loc[2]+500-loc[1])%500;
                            else if(isBlue)loc[6]=(loc[6]+500-loc[5])%500;
                        }
                        else if(six){
                            if(isRed)loc[2]=(loc[2]+loc[1])%500;
                            else if(isBlue)loc[6]=(loc[6]+loc[5])%500;
                        }
                        else if(seven){
                            if(isRed){
                                loc[2]=(loc[2]+500-(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))+1)%500;
                                loc[3]=(loc[3]+500-(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))+1)%500;
                            }
                            else if(isBlue){
                                loc[6]=(loc[6]+500-(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))+1)%500;
                                loc[7]=(loc[7]+500-(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))+1)%500;
                            }
                        }
                        else if(eight){
                            if(isRed)loc[3]=(loc[3]+500-loc[1])%500;
                            else if(isBlue)loc[7]=(loc[7]+500-loc[5])%500;
                        }
                        else if(nine){
                            if(isRed){
                                loc[2]=(loc[2]+(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))-1)%500;
                                loc[3]=(loc[3]+500-(int)Math.floor(Math.sqrt(2*loc[1]*loc[1]))+1)%500;
                            }
                            else if(isBlue){
                                loc[6]=(loc[6]+(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))-1)%500;
                                loc[7]=(loc[7]+500-(int)Math.floor(Math.sqrt(2*loc[5]*loc[5]))+1)%500;
                            }
                        }
                        for(int i=0;i<8;++i)
                            outputToClient.println(loc[i]);
                        outputToClient.flush();
                        break;
                    }
                    case RESET: {
                        lobby.setBlueReady(false);
                        lobby.setRedReady(false);
                        one=two=three=four=six=seven=eight=nine=false;
                        loc[0]=loc[4]=RADIUS;
                        loc[1]=loc[5]=SPEED;
                        loc[2]=loc[3]=100;
                        loc[6]=loc[7]=400;
                        for(int i=0;i<4;++i)pixels[i]=0;
                    }
                }
            }
        }catch(IOException ex) {
            if(isRed)lobby.redExited();
            else if(isBlue)lobby.blueExited();
            Platform.runLater(()->textArea.appendText("Exception in client thread: "+ex.toString()+"\n"));
      }
    }
}