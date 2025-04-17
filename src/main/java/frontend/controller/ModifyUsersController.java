package frontend.controller;

import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ModifyUsersController {
    @FXML
    public RadioButton radioButtonNew;
    @FXML
    public RadioButton radioButtonRead;
    @FXML
    public RadioButton radioButtonEdit;
    @FXML
    public AnchorPane paneNewUser;
    @FXML
    public AnchorPane paneTable;
    @FXML
    public ComboBox statusUserBox;
    @FXML
    public TextField passwordField;
    @FXML
    public ComboBox roleBox;
    @FXML
    public TextField nameField;
    @FXML
    public Text previewText;
    @FXML
    public TextField eMailField;
    @FXML
    public Button saveButton;
    @FXML
    public TableView usersTable;
    @FXML
    public ToggleGroup modeGroup;

    private User user;
    private UserService userService;


    @FXML
    public void initialize() {
        if(user == null) {
            user = SessionManager.getCurrentUser();
        }
        modeGroup = new ToggleGroup();
        this.radioButtonEdit.setToggleGroup(modeGroup);
        this.radioButtonNew.setToggleGroup(modeGroup);
        this.radioButtonRead.setToggleGroup(modeGroup);

        paneTable.setVisible(false);
        radioButtonNew.setSelected(true);
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

    @FXML
    public void onMouseClickedSaveNewUserButton(MouseEvent mouseEvent) {
        System.out.println("onMouseClickedSaveNewUserButton");
    }

    public void handleUserModeChange(ActionEvent actionEvent) {
        System.out.println("handleUserModeChange");

        if (modeGroup.getSelectedToggle() == radioButtonNew) {
            paneTable.setVisible(false);
            paneNewUser.setVisible(true);
        }
        else {
            paneTable.setVisible(true);
            paneNewUser.setVisible(false);
        }
    }
}
