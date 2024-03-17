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

public class MainActivity extends AppCompatActivity implements SensorEventListener, MenuFragment.OnDataChangeListener,dataAnalyse.analyseDoneListener {
    int couter = 0;
    private final double vibrationConstante = 0.285;
    private TextView OutputX, OutputY, OutputZ,data_output_label;
    private Button button_stop,button_param;
    private GraphView data_output1;
    private XYPlot data_output;
    private ViewGroup control_layout,chart_layout,output_control_values_layout,command_layout,output_label,output_content;
    LinearLayout command_layout_setparams;
    SensorManager sensorManager;
    Sensor OutputSensor;
    private final int data_leingh = 2048; // min 256
    private final int print_scale = 256;
    private volatile double[][] data_sensor_array;
    private volatile  double[][] data_fft_array;
    public static long act_timstamp,last_timstamp,step_time;
    private static int data_couter = 0;
    private boolean flag = false, flag2 = false, s_flag, ready = false,filterStatus = false;
    private fft ffftdata;
    private boolean[] switchConfiguration;
    private double rpmConfiguration = 1500 ,powerConfiguration = 15;
    private int bearingConfiguration = 12;
    private double samplingfrequency;
    private double[][] maxfreq;
    private filter Xfilterdata;
    private filter Yfilterdata;
    private filter Zfilterdata;
    private static final int filterOrder = 50;
    private static final double cutOffFrequency = 0.45; // must be less than 0.5
    private Handler dofftHandler,doplotHandler,doprintHandler;
    boolean analyseData = false;
    int analyseBuffer = 2048;
    dataAnalyse dataAnalyseVar;
    @SuppressLint("MissingInflatedId")
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

        data_sensor_array = new double[4][data_leingh];
        data_fft_array = new double[4][data_leingh];
        ffftdata = new fft(data_leingh);
        maxfreq = new double[3][2];
        switchConfiguration = new boolean[5];

