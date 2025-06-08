package uiTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import frontend.Main;
import javafx.stage.Stage;
import org.testfx.util.NodeQueryUtils;

@Tag("ui")
public class SearchControllerTest extends ApplicationTest {

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
        verifyThat("#adminDashboard", NodeQueryUtils.isVisible());
        clickOn("#searchRectangle");
    }

    @BeforeEach
    void setUp(org.junit.jupiter.api.TestInfo testInfo) {
        if (!testInfo.getTestMethod().get().getName().equals("testUserDashboardHasNoSearchAccess")) {
            performLogin();
        }
    }


    @Test
    void testSearchByName() {
        clickOn("#searchField");
        write("Martin");
        verifyThat("#listOfMatches", isVisible());
        //From here AI generated
        verifyThat("#listOfMatches", (Parent parent) ->
                parent.lookupAll(".text").stream()
                        .map(node -> ((Text) node).getText())
                        .anyMatch(text -> text.contains("martin"))
        );
    }

    @Test
    void testSearchByEmail() {
        clickOn("#searchField");
        write("sarah.maier@lunch.at");
        verifyThat("#listOfMatches", isVisible());
        //From here AI generated
        verifyThat("#listOfMatches", (Parent parent) ->
                parent.lookupAll(".text").stream()
                        .map(node -> ((Text) node).getText())
                        .anyMatch(text -> text.contains("sarah.maier@lunch.at"))
        );
    }

    @Test
    void testSearchForNonExistingUser() {
        clickOn("#searchField");
        write("notfound@example.com");
        verifyThat("#listOfMatches", NodeMatchers.isInvisible());
        verifyThat("#userNotFoundLabel", isVisible());
    }

    @Test
    void testUserDashboardHasNoSearchAccess() {
        clickOn("#usernameField");
        write("e@lunch.at");
        clickOn("#passwordField");
        write("e");
        clickOn("#loginButton");

        verifyThat("#userDashboard", NodeMatchers.isVisible());
        assertTrue(lookup("#searchRectangle").tryQuery().isEmpty());
    }
}