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

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import aina.elifran.um5.ensam.ap_os.velocityTracking;


public class MainActivity extends AppCompatActivity implements SensorEventListener, MenuFragment.OnDataChangeListener,dataAnalyse.analyseDoneListener, velocityTracking.velocityTrackingInterface {
    int counter = 0;
    private final double vibrationConstante = 0.285;
    private TextView OutputX, OutputY, OutputZ,data_output_label,analyse_result;
    private Button button_stop,button_param;
    private GraphView data_output1;
    private XYPlot data_output;
    private ViewGroup control_layout,chart_layout,output_control_values_layout,command_layout,output_label,output_content;
    LinearLayout command_layout_setparams;
    SensorManager sensorManager;
    Sensor OutputSensor;
    private final int data_leingh = 2048*2; // min 256
    private final int print_scale = 512;
    private volatile double[][] data_sensor_array;
    private volatile  double[][] data_fft_array;
    public static long act_timestamp, last_timestamp,step_time;
    private static int data_counter = 0;
    private boolean flag = false, flag2 = false, s_flag, ready = false,filterStatus = false;
    private fft fftdata;
    private boolean[] switchConfiguration;
    private static double rpmConfiguration = 1500;
    private static double powerConfiguration = 15;
    private static double powerCoefficientConfiguration = 10E+0;
    private static double noiseCoefficientConfiguration = -140;
    private int bearingConfiguration = 12;
    private double samplingFrequency;
    private double[][] maxFrequency;
    private filter Xfilterdata;
    private filter Yfilterdata;
    private filter Zfilterdata;
    private static final int filterOrder = 50;
    private static final double cutOffFrequency = 0.45; // must be less than 0.5
    private Handler dofftHandler,doplotHandler,doprintHandler;
    boolean analyseData = false;
    int analyseBuffer = 2048*16;
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
        command_layout_setparams = findViewById(R.id.command_layout);
        output_content = findViewById(R.id.output_content);
        output_label = findViewById(R.id.output_label);
        analyse_result = findViewById(R.id.analyse_result);

