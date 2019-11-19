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
import kosa.team1.gcs.main.GcsMain;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class ServiceDroneSelectController implements Initializable {

    JSONArray jsonArray = new JSONArray();
    private MqttClient client;
    @FXML private Button btnOK;
    @FXML private Button btnCancel;
    @FXML private VBox vbox;

    //constructor
    public ServiceDroneSelectController() throws MqttException {
        // Mqtt 생성
        try {
            this.DroneMqttPub();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnOK.setOnAction(btnOKEventHandler);
        btnCancel.setOnAction(btnCancelEventHandler);

        try {
            this.receiveMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mqttSendToWebRequest();

        System.out.println("initVBox 실행");

        try {
            Thread.sleep(500);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        initVBox(jsonArray);
        System.out.println("initVBox 완료");

    }

    //VBox에 보여지는 리스트 편집
    private void initVBox(JSONArray jsonArray) {

        try {
            HBox hboxTitle = (HBox) FXMLLoader.load(ServiceDroneSelect.class.getResource("vbox_title.fxml"));
            vbox.getChildren().add(hboxTitle);

                int index = 0;
                while(true) {

                    if (jsonArray.isNull(index)) {
                        break;
                    }else{

                    // Json Array
                    JSONObject jsonobject = jsonArray.getJSONObject(index);

                    HBox hboxItem = (HBox) FXMLLoader.load(ServiceDroneSelect.class.getResource("vbox_drone.fxml"));
                     vbox.getChildren().add(hboxItem);

                    //index의 max값

                    Label lblNo = (Label) hboxItem.lookup("#lblNo");
                    lblNo.setText(String.valueOf(jsonobject.get("DroneNum")));

                    Label lblLat = (Label) hboxItem.lookup("#lblLat");
                    lblLat.setText((String) jsonobject.get("DroneModel"));

                    Label lblLng = (Label) hboxItem.lookup("#lblLng");

                    if(String.valueOf(jsonobject.get("DroneState")).equals("1")) {
                        lblLng.setText("가능");
                    }else{
                        lblLng.setText("불가능");
                    }
                        Button btnDroneSelect = (Button) hboxItem.lookup("#btnDroneSelect");

                    //Button 선택시
                    btnDroneSelect.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            //System.out.println(lblNo.getText());
                            GcsMain.instance.controller.setDroneNumber((int)jsonobject.get("DroneNum"));
                            mqttSendToWeb(lblNo.getText());
                            Stage stage = (Stage) btnDroneSelect.getScene().getWindow();
                            stage.close();
                        }
                    });
                    index ++;
                    }
                }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    //method

    //MQTT 생성
    public void DroneMqttPub() throws Exception {

        while(true) {
            try {
                client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setConnectionTimeout(5);
                options.setAutomaticReconnect(true);
                client.connect(options);
                System.out.println("Drone MQTT Connected: DroneSelect /drone/select/sub & pub");
                break;
            } catch(Exception e) {
                e.printStackTrace();
                try { client.close(); } catch (Exception e1) {}
                try { sleep(1000); } catch (InterruptedException e1) {}
            }
        }

    }

    public void receiveMessage() throws Exception {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                byte[] payload = mqttMessage.getPayload();
                String json = new String(payload);
                System.out.println("Message Arrived " + json);
                jsonArray = new JSONArray(json);

                int i = 0;
                while(true) {

                    if (jsonArray == null) {
                        break;
                    }
                    System.out.println(jsonArray.getJSONObject(i));
                    i++;
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }

        });
        client.subscribe("/drone/select/sub");


    }

    // 쓰레드 pool생성
    private ExecutorService mqttSendToWebPool = Executors.newFixedThreadPool(1);

    // mqtt 웹으로 선택한 드론 보내기
    public void mqttSendToWeb(String DroneNumber) {
        if(client != null && client.isConnected()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        int getReNum = GcsMain.instance.controller.getReNumber();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("msgid", "DroneSelect");
                        jsonObject.put("DroneNum", DroneNumber);
                        jsonObject.put("re_num", getReNum);
                        String json = jsonObject.toString();
                        System.out.println("Drone Select Pub to JSon data");
                        client.publish("/web/missionStatus", json.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };
            mqttSendToWebPool.submit(runnable);
        }
    }


    // mqtt 웹으로 선택한 드론 보내기
    public void mqttSendToWebRequest() {
        if(client != null && client.isConnected()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("msgid", "DroneRequest");

                        String json = jsonObject.toString();
                        System.out.println("Drone Select Pub to JSon / request data");
                        client.publish("/web/missionStatus", json.getBytes(), 0, false);
                        System.out.println("Drone Select Pub to JSon / request data Done");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            };
            mqttSendToWebPool.submit(runnable);
        }
    }

    // OK와 Cancle button
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
