package aina.elifran.um5.ensam.ap_os;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import aina.elifran.um5.ensam.ap_os.placeholder.SignalDetector;


public class dataAnalyse {
    private double samplingFrequency, rpmConfiguration, powerConfiguration;
    private double powerCoefficientConfiguration;  // the vibration amplitude coefficient
    private double noiseCoefficientConfiguration; // frequency outside the noise
    private final double coeffValue = 1; // frequency pic identification
    private final double frequencyShift = 0.1;
    private int lag;
    private Double threshold;
    private Double influence;
    private static int bearingConfiguration;
    private boolean[] switchConfiguration;

    private double[] dataFftArray;
    private double[] dataMesureArray;
    private long[] dataTimeArray;
    private List<DataPoint> dataFrequencysignificantArray;
    private final int data_buffer;
    private boolean analyseStatus = false;
    private final fft dataAnalyseFft;
    long counter;
    private analyseDoneListener listener;
    List<data> analyseResultData = new ArrayList<>();
    private final Handler analyseHandler;

    dataAnalyse(int buffer,
                double sampling_frequency,
                double rpm_Configuration,
                double power_Configuration,
                int bearing_Configuration,
                boolean[] switch_Configuration,
                double noise_Coefficient_Configuration,
                double power_Coefficient_Configuration,
                int lag,
                Double threshold,
                Double influence

                ) {
        data_buffer = buffer;
        samplingFrequency = sampling_frequency;
        rpmConfiguration = rpm_Configuration;
        powerConfiguration = power_Configuration;
        bearingConfiguration = bearing_Configuration;
        switchConfiguration = switch_Configuration.clone();
        noiseCoefficientConfiguration = noise_Coefficient_Configuration;
        powerCoefficientConfiguration  = power_Coefficient_Configuration;

        this.lag = lag;
        this.threshold = threshold;
        this.influence = influence;

        // initialize all array :
        dataMesureArray = new double[data_buffer];
        dataFftArray = new double[data_buffer / 2];
        dataTimeArray = new long[data_buffer];
        dataAnalyseFft = new fft(data_buffer);

        analyseHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    }

