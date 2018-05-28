/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colorgameclient;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

/**
 * FXML Controller class
 *
 * @author Nguyen Anh Kiet 1
 */
public class FXMLGamePaneController implements Initializable, colorgame.ColorGameConstants {

    private ColorGameGateway gateway;
    private int seconds = TIME;
    private GraphicsContext gc;
    private int redpx = 0;
    private int bluepx = 0;
    private Button ready;
    private ReadOnlyBooleanWrapper bothReady;

    @FXML
    private Circle red;
    @FXML
    private Circle blue;
    @FXML
    private Label redPlayer;
    @FXML
    private Label bluePlayer;
    @FXML
    private Label clock;
    @FXML
    private Canvas canvas;
    @FXML
    private Pane pane;
    @FXML
    private Circle white;
    @FXML
    private VBox vbox;
    @FXML
    private Label redNo;
    @FXML
    private Label blueNo;
    @FXML
    private Label wins;

    public void setGateway(ColorGameGateway gateway) {
        this.gateway = gateway;
    }

    public void setHandles(List<String> handles) {
        redPlayer.setText(handles.get(0));
        bluePlayer.setText(handles.get(1));
    }

    public void setTime(int s) {
        clock.setText(String.valueOf(s));
    }

    public void setProperty(ReadOnlyBooleanWrapper r) {
        bothReady = r;
    }

    public int getTime() {
        return seconds;
    }

    public void updateCanvas(int[] loc) {
        gc.setFill(Color.RED);
        gc.fillOval(red.getCenterX() - loc[0], red.getCenterY() - loc[0], loc[0] * 2, loc[0] * 2);
        red.setCenterX(loc[2]);
        red.setCenterY(loc[3]);

        gc.setFill(Color.BLUE);
        gc.fillOval(blue.getCenterX() - loc[4], blue.getCenterY() - loc[4], loc[4] * 2, loc[4] * 2);
        blue.setCenterX(loc[6]);
        blue.setCenterY(loc[7]);
    }

    @FXML
    private void back() {
        gateway.reset();
        ready.setDisable(false);
        bothReady.set(false);
        blue.getScene().getWindow().hide();
    }

    public void setReadyButton(Button r) {
        ready = r;
    }

    public void calculate() {
        WritableImage snapshot = canvas.snapshot(null, null);
        PixelReader px = snapshot.getPixelReader();

        for (int i = 0; i < canvas.getWidth(); ++i) {
            for (int j = 0; j < canvas.getHeight(); ++j) {
                int p = px.getArgb(i, j);
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                if (r == 255 && b != 255) {
                    ++redpx;
                }
                if (b == 255 && r != 255) {
                    ++bluepx;
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        white.setVisible(false);
        vbox.setVisible(false);
        red = new Circle(100, 100, RADIUS, Color.RED);
        red.setStroke(Color.WHITE);
        red.setStrokeType(StrokeType.OUTSIDE);
        red.setStrokeWidth(1);

        blue = new Circle(400, 400, RADIUS, Color.BLUE);
        blue.setStroke(Color.WHITE);
        blue.setStrokeType(StrokeType.OUTSIDE);
        blue.setStrokeWidth(1);

        pane.getChildren().addAll(red, blue);

        red.setCenterX(100);
        red.setCenterY(100);

        blue.setCenterX(400);
        blue.setCenterY(400);

        new Thread(() -> {
            while (seconds != 0) {
                --seconds;
                Platform.runLater(() -> {
                    clock.setText(String.valueOf(seconds));
                });
                if (seconds == 0) {
                    Platform.runLater(() -> {
                        calculate();
                        red.setVisible(false);
                        blue.setVisible(false);
                        white.setVisible(true);
                        redNo.setText(String.valueOf(redpx));
                        blueNo.setText(String.valueOf(bluepx));
                        if (redpx > bluepx) {
                            wins.setText(redPlayer.getText() + " wins!");
                        } else if (bluepx > redpx) {
                            wins.setText(bluePlayer.getText() + " wins!");
                        } else if (bluepx == redpx) {
                            wins.setText("It's a draw!");
                        }
                        vbox.setVisible(true);
                    });
                }
                try {
                    Thread.sleep(1000);
                  } catch (InterruptedException ex) {

                }
            }
        }).start();
    }

}
