package kosa.team1.gcs.main.MissionRequesting;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
// import syk.sample.gcs.main.GcsMain;
import kosa.team1.gcs.main.GcsMain;

public class MissionRequesting {
    //Field
    private Stage stage;

    //Constructor
    public MissionRequesting() {
        try {
            stage = new Stage(StageStyle.UTILITY);
            //stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(GcsMain.instance.primaryStage);
            BorderPane pane =
                (BorderPane) FXMLLoader.load(getClass().getResource("Service1.fxml"));
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
