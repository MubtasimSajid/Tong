package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import server.ServerController;

public class ClientController implements Initializable {

    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;

    private ServerController server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            server = new ServerController(new ServerSocket(1234));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating server");
        }

        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        server.receiveMessageFromClient(vbox_messages);

        button_send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String messageToSend = tf_message.getText();
                if (!messageToSend) {
                    
                }
            }
        })
    }
}
