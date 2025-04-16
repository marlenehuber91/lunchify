package frontend.controller;

import java.awt.*;
import java.io.IOException;

import backend.Exceptions.AuthenticationException;
import backend.logic.UserService;
import backend.model.User;
import backend.model.UserRole;
import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.fxml.FXML;

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
    private TextFlow messageField;
    @FXML
    private Text message;
    @FXML
    private ImageView warning;
    @FXML
    private Rectangle greenBg;
    @FXML
    private AnchorPane loginPage;

    @FXML
    public void initialize() { //Listener = AI generated + AI idea
        UserService.setConnectionProvider(DatabaseConnection::connect);
        greenBg.heightProperty().bind(loginPage.heightProperty().multiply(0.6));

        // Dynamische Breite des Rectangles (volle Breite des AnchorPane)
        greenBg.widthProperty().bind(loginPage.widthProperty());


        loginButton.setDisable(true);
        loginButton.setStyle("-fx-background-color: grey;");

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidEmail(newValue)) {
                usernameField.setStyle("");
                loginButton.setDisable(false);
                loginButton.setStyle("-fx-background-color: #DEBD94;");
                hideErrorElements();
            } else {
                usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                loginButton.setDisable(true);
                loginButton.setStyle("-fx-background-color: grey;");
                showErrorElements("E-Mail Adresse ungültig");
            }
        });
        passwordField.setOnAction(event -> handleLogin());

    }

    @FXML
    private void handleLogin() {
        hideErrorElements();

        String email = usernameField.getText();
        String password = passwordField.getText();

        if (!isValidEmail(email)) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            loginButton.setDisable(true);
            loginButton.setStyle("-fx-background-color: grey;");
            showErrorElements("E-Mail Adresse ungültig");
            return;
        } else {
            usernameField.setStyle("");
            loginButton.setDisable(false);
            loginButton.setStyle("-fx-background-color: #DEBD94;");
        }

        try {
            User user = UserService.authenticate(email, password);

            FXMLLoader loader;
            if (user.getRole() == UserRole.ADMIN) {
                loader = new FXMLLoader(getClass().getResource("/frontend/views/AdminDashboard.fxml"));
            } else if (user.getRole() == UserRole.EMPLOYEE) {
                loader = new FXMLLoader(getClass().getResource("/frontend/views/UserDashboard.fxml"));
            } else {
                showErrorElements("Unbekannte Rolle: Zugriff verweigert.");
                return;
            }

            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (AuthenticationException e) {
            showErrorElements(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorElements("Systemfehler - bitte später erneut versuchen.");
        }
    }

    @FXML
    private void handleForgotPasswordClick() {
        errorLabel.setVisible(false);
        messageField.setVisible(true);
        message.setVisible(true);
        message.setText("Der Administrator wurde benachrichtigt.");
        warning.setVisible(false);
    }

    private void showErrorElements(String errorMessage) {
        messageField.setVisible(true);
        message.setVisible(true);
        message.setText(errorMessage);
        warning.setVisible(true);
    }

    private void hideErrorElements() {
        errorLabel.setVisible(false);
        messageField.setVisible(false);
        message.setVisible(false);
        warning.setVisible(false);
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