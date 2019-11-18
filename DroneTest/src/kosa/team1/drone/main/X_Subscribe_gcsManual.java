package kosa.team1.drone.main;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

public class X_Subscribe_gcsManual {
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

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                String strData = new String(mqttMessage.getPayload());
                System.out.println("Received : " + strData);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        try {
            client.subscribe("/gcs/droneManual");
            System.out.println("subscribe Done");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
