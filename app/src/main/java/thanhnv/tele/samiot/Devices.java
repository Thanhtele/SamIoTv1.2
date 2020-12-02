package thanhnv.tele.samiot;

import java.util.Map;

import static thanhnv.tele.samiot.R.drawable.ac_off;
import static thanhnv.tele.samiot.R.drawable.ac_on;
import static thanhnv.tele.samiot.R.drawable.fan_off;
import static thanhnv.tele.samiot.R.drawable.fan_on;
import static thanhnv.tele.samiot.R.drawable.gassensor;
import static thanhnv.tele.samiot.R.drawable.humiditysensor;
import static thanhnv.tele.samiot.R.drawable.light_off;
import static thanhnv.tele.samiot.R.drawable.light_on;
import static thanhnv.tele.samiot.R.drawable.temperaturesensor;

public class Devices{
    String controlType;
    String deviceType;
    String room;
    int isOnline;
    String name;
    String uId;
    Map<String,Float> value;

    int deviceOn;
    int deviceOff;
    ///Constructor

    public Devices() { }
    public Devices(String controlType, String deviceType, int isOnline, String name, Map<String,Float> value) {
        this.controlType = controlType;
        this.deviceType = deviceType;
        this.isOnline = isOnline;
        this.name = name;
        this.value = value;
    }

    public String getuId() {
        return uId;
    }

    public String getRoom() {
        return room;
    }
    public void setControlType(String controlType) {
        this.controlType = controlType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        switch (deviceType){
            case "Light":{
                this.deviceOn=light_on;
                this.deviceOff=light_off;
                break;
            }
            case "Fan":{
                this.deviceOn=fan_on;
                this.deviceOff=fan_off;
                break;
            }
            case "GasSensor":{
                this.deviceOn=gassensor;
                this.deviceOff=gassensor;
                break;
            }
            case "TemperatureSensor":{
                this.deviceOn=temperaturesensor;
                this.deviceOff=temperaturesensor;
                break;
            }
            case "HumiditySensor":{
                this.deviceOn=humiditysensor;
                this.deviceOff=humiditysensor;
                break;
            }
            case "AirConditioner":{
                this.deviceOn=ac_on;
                this.deviceOff=ac_off;
            }
        }
    }
    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setValue(Map< String, Float > value) {
        this.value = value;
    }

    ////Get

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setRoom(String room) {
        this.room = room;
    }
    public String getControlType() {
        return controlType;
    }
    public String getDeviceType() {
        return deviceType;
    }
    public int getIsOnline() {
        return isOnline;
    }
    public String getName() {
        return name;
    }
    public Map< String, Float > getValue() {
        return value;
    }
}
