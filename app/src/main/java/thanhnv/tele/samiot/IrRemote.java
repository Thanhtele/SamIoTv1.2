package thanhnv.tele.samiot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import static thanhnv.tele.samiot.R.drawable.fanspeed1;
import static thanhnv.tele.samiot.R.drawable.fanspeed2;
import static thanhnv.tele.samiot.R.drawable.fanspeed3;
import static thanhnv.tele.samiot.R.drawable.fanspeedauto;
import static thanhnv.tele.samiot.R.drawable.fanspeedoff;
import static thanhnv.tele.samiot.R.drawable.power_off;
import static thanhnv.tele.samiot.R.drawable.power_on;

public class IrRemote extends MainActivity {
    androidx.appcompat.widget.Toolbar toolbar;
    ImageButton irPlusBtn;
    ImageButton irSubBtn;
    ImageButton irModeBtn;
    ImageButton irPowerBtn;
    ImageButton irFanBtn;
    ImageButton irFanStatus;
    ImageButton fanSpeedIcon;
    TextView valueIR;
    Devices devices;
    int value;
    int fanSpeed=0;
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.irremote);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
        irModeBtn = (ImageButton) findViewById(R.id.irModeBtn);
        irPlusBtn = (ImageButton) findViewById(R.id.irPlusBtn);
        irSubBtn = (ImageButton) findViewById(R.id.irSubBtn);
        irPowerBtn = (ImageButton) findViewById(R.id.irPowerBtn);
        irFanStatus = (ImageButton) findViewById(R.id.fanspeed);
        irFanBtn=(ImageButton) findViewById(R.id.irFanBtn);
        fanSpeedIcon=(ImageButton) findViewById(R.id.fanIR) ;
        valueIR =(TextView) findViewById(R.id.valueIR);

        Intent intent = getIntent();
        int position = intent.getIntExtra("Position",0);
        devices =(Devices) dataRecyclerView.get(position);
        if(devices.value.get("deviceValue")!=null) {
            value = devices.value.get("deviceValue").intValue();
        }
        else value=23;
         if(value!=0) {
             valueIR.setText(value + "°C");
             irPowerBtn.setBackgroundResource(power_on);
             if (devices.value.get("fanSpeed") != null) {
                 fanSpeed = devices.value.get("fanSpeed").intValue();
             }
             else fanSpeed=4;

             switch (fanSpeed) {
                     case 0:
                         irFanStatus.setBackgroundResource(fanspeedoff);
                         break;
                     case 1:
                         irFanStatus.setBackgroundResource(fanspeed1);
                         break;
                     case 2:
                         irFanStatus.setBackgroundResource(fanspeed2);
                         break;
                     case 3:
                         irFanStatus.setBackgroundResource(fanspeed3);
                         break;
                     case 4:
                         irFanStatus.setBackgroundResource(fanspeedauto);
              }
         }
         else{
             valueIR.setText("OFF");
             irPowerBtn.setBackgroundResource(power_off);
             irFanStatus.setBackgroundResource(fanspeedoff);
         }

        getSupportActionBar().setTitle(devices.name);
        toolbar.setTitleTextColor(R.color.colorAccent);

        irPowerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(value!=0) {
                    value=0;
                    valueIR.setText("OFF");
                    devices.value.put("deviceValue", (float) value);
                    irPowerBtn.setBackgroundResource(power_off);
                    irFanStatus.setBackgroundResource(fanspeedoff);
                    fanSpeed=4;
                    devices.value.put("fanSpeed", (float) fanSpeed);
                }
                else{
                    value=23;
                    if(devices.deviceType.equals("Fan")) value=1;
                    devices.value.put("deviceValue", (float) value);
                    valueIR.setText(value + "°C");
                    irPowerBtn.setBackgroundResource(power_on);
                    irFanStatus.setBackgroundResource(fanspeedauto);
                    fanSpeed=0;
                    devices.value.put("fanSpeed", (float) fanSpeed);
                }
                mData.child("User1").child("Home1").child(devices.uId).child("value/value").setValue(value);
            }
        });
        irSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(value>16){
                    value--;
                    devices.value.put("deviceValue", (float) value);
                    valueIR.setText(value + "°C");
                    mData.child("User1").child("Home1").child(devices.uId).child("value/value").setValue(value);
                }
            }
        });
        irPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(value<32){
                    value++;
                    devices.value.put("deviceValue", (float) value);
                    valueIR.setText(value + "°C");
                    mData.child("User1").child("Home1").child(devices.uId).child("value/value").setValue(value);
                }
            }
        });
        irModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        irFanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(value!=0) {
                    fanSpeed++;
                    if (fanSpeed > 4) fanSpeed = 0;
                    devices.value.put("fanSpeed", (float) fanSpeed);
                    switch (fanSpeed) {
                        case 0:
                            irFanStatus.setBackgroundResource(fanspeedoff);
                            break;
                        case 1:
                            irFanStatus.setBackgroundResource(fanspeed1);
                            break;
                        case 2:
                            irFanStatus.setBackgroundResource(fanspeed2);
                            break;
                        case 3:
                            irFanStatus.setBackgroundResource(fanspeed3);
                            break;
                        case 4:
                            irFanStatus.setBackgroundResource(fanspeedauto);
                            break;
                    }
                }
            }
        });
    }
}