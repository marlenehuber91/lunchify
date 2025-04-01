package frontend.controller;

import backend.logic.UserService;
import backend.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginPageController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Label forgotPassword;
    @FXML
    private Label adminInformed;


    @FXML
    public void initialize() { //Listener = AI generated
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidEmail(newValue)) {
                usernameField.setStyle("");
                loginButton.setDisable(false);
                loginButton.setStyle("-fx-background-color: #DEBD94;");
                errorLabel.setVisible(false);
            } else {
                usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                loginButton.setDisable(true);
                loginButton.setStyle("-fx-background-color: grey;");
                errorLabel.setText("E-Mail Adresse ungültig");
                errorLabel.setVisible(true);
            }
        });
    }

    @FXML
    private void handleLogin() {
        String email = usernameField.getText();
        String password = passwordField.getText();

        if (!isValidEmail(email)) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            loginButton.setDisable(true);
            loginButton.setStyle("-fx-background-color: grey;");
            errorLabel.setText("E-Mail Adresse ungültig");
            errorLabel.setVisible(true);
            return;
        } else {
            usernameField.setStyle("");
            loginButton.setDisable(false);
            loginButton.setStyle("-fx-background-color: #DEBD94;");
        }

        UserRole userRole = UserService.authenticate(email, password);

        if (userRole == null) {
            errorLabel.setText("E-Mail oder Passwort ist nicht korrekt.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            FXMLLoader loader;
            if (userRole == UserRole.ADMIN) {
                loader = new FXMLLoader(getClass().getResource("/frontend/view/AdminDashboard.fxml"));
            } else if (userRole == UserRole.EMPLOYEE) {
                loader = new FXMLLoader(getClass().getResource("/frontend/view/UserDashboard.fxml"));
            } else {
                errorLabel.setText("Unbekannte Rolle: Zugriff verweigert.");
                errorLabel.setVisible(true);
                return;
            }

            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Systemfehler - bitte später probieren");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleForgotPasswordClick() {
        forgotPassword.setVisible(false);
        adminInformed.setVisible(true);
    }

    //AI generated
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Regulärer Ausdruck zur Validierung einer E-Mail-Adresse
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}