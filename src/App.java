import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load());
            Image image = new Image("/views/tongLogo.png");
            stage.getIcons().add(image);
            stage.setScene(scene);
            stage.setTitle("Tong - Login");
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