        dofftHandler = new Handler(Looper.myLooper());
        doplotHandler = new Handler(Looper.myLooper());
        doprintHandler = new Handler(Looper.myLooper());


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        OutputSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, OutputSensor, SensorManager.SENSOR_DELAY_FASTEST);

        setupView();
        setupButton();
        setConfiguration();

            }
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        float[] actSensorValues = event.values.clone();
        act_timstamp = (event.timestamp)/1000000; // in millisecond
        step_time = act_timstamp - last_timstamp;
        data_couter+=1;
        shiftRight(data_sensor_array,step_time);
        data_sensor_array[3][0] = step_time;
        last_timstamp = act_timstamp;
        if (ready){
            data_sensor_array[0][0] =(float)(Xfilterdata.filterData(actSensorValues[0]));
            data_sensor_array[1][0] =(float)(Yfilterdata.filterData(actSensorValues[1]));
            data_sensor_array[2][0] =(float)(Zfilterdata.filterData(actSensorValues[2]));
            if (analyseData){
                dataAnalyseVar.addData(data_sensor_array[0][0],step_time);  // asume that we analyse the first vibration data
            }
            if(     (Xfilterdata.isConfigChange(samplingfrequency,cutOffFrequency*samplingfrequency) ||
                    Yfilterdata.isConfigChange(samplingfrequency,cutOffFrequency*samplingfrequency) ||
                    Xfilterdata.isConfigChange(samplingfrequency,cutOffFrequency*samplingfrequency) ) && flag){
                dataAnalyseVar.setConfig("SAMPLING FREQ",samplingfrequency);
                Toast.makeText(getApplicationContext(), "Configuration filter Have been Changed", Toast.LENGTH_LONG).show();

            }
        }
        else {
            data_sensor_array[0][0] =event.values[0];
            data_sensor_array[1][0] =event.values[1];
            data_sensor_array[2][0] =event.values[2];
                if(couter > data_leingh*2 + 1){
                    if(!filterStatus){
                        Xfilterdata = new filter(filterOrder,samplingfrequency,cutOffFrequency*samplingfrequency);
                        Yfilterdata = new filter(filterOrder,samplingfrequency,cutOffFrequency*samplingfrequency);
                        Zfilterdata = new filter(filterOrder,samplingfrequency,cutOffFrequency*samplingfrequency);
                        filterStatus = true;        // avoid recreation of the filter class
                        Toast.makeText(getApplicationContext(), "filter initialized at Fe :" + samplingfrequency + "Hz", Toast.LENGTH_LONG).show();

                        dataAnalyseVar = new dataAnalyse(analyseBuffer,samplingfrequency,rpmConfiguration,powerConfiguration,bearingConfiguration,switchConfiguration);
                        dataAnalyseVar.setAnalyseDoneListener(this);

                    }
                    if (Xfilterdata.isCreated() && Yfilterdata.isCreated() && Zfilterdata.isCreated()){
                        ready = true;}
                }
                else {
                    couter++;
                }
        }
        if(data_couter > data_leingh/print_scale) {
            if (flag && ready) {
                dofftHandler.post(getfftAbs);
                //doplotHandler.post(data_plot);   //doplotHandler in other way
                doplotHandler.post(data_plot1);   //doplotHandler in other way
            }
            data_couter = 0;
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

    // Implémenter la méthode onDataChanged pour modifier les données dans l'activité
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
                default:
                    break;
            }
            if(ready){
                dataAnalyseVar.setConfig(data.getId(),data.getValue());
            }
        }
            Toast.makeText(getApplicationContext(),"Configuration Saved", Toast.LENGTH_SHORT).show();
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
            public void onClick(View view) {
                if (!ready) {
                    Toast.makeText(getApplicationContext(), "Not Ready Yet", Toast.LENGTH_SHORT).show();
                }else {
                    if (!dataAnalyseVar.isAnalizing()){
                        if(!dataAnalyseVar.beginAnalyse()){
                            Toast.makeText(getApplicationContext(), "Please Wait Before Analysing", Toast.LENGTH_SHORT).show();
                            button_param.setText("WAIT PLS");
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Analyse Begin", Toast.LENGTH_SHORT).show();
                            button_param.setText("ANALISING ...");
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
        //Toast.makeText(getApplicationContext(), data_output_label.getMeasuredHeight() , Toast.LENGTH_SHORT).show();

        // control layout params
        data_output1_params.height = (int)(height - control_layout.getMeasuredHeight() - data_output_label.getMeasuredHeight() - height*0.01);
        data_output1_params.width = width;
        data_output1.setLayoutParams(data_output1_params);
        // control layout params
        //data_output_params.height = (int)(height - control_layout.getMeasuredHeight() - data_output_label.getMeasuredHeight() - height*0.01);
        //data_output_params.width = width;
        //data_output.setLayoutParams(data_output_params);
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
        samplingfrequency = 1000*data_leingh/data_sensor_array[3][data_leingh-1];
    }

    private void switchSetting(boolean[] switchData){
        switchConfiguration = switchData.clone();
    }
    private void rpmSetting(double rpmData){
        rpmConfiguration = rpmData;
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
            double resolution = samplingfrequency/data_leingh;
            for (int i = 0;i<data_leingh;i++){
                //data_fft_array[3][i+1] = data_sensor_array[3][i+1];
                data_fft_array[3][i] = resolution*i;
            }
            //data_fft_array[3][0] = 0;
            data_fft_array[0] = ffftdata.getLogtfft(toAc(data_sensor_array[0]));
            data_fft_array[1] = ffftdata.getLogtfft(toAc(data_sensor_array[1]));
            data_fft_array[2] = ffftdata.getLogtfft(toAc(data_sensor_array[2]));
            maxfreq[0] = getMax(data_fft_array[0]);
            maxfreq[1] = getMax(data_fft_array[1]);
            maxfreq[2] = getMax(data_fft_array[2]);
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
                    "X : f : " + maxfreq[0][1] + "Hz, v : " + maxfreq[0][0] + "dB" + "\n" +
                            "Y : f : " + maxfreq[1][1] + "Hz, v : " + maxfreq[1][0] + "dB" + "\n" +
                            "Z : f : " + maxfreq[2][1] + "Hz, v : " + maxfreq[2][0] + "dB" + "\n" +
                            "Fe : " + samplingfrequency + "Hz");
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

    @Override
    public void analyseDone(boolean status) {
    }

    @Override
    public void analyseResult(List<String> result) {
    }

    @Override
    public void analysePossible() {
        button_param.setText("ANALYSE");
        Toast.makeText(getApplicationContext(), "Analyse Possible now", Toast.LENGTH_SHORT).show();
    }


    /*--------------------------------------------------------------------------------not used function ----------------------------------------------------------*/
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



