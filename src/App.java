import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import Model.User;
import Services.UserService;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        createAdminIfNotExists();
        scene = new Scene(loadFXML("LoginPage"));
        stage.setScene(scene);
        stage.show();
    }

    private void createAdminIfNotExists() {
        UserService userService = UserService.getInstance();

        if (userService.getUser(1) != null) {
            return;
        }

        User admin = userService.createUser("admin@admin.com", "admin", "admin", "admin");
        userService.setAdmin(admin, true, "admin");
    }

        static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("View/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}