    public void setConfig(String Config,
                          Object Value) {
        switch (Config) {
            case "SAMPLING FREQ":
                //counter = 0;
                samplingFrequency = (double) Value;
                break;
            case "RPM":
                rpmConfiguration = (double) Value;
                break;
            case "POWER COEFFICIENT":
                powerCoefficientConfiguration = (double) Value;
                break;
            case "NOISE":
                noiseCoefficientConfiguration = (double)Value;
                break;
            case "POWER":
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

        void analyseResult(List<data> result, HashMap<String,List> signalResultAnalyse);

        void analysePossible();
    }

    public void setAnalyseDoneListener(analyseDoneListener listener) {
        this.listener = listener;
    }

    private void isDone(List<data> result, HashMap<String,List> signalResultAnalyse)  {
        listener.analyseDone(true);
        listener.analyseResult(result,signalResultAnalyse);
    }

    /*----------------------------------------------------------------------------- ask status ------------------------------------------------------------------------*/
    public boolean isAnalyzing() {
        return analyseStatus;
    }

    ;

    /*----------------------------------------------------------------------------- adding data -----------------------------------------------------------------------*/
    public void addData(double data, long timeStamp) { // insert data to the buffer befor analyse
        shiftRight(dataMesureArray, dataTimeArray, data, timeStamp);
        counter++;      // counte any data
        if (counter == (int) (data_buffer + data_buffer * 0.1))
            listener.analysePossible();
    }

    /*--------------------------------------------------------------------  begin the analyse of the data -------------------------------------------------------------*/
    public boolean beginAnalyse() {
        boolean analysePossible;
        if (counter > data_buffer && !analyseStatus) {
            analyseStatus = true; // analyse is running
            analyseHandler.post(dataAnalyse);
            analysePossible = true;
        } else
            analysePossible = false;
        return analysePossible;
    }

    /*------------------------------------------------------------------------- move data to to the left --------------------------------------------------------------*/
    private void shiftRight(@NonNull double[] dataArray, long[] arrayTime, double dataIn, long timestamp) {
        for (int i = data_buffer - 1; i > 0; i--) {
            dataArray[i] = dataArray[i - 1];
            arrayTime[i] = arrayTime[i - 1] + timestamp;
        }
        arrayTime[0] = 0;
        dataArray[0] = dataIn;
    }

    /*----------------------------------------------------------------------------- data analyse task -----------------------------------------------------------------*/
    private final Runnable dataAnalyse = new Runnable() {
        @Override
        public void run() {

            double[] fftArray = dataAnalyseFft.getAbsFft(toAc(dataMesureArray.clone()));
            List<Double> absFftValues = new ArrayList<>();
            for (int i = 0; i < (fftArray.length / 2); i++) {absFftValues.add(fftArray[i]);}
            HashMap<String, List> signalResultAnalyse = SignalDetector.analyzeDataForSignals(absFftValues,lag,threshold,influence);

            for (Map.Entry<String, List> entry : signalResultAnalyse.entrySet()) {
                String key = entry.getKey();
                List value = entry.getValue();
                String listAsString = value.toString();
                Log.d("HashMapContent", key + ": " + listAsString);
            }

            String ID = "";
            analyseResultData.clear();
            int freqShift = (int) (dataFftArray.length * frequencyShift / samplingFrequency);         //  shift frequency
            dataFftArray = cloneTrounce(dataAnalyseFft.getLogtfft(toAc(dataMesureArray.clone())),0.5); // get the absolute value of the fft
            dataFrequencysignificantArray = findSignificantPeaks(dataFftArray, noiseCoefficientConfiguration, coeffValue);
            List<DataPoint> dataFrequencyMultiple = dataFrequencyMultiple(dataFrequencysignificantArray, rpmConfiguration / 60.0);
            List<DataPoint> dataFrequencyMultiplebyhalf = dataFrequencyMultipleHalf(dataFrequencysignificantArray, rpmConfiguration / 60.0);
            List<DataPoint> dataFrequencyMultiplebyBearing = dataFrequencyMultipleBearing(dataFrequencysignificantArray, rpmConfiguration / 60.0);
            ID = "Number of pick frequency fond : ";
            for (DataPoint dataIn: dataFrequencysignificantArray){
                 ID += String.valueOf(dataIn.getY()) + " | " + String.valueOf(dataIn.getX()*samplingFrequency/data_buffer) + "\n";
            }
            analyseResultData.add(new data(ID, dataFrequencysignificantArray.size()));

            /*______________________________________________ static default _________________________________________*/
            if (switchConfiguration[0]) {     // static vibration unbalanced
                int freqCentred = (int) ((rpmConfiguration/60.0) * dataFftArray.length/(samplingFrequency));
                double[] data1 = getMaxAnalyse(dataFftArray, freqCentred - freqShift, freqCentred + freqShift);
                analyseResultData.add(new data("Static Vibration State ----> @ " + data1[1]*samplingFrequency/dataFftArray.length + ": ", data1[0]));//Math.pow(10, data1[0]/20) * powerCoefficientConfiguration / powerConfiguration));
            }
            /*_____________________________________________ dynamic default _________________________________________*/
            if (switchConfiguration[1]) {     //dynamic vibration unbalanced
                int freqCentred = (int) ((rpmConfiguration * 2.0/60.0) * dataFftArray.length/(samplingFrequency));
                double[] data1 = getMaxAnalyse(dataFftArray, freqCentred - freqShift, freqCentred + freqShift);
                analyseResultData.add(new data("Dynamic Vibration State ---> @ " + data1[1]*samplingFrequency/dataFftArray.length + ": ", data1[0]));//Math.pow(10, data1[0]/20) * powerCoefficientConfiguration / powerConfiguration));
            }
            /*______________________________________________ magnet default _________________________________________*/
            if (switchConfiguration[4]) {     //electrical or mechanical default
                int freqCentred = (int) (200*dataFftArray.length/(samplingFrequency));
                double[] data1 = getMaxAnalyse(dataFftArray, freqCentred - freqShift, freqCentred + freqShift);
                analyseResultData.add(new data("Bobine State --------------> @ " + data1[1]*samplingFrequency/dataFftArray.length + ": ", data1[0]));//'Math.pow(10, data1[0]/20) * powerCoefficientConfiguration / powerConfiguration));
            }
            /*__________________________________________ mechanical  looseness ______________________________________*/
            if (switchConfiguration[2]) {     //mechanical looseness : presented by harmonic 0.5 of the main freuqency
                double Temp = 0.0;
                for (DataPoint data : dataFrequencyMultiplebyhalf){
                    Temp += data.getY();
                }
                Temp /= dataFrequencyMultiplebyhalf.isEmpty() ? 1 : dataFrequencyMultiplebyhalf.size();
                analyseResultData.add(new data("Looseness default State -----> ", Temp * powerCoefficientConfiguration / powerConfiguration));
            }
            /*______________________________________________ bearing default _________________________________________*/
            if (switchConfiguration[3]) {     //bearing fault
                double Temp = 0.0;

                for (DataPoint data : dataFrequencyMultiplebyBearing){
                    Temp += data.getY();
                }
                Temp /= dataFrequencyMultiplebyBearing.isEmpty() ? 1 : dataFrequencyMultiplebyBearing.size();
                analyseResultData.add(new data("Bearing default State -----> ", Temp * powerCoefficientConfiguration / powerConfiguration));
            }

            /*______________________________________________ cushions default _________________________________________*/
            if (switchConfiguration[5]) {     //cushions  default
                double Temp = 0.0;
                for (DataPoint data : dataFrequencyMultiple){
                    Temp += data.getY();
                }
                Temp /= dataFrequencyMultiple.isEmpty() ? 1 : dataFrequencyMultiple.size();
                analyseResultData.add(new data("Cushions State -------------> ", Temp * powerCoefficientConfiguration / powerConfiguration));
            }


            /*----------------------------------------------------------**------------------------------------------------------------*/
            isDone(analyseResultData,signalResultAnalyse);     // send to the interface that the analyse is done
            analyseStatus = false;                    // analyse done
            counter = 0;                              //reset the counter
            /*----------------------------------------------------------**------------------------------------------------------------*/
        }
    };

    public double[] getMaxAnalyse(@NonNull double[] data, int low, int height) {
        double max = -1.0E100;
        int pos = 0;
        for (int i = low; i <= height + 1; i++) {
            if (max < data[i]) {
                max = data[i];
                pos = i;
            }
        }
        return (new double[]{max, pos});
    }


    /*-------------------------------------------------------------------------------------get all frequency pic-----------------------------------------------------------*/
    public static List<DataPoint> findSignificantPeaks(double[] dataArray, double threshold, double significanceThreshold) {
        List<DataPoint> peaks = new ArrayList<>();
        // to find the pic significant frequency
        for (int i = 1; i < dataArray.length - 1; i++) {
            if (dataArray[i] > threshold && isSignificantPeak(dataArray, i, significanceThreshold)) {
                peaks.add(new DataPoint(i, dataArray[i]));
            }
        }
        return peaks;
    }

    private static boolean isSignificantPeak(double[] dataArray, int index, double significanceThreshold) {
        return dataArray[index] > Math.max(dataArray[index - 1], dataArray[index + 1]) + 20*Math.log(significanceThreshold);
    }

    /*---------------------------------------------------------------------------------------get only the frequency multiple of rpm frequency--------------------------------------------*/
    private static List<DataPoint> dataFrequencyMultiple(List<DataPoint> dataFrequency, double frequency) {
        List<DataPoint> resultArray = new ArrayList<>();
        for (DataPoint data : dataFrequency) {
            if (Math.abs(data.getX() % frequency) < 1E-3) // remove main frequency
                resultArray.add(data);
        }
        return resultArray;
    }
    private static List<DataPoint> dataFrequencyMultipleHalf(List<DataPoint> dataFrequency, double frequency) {
        List<DataPoint> resultArray = new ArrayList<>();
        for (DataPoint data : dataFrequency) {
            if (Math.abs(data.getX() % frequency/2.0) < 1E-3 && (data.getY() / frequency) > 0.4) // remove main frequency
                resultArray.add(data);
        }
        return resultArray;
    }
    private static List<DataPoint> dataFrequencyMultipleBearing(List<DataPoint> dataFrequency, double frequency) {
        List<DataPoint> resultArray = new ArrayList<>();
        for (DataPoint data : dataFrequency) {
            if (Math.abs(data.getX() % frequency*bearingConfiguration) < 1E-3 && (data.getY() / frequency) > 1.5) // remove main frequency
                resultArray.add(data);
        }
        return resultArray;
    }

    /*--------------------------------------------------------------------------------------convert timline into frequency------------------------------------------------------------------*/
    private static List<DataPoint> lineToFrequency(List<DataPoint> dataList, double samplingFrequency, double Length) {
        List<DataPoint> resultData = new ArrayList<>();
        for (DataPoint data : dataList) {

            resultData.add(new DataPoint(data.getX() * 2 * samplingFrequency / Length, data.getY()));
        }
        return resultData;
    }
    public static double[] cloneTrounce(double[] data, double truncatedCoefficient){
        double[] resultData = new double[(int) (data.length * truncatedCoefficient)];
        System.arraycopy(data, 0, resultData, 0, resultData.length);
        return resultData;
    }

    public double[] toAc(@NonNull double[] data){
        double[] temp = data.clone();
        double val = 0;
        for (double datum : temp) val = val + datum;
        val = val/(temp.length);
        for (int i = 0;i<temp.length;i++) {
            temp[i] = temp[i] - val;
        }
        return temp;
    }
}
