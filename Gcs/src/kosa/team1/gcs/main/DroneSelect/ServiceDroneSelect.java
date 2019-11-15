package kosa.team1.gcs.main.DroneSelect;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kosa.team1.gcs.main.GcsMain;

public class ServiceDroneSelect {
    //Field
    private Stage stage;

    //Constructor
    public ServiceDroneSelect() {
        try {
            stage = new Stage(StageStyle.UTILITY);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(GcsMain.instance.primaryStage);
            BorderPane pane =
                (BorderPane) FXMLLoader.load(getClass().getResource("ServiceDroneSelect.fxml"));
            Scene scene = new Scene(pane);
            scene.getStylesheets().add(GcsMain.class.getResource("style_dark.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
        } catch(Exception e) {}
    }

    //Method
    public void show() {
        stage.show();
    }
}