        data_sensor_array = new double[4][data_leingh];
        data_fft_array = new double[4][data_leingh];
        fftdata = new fft(data_leingh);
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
        setupView();
        setupButton();
        setConfiguration();

            }
    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        float[] actSensorValues = event.values.clone();
        act_timestamp = (event.timestamp)/1000000; // in millisecond
        step_time = act_timestamp - last_timestamp;
        data_counter +=1;
        shiftRight(data_sensor_array,step_time);
        data_sensor_array[3][0] = step_time;
        last_timestamp = act_timestamp;

        data_sensor_array[0][0] =Math.sqrt(Math.pow(event.values[0],2) + Math.pow(event.values[1],2) + Math.pow(event.values[2],2));
        data_sensor_array[1][0] =event.values[1];
        data_sensor_array[2][0] =event.values[2];

        if (ready){
            //data_sensor_array[0][0] =Yfilterdata.filterData(actSensorValues[0]);
            //data_sensor_array[1][0] =Yfilterdata.filterData(actSensorValues[1]);
            //data_sensor_array[2][0] =Zfilterdata.filterData(actSensorValues[2]);

            if (analyseData){ // trying to  analyse the absolute value of the vibration from x,y and z
                dataAnalyseVar.addData(data_sensor_array[0][0],step_time);
                /*dataAnalyseVar.addData(Math.sqrt(
                                Math.pow(data_sensor_array[0][0], 2) +
                                Math.pow(data_sensor_array[1][0], 2) +
                                Math.pow(data_sensor_array[2][0], 2)),step_time);*/  // asume that we analyse the first vibration data
            }
            if((    Xfilterdata.isConfigChange(samplingFrequency,cutOffFrequency* samplingFrequency) ||
                    Yfilterdata.isConfigChange(samplingFrequency,cutOffFrequency* samplingFrequency) ||
                    Xfilterdata.isConfigChange(samplingFrequency,cutOffFrequency* samplingFrequency) ) && flag){
                dataAnalyseVar.setConfig("SAMPLING FREQ", samplingFrequency);
                //Toast.makeText(getApplicationContext(), "Configuration filter Have been Changed", Toast.LENGTH_LONG).show();
            }
        }
        else {
            //data_sensor_array[0][0] =event.values[0];
            //data_sensor_array[1][0] =event.values[1];
            //data_sensor_array[2][0] =event.values[2];
                if(counter > data_leingh*1.2){
                    if(!filterStatus){
                        Xfilterdata = new filter(filterOrder, samplingFrequency,cutOffFrequency*samplingFrequency);
                        Yfilterdata = new filter(filterOrder, samplingFrequency,cutOffFrequency*samplingFrequency);
                        Zfilterdata = new filter(filterOrder, samplingFrequency,cutOffFrequency*samplingFrequency);
                        filterStatus = true;        // avoid recreation of the filter class
                        Toast.makeText(getApplicationContext(), "Filter initialized at Fe :" + samplingFrequency + "Hz", Toast.LENGTH_LONG).show();

                        dataAnalyseVar = new dataAnalyse(analyseBuffer, samplingFrequency,rpmConfiguration,powerConfiguration,bearingConfiguration,switchConfiguration, powerCoefficientConfiguration,noiseCoefficientConfiguration);
                        dataAnalyseVar.setAnalyseDoneListener(this);

                    }
                    if (    Xfilterdata.isCreated() &&
                            Yfilterdata.isCreated() &&
                            Zfilterdata.isCreated()){
                        button_param.setText("READY - COLLECT");
                        ready = true;}
                }
                else {
                    counter++;
                }
        }
        if(data_counter > data_leingh/print_scale) {
            if (flag && ready) {
                dofftHandler.post(getfftAbs);
                //doplotHandler.post(data_plot);   //doplotHandler in other way
                doplotHandler.post(data_plot1);   //doplotHandler in other way
            }
            data_counter = 0;
            doplotHandler.post(print_data);      //doprintHandler in other way
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
                    bearingSetting((int)data.getValue());
                    break;
                case "POWER COEFFICIENT" :
                    powerCoefficientSetting((double)data.getValue());
                    break;
                case "NOISE" :
                    noiseCoefficientSetting((double)data.getValue());
                    break;
                default:
                    break;
            }
            if(ready){
                dataAnalyseVar.setConfig(data.getId(),data.getValue());
            }
        }
            sendDataPreferences();
            Toast.makeText(getApplicationContext(),"Configuration Saved", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void trackData(double rpm) {
        rpmConfiguration = rpm;
        trackVelocity = true;
        closeSetting();
    }
    public void getFromTracking(double selectedVelocity){
        if (!Double.isNaN(selectedVelocity)){
            rpmSetting(selectedVelocity);
            Toast.makeText(getApplicationContext(),"Velocity Set at : " + rpmConfiguration ,Toast.LENGTH_SHORT).show();
        }
        else
        Toast.makeText(getApplicationContext(),"Your selection is not a number, Velocity set at : " + rpmConfiguration,Toast.LENGTH_SHORT).show();
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
                Value = bearingConfiguration;
                break;
            case "POWER COEFFICIENT":
                Value = powerCoefficientConfiguration;
                break;
                case "NOISE":
                Value = noiseCoefficientConfiguration;
                break;

            default:
                break;
        }
        return Value;
    }
    private void setupButton(){
        //resume button
        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(!ready)
                    Toast.makeText(getApplicationContext(), "Not Ready Yet", Toast.LENGTH_SHORT).show();
                else{
                    flag = !flag;
                    button_stop.setText(flag ? "STOP" : "RESUME");
                }
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
            command_layout_setparams.setOrientation(LinearLayout.VERTICAL);
            button_stop_params.width = command_params.width;
            button_param_params.width = command_params.width;
        }else {
            command_layout_setparams.setOrientation(LinearLayout.HORIZONTAL);
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
    public double[] toAc(@NonNull double[] data){
        double[] temp = data.clone();
        double val = 0;
        for (double datum : data) val = val + datum;
        val = val/(data.length);
        for (int i = 0;i<data.length;i++) {
            temp[i] = data[i] - val;
        }
        return temp;
    }
    public double[] getMax(@NonNull double[] data){
        double max =-1.0E100;
        int pos = 0;
            for(int i = 0; i <data.length/2;i++){
                   if(max < data[i]){
                       max = data[i];
                       pos = i;
                   }
            }
        return new double[]{max,data_fft_array[3][pos]};
    }
    private void shiftRight(@NonNull double[][] array, double timestamp){
        for (int i = data_leingh - 1; i > 0; i--){
            array[0][i] = array[0][i-1];
            array[1][i] = array[1][i-1];
            array[2][i] = array[2][i-1];
            array[3][i] = array[3][i-1] + timestamp;
        }
        array[3][0] = 0;
        samplingFrequency = 1000*data_leingh/data_sensor_array[3][data_leingh-1];
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
    private void powerSetting(double powerData){
        powerConfiguration = powerData;
    }
    private void bearingSetting(int bearingNumber){
        bearingConfiguration = bearingNumber;
    }

    private final Runnable  getfftAbs = new Runnable() {
        @Override
        public void run() {
            double resolution = samplingFrequency /data_leingh;
            for (int i = 0;i<data_leingh;i++){
                //data_fft_array[3][i+1] = data_sensor_array[3][i+1];
                data_fft_array[3][i] = resolution*i;
            }
            //data_fft_array[3][0] = 0;
            data_fft_array[0] = fftdata.getLogtfft(toAc(data_sensor_array[0]));
            data_fft_array[1] = fftdata.getLogtfft(toAc(data_sensor_array[1]));
            data_fft_array[2] = fftdata.getLogtfft(toAc(data_sensor_array[2]));

            if (trackVelocity){
                velocityTracking.addDataTrack(data_fft_array[0],data_fft_array[1],data_fft_array[2],samplingFrequency);
            }
            maxFrequency[0] = getMax(data_fft_array[0]);
            maxFrequency[1] = getMax(data_fft_array[1]);
            maxFrequency[2] = getMax(data_fft_array[2]);

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
            for (int i = 0; i < data_leingh/2; i++) {
                xPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[0][i*2]));
                yPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[1][i*2]));
                zPoints.add(new DataPoint(data_fft_array[3][i], data_sensor_array[2][i*2]));

            }
            for (int i = 0; i < data_leingh / 2; i++) {
                xFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[0][i]));
                yFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[1][i]));
                zFftPoints.add(new DataPoint(data_fft_array[3][i], data_fft_array[2][i]));
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
            OutputX.setText(String.valueOf(data_sensor_array[0][0]));
            OutputY.setText(String.valueOf(data_sensor_array[1][0]));
            OutputZ.setText(String.valueOf(data_sensor_array[2][0]));
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
    public void analyseResult(List<data> result) {
        analyse_result.setText("The result is : \n");
        for (data resultdata:result){
            //String truncatedValue = String.valueOf(resultdata.getValue()).substring(0, Math.min(String.valueOf(resultdata.getValue()).length(), 8));
            analyse_result.append(resultdata.getId() +  resultdata.getValue() + "% \n");
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
                    case "bearingConfiguration":
                        bearingConfiguration = (int)entry.getValue();
                        break;
                    case "noiseCoefficientConfiguration":
                        noiseCoefficientConfiguration = (double)(float)entry.getValue();
                        break;
                    case "powerCoefficientConfiguration":
                        powerCoefficientConfiguration = (double)(float)entry.getValue();
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
        preferences.writePreferences(getApplicationContext(),"bearingConfiguration",bearingConfiguration);
        preferences.writePreferences(getApplicationContext(),"noiseCoefficientConfiguration",noiseCoefficientConfiguration);
        preferences.writePreferences(getApplicationContext(),"powerCoefficientConfiguration",powerCoefficientConfiguration);

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
    /*--------------------------------------------------------------------------------not used function -----------------------------------------------------------*/
    private final Runnable data_plot = new Runnable() {
        @Override
        public void run() {
            List<Number> times = new ArrayList<>();
            List<Number> xValues = new ArrayList<>();
            List<Number> yValues = new ArrayList<>();
            List<Number> zValues = new ArrayList<>();
            List<Number> xfftValues = new ArrayList<>();
            List<Number> yfftValues = new ArrayList<>();
            List<Number> zfftValues = new ArrayList<>();
            List<Number> fftfrequency = new ArrayList<>();
            // Populate xValues and yValues from your data_sensor_array
            for (int i = 0; i < data_leingh; i++) {

                xValues.add(data_sensor_array[0][i]); // output x
                yValues.add(data_sensor_array[1][i]); // output y
                zValues.add(data_sensor_array[2][i]); // output z
                times.add(data_sensor_array[3][i]); // time

            }
            for (int i = 0; i < data_leingh / 2; i++) {
                xfftValues.add(data_fft_array[0][i]); // output fft
                yfftValues.add(data_fft_array[1][i]); // output fft
                zfftValues.add(data_fft_array[2][i]); // output fft
                //fftfrequency.add(data_fft_array[3][i*2]); // output fft frequency
                fftfrequency.add(data_sensor_array[3][i * 2]); // output fft frequency
            }
            XYSeries Xseries = new SimpleXYSeries(times, xValues, ""/*"Output x"*/);
            XYSeries Yseries = new SimpleXYSeries(times, yValues, ""/*"Output y"*/);
            XYSeries Zseries = new SimpleXYSeries(times, zValues, ""/*"Output z"*/);
            LineAndPointFormatter Xformatter = new LineAndPointFormatter(Color.BLUE, null, null, null);
            LineAndPointFormatter Yformatter = new LineAndPointFormatter(Color.RED, null, null, null);
            LineAndPointFormatter Zformatter = new LineAndPointFormatter(Color.BLACK, null, null, null);

            XYSeries xFFTseries = new SimpleXYSeries(fftfrequency, xfftValues, ""/*"Output x"*/);
            XYSeries yFFTseries = new SimpleXYSeries(fftfrequency, yfftValues, ""/*"Output y"*/);
            XYSeries zFFTseries = new SimpleXYSeries(fftfrequency, zfftValues, ""/*"Output z"*/);
            LineAndPointFormatter xFFTformatter = new LineAndPointFormatter(Color.BLUE, null, null, null);
            LineAndPointFormatter yFFTformatter = new LineAndPointFormatter(Color.RED, null, null, null);
            LineAndPointFormatter zFFTformatter = new LineAndPointFormatter(Color.BLACK, null, null, null);

            data_output.clear();        // clear the output data
//1
            data_output.addSeries(xFFTseries, xFFTformatter);
            data_output.addSeries(yFFTseries, yFFTformatter);
            data_output.addSeries(zFFTseries, zFFTformatter);
            data_output.addSeries(Xseries, Xformatter);
            data_output.addSeries(Yseries, Yformatter);
            data_output.addSeries(Zseries, Zformatter);
            data_output.setRangeBoundaries(-150, 20, BoundaryMode.FIXED);
            PanZoom.attach(data_output, PanZoom.Pan.BOTH, PanZoom.Zoom.STRETCH_BOTH);
            // Assuming data_output is your XYPlot object
//
            data_output.redraw(); // Refresh the plot
        }
    };


}



