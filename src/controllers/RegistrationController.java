package controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import database.UserDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import models.User;
import utils.EmailService;

public class RegistrationController implements Initializable {

    public String fullname;
    public int ID;

    @FXML
    private Button goBackBtn;

    @FXML
    private PasswordField pwf_password;

    @FXML
    private Button registerBtn;

    @FXML
    private TextField txtf_displayName;

    @FXML
    private TextField txtf_emailAddress;

    @FXML
    private TextField txtf_fullName;

    @FXML
    private TextField txtf_studentID;

    @FXML
    private Label wrongLabel;

    @FXML
    void handleRegister(ActionEvent event) {
        String displayName = txtf_displayName.getText().trim();
        String email = txtf_emailAddress.getText().trim();
        String fullName = txtf_fullName.getText().trim();
        String studentIDText = txtf_studentID.getText().trim();
        String password = pwf_password.getText();

        if (displayName.isEmpty()) {
            wrongLabel.setText("Display name is required");
            wrongLabel.setVisible(true);
            return;
        }
        if (displayName.matches(".*\\d{9}.*")) {
            wrongLabel.setText("Display name cannot contain 9-digit numbers");
            wrongLabel.setVisible(true);
            return;
        }
        if (fullName.isEmpty()) {
            wrongLabel.setText("Full name is required");
            wrongLabel.setVisible(true);
            return;
        }
        if (studentIDText.isEmpty()) {
            wrongLabel.setText("Student ID is required");
            wrongLabel.setVisible(true);
            return;
        }
        if (!studentIDText.matches("^\\d{9}$")) {
            wrongLabel.setText("Student ID must be exactly 9 digits");
            wrongLabel.setVisible(true);
            return;
        }
        if (email.isEmpty() || !email.matches("^[\\w.-]+@iut-dhaka\\.edu$")) {
            wrongLabel.setText("Enter a valid IUT email address");
            wrongLabel.setVisible(true);
            return;
        }
        if (UserDAO.getUserByEmail(email) != null) {
            wrongLabel.setText("Email is already registered");
            wrongLabel.setVisible(true);
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            wrongLabel.setText("Password must be at least 6 characters");
            wrongLabel.setVisible(true);
            return;
        }

        ID = Integer.parseInt(studentIDText);

        wrongLabel.setVisible(false);
        fullname = fullName;

        sendVerificationEmail(email, displayName, password);
    }

    private void sendVerificationEmail(String email, String displayName, String password) {
        try {
            registerBtn.setDisable(true);
            registerBtn.setText("Sending PIN...");

            Platform.runLater(() -> {
                try {
                    EmailService.sendConfirmationPin(email, displayName);

                    Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        registerBtn.setText("Register");

                        showPinVerificationDialog(email, displayName, password);
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        registerBtn.setDisable(false);
                        registerBtn.setText("Register");
                        wrongLabel.setText("Failed to send verification email. Please try again.");
                        wrongLabel.setVisible(true);
                    });
                    e.printStackTrace();
                    System.out.println("RegControl : sendVerMail");
                }
            });

        } catch (Exception e) {
            registerBtn.setDisable(false);
            registerBtn.setText("Register");
            wrongLabel.setText("Registration failed. Please try again.");
            wrongLabel.setVisible(true);
            e.printStackTrace();
            System.out.println("RegControl : showResend");
        }
    }

    private void showPinVerificationDialog(String email, String displayName, String password) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Email Verification");
        dialog.setHeaderText("Verify Your Email Address");
        dialog.setContentText(String.format(
            "We've sent a 6-digit verification PIN to:\n%s\n\nPlease enter the PIN:",
            email
        ));

        dialog.getDialogPane().setPrefWidth(400);

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String pinInput = result.get().trim();

            try {
                int enteredPin = Integer.parseInt(pinInput);

                if (EmailService.verifyPin(email, enteredPin)) {
                    completeRegistration(displayName, email, password);
                } else {
                    wrongLabel.setText("Invalid or expired PIN. Please try again.");
                    wrongLabel.setVisible(true);

                    showResendPinOption(email, displayName, password);
                }

            } catch (NumberFormatException e) {
                wrongLabel.setText("Please enter a valid 6-digit PIN.");
                wrongLabel.setVisible(true);

                Platform.runLater(() -> showPinVerificationDialog(email, displayName, password));
            }
        } else {
            wrongLabel.setText("Email verification cancelled.");
            wrongLabel.setVisible(true);
        }
    }

    private void showResendPinOption(String email, String displayName, String password) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("PIN Verification Failed");
        alert.setHeaderText("Invalid PIN");
        alert.setContentText("The PIN you entered is invalid or has expired.\n\nWould you like us to send a new PIN?");

        javafx.scene.control.ButtonType resendButton = new javafx.scene.control.ButtonType("Resend PIN");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(resendButton, cancelButton);

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == resendButton) {

            try {
                EmailService.sendConfirmationPin(email, displayName);
                showPinVerificationDialog(email, displayName, password);
            } catch (Exception e) {
                wrongLabel.setText("Failed to resend PIN. Please try again later.");
                wrongLabel.setVisible(true);
                e.printStackTrace();
                System.out.println("RegControl : showResend");
            }
        }
    }

    private void completeRegistration(String displayName, String email, String password) {
        try {
            String hashedPassword = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                .hashToString(12, password.toCharArray());

            String randomIdentifier = generateUniqueRandomIdentifier();

            User newUser = new User();
            newUser.id = ID;
            newUser.fullName = fullname;
            newUser.displayName = displayName;
            newUser.emailAddress = email;
            newUser.password = hashedPassword;
            newUser.randomIdentifier = randomIdentifier;

            UserDAO.insertUser(newUser);

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Welcome to Tong!");
            alert.setContentText("Let us log you in");
            alert.showAndWait();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/room.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) registerBtn.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.setTitle("Tong");
            stage.show();

        } catch (Exception e) {
            wrongLabel.setStyle("-fx-text-fill: red;");
            wrongLabel.setText("Registration failed. Please try again.");
            wrongLabel.setVisible(true);
            e.printStackTrace();
            System.out.println("RegControl : completeReg");
        }
    }

    private String generateUniqueRandomIdentifier() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.util.Random random = new java.util.Random();
        String randomIdentifier;

        do {
            StringBuilder sb = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                int index = random.nextInt(characters.length());
                sb.append(characters.charAt(index));
            }
            randomIdentifier = sb.toString();
        } while (UserDAO.getUserByRandomID(randomIdentifier) != null);

        return randomIdentifier;
    }

    @FXML
    void loadLogin(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) goBackBtn.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.setTitle("Tong - Login");
                        Image image = new Image("/views/tongLogo.png");
            stage.getIcons().add(image);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("RegControl : loadLogin");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wrongLabel.setVisible(false);
        txtf_displayName.requestFocus();

        txtf_displayName.setOnAction(e -> handleRegister(null));
        txtf_fullName.setOnAction(e -> handleRegister(null));
        txtf_studentID.setOnAction(e -> handleRegister(null));
        txtf_emailAddress.setOnAction(e -> handleRegister(null));
        pwf_password.setOnAction(e -> handleRegister(null));
    }
}
