/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colorgameclient;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Nguyen Anh Kiet 1
 */
public class FXMLDocumentController implements Initializable {
    private ColorGameGateway gateway;
    private FXMLDocumentController ctrller = this;
    private ReadOnlyBooleanWrapper bothReady = new ReadOnlyBooleanWrapper();
    
    @FXML
    private Label red;
    
    @FXML
    private Label blue;
    
    @FXML
    private Label redReady;
    
    @FXML
    private Label blueReady;
    
    @FXML
    private Button ready;
    
    @FXML
    private Label status;
    
    @FXML
    private void ready(){
        gateway.ready();
        ready.setDisable(true);
    }
    
    public void startGame(){
        Stage parent = (Stage) redReady.getScene().getWindow();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLGamePane.fxml"));
            Parent root = (Parent)loader.load();
            Scene scene = new Scene(root);
            scene.setOnKeyPressed((e)->handleKeyPress(e));
            Stage dialog = new Stage();
            dialog.setScene(scene);
            dialog.setTitle("Paint Game");
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.initOwner(parent);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            
            FXMLGamePaneController controller = (FXMLGamePaneController) loader.getController();
            controller.setGateway(gateway);
            controller.setReadyButton(ready);
            controller.setProperty(bothReady);
            controller.setHandles(gateway.updateLobby());
            new Thread(new MovementCheck(gateway,controller)).start();
            dialog.show();
            
        } catch(Exception ex) {
            System.out.println("Could not start game.");
            ex.printStackTrace();
        }
    }
    
    private void handleKeyPress(KeyEvent e){
        KeyCode code = e.getCode();
        switch(code){
            case NUMPAD1: case Z:
                gateway.sendKey(1);
                break;
            case NUMPAD2: case X:
                gateway.sendKey(2);
                break;
            case NUMPAD3: case C:
                gateway.sendKey(3);
                break;
            case NUMPAD4: case A:
                gateway.sendKey(4);
                break;
            case NUMPAD6: case D:
                gateway.sendKey(6);
                break;
            case NUMPAD7: case Q:
                gateway.sendKey(7);
                break;
            case NUMPAD8: case W:
                gateway.sendKey(8);
                break;
            case NUMPAD9: case E:
                gateway.sendKey(9);
                break;
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gateway = new ColorGameGateway();
        ReadOnlyBooleanProperty bothReadyProperty = bothReady.getReadOnlyProperty();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Color Game");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter player name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if(gateway.sendHandle(name)){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Room is full, please try again later. Game will exit.");
                alert.showAndWait();
                System.exit(0);
            }
        });
        
        new Thread(new LobbyCheck(gateway,red,blue,redReady,blueReady,status,bothReady)).start();
        bothReadyProperty.addListener((o,oldValue,newValue)->{
            if(newValue)startGame();
        });
    }    
}

class MovementCheck implements Runnable,colorgame.ColorGameConstants{
    private static Lock lock = new ReentrantLock();
    private ColorGameGateway gateway;
    private FXMLGamePaneController controller;
    
    public MovementCheck(ColorGameGateway gateway,FXMLGamePaneController controller){
        this.gateway = gateway;
        this.controller = controller;
    }
    
    public void run(){
        while(controller.getTime()!=0){
            lock.lock();
            Platform.runLater(()->controller.updateCanvas(gateway.getLoc()));
            try{
                Thread.sleep(50);
            }catch(InterruptedException ex){}
            finally{
                lock.unlock();
            }
        }
    }
}

class LobbyCheck implements Runnable,colorgame.ColorGameConstants{
    private ColorGameGateway gateway;
    private Label red;
    private Label blue;
    private Label redReady;
    private Label blueReady;
    private Label status;
    private ReadOnlyBooleanWrapper bothReady;
    
    public LobbyCheck(ColorGameGateway gateway,Label red,Label blue,Label redReady,
            Label blueReady,Label status,ReadOnlyBooleanWrapper bothReady){
        this.gateway = gateway;
        this.red=red;
        this.blue=blue;
        this.redReady=redReady;
        this.blueReady=blueReady;
        this.status=status;
        this.bothReady = bothReady;
    }
    
    public void run(){
        while(true){
            List<String> players = gateway.updateLobby();
            if(players!=null){
                if(players.get(0)!=null)Platform.runLater(()->red.setText(players.get(0)));
                else Platform.runLater(()->red.setText("Waiting..."));
                if(players.get(1)!=null)Platform.runLater(()->blue.setText(players.get(1)));
                else Platform.runLater(()->blue.setText("Waiting..."));
                String rr = players.get(2); String br = players.get(3);
                if(rr.equals("true")&&br.equals("true"))Platform.runLater(()->{
                    redReady.setText("READY");
                    blueReady.setText("READY");
                    bothReady.set(true);
                        });
                else if(rr.equals("true")&&players.get(1)==null)Platform.runLater(()->{
                    redReady.setText("READY");
                    blueReady.setText("");
                    status.setText("Waiting for player 2 to join...");
                        });
                else if(rr.equals("true")&&br.equals("false"))Platform.runLater(()->{
                    redReady.setText("READY");
                    blueReady.setText("");
                    status.setText("Waiting for "+players.get(1)+"...");
                        });
                else if(players.get(0)==null&&br.equals("true"))Platform.runLater(()->{
                    blueReady.setText("READY");
                    redReady.setText("");
                    status.setText("Waiting for player 1 to join...");
                        });
                else if(rr.equals("false")&&br.equals("true"))Platform.runLater(()->{
                    redReady.setText("");
                    blueReady.setText("READY");
                    status.setText("Waiting for "+players.get(0)+"...");
                        });
                else if(rr.equals("false")&&br.equals("false"))Platform.runLater(()->{
                    redReady.setText("");
                    blueReady.setText("");
                    status.setText("Are you ready?");
                });
            }
            try{
                Thread.sleep(250);
            }catch(InterruptedException ex){
                
            }
        }
    }
}