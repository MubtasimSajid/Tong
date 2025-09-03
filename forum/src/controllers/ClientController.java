package controllers;

import java.util.ArrayList;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import models.User;

public class ClientController {

    @FXML
    public Pane pnSignIn;

    @FXML
    public Pane pnSignUp;

    @FXML
    public Button btnSignUp;

    @FXML
    public Button getStarted;

    @FXML
    public ImageView btnBack;

    @FXML
    public TextField regName;

    @FXML
    public TextField regEmail;

    @FXML
    public TextField regPhoneNo;

    @FXML
    public RadioButton male;

    @FXML
    public RadioButton female;

    @FXML
    public Label controlRegLabel;

    @FXML
    public Label success;

    @FXML
    public Label goBack;

    @FXML
    public TextField userName;

    @FXML
    public TextField passWord;

    @FXML
    public Label loginNotifier;

    @FXML
    public Label nameExists;

    @FXML
    public Label checkEmail;

    public static String username, password, gender;
    public static ArrayList<User> loggedInUsers = new ArrayList<>();
    public static ArrayList<User> users = new ArrayList<>();

    public void registration() {
        
    }
}
