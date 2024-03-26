package aina.elifran.um5.ensam.ap_os;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidplot.xy.XYPlot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements SensorEventListener, MenuFragment.OnDataChangeListener,dataAnalyse.analyseDoneListener, velocityTracking.velocityTrackingInterface, BlankFragment.clearDataAnalyse {
    int counter = 0;
    private TextView OutputX, OutputY, OutputZ,data_output_label,analyse_result;
    private Button button_stop,button_param;
    private GraphView data_output1;
    private XYPlot data_output;
    private ViewGroup control_layout,chart_layout,output_control_values_layout,command_layout,output_label;
    LinearLayout command_layout_set_params;
    SensorManager sensorManager;
    Sensor OutputSensor;
    private int data_lenght = 512; // min 256
    private final int print_scale = 64;
    //private volatile double[][] data_sensor_array;
    private volatile List<List<Double>> data_sensor_array;
    //private volatile  double[][] data_fft_array;
    private volatile List<List<Double>> data_fft_array;
    public static long act_timestamp, last_timestamp,step_time;
    private static int data_counter = 0;
    private boolean flag = false, flag2 = false, s_flag, ready = false,filterStatus = false;
    private fft fftdata;
    private boolean[] switchConfiguration;
    private static double rpmConfiguration = 1500;
    private static double powerConfiguration = 15;
    private static double powerCoefficientConfiguration = 10E+0;
    private static double noiseCoefficientConfiguration = -140;
    private int bearingBallNumberConfiguration = 12;
    private double bearingBallDiamConfiguration = 25;
    private double bearingPitchConfiguration = 5.0;
    private double bearingAngleConfiguration = Math.PI/3.0;
    private int  lag = 32;
    private double  threshold= 8.0;
    private double  influence= 0.9;
    private double lineFrequency = 50.0;
    int analyseBuffer = 2048;
    private double samplingFrequency;
    private double[][] maxFrequency;
    private filter xfilterdata;
    private filter yfilterdata;
    private filter zfilterdata;
    private static final int filterOrder = 64;
    private static final double cutOffFrequencyHigh = 0.485; // must be less than 0.5
    private static final double cutOffFrequencyLow = 0.000; // must be less than 0.0
    private Handler dofftHandler,doplotHandler,doprintHandler;
    boolean analyseData = false;

    dataAnalyse dataAnalyseVar;
    boolean trackVelocity;
    static velocityTracking velocityTracking;
    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        OutputX = findViewById(R.id.OutputX);
        OutputY = findViewById(R.id.OutputY);
        OutputZ = findViewById(R.id.OutputZ);
        //output_array = findViewById(R.id.output_array);
        data_output = findViewById(R.id.data_output);
        data_output1 = findViewById(R.id.data_output1);

        button_stop = findViewById(R.id.button_stop);
        button_param = findViewById(R.id.button_param);
        data_output_label = findViewById(R.id.data_output_label);
        //setting_button = findViewById(R.id.setting_button);

        control_layout = findViewById(R.id.control_layout);
        chart_layout = findViewById(R.id.chart_layout);
        output_control_values_layout = findViewById(R.id.output_values);
        command_layout = findViewById(R.id.command_layout);
        command_layout_set_params = findViewById(R.id.command_layout);
        //output_content = findViewById(R.id.output_content);
        output_label = findViewById(R.id.output_label);
        analyse_result = findViewById(R.id.analyse_result);
        fftdata = new fft(data_lenght);
        maxFrequency = new double[3][2];
        switchConfiguration = new boolean[10];

        velocityTracking = new velocityTracking(this);

        dofftHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        doplotHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        doprintHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        OutputSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, OutputSensor, SensorManager.SENSOR_DELAY_FASTEST);

        parseDataPreferences(preferences.readPreferences(getApplicationContext()));
        initList(data_lenght);
        setupView();
        setupButton();
        setConfiguration();
            }
    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        float[] actSensorValues = event.values.clone();
        double var;
        act_timestamp = (event.timestamp)/1000000; // in millisecond
        step_time = act_timestamp - last_timestamp;
        data_counter +=1;
        shiftRight(data_sensor_array,step_time);
        //data_sensor_array[3][0] = step_time;
        data_sensor_array.get(3).set(0,(double)step_time) ;
        last_timestamp = act_timestamp;

        if (ready){
            //data_sensor_array[0][0] = xfilterDataxfilterdata.filterData(actSensorValues[0]);
            //data_sensor_array[1][0] = yfilterdata.filterData(actSensorValues[1]);
            //data_sensor_array[2][0] = zfilterdata.filterData(actSensorValues[2]);

            data_sensor_array.get(0).set(0, xfilterdata.filterData(actSensorValues[0]));
            data_sensor_array.get(1).set(0, yfilterdata.filterData(actSensorValues[1]));
            data_sensor_array.get(2).set(0, zfilterdata.filterData(actSensorValues[2]));

            //var = Math.sqrt(
            //        Math.pow(data_sensor_array[0][0], 2) +
            //        Math.pow(data_sensor_array[1][0], 2) +
            //        Math.pow(data_sensor_array[2][0], 2));

            var = Math.sqrt(
                   Math.pow(data_sensor_array.get(0).get(0), 2) +
                   Math.pow(data_sensor_array.get(1).get(0), 2) +
                   Math.pow(data_sensor_array.get(2).get(0), 2));


            if (analyseData){ // trying to  analyse the absolute value of the vibration from x,y and z
                dataAnalyseVar.addData(var,step_time); // asume that we analyse the first vibration data
            }
            if((    xfilterdata.isConfigChange(samplingFrequency/*, cutOffFrequencyHigh * samplingFrequency*/) ||
                    yfilterdata.isConfigChange(samplingFrequency/*, cutOffFrequencyHigh * samplingFrequency*/) ||
                    zfilterdata.isConfigChange(samplingFrequency/*, cutOffFrequencyHigh * samplingFrequency*/) ) && flag){
                dataAnalyseVar.setConfig("SAMPLING FREQ", samplingFrequency);
                //Toast.makeText(getApplicationContext(), "Configuration filterLP Have been Changed", Toast.LENGTH_LONG).show();
            }
        }
        else {
            //data_sensor_array[0][0] =event.values[0];
            //data_sensor_array[1][0] =event.values[1];
            //data_sensor_array[2][0] =event.values[2];
            data_sensor_array.get(0).set(0,(double)event.values[0]);
            data_sensor_array.get(1).set(0,(double)event.values[1]);
            data_sensor_array.get(2).set(0,(double)event.values[2]);

                if(counter > data_lenght *1.2){
                    if(!filterStatus){
                        xfilterdata = new filter(filterOrder, samplingFrequency, cutOffFrequencyLow *samplingFrequency,cutOffFrequencyHigh *samplingFrequency);
                        yfilterdata = new filter(filterOrder, samplingFrequency, cutOffFrequencyLow *samplingFrequency,cutOffFrequencyHigh *samplingFrequency);
                        zfilterdata = new filter(filterOrder, samplingFrequency, cutOffFrequencyLow *samplingFrequency,cutOffFrequencyHigh *samplingFrequency);
                        filterStatus = true;        // avoid recreation of the filterLP class
                        Toast.makeText(getApplicationContext(), "Filter initialized at Fe :" + samplingFrequency + "Hz", Toast.LENGTH_LONG).show();

                        dataAnalyseVar = new dataAnalyse(   analyseBuffer,
                                                            samplingFrequency,
                                                            rpmConfiguration,
                                                            powerConfiguration,
                                                            bearingBallNumberConfiguration,

                                                            bearingBallDiamConfiguration,
                                                            bearingPitchConfiguration,
                                                            bearingAngleConfiguration,

                                                            switchConfiguration,
                                                            noiseCoefficientConfiguration,
                                                            powerCoefficientConfiguration,
                                                            lag,
                                                            threshold,
                                                            influence,
                                                            lineFrequency
                                                            );
                        dataAnalyseVar.setAnalyseDoneListener(this);

                    }
                    if (    xfilterdata.isCreated() &&
                            yfilterdata.isCreated() &&
                            zfilterdata.isCreated()){
                        button_param.setText("READY - COLLECT");
                        ready = true;}
                }
                else {
                    counter++;
                }
        }
        if(data_counter > print_scale) {
            if (flag && ready) {
                dofftHandler.post(getfftAbs);
                //doplotHandler.post(data_plot);   //doplotHandler in other way
                doplotHandler.post(data_plot1);   //doplotHandler in other way
            }
            data_counter = 0;
            doprintHandler.post(print_data);      //doprintHandler in other way
        }
}
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
             //Toast.makeText(getApplicationContext(), "Landscape Mode", Toast.LENGTH_SHORT).show();
         } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //Toast.makeText(getApplicationContext(), "Portrait Mode", Toast.LENGTH_SHORT).show();
         }
        flag2 = false;
        setConfiguration();
}

    // Implémenter la méthode onDataChanged pour modifier les données dans l'activité------------------------------------------------------------------------*/
    @Override
    public void onDataChanged(Object newData) {
        if(newData instanceof data){
            data data= (data) newData;
            switch (data.getId()){
                case "SWITCH" :
                    switchSetting((boolean[]) data.getValue());
                    break;
                case "RPM" :
                    rpmSetting((double)data.getValue());
                    break;
                case "POWER" :
                    powerSetting((double)data.getValue());
                    break;

                case "BEARING" :
                case "BEARING PITCH" :
                case "BEARING BALL DIAM" :
                case "BEARING ANGLE" :
                    bearingSetting(data);
                    break;

                case "POWER COEFFICIENT" :
                    powerCoefficientSetting((double)data.getValue());
                    break;
                case "NOISE" :
                    noiseCoefficientSetting((double)data.getValue());
                    break;
                case "LAG" :
                    lagSetting((int)data.getValue());
                    break;
                case "THRESHOLD" :
                    thresholdSetting((double)data.getValue());
                    break;
                case "INFLUENCE" :
                    influenceSetting((double)data.getValue());
                    break;
                case "RESOLUTION" :
                    bufferSetting((int)data.getValue());
                    break;
                case "LINE" :
                    lineSetting((double)data.getValue());
                    break;
                default:
                    break;
            }
            if(ready){
                dataAnalyseVar.setConfig(data.getId(),data.getValue());
            }
        }
            initList(data_lenght);
            sendDataPreferences();
            Toast.makeText(getApplicationContext(),"Configuration Saved", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void trackData(double rpm) {
        rpmConfiguration = rpm;
        trackVelocity = true;
        closeSetting();
    }
    @SuppressLint("SuspiciousIndentation")
    public void getFromTracking(double selectedVelocity){
        if (!Double.isNaN(selectedVelocity)){
            dataAnalyseVar.setConfig("RPM",selectedVelocity);
            sendDataPreferences();
            rpmSetting(selectedVelocity);
            Toast.makeText(getApplicationContext(),"Velocity Set at : " + rpmConfiguration ,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Your selection is not a number, Velocity set at : " + rpmConfiguration,Toast.LENGTH_SHORT).show();
        }

    }

    public Object getDataMain(String Id){
        Object Value = null;
        switch (Id){
            case "SWITCH" :
                Value = switchConfiguration;
                break;
            case "RPM" :
                Value = rpmConfiguration;
                break;
            case "POWER":
                Value = powerConfiguration;
                break;
            case "BEARING":
                Value = bearingBallNumberConfiguration;
                break;
            case "BEARING PITCH":
                Value = bearingPitchConfiguration;
                break;
            case "BEARING BALL DIAM":
                Value = bearingBallDiamConfiguration;
                break;
            case "BEARING ANGLE":
                Value = bearingAngleConfiguration;
                break;

            case "POWER COEFFICIENT":
                Value = powerCoefficientConfiguration;
                break;
            case "NOISE":
                Value = noiseCoefficientConfiguration;
                break;
            case "RESOLUTION":
                Value = (int)((Math.log(data_lenght/512)/Math.log(2)));
                break;
            case "LAG":
                Value = lag;
                break;
            case "THRESHOLD":
                Value = threshold;
                break;
            case "INFLUENCE":
                Value = influence;
                break;
            case "LINE":
                Value = lineFrequency;
                break;
            default:
                break;
        }
        return Value;
    }
    private void setupButton(){
        //resume button
        button_stop.setOnClickListener(view -> {
            if(!ready)
                Toast.makeText(getApplicationContext(), "Not Ready Yet", Toast.LENGTH_SHORT).show();
            else{
                flag = !flag;
                button_stop.setText(flag ? "STOP" : "RESUME");
            }
        });
        button_param.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SetTextI18n")
            public void onClick(View view) {
                if (!ready) {
                    Toast.makeText(getApplicationContext(), "Not Ready Yet", Toast.LENGTH_SHORT).show();
                    button_param.setText("NOT READY YET");
                    analyse_result.setText("");
                }else {
                    button_param.setText(" READY TO GET DATA");
                    analyse_result.setText("Ready to get Data \n");
                    if (!dataAnalyseVar.isAnalyzing()){
                        analyseData = true;
                        button_param.setText("COLLECTING DATA ...");
                        analyse_result.setText("Collecting data\nWAIT ...");
                        if(!dataAnalyseVar.beginAnalyse()){
                            Toast.makeText(getApplicationContext(), "Please Wait Before Analysing", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Analyse Begin", Toast.LENGTH_SHORT).show();
                            button_param.setText("ANALYSING ...");
                            analyse_result.setText("Analysing the data\nWAIT" );
                            analyseData = false;
                        }

                    }else
                        Toast.makeText(getApplicationContext(), "Analyse is Processing, Please Wait", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void initList(int lenght){
        data_lenght = lenght;
        try {
            data_sensor_array.clear();
            data_fft_array.clear();
        }catch (Exception e){}
        //data_sensor_array = new double[4][data_lenght];
        data_sensor_array = new ArrayList<>();
        data_sensor_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_sensor_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_sensor_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_sensor_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));


        //data_fft_array = new double[4][data_lenght];
        data_fft_array = new ArrayList<>();
        data_fft_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_fft_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_fft_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));
        data_fft_array.add(new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));

        fftdata.chancheResolution(data_lenght);
    }
    private void setupView(){

    }
    private void setConfiguration(){

        ViewGroup.LayoutParams control_params = control_layout.getLayoutParams();
        ViewGroup.LayoutParams chart_params = chart_layout.getLayoutParams();
        ViewGroup.LayoutParams output_control_values_params = output_control_values_layout.getLayoutParams();
        ViewGroup.LayoutParams command_params = command_layout.getLayoutParams();
        ViewGroup.LayoutParams button_stop_params = button_stop.getLayoutParams();
        ViewGroup.LayoutParams button_param_params = button_param.getLayoutParams();
        ViewGroup.LayoutParams output_label_params = output_label.getLayoutParams();
        ViewGroup.LayoutParams data_output1_params = data_output1.getLayoutParams();
        ViewGroup.LayoutParams data_output_label_params = data_output_label.getLayoutParams();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        //Toast.makeText(getApplicationContext(), "Height: " + height + ", Width: " + width, Toast.LENGTH_SHORT).show();


    // control layout params
        control_params.width = width;
        control_layout.setLayoutParams(control_params);

    // output layout params
        output_control_values_params.width = width/2;
        output_control_values_layout.setLayoutParams(output_control_values_params);

    //output label params
        //output_content_params.width = width/4;
        //output_content.setLayoutParams(output_content_params);

    //output content params
        //output_label_params.width = width/4;
        output_label.setPadding((int)(0.05*width),0,0,0);
        output_label.setLayoutParams(output_label_params);


    // command layout paramsb button
        command_params.width = width/2;
        if(height > width){
            command_layout_set_params.setOrientation(LinearLayout.VERTICAL);
            button_stop_params.width = command_params.width;
            button_param_params.width = command_params.width;
        }else {
            command_layout_set_params.setOrientation(LinearLayout.HORIZONTAL);
            button_stop_params.width = command_params.width/2;
            button_param_params.width = command_params.width/2;
        }
    // buttom layout params
        // command_layout.setLayoutParams(command_params);
        button_stop.setLayoutParams(button_stop_params);
        button_param.setLayoutParams(button_param_params);


   /*
    chart layout control
     control layout params // get the width of label
    */
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        control_layout.measure(widthMeasureSpec, heightMeasureSpec);
        data_output_label.measure(widthMeasureSpec, heightMeasureSpec);

        data_output_label_params.width = width;
        data_output_label.setLayoutParams(data_output_label_params);
        data_output_label.setGravity(Gravity.CENTER);

        chart_params.height = (int)(height - control_layout.getMeasuredHeight()- height*0.01);
        chart_params.width = width;
        chart_layout.setLayoutParams(chart_params);

        // control layout params
        data_output1_params.height = (int)(height - control_layout.getMeasuredHeight() - data_output_label.getMeasuredHeight() - height*0.01);
        data_output1_params.width = width;
        data_output1.setLayoutParams(data_output1_params);
}
    public double[] toAc(@NonNull List<Double> data){
        double[] temp = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            temp[i] = data.get(i); // Extracting the double value
        }
        double val = 0;
        for (double datum : data) val = val + datum;
        val = val/(data.size());
        for (int i = 0;i<data.size();i++) {
            temp[i] = data.get(i) - val;
        }
        return temp;
    }
    //public double[] getMax(@NonNull double[] data){
    public double[] getMax(@NonNull List<Double> data){

        double max =-1.0E100;
        int pos = 0;
            for(int i = 0; i <data.size()/2;i++){
                   if( max < data.get(i)){
                       max = data.get(i);
                       pos = i;
                   }
            }
        //return new double[]{max,data_fft_array[3][pos]};
        return new double[]{max,data_fft_array.get(3).get(pos)};
    }
    //private void shiftRight(@NonNull double[][] array, double timestamp){
    private void shiftRight(@NonNull List<List<Double>> array, double timestamp){
        for (int i = data_lenght - 1; i > 0; i--){
            array.get(0).set(i,array.get(0).get(i-1));
            array.get(1).set(i,array.get(1).get(i-1));
            array.get(2).set(i,array.get(2).get(i-1));
            array.get(3).set(i,array.get(3).get(i-1) + timestamp);

        }
        //array[3][0] = 0;
        array.get(0).set(0,0.0);
        //samplingFrequency = 1000* data_lenght /data_sensor_array[3][data_lenght -1];
        samplingFrequency = 1000* data_lenght /data_sensor_array.get(3).get(data_lenght-1);
    }
    private void bufferSetting(int bufferValue){
        analyseBuffer = (int) (Math.pow(2,bufferValue+3)*512);
        data_lenght = (int) (Math.pow(2,bufferValue)*512);
    }

    private void switchSetting(boolean[] switchData){
        switchConfiguration = switchData.clone();
    }
    private void rpmSetting(double rpmData){
        rpmConfiguration = rpmData;
        velocityTracking.setEstimatedVelocity(rpmConfiguration);
    }
    private void noiseCoefficientSetting(double noiseData){
        noiseCoefficientConfiguration = noiseData;
    }
    private void powerCoefficientSetting(double powerCoeffData){
        powerCoefficientConfiguration = powerCoeffData;
    }

    private void lagSetting(int lagData){
        lag = lagData;
    }
    private void thresholdSetting(double powerCoeffData){
        threshold = powerCoeffData;
    }
    private void influenceSetting(double influenceData){
        influence = influenceData;
    }
    private void lineSetting(double lineData){
        lineFrequency = lineData;
    }
    private void powerSetting(double powerData){
        powerConfiguration = powerData;
    }
    private void bearingSetting(Object dataIn){
        data data = (data) dataIn;
        switch (data.getId()){
            case "BEARING":
                bearingBallNumberConfiguration = (int) data.getValue();
                break;
            case "BEARING BALL DIAM":
                bearingBallDiamConfiguration = (double) data.getValue();
                break;
            case "BEARING PITCH":
                bearingPitchConfiguration = (double) data.getValue();
                break;
            case "BEARING ANGLE":
                bearingAngleConfiguration = (double) data.getValue();
                break;
        }

    }

    private final Runnable  getfftAbs = new Runnable() {
        @Override
        public void run() {

            double resolution = samplingFrequency / data_lenght;
            List<Double> freq = (new ArrayList<Double>(Collections.nCopies(data_lenght, 0d)));;
            data_fft_array.clear();
            for (int i = 0; i< data_lenght; i++){
                //data_fft_array[3][i+1] = data_sensor_array[3][i+1];
                //data_fft_array[3][i] = resolution*i;
                freq.set(i,resolution*i);
            }
            //data_fft_array[3][0] = 0;
            //data_fft_array[0] = fftdata.getLogtfft(toAc(data_sensor_array[0]));
            //data_fft_array[1] = fftdata.getLogtfft(toAc(data_sensor_array[1]));
            //data_fft_array[2] = fftdata.getLogtfft(toAc(data_sensor_array[2]));

            List<Double> fftList0 = new ArrayList<>();
            List<Double> fftList1 = new ArrayList<>();
            List<Double> fftList2 = new ArrayList<>();
            for (double data:fftdata.getLogtfft(toAc(data_sensor_array.get(0))))
                fftList0.add(data);
            data_fft_array.add(fftList0);
            for (double data:fftdata.getLogtfft(toAc(data_sensor_array.get(1))))
                fftList1.add(data);
            data_fft_array.add(fftList1);
            for (double data:fftdata.getLogtfft(toAc(data_sensor_array.get(2))))
                fftList2.add(data);
            data_fft_array.add(fftList2);
            data_fft_array.add(freq);

            if (trackVelocity){
                //velocityTracking.addDataTrack(data_fft_array[0],data_fft_array[1],data_fft_array[2],samplingFrequency);
                velocityTracking.addDataTrack(data_fft_array,samplingFrequency);
            }
            //maxFrequency[0] = getMax(data_fft_array[0]);
            //maxFrequency[1] = getMax(data_fft_array[1]);
            //maxFrequency[2] = getMax(data_fft_array[2]);
            maxFrequency[0] = getMax(data_fft_array.get(0));
            maxFrequency[1] = getMax(data_fft_array.get(1));
            maxFrequency[2] = getMax(data_fft_array.get(2));

        }
    };
    private final Runnable data_plot1 = new Runnable() {
        @Override
        public void run() {
            List<DataPoint> xPoints = new ArrayList<>();
            List<DataPoint> yPoints = new ArrayList<>();
            List<DataPoint> zPoints = new ArrayList<>();
            List<DataPoint> xFftPoints = new ArrayList<>();
            List<DataPoint> yFftPoints = new ArrayList<>();
            List<DataPoint> zFftPoints = new ArrayList<>();

            // Remplissez les listes de points à partir de vos tableaux de données
            for (int i = 0; i < data_lenght /2; i++) {
                //xPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[0][i*2]));
                //yPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[1][i*2]));
                //zPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[2][i*2]));

                xPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_sensor_array.get(0).get(i*2)));
                yPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_sensor_array.get(1).get(i*2)));
                zPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_sensor_array.get(2).get(i*2)));

            }
            for (int i = 0; i < data_lenght / 2; i++) {
                //xFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[0][i]));
                //yFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[1][i]));
                //zFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[2][i]));
                xFftPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_fft_array.get(0).get(i)));
                yFftPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_fft_array.get(1).get(i)));
                zFftPoints.add(new DataPoint(data_fft_array.get(3).get(i), data_fft_array.get(2).get(i)));
            }

            LineGraphSeries<DataPoint> xSeries = new LineGraphSeries<>(xPoints.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> ySeries = new LineGraphSeries<>(yPoints.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> zSeries = new LineGraphSeries<>(zPoints.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> xFftSeries = new LineGraphSeries<>(xFftPoints.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> yFftSeries = new LineGraphSeries<>(yFftPoints.toArray(new DataPoint[0]));
            LineGraphSeries<DataPoint> zFftSeries = new LineGraphSeries<>(zFftPoints.toArray(new DataPoint[0]));

            xSeries.setColor(Color.BLUE);
            ySeries.setColor(Color.RED);
            zSeries.setColor(Color.BLACK);
            xFftSeries.setColor(Color.BLUE);
            yFftSeries.setColor(Color.RED);
            zFftSeries.setColor(Color.BLACK);

            data_output1.removeAllSeries(); // Effacez les séries précédentes
            data_output1.addSeries(xFftSeries);
            data_output1.addSeries(yFftSeries);
            data_output1.addSeries(zFftSeries);
            data_output1.addSeries(xSeries);
            data_output1.addSeries(ySeries);
            data_output1.addSeries(zSeries);

            data_output1.getViewport().setYAxisBoundsManual(true);
            data_output1.getViewport().setMinY(-150);
            data_output1.getViewport().setMaxY(20);

            data_output1.getViewport().setScalable(true);
            data_output1.getViewport().setScalableY(true);

            data_output1.invalidate();
        }
    };
    public final Runnable print_data = new Runnable() {
        @Override
        public void run() {
            //OutputX.setText(String.valueOf(data_sensor_array[0][0]));
            //OutputY.setText(String.valueOf(data_sensor_array[1][0]));
            //OutputZ.setText(String.valueOf(data_sensor_array[2][0]));
           OutputX.setText(String.valueOf(data_sensor_array.get(0).get(0)));
           OutputY.setText(String.valueOf(data_sensor_array.get(1).get(0)));
           OutputZ.setText(String.valueOf(data_sensor_array.get(2).get(0)));
            data_output_label.setText("");
            data_output_label.append(
                    "X : f : " + maxFrequency[0][1] + "Hz, v : " + maxFrequency[0][0] + "dB" + "\n" +
                            "Y : f : " + maxFrequency[1][1] + "Hz, v : " + maxFrequency[1][0] + "dB" + "\n" +
                            "Z : f : " + maxFrequency[2][1] + "Hz, v : " + maxFrequency[2][0] + "dB" + "\n" +
                            "Fe : " + samplingFrequency + "Hz");
            if (!flag2) {
                setConfiguration();
                flag2 = true;
            }
            if (flag != s_flag) {
                setConfiguration();
                s_flag = flag;
            }
        }
    };


/*-------------------------------------------------------------------------data analyse implement-------------------------------------------------*/

    //data analyse listener implementation

    @SuppressLint("SetTextI18n")
    @Override
    public void analyseDone(boolean status) {
        button_param.setText("DONE - ANALYSE");
        Toast.makeText(getApplicationContext(), "Analyse Done", Toast.LENGTH_SHORT).show();
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void analyseResult(List<data> result, HashMap<String,List> signalResultAnalyse) {
        analyse_result.setText("");
        Integer cnt = 0,cnt1 = 0;
        if (signalResultAnalyse.containsKey("signals")) {
            List<Integer> res = signalResultAnalyse.get("signals");
            for (Integer Val : res) {
                if (Val == 1) {
                    cnt++;
                    analyse_result.append("@ : " + cnt1*samplingFrequency/(2*res.size()) + "\n");
                }
                cnt1++;
            }
        }
        analyse_result.append("The result is : \n" + "Number Of Pics : " + cnt +"\n");
        for (data resultdata:result){
            //String truncatedValue = String.valueOf(resultdata.getValue()).substring(0, Math.min(String.valueOf(resultdata.getValue()).length(), 8));
            analyse_result.append(resultdata.getId() +  resultdata.getValue() + "\n");
            button_param.setText("RE-DO - ANALYSE");
        }
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void analysePossible() {
        analyseData = false;                        //stop collecting data
        button_param.setText("READY - ANALYSE");
        analyse_result.setText("Ready ...  : \n");
        Toast.makeText(getApplicationContext(), "Analyse Possible now", Toast.LENGTH_SHORT).show();
    }

    /*--------------------------------------------------------------------------------save/get preferences ----------------------------------------------------------*/
    private void parseDataPreferences(Map<String,?> dataSaved){
        if(dataSaved != null){
            for (Map.Entry<String, ?> entry : dataSaved.entrySet()) {
                switch (entry.getKey()){
                    case "rpmConfiguration":
                        rpmConfiguration = (double)((float)entry.getValue());
                        velocityTracking.setEstimatedVelocity(rpmConfiguration);
                        break;
                    case "powerConfiguration":
                        powerConfiguration = (double) ((float)entry.getValue());
                        break;

                    case "bearingBallNumberConfiguration":
                        bearingBallNumberConfiguration = (int)entry.getValue();
                        break;
                    case "bearingPitchConfiguration":
                        bearingPitchConfiguration = (double)(float)entry.getValue();
                        break;
                    case "bearingBallDiamConfiguration":
                        bearingBallDiamConfiguration = (double)(float)entry.getValue();
                        break;
                    case "bearingAngleConfiguration":
                        bearingAngleConfiguration = (double)(float)entry.getValue();
                        break;

                    case "noiseCoefficientConfiguration":
                        noiseCoefficientConfiguration = (double)(float)entry.getValue();
                        break;
                    case "powerCoefficientConfiguration":
                        powerCoefficientConfiguration = (double)(float)entry.getValue();
                        break;
                    case "lag":
                        lag = (int)entry.getValue();
                        break;
                    case "threshold":
                        threshold = (double)(float)entry.getValue();
                        break;
                    case "influence":
                        influence = (double)(float)entry.getValue();
                        break;
                    case "line":
                        lineFrequency = (double)(float)entry.getValue();
                        break;
                    case "resolution":
                        analyseBuffer = (int)entry.getValue();
                        break;
                    case "resolutionbase":
                        data_lenght = (int)entry.getValue();
                        break;

                    case "SW1":
                        switchConfiguration[0]= (boolean)entry.getValue();
                        break;
                    case "SW2":
                        switchConfiguration[1]= (boolean)entry.getValue();
                        break;
                    case "SW3":
                        switchConfiguration[2]= (boolean)entry.getValue();
                        break;
                    case "SW4":
                        switchConfiguration[3]= (boolean)entry.getValue();
                        break;
                    case "SW5":
                        switchConfiguration[4]= (boolean)entry.getValue();
                        break;
                    case "SW6":
                        switchConfiguration[5]= (boolean)entry.getValue();
                        break;

                    default:
                        break;
                }
            }
        }
    }
    private void sendDataPreferences(){


        preferences.writePreferences(getApplicationContext(),"rpmConfiguration",rpmConfiguration);
        preferences.writePreferences(getApplicationContext(),"powerConfiguration",powerConfiguration);

        preferences.writePreferences(getApplicationContext(),"bearingBallNumberConfiguration", bearingBallNumberConfiguration);
        preferences.writePreferences(getApplicationContext(),"bearingPitchConfiguration", bearingPitchConfiguration);
        preferences.writePreferences(getApplicationContext(),"bearingBallDiamConfiguration", bearingBallDiamConfiguration);
        preferences.writePreferences(getApplicationContext(),"bearingAngleConfiguration", bearingAngleConfiguration);

        preferences.writePreferences(getApplicationContext(),"noiseCoefficientConfiguration",noiseCoefficientConfiguration);
        preferences.writePreferences(getApplicationContext(),"powerCoefficientConfiguration",powerCoefficientConfiguration);

        preferences.writePreferences(getApplicationContext(),"lag",lag);
        preferences.writePreferences(getApplicationContext(),"threshold",threshold);
        preferences.writePreferences(getApplicationContext(),"influence",influence);

        preferences.writePreferences(getApplicationContext(),"line",lineFrequency);

        preferences.writePreferences(getApplicationContext(),"resolution",analyseBuffer);
        preferences.writePreferences(getApplicationContext(),"resolutionbase",data_lenght);

        preferences.writePreferences(getApplicationContext(),"SW1",switchConfiguration[0]);
        preferences.writePreferences(getApplicationContext(),"SW2",switchConfiguration[1]);
        preferences.writePreferences(getApplicationContext(),"SW3",switchConfiguration[2]);
        preferences.writePreferences(getApplicationContext(),"SW4",switchConfiguration[3]);
        preferences.writePreferences(getApplicationContext(),"SW5",switchConfiguration[4]);
        preferences.writePreferences(getApplicationContext(),"SW6",switchConfiguration[5]);
    }

    /*------------------------------------------------------------------------------- velocity tracking manual------------------------------------------------------*/
    @Override
    public void velocityResult(List<List<DataPoint>> returnValue) {
        // function i will define -----  /
        velocityTrackResultSend(returnValue);
    }
    public void velocityTrackResultSend(List<List<DataPoint>> returnValue){
        TrackVelocity.velocityTrackResul(returnValue);
        //Toast.makeText(getApplicationContext(), "Good for result", Toast.LENGTH_LONG).show();
    }

    /*--------------------------------------------------------------------------------open velocity tyracking-----------------------------------------------------*/
    private void closeSetting(){
        // Begin the transaction for Fragment1
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
        BlankFragment fragment1 = new BlankFragment();
        fragmentTransaction1.replace(R.id.fragmentContainer, fragment1);
        fragmentTransaction1.commit();
        FragmentTransaction fragmentTransaction2 = fragmentManager.beginTransaction();
        TrackVelocity fragment2 = new TrackVelocity();
        fragmentTransaction2.replace(R.id.fragmentContainerVelocity, fragment2);
        fragmentTransaction2.commit();
    }

    @Override
    public void clearData() {
        if (analyseData){
            analyseData = false;
            button_param.setText("RE-DO - ANALYSE");
        }
        analyse_result.setText("");
    }
    /*--------------------------------------------------------------------------------not used function -----------------------------------------------------------*/


}



