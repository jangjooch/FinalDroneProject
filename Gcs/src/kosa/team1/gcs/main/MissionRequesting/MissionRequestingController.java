package kosa.team1.gcs.main.MissionRequesting;

import javafx.application.Platform;
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
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import kosa.team1.gcs.main.MissionRequesting.*;
import kosa.team1.gcs.main.GcsMain;

import java.net.URL;
import java.util.ResourceBundle;

public class MissionRequestingController implements Initializable {
    @FXML private Button btnOK;
    @FXML private Button btnCancel;
    @FXML private VBox vbox;

    public JSONArray missions = null;

    public MissionMqttClient missionMqttClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnOK.setOnAction(btnOKEventHandler);
        btnCancel.setOnAction(btnCancelEventHandler);
        missionMqttClient = new MissionMqttClient();
        initVBox();
    }

    private void initVBox() {
        System.out.println("initVBOX Activate");
        if(missions == null){
            System.out.println("mission NULL");
            vbox.getChildren().clear();
            try {
                HBox hboxTitle = (HBox) FXMLLoader.load(MissionRequesting.class.getResource("vbox_title.fxml"));
                vbox.getChildren().add(hboxTitle);

                HBox hboxItem = (HBox) FXMLLoader.load(MissionRequesting.class.getResource("vbox_item.fxml"));
                vbox.getChildren().add(hboxItem);
                Label lblNo = (Label) hboxItem.lookup("#lblNo");
                lblNo.setText("No Data");
                Label lblLat = (Label) hboxItem.lookup("#lblLat");
                lblLat.setText("No Data");
                Label lblLng = (Label) hboxItem.lookup("#lblLng");
                lblLng.setText("No Data");
                Button btnMap = (Button) hboxItem.lookup("#btnMap");
                    // 버튼 클릭 시 이벤트 생성
                btnMap.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        System.out.println(lblLat.getText());
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("mission In");
            vbox.getChildren().clear();
            try {
                HBox hboxTitle = (HBox) FXMLLoader.load(MissionRequesting.class.getResource("vbox_title.fxml"));
                vbox.getChildren().add(hboxTitle);
                for (int i = 0 ; i < missions.length() ; i++) {
                    HBox hboxItem = (HBox) FXMLLoader.load(MissionRequesting.class.getResource("vbox_item.fxml"));
                    vbox.getChildren().add(hboxItem);
                    JSONObject mission = (JSONObject) missions.get(i);
                    Label lblNo = (Label) hboxItem.lookup("#lblNo");
                    // lblNo.setText(String.valueOf(1));
                    lblNo.setText(String.valueOf(mission.get("missionNumber")));

                    Label lblLat = (Label) hboxItem.lookup("#lblLat");
                    // lblLat.setText(String.valueOf(37.123456));
                    lblLat.setText(String.valueOf(mission.get("lat")));

                    Label lblLng = (Label) hboxItem.lookup("#lblLng");
                    // lblLng.setText(String.valueOf(125.123456));
                    lblLng.setText(String.valueOf(mission.get("lng")));

                    Button btnMap = (Button) hboxItem.lookup("#btnMap");
                    // 버튼 클릭 시 이벤트 생성
                    btnMap.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            double destiLat = (double) mission.get("lat");
                            double destiLng = (double) mission.get("lng");
                            GcsMain.instance.controller.flightMap.controller.requestMarkClear();
                            GcsMain.instance.controller.flightMap.controller.requestMark( destiLat, destiLng);
                            GcsMain.instance.controller.setDestination(destiLat, destiLng);
                            missionMqttClient.SendMissionStart((Integer) mission.get("missionNumber"));
                            Stage stage = (Stage) btnCancel.getScene().getWindow();
                            stage.close();
                        }
                    });
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
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

    public class MissionMqttClient{
        private MqttClient client;

        public MissionMqttClient(){
            while(true){
                try {
                    client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
                    MqttConnectOptions options = new MqttConnectOptions();
                    client.connect();
                    System.out.println("GcsMainMqtt client Connect Done");
                    break;
                }
                catch (MqttException e){
                    System.out.println("GcsMainMqtt client Connect Error");
                    System.out.println(e.getMessage());
                }
            }

            make_sub();

        }

        public void make_sub(){
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage){
                    System.out.println("Message Arrived from Web Mission Request");
                    byte[] data = mqttMessage.getPayload();
                    String strmsg = new String(data);
                    System.out.println("String : " + strmsg);
                    JSONArray array = null;
                    try{
                        array = new JSONArray(strmsg);
                        System.out.println(array.get(1));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    // System.out.println("JsonArray : " + array);
                    System.out.println("JsonArray : " + array.get(1).toString());
                    for(int i = 0 ; i < array.length() ; i++){
                        System.out.println(array.get(i).toString());
                    }
                    missions = new JSONArray(strmsg);
                    // 무엇인가 받으면 박스 재 배치
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("initVBox Activated");
                            initVBox();
                        }
                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

            try {
                client.subscribe("/gcs/missionIn");
                System.out.println("MissionRequesting Client Sub Done");
            } catch (MqttException e) {
                System.out.println("MissionRequesting Client Sub Error");
                e.printStackTrace();
            }
        }

        public void SendMissionStart(int MSNumber){
            byte[] data = String.valueOf(MSNumber).getBytes();
            try {
                System.out.println("Try Sending Mission Start. " + MSNumber);
                client.publish("/web/missionstart", data, 0, false);
                System.out.println("Publish Done MissionStart. " + MSNumber);
            } catch (MqttException e) {
                System.out.println("Publish Error Mission Start. " + MSNumber);
                e.printStackTrace();
            }
        }
    }
}
