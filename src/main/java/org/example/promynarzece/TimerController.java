package org.example.promynarzece;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.concurrent.TimeUnit;

public class TimerController extends Thread{

    @FXML
    private Label timerLabel;

    private volatile boolean running = false;
    private final int maxTimeMillis; // Set maximum time in milliseconds

    TimerController(int maxTimeMillis,Label timerLabel){
        this.maxTimeMillis = maxTimeMillis;
        this.timerLabel = timerLabel;

    }

    @Override
    public void run() {
        running = true;
        try {
            long startTime = System.currentTimeMillis();
            while (running ) {
                if (Controller.isStop())
                    continue;

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                if (elapsedTime >= maxTimeMillis) {
                    elapsedTime = maxTimeMillis;
                    running = false;
                    Thread.currentThread().interrupt();
                }
                long seconds = (elapsedTime / 1000) % 60;
                long milliseconds = elapsedTime % 1000;
                Platform.runLater(() -> timerLabel.setText(String.format("%02d : %03d", seconds, milliseconds)));
                if (elapsedTime >= maxTimeMillis) {
                    Platform.runLater(() -> timerLabel.setText(""));
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(1); // Update every 1 millisecond
            }
        } catch (InterruptedException e) {
            Platform.runLater(() -> timerLabel.setText("")); // Clear the label on interruption
            Thread.currentThread().interrupt();
        }
    }
}
