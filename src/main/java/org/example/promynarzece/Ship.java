package org.example.promynarzece;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Ship extends Thread {
    @FXML
    public AnchorPane AnchorPane;
    //każdy statek ma imię
    public String Name;
    //każdy statek zna swój moment
    private final List<Ship> Queue;
    //semafor do odpowiedniej kolejki
    private final Semaphore queueSemaphore;
    private Lock queueLock;
    //wskaźnik na kolejny statek
    private Ship shipAhead;

   //dwie opcje wyglądu statku
    private final Image imageL = new Image("ship_left_beztła.png");
    private final Image imageR = new Image("ship_right_beztła.png");

    //obiekt do sceneBuildera
    public ImageView imageView;

    //pozycja X i Y statku na scenie
    private double positionX;
    private double positionY;

    //Pojemność statku
    public int capacity;
    //Ile aktualnie posiada
    public int currentlyHolding = 0;

    // parametr odpowiedzialny za prędkość statków
    public static int sleepTime = 50;
    //Lock do niewpuszczania statku do portu po lewej stronie
    private final Lock stopLockLeft;
    //Lock, aby umożliwić odbiór statków z portu po lewej stronie
    private final Lock shipPortLockLeft;
    //Lock do niewpuszczania statku do portu po prawej stronie
    private final Lock stopLockRight;
    //Lock, aby umożliwić odbiór statków z portu po prawej stronie
    private final Lock shipPortLockRight;

    private final Car_Thread_Left threadCarLeft;
    private final Car_Thread_Right threadCarRight;

    private final int maxWaitTime;
    private int nThreads;

    @FXML
    public Label timerLabelL;
    @FXML
    public Label timerLabelR;

    public Ship(AnchorPane AnchorPane,
                int N,
                int capacity,
                String name,
                int maxWaitTime,
                List<Ship> Queue,
                Semaphore queueSemaphore,
                Lock queueLock,
                Lock stopLockLeft,
                Lock shipPortLockLeft,
                Car_Thread_Left threadCarLeft,
                Lock stopLockRight,
                Lock shipPortLockRight,
                Car_Thread_Right threadCarRight,
                Label timerLabelL,
                Label timerLabelR
    ){

        this.imageView = new ImageView(imageL);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        this.stopLockLeft = stopLockLeft;
        this.stopLockRight = stopLockRight;
        this.capacity = capacity;
        this.Name = name;
        this.maxWaitTime = maxWaitTime;
        this.Queue = Queue;
        this.queueSemaphore = queueSemaphore;
        this.queueLock = queueLock;
        this.shipPortLockLeft = shipPortLockLeft;
        this.shipPortLockRight = shipPortLockRight;
        this.threadCarLeft = threadCarLeft;
        this.threadCarRight = threadCarRight;
        this.AnchorPane = AnchorPane;
        this.nThreads = N;
        this.timerLabelL = timerLabelL;
        this.timerLabelR = timerLabelR;

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.ZERO);

        // Set up the event handlers for showing the tooltip
        imageView.setOnMouseEntered(event -> {
            tooltip.setText("Posiada samochodów: " + this.currentlyHolding);
            tooltip.show(imageView, event.getScreenX(), event.getScreenY() + 10);
        });

        imageView.setOnMouseExited(event -> {
            tooltip.setText("Posiada samochodów: " + this.currentlyHolding);
            tooltip.hide();
        });

        imageView.setOnMouseDragged(event -> {
            tooltip.setText("Posiada samochodów: " + this.currentlyHolding);
            tooltip.show(imageView, event.getScreenX(), event.getScreenY() + 10);
        });
    }

    @Override
    public void run() {

        // Puszczenie statków w odpowiedniej kolejności
        while(true){
            try {
                queueLock.lock();
                if (Objects.equals(Queue.getFirst(), this)) {
                    Queue.removeFirst();
                    queueSemaphore.acquire();

                    break;
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            finally {
                queueLock.unlock();
            }
        }


        while(!Controller.end) {
            if (Controller.isStop())
                continue;
            else {
                if (!isCleanRoad())
                    continue;
                else {
                    if (positionX == 190.00 && positionY == 109.00) {
                        stopLockLeft.lock();
                        try {
                            parkL();
                            imageView.setImage(imageR);
                            pickUpLeft();
                            awayL();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        finally {
                            stopLockLeft.unlock();
                        }
                    }
                    else if (positionX == 730.00 && positionY == 349.00) {
                        try {
                            stopLockRight.lock();
                            parkR();
                            imageView.setImage(imageL);
                            pickUpRight();
                            awayR();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        finally {
                            stopLockRight.unlock();
                        }
                    }
                    else if (positionY == 349.00) {
                        try {
                            moveRight();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (positionY == 109.00) {
                        try {
                            moveLeft();
                            queueSemaphore.release();

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
    }

    public void setPositionX(double X){
        this.positionX = X;
    }
    public void setPositionY(double Y) {
        this.positionY = Y;
    }
    public void setShipAhead(Ship shipAhead) {
        this.shipAhead = shipAhead;
    }
    public void updateImagePosition() {
        Platform.runLater(() -> {
            javafx.scene.layout.AnchorPane.setLeftAnchor(this.imageView, this.positionX);
            javafx.scene.layout.AnchorPane.setTopAnchor(this.imageView, this.positionY);
        });
    }
    private void sleep() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(sleepTime);
    }
    public double getPositionX(){
        return this.positionX;
    }
    public double getPositionY(){
        return this.positionY;
    }
    public void moveLeft() throws InterruptedException {

        positionX -= 10;
        updateImagePosition();

        sleep();
    }
    public void moveRight() throws InterruptedException {

        positionX += 10;
        updateImagePosition();

        sleep();
    }
    public void moveDLD(){
        positionX -=5;
        positionY +=12;
    }
    public void moveDRD(){
        positionX +=5;
        positionY +=12;
    }
    public void moveDRU(){
        positionX += 5;
        positionY -= 12;
    }
    public void moveDLU(){
        positionX -= 5;
        positionY -= 12;
    }
    public void parkL() throws InterruptedException {
        int cycle=10;
        while (cycle!=0)
        {
            if (Controller.isStop())
                continue;
            moveDLD();
            updateImagePosition();
            sleep();
            cycle--;
        }
    }
    public void awayL() throws InterruptedException {
        int cycle=10;
        while (cycle!=0)
        {
            if (Controller.isStop())
                continue;
            moveDRD();
            updateImagePosition();
            cycle--;
            sleep();

        }
    }
    public void parkR() throws InterruptedException {
        int cycle=10;
        while (cycle!=0)
        {
            if (Controller.isStop())
                continue;
            moveDRU();
            updateImagePosition();
            sleep();
            cycle--;
        }
    }
    public void awayR() throws InterruptedException {
        int cycle=10;
        while (cycle!=0)
        {
            if (Controller.isStop())
                continue;
            moveDLU();
            updateImagePosition();
            cycle--;
            sleep();

        }
    }

    public boolean isCleanRoad() {
        double Ya = shipAhead.getPositionY();
        double Xa = shipAhead.getPositionX();
        if (nThreads == 1)
            return true;
        else if(Ya != getPositionY()) {
            return true;
        } else {
            double distance = Math.abs(Xa - getPositionX());
            return distance >= 100;
        }
    }
    public void carAnimationLeftOut() throws InterruptedException {
        Image carImage = new Image("carL.png");
        ImageView samochod = new ImageView(carImage);
        samochod.setFitWidth(100);
        samochod.setFitHeight(100);
        samochod.setPreserveRatio(true);
        javafx.scene.layout.AnchorPane.setTopAnchor(samochod,209.00); // Adjust spacing as needed
        javafx.scene.layout.AnchorPane.setLeftAnchor(samochod, 49.00);
        Platform.runLater(() -> {
            AnchorPane.getChildren().add(samochod);
        });

        for (int i = 1; i < 11; i++){
            while(Controller.isStop())
                ;
            double newCordsY = 209.00 - 10*i;
            double newCordsX = 49.00 - 10*i;

            if (i>4) {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, 159.00);
                });
            }
            else {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, newCordsY);
                });
            }

            Platform.runLater(() -> {
            javafx.scene.layout.AnchorPane.setLeftAnchor(samochod,newCordsX); // Adjust spacing as needed
            });

            TimeUnit.MILLISECONDS.sleep(50);
        }
        Platform.runLater(() -> {
            AnchorPane.getChildren().remove(samochod);
        });
    }
    public void carAnimationLeftIn() throws InterruptedException {
        Image carImage = new Image("carR.png");
        ImageView samochod = new ImageView(carImage);
        samochod.setFitWidth(100);
        samochod.setFitHeight(100);
        samochod.setPreserveRatio(true);
        javafx.scene.layout.AnchorPane.setTopAnchor(samochod,289.00); // Adjust spacing as needed
        javafx.scene.layout.AnchorPane.setLeftAnchor(samochod, -30.00);
        Platform.runLater(() -> {
            AnchorPane.getChildren().add(samochod);
        });

        for (int i = 1; i < 11; i++){
            while(Controller.isStop())
                ;
            double newCordsY = 289.00 - 10*i;
            double newCordsX = -30 + 10*i;

            if (i>6) {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, 219.00);
                });
            }
            else {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, newCordsY);
                });
            }

            Platform.runLater(() -> {
                javafx.scene.layout.AnchorPane.setLeftAnchor(samochod,newCordsX); // Adjust spacing as needed
            });

            TimeUnit.MILLISECONDS.sleep(50);
        }
        Platform.runLater(() -> {
            AnchorPane.getChildren().remove(samochod);
        });
    }

    public void carAnimationRightOut() throws InterruptedException {
        Image carImage = new Image("carR.png");
        ImageView samochod = new ImageView(carImage);
        samochod.setFitWidth(100);
        samochod.setFitHeight(100);
        samochod.setPreserveRatio(true);
        javafx.scene.layout.AnchorPane.setTopAnchor(samochod,209.00); // Adjust spacing as needed
        javafx.scene.layout.AnchorPane.setLeftAnchor(samochod, 849.00);
        Platform.runLater(() -> {
            AnchorPane.getChildren().add(samochod);
        });

        for (int i = 0; i < 15; i++){
            while(Controller.isStop())
                ;

            double newCordsY = 209.00 + 10*i;
            double newCordsX = 849.00 + 10*i;

            if (i>8) {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, 299.00);
                });
            }
            else {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, newCordsY);
                });
            }

            Platform.runLater(() -> {
                javafx.scene.layout.AnchorPane.setLeftAnchor(samochod,newCordsX); // Adjust spacing as needed
            });

            TimeUnit.MILLISECONDS.sleep(50);
        }
        Platform.runLater(() -> {
            AnchorPane.getChildren().remove(samochod);
        });
    }

    public void carAnimationRightIn() throws InterruptedException {
        Image carImage = new Image("carL.png");
        ImageView samochod = new ImageView(carImage);
        samochod.setFitWidth(100);
        samochod.setFitHeight(100);
        samochod.setPreserveRatio(true);
        javafx.scene.layout.AnchorPane.setTopAnchor(samochod,159.00); // Adjust spacing as needed
        javafx.scene.layout.AnchorPane.setLeftAnchor(samochod, 959.00);
        Platform.runLater(() -> {
            AnchorPane.getChildren().add(samochod);
        });

        for (int i = 1; i < 13; i++){
            while(Controller.isStop())
                ;

            double newCordsY = 159.00 + 10*i;
            double newCordsX = 959.00 - 10*i;

            if (i>4) {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, 209.00);
                });
            }
            else {
                Platform.runLater(() -> {
                    javafx.scene.layout.AnchorPane.setTopAnchor(samochod, newCordsY);
                });
            }

            Platform.runLater(() -> {
                javafx.scene.layout.AnchorPane.setLeftAnchor(samochod,newCordsX); // Adjust spacing as needed
            });

            TimeUnit.MILLISECONDS.sleep(50);
        }
        Platform.runLater(() -> {
            AnchorPane.getChildren().remove(samochod);
        });
    }

    private void pickUpRight() throws InterruptedException{



        synchronized (shipPortLockRight){
            threadCarRight.setCurrentVisitor(this);
            int toLetGo = this.currentlyHolding;

            for (int i = 0; i<toLetGo; i++) {
                carAnimationRightOut();
                this.currentlyHolding--;
            }
            currentlyHolding = 0 ;

            while( currentlyHolding!=capacity && currentlyHolding<1 ){
                TimerController timer = new TimerController(this.maxWaitTime, this.timerLabelR);
                timer.start();
                shipPortLockRight.wait(maxWaitTime);
                timer.interrupt();
                Platform.runLater(() -> timerLabelR.setText(""));
            }


            threadCarRight.setCurrentVisitor(null);
        }
    }

    private void pickUpLeft() throws InterruptedException {


        synchronized (shipPortLockLeft){
            threadCarLeft.setCurrentVisitor(this);
            for (int i = 0; i<this.currentlyHolding; i++) {
                carAnimationLeftOut();
            }
            currentlyHolding = 0 ;

            while( currentlyHolding!=capacity && currentlyHolding<1 ){
                TimerController timer = new TimerController(this.maxWaitTime, this.timerLabelL);
                timer.start();
                shipPortLockLeft.wait(maxWaitTime);
                timer.interrupt();
                Platform.runLater(() -> timerLabelL.setText(""));
            }

            threadCarLeft.setCurrentVisitor(null);
        }
    }
}
