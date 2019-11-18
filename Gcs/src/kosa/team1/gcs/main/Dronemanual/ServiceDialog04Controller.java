package kosa.team1.gcs.main.Dronemanual;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import kosa.team1.gcs.main.GcsMain;

import java.net.URL;
import java.util.ResourceBundle;

public class ServiceDialog04Controller implements Initializable {

    // Raspi 를 통하여 FC 를 컨트롤하기 위한 pub을 하는 client
    // 즉 드론 수동 조종용 client
    private RaspiMqttClient raspiMqttClient;

    private boolean mobileRequest;
    // mobileRequest 가 false라면 GCS 에서 동작하지 못하도록 제어한다.
    // GCS 에서 버튼을 누렀을 때, 실행 될 수 있도록 함
    // true 라고 한다면 GCS 에서 드론을 제어할 수 있도록 한다.

    public ServiceDialog04Controller() throws MqttException {
        GcsMain.instance.controller.flightMap.controller.setMode("GUIDED");
        mobileRequest = false;

        raspiMqttClient = new RaspiMqttClient();
        System.out.println("RaspiMqttClient client Created");

        System.out.println("MobileRequest : " + mobileRequest);

    }

    @FXML private Button Btn_Up;
    @FXML private Button Btn_Down;
    @FXML private Button Btn_Right;
    @FXML private Button Btn_Left;
    @FXML private Button Btn_Drop;
    @FXML private TextArea textAreaAlternative;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Button 이 클릭하면 진행될 이벤트 헨들러를 등록한다.
        Btn_Up.setOnAction(Btn_Up_Handler);
        Btn_Down.setOnAction(Btn_Down_Handler);
        Btn_Left.setOnAction(Btn_Left_Handler);
        Btn_Right.setOnAction(Btn_Right_Handler);
        Btn_Drop.setOnAction(Btn_Drop_Handler);
    }

    private EventHandler<ActionEvent> Btn_Drop_Handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("MobileRequest : " + mobileRequest);
            if(mobileRequest){
                String msg = "off";
                raspiMqttClient.ControlMagnet();
                raspiMqttClient.takeSnapShot();
                System.out.println("Published Message to Raspi Magnet " + msg);
                Stage stage = (Stage) Btn_Drop.getScene().getWindow();
                System.out.println("Magent Drop Done");
                stage.close();
            }
            else{
                System.out.println("Mobile is Control");
            }
        }
    };

    private EventHandler<ActionEvent> Btn_Up_Handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("MobileRequest : " + mobileRequest);
            if(mobileRequest){
                try {
                    raspiMqttClient.DroneControl("up", 1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Mobile is Control");
            }
        }
    };

    private EventHandler<ActionEvent> Btn_Down_Handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("MobileRequest : " + mobileRequest);
            if(mobileRequest){
                try {
                    raspiMqttClient.DroneControl("down", 1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Mobile is Control");
            }
        }
    };

    private EventHandler<ActionEvent> Btn_Left_Handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("MobileRequest : " + mobileRequest);
            if(mobileRequest){
                try {
                    raspiMqttClient.DroneControl("left", 1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Mobile is Control");
            }
        }
    };

    private EventHandler<ActionEvent> Btn_Right_Handler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("MobileRequest : " + mobileRequest);
            if(mobileRequest){
                try {
                    raspiMqttClient.DroneControl("right", 1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Mobile is Control");
            }
        }
    };

    // mobile 로 부터 받으면 실행이 가능하도록함.
    public class RaspiMqttClient{

        private MqttClient client;

        public RaspiMqttClient() throws MqttException {
            while(true){
                try {
                    client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
                    MqttConnectOptions options = new MqttConnectOptions();
                    client.connect();
                    break;
                }
                catch (MqttException e){
                    System.out.println(e.getMessage());
                }
            }

            System.out.println("ServiceDialog04Controller RaspuMqttClient MQTT Connected");

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("ServiceDialog04Controller Connection Lost");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    System.out.println("Message Received from Mobile");
                    String getMsg = new String(mqttMessage.getPayload());
                    System.out.println("Received : " + getMsg);
                    JSONObject jsonObject = new JSONObject(getMsg);
                    System.out.println("Received Parsed Done : " + jsonObject.toString());

                    if(jsonObject.get("msgid").equals("emergency")){
                        System.out.println("try Emergency");
                        mobileRequest = true;
                        textAreaAlternative.setText("GCS Control");
                        System.out.println("done Emergency");
                        //Stage stage = (Stage) Btn_Drop.getScene().getWindow();
                        //System.out.println("Magent Drop Done");
                        //stage.close();
                    }
                    else if(jsonObject.get("msgid").equals("control")){
                        System.out.println("try control");
                        int length = (int) jsonObject.get("speed");
                        System.out.println("Moobile Control "+ jsonObject.get("direction") + " " + length );
                        if(jsonObject.get("magnet").equals("off")){
                            System.out.println("Mobile Magnet Off Activate");
                            new Thread(){
                                @Override
                                public void run(){
                                    System.out.println("Thread Run");
                                    ControlMagnet();
                                    takeSnapShot();
                                    supplyDone();
                                    System.out.println("Magent Drop Done");
                                }
                            }.start();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        System.out.println("Drop To Close Stage");
                                        Stage stage = (Stage) Btn_Drop.getScene().getWindow();
                                        stage.close();
                                        // drop 되면 화면 꺼지도록
                                        System.out.println("Service04 Successfully Done");
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        else if(jsonObject.get("direction").equals("up")){
                            System.out.println("Mobile control up Activate");
                            new Thread(){
                                @Override
                                public void run(){
                                    try {
                                        DroneControl("up",1);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                        }
                        else if(jsonObject.get("direction").equals("down")){
                            System.out.println("Mobile control down Activate");
                            new Thread(){
                                @Override
                                public void run(){
                                    try {
                                        DroneControl("down",1);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                        else if(jsonObject.get("direction").equals("right")){
                            System.out.println("Mobile control right Activate");
                            new Thread(){
                                @Override
                                public void run(){
                                    try {
                                        DroneControl("right",1);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                        else if(jsonObject.get("direction").equals("left")){
                            System.out.println("Mobile control left Activate");
                            new Thread(){
                                @Override
                                public void run(){
                                    try {
                                        System.out.println("");
                                        DroneControl("left",1);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                        System.out.println("done control");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            client.subscribe("/gcs/droneManual");
        }

        public void DroneControl(String message, int speed) throws MqttException {
            System.out.println("Mobile DroneControl Activate");
            JSONObject controlJson = new JSONObject();
            controlJson.put("msgid","MAVJSON_MSG_ID_FINE_CONTROL");
            if(message.equals("up")){
                controlJson.put("velocityNorth", speed);
                controlJson.put("velocityEast", 0);
            }
            else if(message.equals("down")){
                controlJson.put("velocityNorth", -speed);
                controlJson.put("velocityEast", 0);
            }
            else if(message.equals("right")){
                controlJson.put("velocityNorth", 0);
                controlJson.put("velocityEast", speed);
            }
            else if(message.equals("left")){
                controlJson.put("velocityNorth", 0);
                controlJson.put("velocityEast", -speed);
            }

            client.publish("/drone/fc/sub", controlJson.toString().getBytes(), 0, false);
            System.out.println("Published Message to Raspi Direction " + message);
        }

        public void ControlMagnet(){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("magnet","off");
                System.out.println("Try Publish Message to Raspi Magnet");
                client.publish("/drone/magnet/sub", jsonObject.toString().getBytes(), 0, false);
                System.out.println("Done Published Message to Raspi Magnet");
            } catch (MqttException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        public void supplyDone(){

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgid","drop");
            try {
                System.out.println("Try Publish Android that mission finished");
                client.publish("/andorid/page2", jsonObject.toString().getBytes(), 0, false);
                System.out.println("Done Publish Android that mission finished");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        public void takeSnapShot(){

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msgid", "saveSnapShot");
                jsonObject.put("snapShot", true);
                client.publish("/drone/cam0/gcs",jsonObject.toString().getBytes(),0,false);
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }


}
