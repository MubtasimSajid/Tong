package controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import at.favre.lib.crypto.bcrypt.BCrypt;
import database.UserDAO;
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

public class LoginController implements Initializable {

    @FXML
    private Button createAccountBtn;

    @FXML
    private Button forgotPasswordBtn;

    @FXML
    private PasswordField pwf_password;

    @FXML
    private Button signInBtn;

    @FXML
    private TextField txtf_mail;

    @FXML
    private Label wrongLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wrongLabel.setVisible(false);
        txtf_mail.requestFocus();
        pwf_password.setFocusTraversable(true);

        txtf_mail.setOnAction(event -> checkLogin());
        pwf_password.setOnAction(event -> checkLogin());
    }

    public void checkLogin() {
        String mail = txtf_mail.getText(), password = pwf_password.getText();
        User found = UserDAO.getUserByEmail(mail);

        if (found != null && BCrypt.verifyer().verify(password.toCharArray(), found.password).verified) {
            try {
                signInBtn.getScene().getWindow().hide();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/room.fxml"));
                javafx.scene.Parent root = loader.load();

                controllers.RoomController roomController = loader.getController();
                roomController.setCurrentUser(found);

                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Tong");
                stage.setResizable(false);
                Image image = new Image("/views/tongLogo.png");
                stage.getIcons().add(image);

                stage.setOnCloseRequest(event -> {
                    roomController.cleanup();
                    javafx.application.Platform.exit();
                    System.exit(0);
                });

                stage.show();
            } catch (Exception e) {
                System.out.println("LoginController : checkLogin");
                e.printStackTrace();
            }
        } else {
            wrongLabel.setVisible(true);
            txtf_mail.requestFocus();
        }
    }

    public void loadCreateAccount() {
        try {
            createAccountBtn.getScene().getWindow().hide();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/register.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Tong - Registration");
            stage.setResizable(false);
                        Image image = new Image("/views/tongLogo.png");
            stage.getIcons().add(image);
            stage.show();
        } catch (Exception e) {
            System.out.println("LoginController : loadCreateAccount");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgetPassword() {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Password Reset");
        emailDialog.setHeaderText("Reset Your Password");
        emailDialog.setContentText("Please enter your email address:");
        emailDialog.getDialogPane().setPrefWidth(400);

        Optional<String> emailResult = emailDialog.showAndWait();

        if (!emailResult.isPresent() || emailResult.get().trim().isEmpty()) {
            showAlert("Email Not Found", "No account found with this email address.");
            return;
        }

        String email = emailResult.get().trim();

        if (!email.matches("^[\\w.-]+@iut-dhaka\\.edu$")) {
            showAlert("Invalid Email", "Please enter a valid IUT email address.");
            return;
        }

        User user = UserDAO.getUserByEmail(email);
        if (user == null) {
            showAlert("Email Not Found", "No account found with this email address.");
            return;
        }

        try {
            EmailService.sendConfirmationPin(email, user.displayName);

            TextInputDialog pinDialog = new TextInputDialog();
            pinDialog.setTitle("Email Verification");
            pinDialog.setHeaderText("Verify Your Email");
            pinDialog.setContentText(String.format(
                "We've sent a 6-digit verification PIN to:\n%s\n\nPlease enter the PIN:",
                email
            ));
            pinDialog.getDialogPane().setPrefWidth(400);

            Optional<String> pinResult = pinDialog.showAndWait();

            if (!pinResult.isPresent() || pinResult.get().trim().isEmpty()) {
                return;
            }

            String pinInput = pinResult.get().trim();
            int enteredPin;

            try {
                enteredPin = Integer.parseInt(pinInput);
            } catch (NumberFormatException e) {
                showAlert("Invalid PIN", "Please enter a valid 6-digit PIN.");
                return;
            }

            if (!EmailService.verifyPin(email, enteredPin)) {
                showAlert("Invalid PIN", "The PIN you entered is invalid or has expired.");
                return;
            }

            javafx.scene.control.Dialog<String> passwordDialog = new javafx.scene.control.Dialog<>();
            passwordDialog.setTitle("Reset Password");
            passwordDialog.setHeaderText("Set New Password");

            javafx.scene.control.ButtonType confirmButtonType = new javafx.scene.control.ButtonType("Reset Password", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            passwordDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("New Password");
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm Password");

            grid.add(new Label("New Password:"), 0, 0);
            grid.add(newPasswordField, 1, 0);
            grid.add(new Label("Confirm Password:"), 0, 1);
            grid.add(confirmPasswordField, 1, 1);

            passwordDialog.getDialogPane().setContent(grid);

            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    String newPassword = newPasswordField.getText();
                    String confirmPassword = confirmPasswordField.getText();

                    if (newPassword.isEmpty() || newPassword.length() < 6) {
                        showAlert("Invalid Password", "Password must be at least 6 characters long.");
                        return null;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        showAlert("Password Mismatch", "Passwords do not match. Please try again.");
                        return null;
                    }

                    return newPassword;
                }
                return null;
            });

            Optional<String> passwordResult = passwordDialog.showAndWait();

            if (!passwordResult.isPresent()) {
                return; // User cancelled
            }

            String newPassword = passwordResult.get();

            String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

            if (UserDAO.updateUserPassword(email, hashedPassword)) {
                showAlert("Success", "Your password has been reset successfully! You will now be logged in.");

                loginUser(user);
            } else {
                showAlert("Error", "Failed to update password. Please try again later.");
            }

        } catch (Exception e) {
            showAlert("Error", "Failed to send verification email. Please try again later.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loginUser(User user) {
        try {
            signInBtn.getScene().getWindow().hide();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/room.fxml"));
            javafx.scene.Parent root = loader.load();

            controllers.RoomController roomController = loader.getController();
            roomController.setCurrentUser(user);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Tong");
            stage.setResizable(true);
            Image image = new Image("/views/tongLogo.png");
            stage.getIcons().add(image);

            stage.setOnCloseRequest(event -> {
                roomController.cleanup();
                javafx.application.Platform.exit();
                System.exit(0);
            });

            stage.show();
        } catch (Exception e) {
            System.out.println("LoginController : loginUser");
            e.printStackTrace();
        }
    }
}
