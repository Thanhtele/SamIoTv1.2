package thanhnv.tele.samiot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static thanhnv.tele.samiot.R.drawable.*;

public class DeviceAdapter extends RecyclerView.Adapter< DeviceAdapter.DeviceHolder > {
    Devices devices;
    private List<Devices> devicesList;
    public DeviceAdapter(List< Devices > devicesList) {
        this.devicesList = devicesList;
    }

    //////Event Click, change Value
    public interface OnButtonClick {
        void onButtonClick(int value, int position, int listSize);
        void onSeekBarChange(int value, int position,int listSize);
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

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devicesdisplay, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceHolder holder, final int position) {
        if(devicesList.size()!=0) {
            devices = devicesList.get(position);

            holder.deviceName.setText(devices.getName());

            switch (devices.getControlType()) {
                case "On-Off": {
                    holder.drawValue.setVisibility(View.INVISIBLE);
                    holder.sensorValue.setText("");
                    break;
                }
                case "MultiValue": {
                    holder.btPower.setVisibility(View.INVISIBLE);
                    holder.sensorValue.setText("");
                    break;
                }
                case "NonControl": {
                    holder.btPower.setVisibility(View.INVISIBLE);
                    holder.drawValue.setVisibility(View.INVISIBLE);
                    break;
                }
            }

            ///Online or offline
            if (devices.getIsOnline() == 1) {
                holder.connectStatus.setImageResource(status_online);
                if ( getValue(devices) != 0) {
                    holder.iconDevice.setImageResource(devices.deviceOn);
                    switch (devices.getControlType()) {
                        case "NonControl": {
                            holder.onOffStatus.setText("Online");
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
                    }

                    switch (devices.deviceType) {
                        case "GasSensor": {
                            holder.sensorValue.setText("Danger!");
                            holder.backgroundLayout.setBackgroundResource(redbackground);
                            break;
                        }
                        case "TemperatureSensor": {
                            holder.sensorValue.setText(getValue(devices) + "%C");
                            break;
                        }
                        case "HumiditySensor": {
                            holder.sensorValue.setText(getValue(devices) + "%");
                            break;
                        }
                    }
                } else {
                    holder.iconDevice.setImageResource(devices.deviceOff);
                    switch (devices.getControlType()) {
                        case "NonControl": {
                            holder.onOffStatus.setText("Online");
                            holder.sensorValue.setText("Unknow");
                            break;
                        }
                        case "MultiValue": {
                            holder.drawValue.setIndeterminate(false);
                            holder.onOffStatus.setText("Off");
                            holder.drawValue.setProgress(0);
                            break;
                        }
                        case "On-Off": {
                            holder.onOffStatus.setText("Off");
                            holder.btPower.setBackgroundResource(power_off);
                            break;
                        }
                    }

                    switch (devices.deviceType) {
                        case "GasSensor": {
                            holder.sensorValue.setText("Safety");
                            holder.backgroundLayout.setBackgroundResource(custombackground);
                            break;
                        }
                        case "TemperatureSensor": {
                            holder.sensorValue.setText("0 °C");
                            break;
                        }
                        case "HumiditySensor": {
                            holder.sensorValue.setText("0 %");
                            break;
                        }
                    }
                }
            } else {
                holder.connectStatus.setImageResource(status_offline);
                holder.onOffStatus.setText("Offline");
                holder.iconDevice.setImageResource(devices.deviceOff);
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
                }
            }

            /////////////////////////////Btn Click event
            holder.btPower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    devices = devicesList.get(position);

                    if (devices.getIsOnline() == 1) {
                        if (getValue(devices) == 0) {
                            holder.btPower.setBackgroundResource(power_on);
                            holder.iconDevice.setImageResource(devices.deviceOn);
                            devices.value.put("value", (float) 1);
                            holder.onOffStatus.setText("On");
                        }
                        else {
                            holder.btPower.setBackgroundResource(power_off);
                            holder.iconDevice.setImageResource(devices.deviceOff);
                            devices.value.put("value", (float) 0);
                            holder.onOffStatus.setText("Off");
                        }
                    }
                    if (listener != null){
                        listener.onButtonClick((int) getValue(devices), position, getItemCount());
                    }
                }
            });

            ////SeekbarChangeValue
            holder.drawValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    holder.sensorValue.setText(seekBar.getProgress() + "");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    devices = devicesList.get(position);
                    devices.value.put("value", (float) seekBar.getProgress());
                    if (seekBar.getProgress() != 0){
                        holder.iconDevice.setImageResource(devices.deviceOn);
                        holder.onOffStatus.setText("On");
                    }
                    else {
                        holder.onOffStatus.setText("Off");
                        holder.iconDevice.setImageResource(devices.deviceOff);
                    }
                    if (listener != null) {
                        listener.onSeekBarChange(seekBar.getProgress(), position, getItemCount());
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    //////Device View Holder
    public static class DeviceHolder extends RecyclerView.ViewHolder{
        ImageButton btPower;
        ImageView iconDevice;
        TextView deviceName;
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
        }
    }
}
