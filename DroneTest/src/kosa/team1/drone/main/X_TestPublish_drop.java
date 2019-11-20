package kosa.team1.drone.main;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;

public class X_TestPublish_drop {

    public static void main(String[] args) {
        MqttClient client;
        while (true) {
            try {
                client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
                MqttConnectOptions options = new MqttConnectOptions();
                client.connect();
                System.out.println("GcsMainMqtt client Connect Done");
                break;
            } catch (MqttException e) {
                System.out.println("GcsMainMqtt client Connect Error");
                System.out.println(e.getMessage());
            }
        }

        JSONObject jsonObject = new JSONObject();
        //jsonObject.put("msgid", "emergency");
        jsonObject.put("msgid", "control");
        jsonObject.put("direction", "up");
        jsonObject.put("speed", 1);
        jsonObject.put("missionNumber", 4);
        jsonObject.put("magnet","off");
        System.out.println(jsonObject.toString());

        JSONObject toWebService = new JSONObject();
        toWebService.put("msgid", "saveSnapShot");
        toWebService.put("snapShot", true);
        toWebService.put("missionNumber", 4);
        try {
            System.out.println("Publish Try");
            client.publish("/drone/cam1/gcs", toWebService.toString().getBytes(), 0, false);
            client.publish("/jang/drone", jsonObject.toString().getBytes(), 0 , false);
            System.out.println("Publish Done");

        } catch (MqttException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
