package thanhnv.tele.samiot;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    ImageButton addDevices;
    TextView textView;

    /////Firebase
    DatabaseReference mData;
    DatabaseReference roomData;
    ArrayList<String> roomNameSave=new ArrayList<>();
    Map<String,Long> deviceSum= new HashMap<String, Long>();
    RecyclerView recyclerView;
    DeviceAdapter deviceAdapter;
    ArrayList<Devices> devicesFirebase0= new ArrayList<Devices>();
    ArrayList<Devices> devicesFirebase1= new ArrayList<Devices>();
    ArrayList<Devices> devicesRecyclerView= new ArrayList<Devices>();
    GridLayoutManager manager =new GridLayoutManager(this, 2);
    Gson gson;

    ///Line Chart
    LineChart mChart;
    LineData lineData;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date

    ArrayList<Entry> Temp1 = new ArrayList<>();
    ArrayList<Entry> Humi1 = new ArrayList<>();
    ArrayList<String> time1= new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.textview);
        addDevices=(ImageButton) findViewById(R.id.addDevice);

        ///Firebase
        mData = FirebaseDatabase.getInstance().getReference();
        roomData = FirebaseDatabase.getInstance().getReference("User1");

        /////RecyclerView
        getRoomNameFromFirebase();
        getRoomNameFromStorage();
        initView();
        showView();

        ///Temp_Humidity_Chart
        initChart();
        getDataChart();
        showChart();

        ///Button add devices
        addDevices.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                ////Add Devices
            }
        });
        ////Item click
        deviceAdapter.setOnButtonClick(new DeviceAdapter.OnButtonClick() {
            @Override
            public void onButtonClick(int value, int position, int listSize) {
                int indexDevice=0;
                for(String room : roomNameSave){
                    if(indexDevice+deviceSum.get(room)>position){
                        String pathRoom="Room"+ (roomNameSave.indexOf(room)+1);
                        String pathDevice="Device"+(position-indexDevice+1);
                        mData.child("User1").child(pathRoom).child("Devices")
                                .child(pathDevice).child("value/value").setValue(devicesRecyclerView.get(position).value.values()
                                .toArray()[devicesRecyclerView.get(position).value.values().toArray().length-1]);
                        break;
                    }
                    else {
                        indexDevice+=deviceSum.get(room);
                    }
                }
            }

            @Override
            public void onSeekBarChange(int value, int position, int listSize) {
                int indexDevice=0;
                for(String room : roomNameSave){
                    if(indexDevice+deviceSum.get(room)>position){
                       String pathRoom="Room"+ (roomNameSave.indexOf(room)+1);
                       String pathDevice="Device"+(position-indexDevice+1);
                        mData.child("User1").child(pathRoom).child("Devices")
                                .child(pathDevice).child("value/value").setValue(devicesRecyclerView.get(position).value.values()
                                .toArray()[devicesRecyclerView.get(position).value.values().toArray().length-1]);
                        break;
                    }
                    else {
                        indexDevice+=deviceSum.get(room);
                    }
                }
            }
        });
    }
    public void initChart(){
        mChart =(LineChart) findViewById(R.id.chart);
        mChart.setDrawGridBackground(false);
        mChart.setDescription("Bed Room");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        mChart.getXAxis().setTextSize(15f);
        mChart.getAxisLeft().setTextSize(15f);
        XAxis xAxis = mChart.getXAxis();
        YAxis leftAxis = mChart.getAxisLeft();
        YAxis rightAxis = mChart.getAxisRight();

        xAxis.setAvoidFirstLastClipping(true);
        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);
        rightAxis.setEnabled(false);

        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getDataChart(){
        if(getData("BedRoom").size()!=0) {
            Map< String, Float > data = getData("BedRoom").get(1).value;
            TreeMap<String, Float> dataChart = new TreeMap<>(data);
            int index = 0;
            Humi1.clear();
            time1.clear();
            for (String key : dataChart.keySet()) {
                Date date = new Date(Long.parseLong(key)*1000L);
                String formattedDtm = dateFormat.format(date);

                time1.add(formattedDtm);
                Humi1.add(new Entry(dataChart.get(key), index));
                index++;
            }
            if (getData("BedRoom").size() > 2) {
                data = getData("BedRoom").get(2).value;
                dataChart = new TreeMap<>(data);
                index = 0;
                Temp1.clear();
                for (String key : dataChart.keySet()) {
                    Temp1.add(new Entry(dataChart.get(key), index));
                    index++;
                }
            }
        }
    }
    public void showChart(){
            ArrayList< ILineDataSet > dataSets = new ArrayList<>();
            LineDataSet set1 = new LineDataSet(Temp1, "Temperature");
            set1.enableDashedLine(10f, 0f, 0f);
            set1.enableDashedHighlightLine(10f, 0f, 0f);
            set1.setColor(Color.RED);
            set1.setCircleColor(Color.RED);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFillColor(Color.RED);


            LineDataSet set2 = new LineDataSet(Humi1, "Humidity");
            set2.enableDashedLine(10f, 0f, 0f);
            set2.enableDashedHighlightLine(10f, 0f, 0f);
            set2.setColor(Color.BLUE);
            set2.setCircleColor(Color.BLUE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setDrawCircleHole(false);
            set2.setValueTextSize(9f);
            set2.setDrawFilled(true);

            dataSets.add(set1);
            dataSets.add(set2);
            lineData = new LineData(time1, dataSets);

            mChart.animateX(2000);
            mChart.setData(lineData);
            mChart.invalidate();
    }
    public void initView(){
        recyclerView =(RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        manager.setSmoothScrollbarEnabled(true);
    }
    public  void showView(){
        devicesRecyclerView.clear();
        for (String room: roomNameSave) {
            ArrayList< Devices > data= new ArrayList< Devices >();
            data=getData(room);
            devicesRecyclerView.addAll(data);
        }
        deviceAdapter = new DeviceAdapter(devicesRecyclerView);
        recyclerView.setAdapter(deviceAdapter);
    }
    public void getRoomNameFromFirebase(){
        roomData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomNameSave.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Map<String, String> deviceData = (Map<String, String>) ds.getValue();
                    int i=0;
                    long size = 0;
                    String nameRoom =deviceData.get("name");
                    for(DataSnapshot datas : ds.getChildren()){
                        if(i==0) size = datas.getChildrenCount();
                        i++;
                    }
                    deviceSum.put(nameRoom,size);
                    //roomNameSave.add(nameRoom);
                    saveData(devicesFirebase0,nameRoom);
                    saveData(devicesFirebase1,nameRoom);
                    devicesRecyclerView.clear();
                    EventValueChanged(ds.getKey(),nameRoom);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void getRoomNameFromStorage(){
        roomNameSave.clear();
        SharedPreferences pre=getSharedPreferences("IoTData", MODE_PRIVATE);
        Map<String,?> keys = pre.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            roomNameSave.add(entry.getKey());
        }
    }
    public void EventValueChanged(final String roomKey, final String roomName){

        mData.child("User1").child(roomKey).child("Devices").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Devices devices = dataSnapshot.getValue(Devices.class);
                String[] str = dataSnapshot.getKey().split("ce");///"Devi-ce-3
                int index = Integer.parseInt(str[1]) - 1;
                getRoomNameFromStorage();
                switch (roomNameSave.indexOf(roomName)){
                    case 0: {
                        if(devicesFirebase0.size()>index) {
                            devicesFirebase0.add(index, devices);
                        }
                        else devicesFirebase0.add(devices);
                        saveData(devicesFirebase0,roomName);
                        if(index==1 || index==2){
                            getDataChart();
                            lineData.notifyDataChanged();
                            mChart.notifyDataSetChanged();
                            mChart.invalidate();
                        }
                        break;
                    }
                    case 1: {
                        if(devicesFirebase1.size()>index) {
                            devicesFirebase1.add(index, devices);
                        }
                        else devicesFirebase1.add(devices);
                        saveData(devicesFirebase1,roomName);
                        break;
                    }
                }
                long sumDevice=0;
                int Index=0;
                for(String room : roomNameSave){
                    if(room.equals(roomName)){
                        Index = (int) (sumDevice+index);
                        if(devicesRecyclerView.size()>Index){
                            devicesRecyclerView.add(Index,devices);
                        }
                        else {
                            devicesRecyclerView.add(devices);
                        }
                        break;
                    }
                    recyclerView.setAdapter(deviceAdapter);
                    sumDevice+=deviceSum.get(room);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Devices devices = dataSnapshot.getValue(Devices.class);
                String[] str = dataSnapshot.getKey().split("ce");///"Devi-ce-3
                int index = Integer.parseInt(str[1]) - 1;
                getRoomNameFromStorage();
                switch (roomNameSave.indexOf(roomName)){
                    case 0: {
                        if(devicesFirebase0.size()>index) {
                            devicesFirebase0.remove(index);
                            devicesFirebase0.add(index, devices);
                        }
                        else devicesFirebase0.add(devices);
                        saveData(devicesFirebase0,roomName);
                        if(index==1 || index==2){
                            getDataChart();
                            lineData.notifyDataChanged();
                            mChart.notifyDataSetChanged();
                            mChart.invalidate();
                        }
                        break;
                    }
                    case 1: {
                        if(devicesFirebase1.size()>index) {
                            devicesFirebase1.remove(index);
                            devicesFirebase1.add(index, devices);
                        }
                        else devicesFirebase1.add(devices);
                        saveData(devicesFirebase1,roomName);
                        break;
                    }
                }

                long sumDevice=0;
                for(String room : roomNameSave){
                    if(room.equals(roomName)){
                        int Index = (int) (sumDevice+index);
                        if(devicesRecyclerView.size()>Index){
                            devicesRecyclerView.remove(Index);
                            devicesRecyclerView.add(Index,devices);
                        }
                        else {
                            devicesRecyclerView.add(devices);
                        }
                        deviceAdapter.notifyItemChanged(Index,devices);
                        break;
                    }
                    sumDevice+=deviceSum.get(room);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Devices devices = dataSnapshot.getValue(Devices.class);
                String[] str = dataSnapshot.getKey().split("ce");///"Devi-ce-3
                int index = Integer.parseInt(str[1]) - 1;

                getRoomNameFromStorage();
                switch (roomNameSave.indexOf(roomName)){
                    case 0: {
                        devicesFirebase0.remove(index);
                        saveData(devicesFirebase0,roomName);
                        break;
                    }
                    case 1: {
                        devicesFirebase1.remove(index);
                        saveData(devicesFirebase1,roomName);
                        break;
                    }
                }

                long sumDevice=0;
                for(String room : roomNameSave){
                    if(room.equals(roomName)){
                        int Index = (int) (sumDevice+index);
                        devicesRecyclerView.remove(Index);
                        break;
                    }
                    sumDevice+=deviceSum.get(room);
                }
                recyclerView.setAdapter(deviceAdapter);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public ArrayList<Devices> getData(String nameRoom){
        SharedPreferences pre=getSharedPreferences("IoTData", MODE_PRIVATE);
        String response = pre.getString(nameRoom,"");

        if(response!="") {
            Gson gson = new Gson();
            return gson.fromJson(response, new TypeToken< ArrayList<Devices> >() {}.getType());
        }
        return new ArrayList<Devices>();
    }
    public void saveData(ArrayList<Devices> device, String nameRoom){
        gson = new Gson();
        String jsonDevices = gson.toJson(device);

        SharedPreferences pre=getSharedPreferences("IoTData", MODE_PRIVATE);
        SharedPreferences.Editor editor=pre.edit();
        editor.putString(nameRoom,jsonDevices);
        editor.apply();
    }
}


