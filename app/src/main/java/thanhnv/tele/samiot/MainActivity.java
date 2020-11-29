package thanhnv.tele.samiot;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity {
    private Spinner ListSensor;
    private ImageButton menuBtn;
    TextView nameSensor;
    /////Firebase
    private FirebaseAuth auth;
    DatabaseReference mData;
    ArrayList<Devices> dataFromFirebase= new ArrayList<Devices>();

    ArrayList<Devices> ArrValueSensor = new ArrayList<Devices>();
    ArrayList<String> StrSensorName = new ArrayList<String>();
    ArrayList<String> StrRoomName=new ArrayList<>();
    Map<String,Long> deviceCount= new HashMap<String, Long>();
    ArrayAdapter arrayAdapter;

    RecyclerView recyclerView;
    DeviceAdapter deviceAdapter;
    ArrayList<Object> dataRecyclerView= new ArrayList<Object>();
    GridLayoutManager manager =new GridLayoutManager(this, 2);
    Gson gson;
    ///Line Chart
    LineChart mChart;
    LineData lineData;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm / dd-MM-yy     "); // the format of date
    ArrayList<Entry> sensorValues = new ArrayList<>();
    ArrayList<String> timeValue= new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuBtn=(ImageButton) findViewById(R.id.menuBtn);

        ///Firebase
        mData = FirebaseDatabase.getInstance().getReference();

        /////RecyclerView
        initView();
        getDataFromStorage();

        ///Temp_Humidity_Chart;
        nameSensor = (TextView) findViewById(R.id.nameSensor);
        ListSensor = (Spinner) findViewById(R.id.spinnerNameSensor);
        initChart();

        ///Menu Button
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                ShowMenu();
            }
        });
        ///Event Value Changed
        EventValue();
        ////Item click On/Off or Dimmer
        deviceAdapter.setOnButtonClick(new DeviceAdapter.OnButtonClick() {
            //// On/Off Button
            @Override
            public void onButtonClick(int value, int position, int listSize) {
                long indexStartOfRoom = 0,
                    numDevices = 0;
                Devices devices = (Devices) dataRecyclerView.get(position);
                for(String room : StrRoomName){
                    numDevices = deviceCount.get(room);
                    indexStartOfRoom = dataRecyclerView.indexOf(room);
                    if(position <= indexStartOfRoom + numDevices){
                        int indexOfLastValue=devices.value.values().toArray().length-1;
                        mData.child("User1").child("Home1").child(devices.uId).child("value/value")
                                .setValue(devices.value.values().toArray()[indexOfLastValue]);
                        break;
                    }
                }
            }
            //// Dimmer SeekBar
            @Override
            public void onSeekBarChange(int value, int position, int listSize) {
                long indexStartOfRoom = 0,
                        numDevices = 0;
                Devices devices = (Devices) dataRecyclerView.get(position);
                for(String room : StrRoomName){
                    numDevices = deviceCount.get(room);
                    indexStartOfRoom = dataRecyclerView.indexOf(room);
                    if(position <= indexStartOfRoom + numDevices){
                        int indexOfLastValue=devices.value.values().toArray().length-1;
                        mData.child("User1").child("Home1").child(devices.uId).child("value/value")
                                .setValue(devices.value.values().toArray()[indexOfLastValue]);
                        break;
                    }
                }
            }
        });
    }
    private void ShowMenu(){
        PopupMenu popupMenu = new PopupMenu(this,menuBtn);
        popupMenu.getMenuInflater().inflate(R.menu.mainmenu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.aboutAppMenu : {
                        break;
                    }
                    case R.id.logOutMenu :{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Are you sure?");
                        alertDialog.setIcon(R.mipmap.ic_launcher);
                        alertDialog.setMessage("Are you sure you want to log out?");
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                auth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog alertDialog1 = alertDialog.create();
                        alertDialog1.show();
                        break;
                    }
                    case R.id.exitMenu :{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Are you sure?");
                        alertDialog.setIcon(R.mipmap.ic_launcher);
                        alertDialog.setMessage("Are you sure you want to close this app?");
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        AlertDialog alertDialog1 = alertDialog.create();
                        alertDialog1.show();
                        break;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }
    public void EventValue(){
        mData.child("User1").child("Home1").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ProcessEvent(snapshot);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ProcessEvent(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Devices devices = snapshot.getValue(Devices.class);
                String nameRoom = devices.room;
                int indexStartRoom = dataRecyclerView.indexOf(nameRoom);
                int indexOfDevice = CheckUid(devices);
                long numDevcie = deviceCount.get(nameRoom);

                deviceCount.put(nameRoom, numDevcie-1);
                dataRecyclerView.remove(indexOfDevice);
                if(numDevcie-1 <= 0) {
                    dataRecyclerView.remove(nameRoom);
                    StrRoomName.remove(nameRoom);
                    deviceCount.remove(nameRoom);
                }
                deviceAdapter.notifyDataSetChanged();
                if (devices.deviceType.equals("TemperatureSensor") || devices.deviceType.equals("HumiditySensor")){
                    updateChart(devices,"Remove");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void ProcessEvent(DataSnapshot snapshot){
        Devices devices = snapshot.getValue(Devices.class);
        String nameRoom = devices.room;
        int indexStartRoom = dataRecyclerView.indexOf(nameRoom);
        int indexOfDevice = CheckUid(devices);

        if(indexStartRoom == -1){  /// New Room
            deviceCount.put(nameRoom, (long) 1);
            StrRoomName.add(nameRoom);
            dataRecyclerView.add(nameRoom);
            dataRecyclerView.add(devices);
            dataFromFirebase.clear();
            dataFromFirebase.add(devices);
            saveData(dataFromFirebase,nameRoom);
        }
        else{
            if(indexOfDevice == -1) { /// Id Device chua ton tai
                long numDevcie = deviceCount.get(nameRoom);
                deviceCount.put(nameRoom, numDevcie + 1);
                dataRecyclerView.add((int) (indexStartRoom + numDevcie + 1), devices);
            }
            else{ //// Id Device da ton tai
                dataRecyclerView.set(indexOfDevice,devices);
            }
            dataFromFirebase.clear();
            for(int i=indexStartRoom+1; i<=indexStartRoom+deviceCount.get(nameRoom); i++) {
                dataFromFirebase.add((Devices) dataRecyclerView.get(i));
            }
            saveData(dataFromFirebase,nameRoom);
        }
        deviceAdapter.notifyDataSetChanged();
        if (devices.deviceType.equals("TemperatureSensor") || devices.deviceType.equals("HumiditySensor")){
            if(StrSensorName.size()==0){
                StrSensorName.add(devices.name);
                initChart();
            }
            updateChart(devices,"Changed");
        }
    }
    public int CheckUid(Devices dv1){
        for(Object dvx : dataRecyclerView) {
            if(dvx instanceof Devices) {
                Devices dv2 = (Devices) dvx;
                if (dv1.uId.equals(dv2.uId)) return dataRecyclerView.indexOf(dv2);
            }
        }
        return -1;
    }
    public void initChart(){
        mChart =(LineChart) findViewById(R.id.chart);
        mChart.setDrawGridBackground(false);
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

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, StrSensorName);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ListSensor.setAdapter(arrayAdapter);
        ListSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView< ? > adapterView, View view, int i, long l) {
                setDataChart(ArrValueSensor.get(i));
                showChart(ArrValueSensor.get(i).deviceType);
            }

            @Override
            public void onNothingSelected(AdapterView< ? > adapterView) {

            }
        });
    }
    public void setDataChart(Devices sensor){
            Map< String, Float > data = sensor.value;
            TreeMap< String, Float > dataChart = new TreeMap<>(data);
            int index = 0;
            sensorValues.clear();
            timeValue.clear();
            for (String key : dataChart.keySet()) {
                Date date = new Date(Long.parseLong(key) * 1000L);
                String formattedDtm = dateFormat.format(date);
                timeValue.add(formattedDtm);
                sensorValues.add(new Entry(dataChart.get(key), index));
                index++;
            }
    }
    public void showChart(String TypeDevice){
            ArrayList< ILineDataSet > dataSets = new ArrayList<>();
            LineDataSet set1 = new LineDataSet(sensorValues, TypeDevice);
            set1.enableDashedLine(10f, 0f, 0f);
            set1.enableDashedHighlightLine(10f, 0f, 0f);
            if(TypeDevice.equals("HumiditySensor")){
                set1.setColor(Color.BLUE);
                set1.setCircleColor(Color.BLUE);
                set1.setFillColor(Color.BLUE);
            }
            else {
                set1.setColor(Color.RED);
                set1.setCircleColor(Color.RED);
                set1.setFillColor(Color.RED);
            }
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            mChart.setDescription("Sensor Monitor");
            dataSets.add(set1);
            lineData = new LineData(timeValue, dataSets);
            mChart.animateX(400);
            mChart.setData(lineData);
            mChart.invalidate();
    }
    public void updateChart(Devices device, String action){
        String currentSensor = ListSensor.getSelectedItem().toString();
        int Index = -1;
        for(Devices dv : ArrValueSensor){
            if(device.uId.equals(dv.uId)){
                Index=ArrValueSensor.indexOf(dv);
                break;
            }
        }
        if (Index != -1) {//// Value Sensor Changed
            if(action.equals("Remove")){
                ArrValueSensor.remove(device);
                StrSensorName.remove(device.name);
            }
            else ArrValueSensor.set(Index, device);
        } else {///New Sensor
            ArrValueSensor.add(device);
            StrSensorName.add(device.name);
        }
        if(device.name.equals(currentSensor)) {
            setDataChart(device);
            if(lineData!=null) lineData.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }
    public void initView(){
        recyclerView =(RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        manager.setSmoothScrollbarEnabled(true);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (dataRecyclerView.get(position) instanceof String)
                    return 2;  //// item size = 2
                else return 1; //// item size =  1
            }
        });
        deviceAdapter = new DeviceAdapter(dataRecyclerView);
        recyclerView.setAdapter(deviceAdapter);
    }
    public void getDataFromStorage(){
        SharedPreferences pre = getSharedPreferences("IoTData", MODE_PRIVATE);
        Map<String,?> keys = pre.getAll();
        dataRecyclerView.clear();
        StrRoomName.clear();
        ArrValueSensor.clear();
        StrSensorName.clear();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            String nameRoom = entry.getKey();
            long sizeOfRoom = getData(nameRoom).size();

            StrRoomName.add(nameRoom);
            dataRecyclerView.add(nameRoom);
            dataRecyclerView.addAll(getData(nameRoom));
            deviceCount.put(nameRoom,sizeOfRoom);
            if(getData(entry.getKey()).size() == 0) {
                deviceCount.remove(nameRoom);
                StrRoomName.remove(nameRoom);
                dataRecyclerView.remove(nameRoom);
            }
            if (getData(nameRoom).size() != 0) {
                for (Devices dv : getData(nameRoom)) {
                    if (dv.deviceType.equals("TemperatureSensor") || dv.deviceType.equals("HumiditySensor")) {
                        ArrValueSensor.add(dv);
                        StrSensorName.add(dv.name);
                    }
                }
            }
            deviceAdapter.notifyDataSetChanged();
        }
    }
    public ArrayList<Devices> getData(String roomName){
        SharedPreferences pre=getSharedPreferences("IoTData", MODE_PRIVATE);
        String response = pre.getString(roomName,"");
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