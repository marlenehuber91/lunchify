package uiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.isVisible;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import frontend.Main;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

@Tag("ui")
public class LoginPageControllerTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {
        try {
            new Main().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInvalidEmailFormat() {
        clickOn("#usernameField");
        write("failtest.gmx.at");
        verifyThat("#loginButton", Node::isDisabled);
        verifyThat("#warning", Node::isVisible);
        verifyThat("#messageField", Node::isVisible);
    }

    @Test
    void testValidEmailFormat() {
        clickOn("#usernameField");
        write("martin.lechner@lunch.at");
        verifyThat("#warning", NodeMatchers.isInvisible());
        verifyThat("#messageField", NodeMatchers.isInvisible());
    }

    @Test
    void testInvalidLoginIncorrectPassword() {
        // Simuliere das Eingeben von Username und falschem Passwort
        clickOn("#usernameField");
        write("martin.lechner@lunch.at");
        clickOn("#passwordField");
        write("wrongPassword");
        clickOn("#loginButton");

        verifyThat("#warning", Node::isVisible);

        // AI generated from here until end of testInvalidLoginIncorrectPassword
        TextFlow messageField = lookup("#messageField").queryAs(TextFlow.class);

        String actualText = messageField.getChildren().stream()
                .filter(node -> node instanceof Text)
                .map(node -> ((Text) node).getText())
                .collect(Collectors.joining());

        assertEquals("Passwort ist nicht korrekt.", actualText, "Die Fehlermeldung stimmt nicht überein.");
    }

    @Test
    void testInvalidLoginIncorrectEmail() { //partially AI generated
        clickOn("#usernameField");
        write("wrongEmail@gmx.at");
        clickOn("#passwordField");
        write("anyPassword");
        clickOn("#loginButton");

        TextFlow messageField = lookup("#messageField").queryAs(TextFlow.class);

        String actualText = messageField.getChildren().stream()
                .filter(node -> node instanceof Text)
                .map(node -> ((Text) node).getText())
                .collect(Collectors.joining());

        assertEquals("E-Mail-Adresse wurde nicht gefunden.", actualText, "Die Fehlermeldung stimmt nicht überein.");
    }

    @Test
    void testForgotPassword() { //partially AI generated
        clickOn("#forgotPassword");

        TextFlow messageField = lookup("#messageField").queryAs(TextFlow.class);

        String actualText = messageField.getChildren().stream()
                .filter(node -> node instanceof Text)
                .map(node -> ((Text) node).getText())
                .collect(Collectors.joining());

        assertEquals("Der Administrator wurde benachrichtigt.", actualText, "Die Benachrichtigungsnachricht stimmt nicht überein.");
    }

    @Test
    void testValidUserLogin() {
        clickOn("#usernameField");
        write("sarah.maier@lunch.at");
        clickOn("#passwordField");
        write("sarah123");
        clickOn("#loginButton");
        verifyThat("#userDashboard", isVisible());
    }

    @Test
    void testAdminLogin() {
        clickOn("#usernameField");
        write("martin.lechner@lunch.at");
        clickOn("#passwordField");
        write("martin123");
        clickOn("#loginButton");
        verifyThat("#adminDashboard", isVisible());
    }
}

