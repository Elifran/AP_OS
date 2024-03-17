package aina.elifran.um5.ensam.ap_os;

import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;


public class dataAnalyse {
    private double samplingFrequency,rpmConfiguration,powerConfiguration;
    private final double powerCoefficient = 0.142;  // the vibration amplitude coefficient
    private int bearingConfiguration;
    private boolean[] switchConfiguration;
    private double[] dataFftArray;
    private double[] dataMesureArray;
    private long[] dataTimeArray;
    private int data_buffer;
    private boolean analyseStatus = false;
    private fft dataAnalyseFft;
    long counter;
    private analyseDoneListener listener;
    List<data> analyseResultData = new ArrayList<>();
    private Handler analyseHandler = new Handler(Looper.myLooper());;
    dataAnalyse(@NonNull int buffer,
                @NonNull double sampling_frequency,
                @NonNull double rpm_Configuration,
                @NonNull double power_Configuration,
                @NonNull int bearing_Configuration ,
                @Nullable boolean[] switch_Configuration) {
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
        dataAnalyseFft = new fft(data_buffer);

    }

    public void setConfig(String Config,
                          Object Value){
        switch (Config){
            case "SAMPLING FREQ":
                //counter = 0;
                                                    /* this might take alot of time if the mobile phone is not stable
                                                    *  reset the data to avoid som error on calculating frequenncy
                                                    *
                                                    * */
                samplingFrequency = (double) Value;
                break;
            case "RPM":
                rpmConfiguration = (double) Value;
                break;
            case "POWER" :
                powerConfiguration = (double) Value;
                break;
            case "BEARING":
                bearingConfiguration = (int) Value;
                break;
            case "SWITCH":
                switchConfiguration = (boolean[]) Value;
            default:
                break;
        }
    }
    /*-------------------------------------------------------------- analyse status interface to give data ----------------------------------------------------------*/
    // analyse status listener on done
    public interface analyseDoneListener {
        void analyseDone(boolean status);
        void analyseResult(List<data> result);
        void analysePossible();
    }

    public void setAnalyseDoneListener(analyseDoneListener listener) {
        this.listener = listener;
    }
    private void isDone(boolean status, List<data> result){
        listener.analyseDone(status);
        listener.analyseResult(result);
    }

    /*----------------------------------------------------------------------------- ask status ------------------------------------------------------------------------*/
    public boolean isAnalizing(){return analyseStatus;};

    /*----------------------------------------------------------------------------- adding data -----------------------------------------------------------------------*/
    public void addData(double data, long timeStamp){ // insert data to the buffer befor analyse
        shiftRight(dataMesureArray,dataTimeArray,data,timeStamp);
        counter++;      // counte any data
        if(counter ==(int) (data_buffer +data_buffer*0.1))
            listener.analysePossible();
    }

    /*--------------------------------------------------------------------  begin the analyse of the data -------------------------------------------------------------*/
    public boolean beginAnalyse(){
        boolean analysePossible;
        if (counter > data_buffer && analyseStatus == false){
            analyseStatus = true; // analyse is running
            analyseHandler.post(dataAnalyse);
            analysePossible = true;
        }
        else
            analysePossible = false;
        return analysePossible;
    }

    /*------------------------------------------------------------------------- move data to to the left --------------------------------------------------------------*/
    private void shiftRight(@NonNull double[] dataArray , long[] arrayTime, double dataIn , long timestamp){
        for (int i = data_buffer - 1; i > 0; i--){
            dataArray[i] = dataArray[i-1];
            arrayTime[i] = arrayTime[i-1] + timestamp;
        }
        arrayTime[0] = 0;
        dataArray[0] = dataIn;
    }

    /*----------------------------------------------------------------------------- data analyse task -----------------------------------------------------------------*/
    private final Runnable dataAnalyse = new Runnable() {
        @Override
        public void run() {
            analyseResultData.clear();
            int freqShift = (int) (data_buffer*0.1/samplingFrequency);         // 0.1Hz shift frequency
            dataFftArray  = dataAnalyseFft.getAbsfft(dataMesureArray.clone()); // get the absolute value of the fft
            double[] data = dataFftArray.clone();

            /*______________________________________________ static default _________________________________________*/
            if(switchConfiguration[0]){     // static vibration unbalanced
                int freqCentred =(int) (data_buffer*rpmConfiguration/(60*2*samplingFrequency));
                double[] data1  =  getMaxAnalyse(data,freqCentred - freqShift,freqCentred + freqShift);
                analyseResultData.add(new data("Static Vibration State ----> ",data1[0] * 100*powerCoefficient/powerCoefficient));
            }
            /*_____________________________________________ dynamic default _________________________________________*/
            if(switchConfiguration[1]){     //dynamic vibration unbalanced
                int freqCentred =(int) (data_buffer*rpmConfiguration/(60*samplingFrequency));
                double[] data1  =  getMaxAnalyse(data,freqCentred - freqShift,freqCentred + freqShift);
                analyseResultData.add(new data("Dynamic Vibration State ---> ",data1[0] * 100*powerCoefficient/powerCoefficient));
            }
            /*__________________________________________ mechanical  looseness ______________________________________*/
            if(switchConfiguration[2]){     //mechanical looseness
                ;
            }
            /*______________________________________________ bearing default _________________________________________*/
            if(switchConfiguration[3]){     //bearing fault
                double[][] dataTep = new double[4][2];
                for (int i = 0; i < 3;i++){
                    int freqCentred =(int) (data_buffer*rpmConfiguration*(bearingConfiguration - 1 + i)/(60*2*samplingFrequency));
                    dataTep[0]  =  getMaxAnalyse(data,freqCentred - freqShift,freqCentred + freqShift);
                }
                double Temp = 0.0;
                for (int i =  0 ; i<3 ; i++){
                    Temp+= Math.pow(dataTep[i][0],2);
                }
                Temp = Math.sqrt(Temp);
                analyseResultData.add(new data("Bearing default State -----> ", Temp*100*powerCoefficient/powerCoefficient));
            }
            /*______________________________________________ magnet default _________________________________________*/
            if(switchConfiguration[4]){     //electrical or mechanical default
                int freqCentred =(int) (50);
                double[] data1  =  getMaxAnalyse(data,freqCentred - freqShift,freqCentred + freqShift);
                analyseResultData.add(new data("Bobine State --------------> ",data1[0] * 100*powerCoefficient/powerCoefficient));
            }

/*----------------------------------------------------------**------------------------------------------------------------*/
            isDone(true,analyseResultData);     // send to the interface that the analyse is done
            analyseStatus = false;                    // analyse done
            counter = 0;                              //reset the counter
/*----------------------------------------------------------**------------------------------------------------------------*/
        }};
    public double[] getMaxAnalyse(@NonNull double[] data, int low, int hight){
        double max =-1.0E100;
        int pos = 0;
        for(int i = low; i <hight;i++){
            if(max < data[i]){
                max = data[i];
                pos = i;
            }
        }
        return (new double[]{max,pos});
    }


}
