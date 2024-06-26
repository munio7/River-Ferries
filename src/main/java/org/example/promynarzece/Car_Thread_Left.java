package org.example.promynarzece;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Car_Thread_Left extends Thread {
    public int cars = 0;
    private final int capacity;
    private final Label nCarsLeftLabel;
    private final Lock shipPortLockLeft;
    private volatile Ship currentVisitor;

    public Car_Thread_Left(int capacity,Label nCarsLeftLabel, Lock shipPortLockLeft) {

        this.capacity = capacity;
        this.nCarsLeftLabel = nCarsLeftLabel;
        this.shipPortLockLeft = shipPortLockLeft;
    }

    @Override
    public void run() {
        try {

            while (!Controller.end) {
                if (Controller.isStop())
                    continue;
                synchronized (shipPortLockLeft) {
                    if (currentVisitor != null) {
                        while (currentVisitor.capacity > currentVisitor.currentlyHolding && cars != 0) {
                            cars--;
                            currentVisitor.carAnimationLeftIn();
                            currentVisitor.currentlyHolding++;
                            updateCarLabel();
                            TimeUnit.MILLISECONDS.sleep(50);

                            if (currentVisitor==null) {break;}
                            else if (currentVisitor.capacity == currentVisitor.currentlyHolding)
                            {
                                shipPortLockLeft.notifyAll();
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
    public void updateCarLabel(){Platform.runLater(() -> nCarsLeftLabel.setText("" + cars));}

    public void setCurrentVisitor(Ship visitor){currentVisitor = visitor;}
}
