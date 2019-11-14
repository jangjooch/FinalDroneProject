package kosa.team1.drone.main;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;

public class X_PublishTest_Missions {

    public static void main(String[] args){
        MqttClient client;
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

        JSONArray jsonArray = new JSONArray();

        for(int i = 0 ; i < 5 ; i++){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("missionNumber", i);
            //jsonObject.put("lat",37.504000);
            //jsonObject.put("lng",127.122000);
            // 협회 좌표
            jsonObject.put("lat", 37.5469722);
            jsonObject.put("lng", 127.1196016);
            jsonArray.put(jsonObject);
        }

        try {
            System.out.println("Publish Try");
            client.publish("/gcs/missionIn", jsonArray.toString().getBytes(),0,false);
            System.out.println("Send idx 1 : " + jsonArray.get(1).toString());
            System.out.println("Publish Done");
        } catch (MqttException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
