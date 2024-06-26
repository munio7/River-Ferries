package org.example.promynarzece;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Car_Thread_Right extends Thread {
    public int cars = 0;
    private final int capacity;
    private final Label nCarsRightLabel;
    private final Lock shipPortLockRight;
    private volatile Ship currentVisitor;

    public Car_Thread_Right(int capacity,Label nCarsRightLabel, Lock shipPortLockLeft) {

        this.capacity = capacity;
        this.nCarsRightLabel = nCarsRightLabel;
        this.shipPortLockRight = shipPortLockLeft;
    }

    @Override
    public void run() {
        try {

            while (!Controller.end) {
                if (Controller.isStop())
                    continue;
                synchronized (shipPortLockRight) {
                    if (currentVisitor != null) {
                        while (currentVisitor.capacity > currentVisitor.currentlyHolding && cars != 0) {
                            cars--;
                            currentVisitor.carAnimationRightIn();
                            currentVisitor.currentlyHolding++;
                            updateCarLabel();
                            TimeUnit.MILLISECONDS.sleep(50);

                            if (currentVisitor==null) {break;}
                            else if (currentVisitor.capacity == currentVisitor.currentlyHolding)
                            {
                                shipPortLockRight.notifyAll();
                                setCurrentVisitor(null);
                                break;
                            }
                        }
                    }
                }

                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 3));
                if(cars == capacity)
                    continue;
                else
                    cars++;
                updateCarLabel();

            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateCarLabel(){Platform.runLater(() -> nCarsRightLabel.setText("" + cars));}

    public void setCurrentVisitor(Ship visitor){currentVisitor = visitor;}
}
