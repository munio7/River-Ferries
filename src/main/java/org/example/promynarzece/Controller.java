package org.example.promynarzece;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Controller implements Initializable {

    public Semaphore mainMenuSemaphore;
    @FXML
    public AnchorPane AnchorPane;
    private final List<Ship> Queue = new ArrayList<>();
    private final Semaphore queueSemaphore = new Semaphore(1);
    private final Lock queueLock = new ReentrantLock();

    public int N;
    public static volatile boolean end = false;
    public Car_Thread_Left threadCarLeft;
    public Car_Thread_Right threadCarRight;
    @FXML
    public Button startButton;
    @FXML
    public Label nCarsLeft;
    @FXML
    public Label nCarsRight;
    @FXML
    public Label timerLabelL;
    @FXML
    public Label timerLabelR;
    @FXML
    public Label iloscWatkowLabel;
    @FXML
    public Label pojemnoscPortuLabel;
    @FXML
    public Label czasOczekiwaniaLabel;
    @FXML
    public Label pojemnoscStatkowLabel;

    private boolean isStarted = false;
    @FXML
    public Button endButton;
    @FXML
    public Button changeParametersButton;
    @FXML
    public Button stopButton;
    @FXML
    public Button slowerButton;
    @FXML
    public Button fasterButton;
    public static volatile boolean stop = false;
    public synchronized static void setStop(boolean value) {
        stop = value;
    }
    public synchronized static boolean isStop() {
        return stop;
    }

    private final Lock stopLockLeft = new ReentrantLock(true);
    private final Lock shipPortLockLeft = new ReentrantLock(true);
    private final Lock stopLockRight = new ReentrantLock(true);
    private final Lock shipPortLockRight = new ReentrantLock(true);

    private int maxWaitTime;
    private int shipsCapacity;
    private int portCapacity;

    public void setN(int N){
        this.N = N;
    }
    public void setMaxWaitTime(int maxWaitTime) {this.maxWaitTime = maxWaitTime;}
    public void setShipsCapacity(int shipsCapacity) {this.shipsCapacity = shipsCapacity;}
    public void setPortCapacity(int portCapacity) {this.portCapacity = portCapacity;}


    public void stop(ActionEvent e){
        setStop(true);
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }
    public void start(ActionEvent e){
        end = false;
        if (!isStarted) {
            threadCarLeft = new Car_Thread_Left(portCapacity,nCarsLeft,shipPortLockLeft);
            threadCarRight = new Car_Thread_Right(portCapacity,nCarsRight,shipPortLockRight);

            Ship[] shipsArray = new Ship[N];
            for (int i = 0; i < N; i++) {
                shipsArray[i] = new Ship(
                        AnchorPane,
                        N,
                        shipsCapacity,
                        "Statek " + i ,
                        maxWaitTime,
                        Queue,
                        queueSemaphore,
                        queueLock,
                        stopLockLeft,
                        shipPortLockLeft,
                        threadCarLeft,
                        stopLockRight,
                        shipPortLockRight,
                        threadCarRight,
                        timerLabelL,
                        timerLabelR
                );
                Queue.add(shipsArray[i]);

                shipsArray[i].setPositionX(i * 100 + 200.0);
                shipsArray[i].setPositionY(109.00);

                // Set the position of the ImageView within the AnchorPane
                javafx.scene.layout.AnchorPane.setTopAnchor(shipsArray[i].imageView, shipsArray[i].getPositionY()); // Adjust spacing as needed
                javafx.scene.layout.AnchorPane.setLeftAnchor(shipsArray[i].imageView, shipsArray[i].getPositionX());

                AnchorPane.getChildren().add(shipsArray[i].imageView);


            }
            for (int i = 0; i < N; i++) {
                if (i > 0) {
                    shipsArray[i].setShipAhead(shipsArray[i - 1]);
                } else {
                    shipsArray[0].setShipAhead(shipsArray[N - 1]);
                }
            }
            threadCarLeft.start();
            threadCarRight.start();

            for (int i = 0; i < N; i++) {
                shipsArray[i].start();
            }

            isStarted = true;
        }
        else
            setStop(false);
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }
    public void end(ActionEvent e) throws InterruptedException {
        end = true;
        TimeUnit.SECONDS.sleep(2);
        Platform.exit();
        System.exit(0);
    }
    public void faster(ActionEvent e){
        int currentSpeed = Ship.sleepTime;

        if (currentSpeed == 10)
            fasterButton.setText("MAX");
        else
        {
            Ship.sleepTime -=10;
            slowerButton.setText("Wolniej");
        }
    }
    public void slower(ActionEvent e ){
        int currentSpeed = Ship.sleepTime;

        if (currentSpeed == 100)
            slowerButton.setText("MAX");
        else
        {
            Ship.sleepTime +=10;
            fasterButton.setText("Szybciej");
        }

    }
    public void normal(ActionEvent e){
        Ship.sleepTime = 50;
        slowerButton.setText("Wolniej");
        fasterButton.setText("Szybciej");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainMenuSemaphore = MainMenuController.semaphore;
        Platform.runLater(() -> {
        try {
            mainMenuSemaphore.acquire();
            iloscWatkowLabel.setText("" + N);
            pojemnoscPortuLabel.setText("" + shipsCapacity);
            czasOczekiwaniaLabel.setText("" + maxWaitTime / 1000);
            pojemnoscStatkowLabel.setText("" + shipsCapacity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            mainMenuSemaphore.release();
        }
        });
    }

    public void ChangeParameters(ActionEvent e) throws IOException {
        end = true;
        setStop(false);
        Ship.sleepTime = 50;
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MainMenuScene.fxml"));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), Color.LIGHTSKYBLUE);

        stage.setScene(scene);
        stage.show();

    }



}