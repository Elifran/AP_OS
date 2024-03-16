package aina.elifran.um5.ensam.ap_os.placeholder;

import androidx.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;



public class dataAnalyse {
    private double samplingFrequency,rpmConfiguration,powerConfiguration;
    private int bearingConfiguration;
    private boolean[] switchConfiguration;
    private double[] dataFftArray;
    private double[] dataMesureArray;
    private long[] dataTimeArray;
    private int data_buffer;
    private boolean analyseStatus = false;
    long counter;
    dataAnalyse(int buffer, double sampling_frequency, double rpm_Configuration,double power_Configuration, int bearing_Configuration , boolean[] switch_Configuration){
        data_buffer = buffer;
        samplingFrequency = sampling_frequency;
        rpmConfiguration = rpm_Configuration;
        powerConfiguration = power_Configuration;
        bearingConfiguration = bearing_Configuration;
        switchConfiguration = switch_Configuration.clone();

    // initialize all array :
        dataMesureArray = new double[data_buffer];
        dataFftArray = new double[data_buffer];
        dataTimeArray = new long[data_buffer];

    }
    public boolean beginAnalyse(){
        boolean analysePossible;
        if (counter > data_buffer)
            analysePossible = true;
        else
            analysePossible = false;

        return analysePossible;
    }
    public void addData(double data, long time){
        shiftRight(dataMesureArray,dataTimeArray,data,time);
        counter++;      // counte any data
    }
    private void shiftRight(@NonNull double[] dataArray , long[] time, double dataIn , long timestamp){
        for (int i = data_buffer - 1; i > 0; i--){
            dataArray[i] = dataArray[i-1];
            time[i] = time[i-1] + timestamp;
        }
        time[0] = 0;
    }
    private final Runnable dataAnalyse = new Runnable() {
        @Override
        public void run() {
            List<DataPoint> datafrequency0 = new ArrayList<>();
            double[] data = dataFftArray.clone();

            for( int i = 0; i<samplingFrequency + 1 ; i+= (int) (samplingFrequency*60/rpmConfiguration)){
                double[] TempData = getMaxAnalyse(data);
                datafrequency0.add(new DataPoint(TempData[0],dataFftArray[(int)TempData[1]]));
                if((int)TempData[1] > 10)
                    for (int j = (int)TempData[1]-10;j<(int)TempData[1]+10;j++) data[j] = 1E-100;
                else
                    for (int j = 0;j<(int)TempData[1]+10;j++) data[j] = 1E-100;
            }
            // static analyse
            if(switchConfiguration[0]){     // static vibration unbalanced
                for (int i = 0; i < datafrequency0.size(); i++) {
                    if (Math.abs(datafrequency0.get(i).getY() - rpmConfiguration/60.0)  < 1.0) {
                        // there are static  default

                    }
                }
            }
            if(switchConfiguration[1]){     //dynamic vibration unbalanced
                for (int i = 0; i < datafrequency0.size(); i++) {
                    if (Math.abs(datafrequency0.get(i).getY() - 2.0 * rpmConfiguration/60.0)  < 1.0) {
                        // there are dynamic default

                    }
                }}
            if(switchConfiguration[2]){     //mecanical loosness
            }
            if(switchConfiguration[3]){     //bearing fault
            }
            if(switchConfiguration[4]){     //electrical or mecanical default
                for (int i = 0; i < datafrequency0.size(); i++) {
                    if (Math.abs(datafrequency0.get(i).getY() - 100.0)  < 0.1) {
                        // there are electrical default

                    }
                }
            }
        }

    };
    public double[] getMaxAnalyse(@NonNull double[] data){
        double max =-1.0E100;
        int pos = 0;
        for(int i = 0; i <data.length/2;i++){
            if(max < data[i]){
                max = data[i];
                pos = i;
            }
        }
        return (new double[]{max,pos});
    }


}
