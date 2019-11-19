package kosa.team1.gcs.main.Dronemanual;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

    public Thread sendingThread;

    public ServiceDialog04Controller() throws MqttException {
        GcsMain.instance.controller.flightMap.controller.setMode("GUIDED");
        mobileRequest = false;

        raspiMqttClient = new RaspiMqttClient();
        System.out.println("RaspiMqttClient client Created");

        sendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    raspiMqttClient.sendGpsToAndroid();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendingThread.start();
    }

    @FXML private Button Btn_Up;
    @FXML private Button Btn_Down;
    @FXML private Button Btn_Right;
    @FXML private Button Btn_Left;
    @FXML private Button Btn_Drop;
    @FXML private Label labelStatus;


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
                raspiMqttClient.supplyDone();
                raspiMqttClient.ResetDroneAlt();
                sendingThread.interrupt();
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
                    if(new String(mqttMessage.getPayload()).contains("going")){
                        System.out.println("Fuck Stack");
                    }
                    else{
                        System.out.println("Rightful Message Arrived");
                        JSONObject jsonObject = new JSONObject(getMsg);
                        System.out.println("Received Parsed Done : " + jsonObject.toString());

                        if(jsonObject.get("msgid").equals("emergency")){
                            System.out.println("try Emergency");
                            mobileRequest = true;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        System.out.println("Try Label Change");
                                        labelStatus.setText("GCS Control");
                                        labelStatus.setTextFill(Color.BLACK);
                                        System.out.println("Done Label Change");
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                            System.out.println("done Emergency");
                            //Stage stage = (Stage) Btn_Drop.getScene().getWindow();
                            //System.out.println("Magent Drop Done");
                            //stage.close();
                        }
                        else if(jsonObject.get("msgid").equals("control")){
                            System.out.println("try control");
                            int length = (int) jsonObject.get("speed");
                            System.out.println("Mobile Control "+ jsonObject.get("direction") + " " + length);
                            String forLabel = "Mobile Control "+ jsonObject.get("direction") + " " + length;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        System.out.println("Try Label Change");
                                        labelStatus.setText(forLabel);
                                        labelStatus.setTextFill(Color.BLACK);
                                        System.out.println("Done Label Change");
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                            if(jsonObject.get("magnet").equals("off")){
                                System.out.println("Mobile Magnet Off Activate");
                                new Thread(){
                                    @Override
                                    public void run(){
                                        System.out.println("Thread Run");
                                        ControlMagnet();
                                        takeSnapShot();
                                        supplyDone();
                                        raspiMqttClient.ResetDroneAlt();
                                        sendingThread.interrupt();
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
                                            DroneControl("up",length);
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
                                            DroneControl("down",length);
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
                                            DroneControl("right",length);
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
                                            DroneControl("left",length);
                                        } catch (MqttException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                            else if(jsonObject.get("direction").equals("high")){
                                System.out.println("Mobile control high");
                                new Thread(){
                                    @Override
                                    public void run() {
                                        System.out.println("Try High DroneAltControl");
                                        DroneAltControl("high", length);
                                        System.out.println("Done High DroneAltControl");
                                    }
                                }.start();
                            }

                            else if(jsonObject.get("direction").equals("low")){
                                System.out.println("Mobile control low");
                                new Thread(){
                                    @Override
                                    public void run() {
                                        System.out.println("Try Low DroneAltControl");
                                        DroneAltControl("low", length);
                                        System.out.println("Done Low DroneAltControl");
                                    }
                                }.start();
                            }
                            System.out.println("done control");
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            client.subscribe("/gcs/droneManual");

        }

        public void sendGpsToAndroid(){

            double currLat = Double.parseDouble(GcsMain.instance.controller.getCurrLat());
            double currLng = Double.parseDouble(GcsMain.instance.controller.getCurrLng());
            double currAlt = Double.parseDouble(GcsMain.instance.controller.getCurrAlt());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgid", "droneStatus");
            jsonObject.put("lat", currLat);
            jsonObject.put("lng", currLng);
            jsonObject.put("alt" , currAlt);

            try {
                System.out.println("try Sending Drone Status");
                client.publish("/android/page2", jsonObject.toString().getBytes(), 0, false);
                System.out.println("done Sending Drone Status");
            } catch (MqttException e) {
                e.printStackTrace();
            }
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

        public void DroneAltControl(String way, int alt){
            double Currlat = Double.parseDouble(GcsMain.instance.controller.getCurrLat());
            double Currlng = Double.parseDouble(GcsMain.instance.controller.getCurrLng());
            double Curralt = Double.parseDouble(GcsMain.instance.controller.getCurrAlt());
            double calculatedAlt = 0;
            if(way.equals("high")){
                calculatedAlt = Curralt + alt;
            }
            else {
                calculatedAlt = Curralt - alt;
            }

            if(calculatedAlt < 3){
                calculatedAlt = 3;
            }
            System.out.println("Mobile DroneAltControl Activate");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgid", "SET_POSITION_TARGET_GLOBAL_INT");
            jsonObject.put("lng", Currlng);
            jsonObject.put("lat", Currlat);
            jsonObject.put("alt", calculatedAlt);
            try {
                System.out.println("Try Alt Control");
                client.publish("/drone/fc/sub", jsonObject.toString().getBytes(), 0, false);
                System.out.println("Done Alt Control");
            } catch (MqttException e) {
                e.printStackTrace();
            }
            System.out.println("Published Message to Raspi Direction " + jsonObject.toString());
        }

        public void ResetDroneAlt(){
            double Currlat = Double.parseDouble(GcsMain.instance.controller.getCurrLat());
            double Currlng = Double.parseDouble(GcsMain.instance.controller.getCurrLng());
            // double Curralt = Double.parseDouble(GcsMain.instance.controller.getCurrAlt());
            double calculatedAlt = 10;
            if(calculatedAlt < 3){
                calculatedAlt = 3;
            }
            System.out.println("Reset Drone Alt");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msgid", "SET_POSITION_TARGET_GLOBAL_INT");
            jsonObject.put("lng", Currlng);
            jsonObject.put("lat", Currlat);
            jsonObject.put("alt", calculatedAlt);

            try {
                System.out.println("Try Alt Control");
                client.publish("/drone/fc/sub", jsonObject.toString().getBytes(), 0, false);
                System.out.println("Done Alt Control");
            } catch (MqttException e) {
                e.printStackTrace();
            }
            System.out.println("Published Message to Raspi Direction " + jsonObject.toString());
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
                System.out.println("Test SnapShot Try");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msgid", "saveSnapShot");
                jsonObject.put("snapShot", true);
                client.publish("/drone/cam1/gcs",jsonObject.toString().getBytes(),0,false);
                System.out.println("Test SnapShot Done");
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }


}
