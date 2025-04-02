package lunchifyTests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import frontend.controller.InvoiceUploadController;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//@ExtendWith(MockitoExtension.class)
class InvoiceUploadControllerTest /*extends ApplicationTest */{
	@Mock private Stage mockStage;
	@Mock private FileChooser mockFileChooser;
    @Mock private File mockFile;
    @Mock private ImageView uploadedImageView;
    @Mock private Alert alert;
    private InvoiceUploadController invoiceUploadController;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
	}

	@Test
	void test() {
		fail("Not yet implemented");
	}

}
