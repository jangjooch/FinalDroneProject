/*
java -Djava.library.path=/usr/lib/jni:/home/pi/opencv/opencv-3.4.5/build/lib -cp classes:lib/'*' companion.companion.RealMain
 */

package kosa.team1.drone.main;

import kosa.team1.drone.network.NetworkConfig;
import syk.drone.device.Camera;
import syk.drone.device.FlightController;
//import syk.sample.drone.network.NetworkConfig;

public class RealMain {
    public static void main(String[] args) {
        NetworkConfig networkConfig = new NetworkConfig();

        FlightController flightController = new FlightController();
        flightController.mavlinkConnectRxTx("/dev/ttyAMA0");
        flightController.mqttConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic +"/fc/pub",
                networkConfig.droneTopic +"/fc/sub"
        );
        // USB 0번 1번에 따른 카메라 설정.
        // 먼저 꽃는 것에 따라 0번과 1이 바뀐다.
        // 만약 전방이 0이 아닐경우 Topic 번호만
        // 바꾸어주면 바뀌게 될것이다.
        Camera camera0 = new Camera();
        // 하단캠
        camera0.cameraConnect(0, 320, 240, 270);
        // angle은 화면 돌아가는 방향
        camera0.mattConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic + "/cam1/pub",
                networkConfig.droneTopic + "/cam1/sub"
        );
        // 상단캠
        Camera camera1 = new Camera();
        camera1.cameraConnect(1, 320, 240, 0);
        camera1.mattConnect(
                networkConfig.mqttBrokerConnStr,
                networkConfig.droneTopic +"/cam0/pub",
                networkConfig.droneTopic +"/cam0/sub"
        );
    }
}
