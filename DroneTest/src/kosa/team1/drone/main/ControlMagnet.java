package kosa.team1.drone.main;

import com.pi4j.io.gpio.RaspiPin;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

public class ControlMagnet {
    private MqttClient client;
    private ElectroMagnet laserEmitter1;
    private ElectroMagnet laserEmitter2;
    private String mqttBrokerConnStr;
    private String pubTopic;
    private String subTopic;

    public ControlMagnet() throws MqttException {
        laserEmitter1 = new ElectroMagnet(RaspiPin.GPIO_24);
        laserEmitter2 = new ElectroMagnet(RaspiPin.GPIO_25);

    }

    public void Creating_Connection(String mqttBrokerConnStr, String pubTopic, String subTopic) throws MqttException {
        while(true){
            try {
                this.mqttBrokerConnStr = mqttBrokerConnStr;
                this.pubTopic = pubTopic;
                this.subTopic = subTopic;
                client = new MqttClient(mqttBrokerConnStr, MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                client.connect();
                System.out.println("RealMain Control Magnet Making_Connect MQTT Connected");
                break;
            } catch (MqttException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        makeSub();

    }

    public void sendMagnetState(){
        try {
            client.publish("/jang/gcs/serviceDialog03",laserEmitter1.getStatus().getBytes(),0,false);
        } catch (MqttException e) {
            System.out.println("sendMagnetState Error");
            e.printStackTrace();
        }
    }
    public ElectroMagnet getElectroMagnet(){
        return laserEmitter1;
    }

    public String getMagnetStatus(){
        return laserEmitter1.getStatus();
    }

    public void makeSub() throws MqttException {
        client.setCallback(new MqttCallback(){
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Magnet Connection Lost");

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println("Message Received");
                byte[] rowData = mqttMessage.getPayload();
                String strData = new String(rowData);

                if(mqttMessage == null){
                    System.out.println("Message Error");
                }
                else if(mqttMessage.getPayload().toString().equals(" ")){
                    System.out.println("Message Error ** **");
                }
                else if(mqttMessage.getPayload().toString().equals("")){
                    System.out.println("Message Error ****");
                }
                else{
                    String getMsg = new String(mqttMessage.getPayload());
                    JSONObject object = new JSONObject(getMsg);
                    System.out.println("Received : " + object.get("magnet"));

                    if(object.get("magnet").equals("on")){
                        System.out.println("Activate attach");
                        laserEmitter1.attach();
                        laserEmitter2.attach();
                    }
                    else{
                        System.out.println("Activate detach");
                        laserEmitter1.detach();
                        laserEmitter2.detach();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        client.subscribe("/drone/magnet/sub");
    }
}
