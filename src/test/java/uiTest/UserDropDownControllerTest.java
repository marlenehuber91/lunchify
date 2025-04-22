package uiTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.isVisible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import frontend.Main;
import javafx.stage.Stage;

@Tag("ui")
public class UserDropDownControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        try {
            new Main().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performLogin() {
        clickOn("#usernameField");
        write("a@lunch.at");
        clickOn("#passwordField");
        write("a");
        clickOn("#loginButton");
        verifyThat("#adminDashboard", isVisible());
    }

    @BeforeEach
    void setUp() {
        performLogin();
    }

    @Test
    void testLogoutButtonVisibility() {
        verifyThat("#userImageView", isVisible());
        clickOn("#userImageView");
        verifyThat("#logoutButton", isVisible());
    }

    @Test
    void testLogoutFunctionality() {
        clickOn("#userImageView");
        clickOn("#logoutButton");
        verifyThat("#loginPage", isVisible());
    }
}