package thanhnv.tele.samiot;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static thanhnv.tele.samiot.R.drawable.*;

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int Room = 1;
    public static final int Device = 2;

    Devices devices;
    private ArrayList<Object> devicesList;
    public DeviceAdapter(ArrayList< Object > devicesList) {
        this.devicesList = devicesList;
    }

    @Override
    public int getItemViewType(int position) {
        if (devicesList.get(position) instanceof String)
            return Room;
        else if (devicesList.get(position) instanceof Devices)
            return Device;
        return -1;

    }
    //////Event Click, change Value
    public interface OnButtonClick {
        void onButtonClick(int value, int position, int listSize);
        void onSeekBarChange(int value, int position,int listSize);
        void onitemClick(int position);
    }
    private OnButtonClick listener;
    public void setOnButtonClick(OnButtonClick listener){
        this.listener = listener;
    }

    public float getValue(Devices devices){
        Map< String, Float > data = devices.value;
        TreeMap<String, Float> device = new TreeMap<>(data);
        return (Float) device.values().toArray()[device.values().toArray().length-1];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case Room :
                View viewRoom = li.inflate(R.layout.roomname, parent, false);
                return new RoomNameHolder(viewRoom);
            case Device : {
                View viewDevice = li.inflate(R.layout.devicesdisplay,parent,false);
                return new DeviceHolder(viewDevice);
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderBind, int position1) {
        final int position = position1;
        switch (getItemViewType(position)) {
            case Device: {
                final DeviceHolder holder = (DeviceHolder) holderBind;
                devices = (Devices) devicesList.get(position);
                if(devices.controlType.equals("IrRemote") && devices.value.get("deviceValue")==null) devices.value.put("deviceValue", (float) 23);
                    holder.deviceName.setText(devices.getName());
                    switch (devices.getControlType()) {
                        case "On-Off": {
                            holder.btPower.setVisibility(View.VISIBLE);
                            holder.drawValue.setVisibility(View.INVISIBLE);
                            holder.sensorValue.setText("");
                            holder.irValue.setText("");
                            break;
                        }
                        case "IrRemote":{
                            holder.btPower.setVisibility(View.VISIBLE);
                            holder.drawValue.setVisibility(View.INVISIBLE);
                            holder.sensorValue.setText("");
                            break;
                        }
                        case "MultiValue": {
                            holder.btPower.setVisibility(View.INVISIBLE);
                            holder.drawValue.setVisibility(View.VISIBLE);
                            holder.sensorValue.setText("");
                            holder.irValue.setText("");
                            break;
                        }
                        case "NonControl": {
                            holder.btPower.setVisibility(View.INVISIBLE);
                            holder.drawValue.setVisibility(View.INVISIBLE);
                            holder.irValue.setText("");
                            break;
                        }
                    }

                    ///Online or offline
                    if (devices.getIsOnline() == 1) {
                        holder.connectStatus.setImageResource(status_online);
                        holder.onOffStatus.setText("Online");
                        if (getValue(devices) != 0) {
                            holder.iconDevice.setImageResource(devices.deviceOn);
                            switch (devices.getControlType()) {
                                case "NonControl": {
                                    switch (devices.deviceType) {
                                        case "TemperatureSensor": {
                                            holder.sensorValue.setText(getValue(devices) + "°C");
                                            break;
                                        }
                                        case "HumiditySensor": {
                                            holder.sensorValue.setText(getValue(devices) + "%");
                                            break;
                                        }
                                    }
                                    break;
                                }
                                case "MultiValue": {
                                    holder.drawValue.setIndeterminate(false);
                                    holder.onOffStatus.setText("On");
                                    holder.drawValue.setProgress((int) getValue(devices));
                                    holder.sensorValue.setText((int) getValue(devices) + "");
                                    break;
                                }
                                case "On-Off": {
                                    holder.onOffStatus.setText("On");
                                    holder.btPower.setBackgroundResource(power_on);
                                    break;
                                }
                                case "IrRemote":{
                                    holder.btPower.setBackgroundResource(power_on);
                                    holder.onOffStatus.setText("On");
                                    holder.irValue.setText(devices.value.get("deviceValue").intValue()+"°C");
                                    break;
                                }
                            }
                        }
                        else {
                            holder.iconDevice.setImageResource(devices.deviceOff);
                            switch (devices.getControlType()) {
                                case "NonControl": {
                                    holder.onOffStatus.setText("Online");
                                    switch (devices.deviceType) {
                                        case "TemperatureSensor": {
                                            holder.sensorValue.setText("0 °C");
                                            break;
                                        }
                                        case "HumiditySensor": {
                                            holder.sensorValue.setText("0 %");
                                            break;
                                        }
                                    }
                                    break;
                                }
                                case "MultiValue": {
                                    holder.drawValue.setIndeterminate(false);
                                    holder.onOffStatus.setText("Off");
                                    holder.drawValue.setProgress(0);
                                    holder.sensorValue.setText("0");
                                    break;
                                }
                                case "On-Off": {
                                    holder.onOffStatus.setText("Off");
                                    holder.btPower.setBackgroundResource(power_off);
                                    break;
                                }
                                case "IrRemote":{
                                    holder.btPower.setBackgroundResource(power_off);
                                    holder.onOffStatus.setText("Off");
                                    holder.irValue.setText("OFF");
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        switch (devices.getControlType()) {
                            case "NonControl": {
                                holder.sensorValue.setText("Unknow");
                                break;
                            }
                            case "MultiValue": {
                                holder.drawValue.setIndeterminate(true);///Disable seekbar

                                break;
                            }
                            case "On-Off": {
                                holder.btPower.setBackgroundResource(power_off);
                                break;
                            }
                            case "IrRemote":{
                                holder.irValue.setText("");
                                holder.btPower.setBackgroundResource(power_off);
                                break;
                            }
                        }
                        holder.connectStatus.setImageResource(status_offline);
                        holder.onOffStatus.setText("Offline");
                        holder.iconDevice.setImageResource(devices.deviceOff);
                    }

                    /////////////////////////////Btn Click event
                    holder.btPower.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            devices = (Devices) devicesList.get(position);
                            if (devices.getIsOnline() == 1) {
                                if (getValue(devices) == 0) {
                                    holder.btPower.setBackgroundResource(power_on);
                                    holder.iconDevice.setImageResource(devices.deviceOn);
                                    devices.value.put("value", (float) 1);
                                    if(devices.controlType.equals("IrRemote")) devices.value.put("value", (float) 23);
                                    holder.onOffStatus.setText("On");
                                } else {
                                    holder.btPower.setBackgroundResource(power_off);
                                    holder.iconDevice.setImageResource(devices.deviceOff);
                                    devices.value.put("value", (float) 0);
                                    holder.onOffStatus.setText("Off");
                                }
                            }
                            if (listener != null) {
                                listener.onButtonClick((int) getValue(devices), position, getItemCount());
                            }
                        }
                    });

                    ////SeekbarChangeValue
                    holder.drawValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            if (devices.getIsOnline() == 1) holder.sensorValue.setText(seekBar.getProgress() + "");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            devices = (Devices) devicesList.get(position);
                            if(devices.isOnline == 1) {
                                devices.value.put("value", (float) seekBar.getProgress());
                                if (seekBar.getProgress() != 0) {
                                    holder.iconDevice.setImageResource(devices.deviceOn);
                                    holder.onOffStatus.setText("On");
                                } else {
                                    holder.onOffStatus.setText("Off");
                                    holder.iconDevice.setImageResource(devices.deviceOff);
                                }
                                if (listener != null) {
                                    listener.onSeekBarChange(seekBar.getProgress(), position, getItemCount());
                                }
                            }
                        }
                    });
                    ////
                    holder.backgroundLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (listener != null) {
                                    listener.onitemClick(position);
                                }
                            }
                    });
                break;
            }
            case Room:{
                RoomNameHolder holder = (RoomNameHolder) holderBind;
                holder.roomName.setText(devicesList.get(position).toString());
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    //////Device View Holder
    public class DeviceHolder extends RecyclerView.ViewHolder{
        ImageButton btPower;
        ImageView iconDevice;
        TextView deviceName;
        TextView irValue;
        TextView onOffStatus;
        ImageView connectStatus;
        TextView sensorValue;
        SeekBar drawValue;
        RelativeLayout backgroundLayout;
        public DeviceHolder(@NonNull View itemView) {
            super(itemView);
            backgroundLayout=(RelativeLayout) itemView.findViewById(R.id.backgroundLayout);
            btPower=(ImageButton) itemView.findViewById(R.id.btPower);
            iconDevice=(ImageView) itemView.findViewById(R.id.iconDevice);
            deviceName=(TextView) itemView.findViewById(R.id.deviceName);
            onOffStatus=(TextView) itemView.findViewById(R.id.onOffStatus);
            connectStatus=(ImageView) itemView.findViewById(R.id.connectStatus);
            sensorValue=(TextView) itemView.findViewById(R.id.sensorValue);
            drawValue=(SeekBar) itemView.findViewById(R.id.drawValue);
            irValue=(TextView) itemView.findViewById(R.id.irValue);
        }
    }
    public class RoomNameHolder extends RecyclerView.ViewHolder{
        TextView roomName;
        public RoomNameHolder(@NonNull View itemView) {
            super(itemView);
            roomName = (TextView) itemView.findViewById(R.id.roomId);
        }
    }
}
