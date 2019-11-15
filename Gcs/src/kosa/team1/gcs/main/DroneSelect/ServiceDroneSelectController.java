package kosa.team1.gcs.main.DroneSelect;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ServiceDroneSelectController implements Initializable {
    @FXML private Button btnOK;
    @FXML private Button btnCancel;
    @FXML private VBox vbox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnOK.setOnAction(btnOKEventHandler);
        btnCancel.setOnAction(btnCancelEventHandler);
        initVBox();
    }

    private void initVBox() {
        try {
            HBox hboxTitle = (HBox) FXMLLoader.load(ServiceDroneSelect.class.getResource("vbox_title.fxml"));
            vbox.getChildren().add(hboxTitle);

            for(int i = 1; i<5 ;i++) {

                HBox hboxItem = (HBox) FXMLLoader.load(ServiceDroneSelect.class.getResource("vbox_drone.fxml"));
                vbox.getChildren().add(hboxItem);
                Label lblNo = (Label) hboxItem.lookup("#lblNo");
                //변경
                lblNo.setText(String.valueOf(i));
                Label lblLat = (Label) hboxItem.lookup("#lblLat");
                //변경
                lblLat.setText(String.valueOf("KosaOne"));
                Label lblLng = (Label) hboxItem.lookup("#lblLng");
                //변경
                lblLng.setText(String.valueOf("대기중(양호)"));
                Button btnDroneSelect = (Button) hboxItem.lookup("#btnDroneSelect");
                btnDroneSelect.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        //System.out.println(lblLat.getText());

                        Stage stage = (Stage) btnDroneSelect.getScene().getWindow();
                        stage.close();
                    }
                });
            }


        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private EventHandler<ActionEvent> btnOKEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    };

    private EventHandler<ActionEvent> btnCancelEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    };
}
