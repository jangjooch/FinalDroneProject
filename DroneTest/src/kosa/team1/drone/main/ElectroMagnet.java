package kosa.team1.drone.main;

import com.pi4j.io.gpio.*;

public class ElectroMagnet {

    private final static String ATTACH = "attach";
    private final static String DETACH = "detach";

    private GpioPinDigitalOutput lagerPin;

    private String status;

    // Pin 생성자
    public ElectroMagnet(Pin laserPinNo){
        status = DETACH;
        GpioController gpioController = GpioFactory.getInstance();
        lagerPin = gpioController.provisionDigitalOutputPin(laserPinNo, PinState.LOW);
        lagerPin.setShutdownOptions(true, PinState.LOW);
    }

    public void attach(){
        lagerPin.high();
        this.status = ATTACH;
    }

    public void detach(){
        lagerPin.low();
        this.status = DETACH;
    }

    public String getStatus(){
        return status;
    }
}
