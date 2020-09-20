package thanhnv.tele.samiot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class test {
    String controlType;
    String deviceType;
    int isOnline;
    String name;
    Map<String,Values> value;

    int deviceOn;
    int deviceOff;

    public test() {
    }

    public test(String controlType, String deviceType, int isOnline, String name) {
        this.controlType = controlType;
        this.deviceType = deviceType;
        this.isOnline = isOnline;
        this.name = name;
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
}
