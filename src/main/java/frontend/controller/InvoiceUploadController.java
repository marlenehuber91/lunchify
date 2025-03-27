package frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Label;
import java.io.File;

public class InvoiceUploadController {

	@FXML
    private StackPane uploadPane; // Referenz zum StackPane

    @FXML
    private ImageView uploadedImageView; // Füge dies zu FXML hinzu, falls du das Bild anzeigen willst

    @FXML
    private Text uploadText;
    
    @FXML
    Text previewText;
    
    @FXML
    private void openFileChooser() {
        Stage stage = (Stage) uploadPane.getScene().getWindow(); // Hole die aktuelle Stage

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Bild oder PDF hochladen");

        // Erlaube nur bestimmte Dateiformate
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Bilddateien und PDF", "*.jpg", "*.jpeg", "*.png", "*.pdf")
            //new FileChooser.ExtensionFilter("PDF Dateien", "*.pdf")
        );

        // Zeige den Datei-Dialog
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            String filePath = file.getAbsolutePath();
            System.out.println("Datei ausgewählt: " + filePath);

            // Wenn es ein Bild ist, zeige es in der ImageView an
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png")) {
                uploadedImageView.setImage(new Image(file.toURI().toString()));
                previewText.setText("Vorschau");
            } else {
                showAlert("Datei hochgeladen", "Die Datei wurde erfolgreich ausgewählt:\n" + filePath);
            }
            uploadText.setText("Foto hochgeladen");
        } else {
            showAlert("Keine Datei", "Es wurde keine Datei ausgewählt.");
        }
        
    }

   private void showAlert(String title, String content) {
       Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
