package frontend.controller;

import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ModifyUsersController {

    @FXML
    private RadioButton radioButtonNew;
    @FXML
    private RadioButton radioButtonRead;
    @FXML
    private RadioButton radioButtonEdit;
    @FXML
    private ToggleGroup modeGroup;

    //new User
    @FXML
    private AnchorPane paneNewUser;
    @FXML
    private ComboBox<UserState> statusNewUserBox;
    @FXML
    private TextField passwordNewField;
    @FXML
    private ComboBox<UserRole> roleNewBox;
    @FXML
    private TextField nameNewField;
    @FXML
    private TextField eMailNewField;

    //edit User
    @FXML
    private AnchorPane paneEditUser;
    @FXML
    private ComboBox<User> userEditUserBox;
    @FXML
    private ComboBox<UserState> statusEditUserBox;
    @FXML
    private TextField nameEditField;
    @FXML
    private ComboBox<UserRole> roleEditBox;
    @FXML
    private TextField passwordEditField;
    @FXML
    private TextField eMailEditField;



    //view Users
    @FXML
    private AnchorPane paneViewTable;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, UserRole> roleColumn;
    @FXML
    private TableColumn<User, UserState> stateColumn;


    private User user;
    private final UserService userService = new UserService();


    @FXML
    public void initialize() {
        if(user == null) {
            user = SessionManager.getCurrentUser();
        }
        modeGroup = new ToggleGroup();
        this.radioButtonEdit.setToggleGroup(modeGroup);
        this.radioButtonNew.setToggleGroup(modeGroup);
        this.radioButtonRead.setToggleGroup(modeGroup);

        paneViewTable.setVisible(false);
        paneEditUser.setVisible(false);
        radioButtonNew.setSelected(true);

        //Table column bindings
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRole()));
        stateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getState()));

        loadNewUserPanel();
    }

    //from Codepilot
    private void loadNewUserPanel() {
        statusNewUserBox.setItems(FXCollections.observableArrayList(UserState.values()));
        roleNewBox.setItems(FXCollections.observableArrayList(UserRole.values()));
    }

    @FXML
    public void handleBackToDashboard(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/AdminDashboard.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Dashboard");
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleUserModeChange(ActionEvent actionEvent) {
        if (modeGroup.getSelectedToggle() == radioButtonNew) {
            paneViewTable.setVisible(false);
            paneEditUser.setVisible(false);
            paneNewUser.setVisible(true);
            loadNewUserPanel();
        }
        if (modeGroup.getSelectedToggle() == radioButtonRead) {
            paneViewTable.setVisible(true);
            loadUserList();
            paneEditUser.setVisible(false);
            paneNewUser.setVisible(false);
        }
        if (modeGroup.getSelectedToggle() == radioButtonEdit) {
            paneViewTable.setVisible(false);
            paneEditUser.setVisible(true);
            loadUserToEdit();
            paneNewUser.setVisible(false);
        }
    }

    //from Codepilot
    private void loadUserToEdit() {
        List<User> userList = userService.getAllUsers();
        userEditUserBox.setItems(FXCollections.observableArrayList(userList));

        // Set full list once
        roleEditBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        statusEditUserBox.setItems(FXCollections.observableArrayList(UserState.values()));

        userEditUserBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameEditField.setText(newVal.getName());
                eMailEditField.setText(newVal.getEmail());
                passwordEditField.setText("");
                roleEditBox.setValue(newVal.getRole());
                statusEditUserBox.setValue(newVal.getState());
            }
        });
    }

    private void loadUserList() {
        usersTable.getItems().clear();
        List<User> userList = userService.getAllUsers();
        usersTable.getItems().addAll(userList);
    }

    //created by AI
    @FXML
    public void onMouseClickedSaveNewUserButton(MouseEvent mouseEvent) {

        try {
            String name = nameNewField.getText();
            String email = eMailNewField.getText();
            String password = passwordNewField.getText();
            UserRole role = roleNewBox.getValue();
            UserState state = statusNewUserBox.getValue();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || state == null) {
                showAlert(Alert.AlertType.WARNING, "Eingabe unvollständig", "Bitte alle Felder ausfüllen.");
                return;
            }

            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setState(state);

            if (!newUser.isNameValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültiger Name", "Bitte einen gültigen Namen eingeben.");
                return;
            }
            if (!newUser.isEmailValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültige E-Mail", "Bitte eine gültige E-Mail-Adresse eingeben.");
                return;
            }
            if (!newUser.isPasswordValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültiges Passwort", "Das Passwort muss mindestens 6 Zeichen lang sein.");
                return;
            }
            if (!newUser.isRoleValid()) {
                showAlert(Alert.AlertType.WARNING, "Keine Rolle gewählt", "Bitte eine Benutzerrolle auswählen.");
                return;
            }
            if (!newUser.isStateValid()) {
                showAlert(Alert.AlertType.WARNING, "Kein Status gewählt", "Bitte einen Benutzerstatus auswählen.");
                return;
            }

            userService.insertUser(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Benutzer erstellt", "Der Benutzer wurde erfolgreich gespeichert.");

            nameNewField.clear();
            eMailNewField.clear();
            passwordNewField.clear();
            roleNewBox.getSelectionModel().clearSelection();
            statusNewUserBox.getSelectionModel().clearSelection();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Fehler beim Speichern", "Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    public void onMouseClickedSaveEditUserButton(MouseEvent mouseEvent) {

        User selectedUser = userEditUserBox.getValue();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Benutzer ausgewählt", "Bitte wählen Sie einen Benutzer aus.");
            return;
        }
        try {
            selectedUser.setName(nameEditField.getText());
            selectedUser.setEmail(eMailEditField.getText());
            selectedUser.setPassword(passwordEditField.getText());
            selectedUser.setRole(roleEditBox.getValue());
            selectedUser.setState(statusEditUserBox.getValue());

            if (!selectedUser.isNameValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültiger Name", "Bitte einen gültigen Namen eingeben.");
                return;
            }
            if (!selectedUser.isEmailValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültige E-Mail", "Bitte eine gültige E-Mail-Adresse eingeben.");
                return;
            }
            if (!selectedUser.isPasswordValid()) {
                showAlert(Alert.AlertType.WARNING, "Ungültiges Passwort", "Das Passwort muss mindestens 6 Zeichen lang sein.");
                return;
            }
            if (!selectedUser.isRoleValid()) {
                showAlert(Alert.AlertType.WARNING, "Keine Rolle gewählt", "Bitte eine Benutzerrolle auswählen.");
                return;
            }
            if (!selectedUser.isStateValid()) {
                showAlert(Alert.AlertType.WARNING, "Kein Status gewählt", "Bitte einen Benutzerstatus auswählen.");
                return;
            }

            userService.updateUser(selectedUser);
            loadUserList();

            showAlert(Alert.AlertType.INFORMATION, "Benutzer gespeichert", "Die Änderungen wurden erfolgreich gespeichert.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Fehler beim Speichern", "Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //from Codepilot
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
