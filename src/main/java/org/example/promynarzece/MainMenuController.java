package org.example.promynarzece;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

public class MainMenuController implements Initializable {

    public static Semaphore semaphore = new Semaphore(1);
    @FXML
    public ChoiceBox<String> maxWaitTimeBox;
    @FXML
    public ChoiceBox<Integer> iloscWatkowBox;
    @FXML
    public ChoiceBox<Integer>  shipCapacityBox;
    @FXML
    public ChoiceBox<Integer>  portCapacityBox;

    public ObservableList<String> maxWaitTimeChoice = FXCollections.
            observableArrayList("5 sekund","10 sekund","15 sekund");
    public ObservableList<Integer> threadsNumberChoice = FXCollections.
            observableArrayList(1,2,3,4,5,6,7);
    public ObservableList<Integer> shipCapacityChoice = FXCollections.
            observableArrayList(5,10,15);
    public ObservableList<Integer> portCapacityChoice = FXCollections.
            observableArrayList(5,10,15);

    @FXML
    public Button simulationStart;
    @FXML
    public Button loadFromFile;
    private int maxWaitTime;

    public void switchToShipsScene(ActionEvent e) throws IOException, InterruptedException {

        FXMLLoader root = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        semaphore.acquire();

        Scene scene = new Scene(root.load());
        Controller newController = root.getController();

        newController.setN( (int) iloscWatkowBox.getValue() );
        newController.setShipsCapacity( (int) shipCapacityBox.getValue() );
        newController.setPortCapacity( (int) portCapacityBox.getValue() );
        newController.setMaxWaitTime( setMaxWaitTime() );

        semaphore.release();

        stage.setScene(scene);
        stage.show();
    }
    public void loadFromFile(ActionEvent e) throws IOException, InterruptedException {

        FXMLLoader root = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        semaphore.acquire();

        Scene scene = new Scene(root.load());
        Controller newController = root.getController();

        Properties props = new Properties();

        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        newController.setN( Integer.parseInt(props.getProperty("app.threadNumber")) );
        newController.setShipsCapacity( Integer.parseInt(props.getProperty("app.shipCapacity")) );
        newController.setPortCapacity( Integer.parseInt(props.getProperty("app.portCapacity")) );
        newController.setMaxWaitTime( Integer.parseInt(props.getProperty("app.maxWaitTime")) );

        semaphore.release();

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ChangeListener<Object> changeListener = (ObservableValue<?> observable, Object oldValue, Object newValue) -> {
            // Check if all ChoiceBoxes have a selected value
            if (maxWaitTimeBox.getValue() != null &&
                    iloscWatkowBox.getValue() != null &&
                    shipCapacityBox.getValue() != null &&
                    portCapacityBox.getValue() != null) {
                simulationStart.setDisable(false);
            } else {
                simulationStart.setDisable(true);
            }
        };

        simulationStart.setDisable(true);

        //maxWaitTimeBox.setValue("Normalnie");
        maxWaitTimeBox.setItems(maxWaitTimeChoice);
        maxWaitTimeBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        iloscWatkowBox.setItems(threadsNumberChoice);
        iloscWatkowBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        shipCapacityBox.setItems(shipCapacityChoice);
        shipCapacityBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        portCapacityBox.setItems(portCapacityChoice);
        portCapacityBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private int setMaxWaitTime()
    {
        return switch ((String) maxWaitTimeBox.getValue()) {
            case "5 sekund" -> {
                maxWaitTime = 5000;
                yield maxWaitTime;
            }
            case "10 sekund" -> {
                maxWaitTime = 10000;
                yield maxWaitTime;
            }
            case "15 sekund" -> {
                maxWaitTime = 15000;
                yield maxWaitTime;
            }
            default -> maxWaitTime;
        };
    }


}
