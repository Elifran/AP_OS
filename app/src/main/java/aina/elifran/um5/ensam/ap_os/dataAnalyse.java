package aina.elifran.um5.ensam.ap_os;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Collections;
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
    private final double frequencyShift = 1.0E-1;
    private final int filterOrder = 50;
    private int lag;
    private Double threshold;
    private Double influence;
    private  int bearingConfiguration_ballsNumber;
    private  double bearingConfiguration_PitchiDameter = 25;
    private  double bearingConfiguration_ballsDiameter = 2.5;
    private  double bearingConfiguration_BetaAngle = Math.PI/4;

    private boolean[] switchConfiguration;
    private List<Double> dataFftArray;
    private List<Double> dataMeasureArray;
    private List<Integer> dataTimeArray;
    private List<DataPoint> dataFrequencysignificantArray;
    private int data_buffer;
    private boolean analyseStatus = false;
    private final fft dataAnalyseFft;
    long counter;
    private analyseDoneListener listener;
    List<data> analyseResultData = new ArrayList<>();
    private final Handler analyseHandler;

    private double lineFrequency = 50.0;
    dataAnalyse(int buffer,
                double sampling_frequency,
                double rpm_Configuration,
                double power_Configuration,
                int bearing_Configuration,
                double bearing_BallDiamConfiguration,
                double bearing_PitchConfiguration,
                double bearing_AngleConfiguration,
                boolean[] switch_Configuration,
                double noise_Coefficient_Configuration,
                double power_Coefficient_Configuration,
                int lag,
                Double threshold,
                Double influence,
                double line_frequency
                ) {
        lineFrequency = line_frequency;
        data_buffer = buffer;
        samplingFrequency = sampling_frequency;
        rpmConfiguration = rpm_Configuration;
        bearingConfiguration_ballsDiameter = bearing_BallDiamConfiguration;
        bearingConfiguration_PitchiDameter = bearing_PitchConfiguration;
        bearingConfiguration_BetaAngle = bearing_AngleConfiguration;
        powerConfiguration = power_Configuration;
        bearingConfiguration_ballsNumber = bearing_Configuration;
        switchConfiguration = switch_Configuration.clone();
        noiseCoefficientConfiguration = noise_Coefficient_Configuration;
        powerCoefficientConfiguration  = power_Coefficient_Configuration;

        this.lag = lag;
        this.threshold = threshold;
        this.influence = influence;

        // initialize all array :
        //dataMeasureArray = new double[data_buffer];
        changeResolution(buffer);
        //dataTimeArray = new long[data_buffer];
        dataAnalyseFft = new fft(data_buffer);
        analyseHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    }
    private void changeResolution(int length){
        data_buffer = length;
        try {dataMeasureArray.clear();} catch (Exception ignored){}
        try {dataFftArray.clear();}catch (Exception ignored){}
        try {dataTimeArray.clear();}catch (Exception ignored){}

        dataMeasureArray = new ArrayList<>(Collections.nCopies(data_buffer, 0.0d));
        dataFftArray = new ArrayList<>(Collections.nCopies(data_buffer/2, 0.0d));
        dataTimeArray = new ArrayList<>(Collections.nCopies(data_buffer, 0));
    }
    public void setConfig(String Config,
                          Object Value) {
        switch (Config) {
            case "RESOLUTION":
                //counter = 0;
                data_buffer = (int) (Math.pow(2,(int)Value+3)*512);
                dataAnalyseFft.chancheResolution(data_buffer);
                changeResolution(data_buffer);
                break;
            case "SAMPLING FREQ":
                //counter = 0;
                samplingFrequency = (double) Value;
                break;
            case "RPM":
                rpmConfiguration = (double) Value;
                break;
            case "LINE":
                lineFrequency = (double) Value;
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
                bearingConfiguration_ballsNumber = (int) Value;
                break;
            case "BEARING PITCH" :
                bearingConfiguration_PitchiDameter = (double) Value;
                break;
            case "BEARING BALL DIAM" :
                bearingConfiguration_ballsDiameter = (double) Value;
                break;
            case "BEARING ANGLE" :
                bearingConfiguration_BetaAngle = (double) Value;
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
        shiftRight(dataMeasureArray, dataTimeArray, data, timeStamp);
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
    //private void shiftRight(@NonNull double[] dataArray, long[] arrayTime, double dataIn, long timestamp) {
    private void shiftRight(@NonNull List<Double> dataArray,List<Integer> arrayTime, double dataIn, long timestamp) {
        for (int i = data_buffer - 1; i > 0; i--) {
            dataArray.set(i,dataArray.get(i - 1));
            arrayTime.set(i,arrayTime.get(i - 1) + (int) timestamp);
        }
        arrayTime.set(0,0);
        dataArray.set(0,dataIn);
    }

    /*----------------------------------------------------------------------------- data analyse task -----------------------------------------------------------------*/
    private final Runnable dataAnalyse = new Runnable() {
        @Override
        public void run() {

            dataFftArray = cloneTrounce(dataAnalyseFft.getAbsFft(toAc(dataMeasureArray)),0.5); // get the absolute value of the fft
            HashMap<String, List> signalResultAnalyse = SignalDetector.analyzeDataForSignals(dataFftArray,lag,threshold,influence);
            dataFrequencysignificantArray =  getPeakPos(Objects.requireNonNull(signalResultAnalyse.get("signals")));

    // log the result
            for (Map.Entry<String, List> entry : signalResultAnalyse.entrySet()) {
                String key = entry.getKey();
                List value = entry.getValue();
                String listAsString = value.toString();
                Log.d("HashMapContent", key + ": " + listAsString);
            }
            List<DataPoint> dataFrequencyMultiple = dataFrequencyMultiple(dataFrequencysignificantArray, rpmConfiguration /60.0,frequencyShift);

            analyseResultData.clear();
            String ID;
            ID ="Line Freq @ " + lineFrequency + "Hz\n" + "Number of pick frequency ML fond : \n";
            for (DataPoint dataIn: dataFrequencyMultiple){
                ID += " | @ " + String.valueOf(dataIn.getX()) + String.valueOf(dataIn.getY())+ "\n";
            }
            analyseResultData.add(new data(ID, dataFrequencyMultiple.size()));
            ID = "Resolution Level : ";
            analyseResultData.add(new data(ID,(int)(Math.log((double) data_buffer /512)/Math.log(2)-3)));
            /*______________________________________________ static default _________________________________________*/
            if (switchConfiguration[0]) {     // static vibration unbalanced
                analyseResultData.add(staticVibration(dataFrequencysignificantArray));
            }
            /*_____________________________________________ dynamic default _________________________________________*/
            if (switchConfiguration[1]) {     //dynamic vibration unbalanced
                analyseResultData.add(dynamicVibration(dataFrequencysignificantArray));
           }
            /*______________________________________________ magnet default _________________________________________*/
            if (switchConfiguration[4]) {     //electrical or mechanical default
                analyseResultData.add(electricalVibration(dataFrequencysignificantArray));
            }
            /*__________________________________________ mechanical  looseness ______________________________________*/
            if (switchConfiguration[2]) {     //mechanical looseness : presented by harmonic 0.5 of the main freuqency
                analyseResultData.add(loosenessVibration(dataFrequencysignificantArray));
            }
            /*______________________________________________ bearing default _________________________________________*/
            if (switchConfiguration[3]) {     //bearing fault
                analyseResultData.add(bearingVibration(dataFrequencysignificantArray, dataMeasureArray));
            }

            /*______________________________________________ cushions default _________________________________________*/
            if (switchConfiguration[5]) {     //cushions  default
                analyseResultData.add(cushionsVibration(dataFrequencysignificantArray));
            }
            /*----------------------------------------------------------**------------------------------------------------------------*/
            isDone(analyseResultData,signalResultAnalyse);     // send to the interface that the analyse is done
            analyseStatus = false;                    // analyse done
            counter = 0;                              //reset the counter
            /*----------------------------------------------------------**------------------------------------------------------------*/
        }
    };

    // analyse Function : --------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    private data staticVibration(List<DataPoint> postResult){
        double freqCentred = rpmConfiguration/60.0;
        DataPoint data1 = getMaxAnalyse(postResult, freqCentred - frequencyShift, freqCentred + frequencyShift);
        return new data("Static Vibration State ----> @ " + data1.getX() + ": ", data1.getY());

    }
    private data dynamicVibration(List<DataPoint> postResult){
        double freqCentred = rpmConfiguration * 2.0/60.0;
        DataPoint data1 = getMaxAnalyse(postResult, freqCentred - frequencyShift, freqCentred + frequencyShift);
        return new data("Dynamic Vibration State ---> @ " + data1.getX() + ": ", data1.getY());

    }
    private data electricalVibration(List<DataPoint> postResult){
        DataPoint data1 = getMaxAnalyse(postResult, lineFrequency - frequencyShift, lineFrequency + frequencyShift);
        return new data("Bobine State --------------> @ " + data1.getX() + ": ", data1.getY());

    }
    private data cushionsVibration(List<DataPoint> postResult){  //
        List<DataPoint> cushionsAccumulo = new ArrayList<>();
        double Temp = 0.0;
        double rotorSpeed = rpmConfiguration/60.0;
        for (int i = 3; i<postResult.size();i++) {
            DataPoint dataPt = getMaxAnalyse(postResult, (rotorSpeed * i - frequencyShift), (rotorSpeed * i + frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0)
                cushionsAccumulo.add(dataPt);
        }
        for ( DataPoint dataPt : cushionsAccumulo)
            Temp += dataPt.getY();
        Temp /= cushionsAccumulo.isEmpty() ? 1 : cushionsAccumulo.size();

        return new data("Cushions default State -----> ", Temp * powerCoefficientConfiguration / powerConfiguration);
    }
    private data bearingVibration(List<DataPoint> postResult, List<Double> dataMeasure){
        double rotorSpeed = rpmConfiguration/60.0;
        boolean bearingFaultStatus = false,BFPIs = false,BSFs = false,BFPOs = false;
        double freqInterval = 10;
        double Bd_Pd_Beta = Math.cos(bearingConfiguration_BetaAngle) * bearingConfiguration_ballsDiameter / bearingConfiguration_PitchiDameter;
        double Pd_Bd = bearingConfiguration_PitchiDameter / bearingConfiguration_ballsDiameter;

        double FTF = 0.5 * rotorSpeed * (1 - Pd_Bd);
        double BSF = 0.5 * rotorSpeed * Pd_Bd * (1 - Math.pow(Bd_Pd_Beta, 2));
        double BFPO = 0.5  * rotorSpeed * bearingConfiguration_ballsNumber * (1 - Bd_Pd_Beta);
        double BPFI = 0.5 * rotorSpeed * bearingConfiguration_ballsNumber * (1 + Bd_Pd_Beta);

        List<DataPoint> bearingTest = new ArrayList<>();
        List<Double> dataMesureFiltred = new ArrayList<>();

        filter dataMeasureCut = new filter(filterOrder,samplingFrequency,BFPO-freqInterval,BFPO+freqInterval);
        for (Double data : dataMeasure)
            dataMesureFiltred.add(dataMeasureCut.filterData(data));

        for (int i = 1; i<postResult.size();i++) {
            DataPoint dataPt = (getMaxAnalyse(postResult, (BFPO * i - frequencyShift), (BFPO * i + frequencyShift)));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0){
                bearingTest.add(dataPt);
                BFPOs = true;
            }
        }
        for (int i = 1; i<postResult.size();i++) {
            DataPoint dataPt= getMaxAnalyse(postResult, (BSF * i - frequencyShift), (BSF * i + frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0){
                bearingTest.add(dataPt);
                BSFs = true;
            }
        }
        for (int i = 1; i<postResult.size();i++) {
            DataPoint dataPt=getMaxAnalyse(postResult, (BPFI * i - frequencyShift), (BPFI * i + frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0){
                bearingTest.add(dataPt);
                BFPIs = true;
            }
        }
        if (!bearingTest.isEmpty())
            bearingFaultStatus = true;
        return new data(
                "Bearing fault state : \n" + "BSF : " + BSFs + " | " + "BFPO : " + BFPOs + " | " + "BPFI : " +  BFPIs + " || " + bearingTest.size() +
                        "-----> ", bearingFaultStatus);

    }
    private data loosenessVibration(List<DataPoint> postResult){
        List<DataPoint> loosenessAccumulo = new ArrayList<>();
        boolean loosnessFault =  false;
        double Temp = 0.0;
        double rotorSpeed = rpmConfiguration / 60.0;
        for (int i = 1; i<postResult.size();i++) {  // internal looseness 1/2,1/3,1.5
            DataPoint dataPt = getMaxAnalyse(postResult, (rotorSpeed * i*1/3 - frequencyShift), (rotorSpeed * i*1/3 + frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0)
                loosenessAccumulo.add(dataPt);
        }
        for (int i = 0; i<postResult.size();i++) {  // internal looseness 1/2,1/3,1.5
            DataPoint dataPt = getMaxAnalyse(postResult, (rotorSpeed * (i + 1/2) - frequencyShift), (rotorSpeed * (i + 1/2)+ frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0)
                loosenessAccumulo.add(dataPt);
        }
        for (int i = 1; i<postResult.size();i++) {  // internal looseness 1/2,1/3,1.5
            DataPoint dataPt = getMaxAnalyse(postResult, (rotorSpeed * i*2/3 - frequencyShift), (rotorSpeed * i*2/3 + frequencyShift));
            if (dataPt.getY() != 0.0 && dataPt.getX() != 0.0)
                loosenessAccumulo.add(dataPt);
        }
        for (DataPoint dataDt : loosenessAccumulo){
            Temp += dataDt.getY();
            loosnessFault =true;
        }
        Temp /= loosenessAccumulo.isEmpty() ? 1 : postResult.size();
        return new data(
                "Looseness fault state : " + loosnessFault + "-----> ", Temp*powerCoefficientConfiguration/powerConfiguration);
    }
    /*-------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    public DataPoint getMaxAnalyse(@NonNull List<DataPoint> dataIn, double low, double  high) {
        DataPoint dataPoint = new DataPoint(0.0,0.0);
        for (DataPoint data : dataIn)
            if (data.getX() >= low && data.getX() <= high && dataPoint.getY() < data.getY())
                        dataPoint = data;
        return dataPoint;
    }

    /*-------------------------------------------------------------------------------------get all frequency pic-----------------------------------------------------------*/
    public List<DataPoint> findSignificantPeaks(List<Double> dataList, double threshold, double significanceThreshold) {
        List<DataPoint> peaks = new ArrayList<>();
        double[] dataArray = new double[dataList.size()];
        int i = 0;
        for (double data:dataList) {
            dataArray[i] = data;
            i++;
        }
        // to find the pic significant frequency
        for (i = 1; i < dataArray.length - 1; i++) {
            if (dataArray[i] > threshold && isSignificantPeak(dataArray, i, significanceThreshold)) {
                peaks.add(new DataPoint(i, dataArray[i]));
            }
        }
        return peaks;
    }

    private  boolean isSignificantPeak(double[] dataArray, int index, double significanceThreshold) {
        return dataArray[index] > Math.max(dataArray[index - 1], dataArray[index + 1]) + 20*Math.log(significanceThreshold);
    }

    /*---------------------------------------------------------------------------------------get only the frequency multiple of rpm frequency--------------------------------------------*/
    private List<DataPoint> dataFrequencyMultiple0(List<DataPoint> dataFrequency, double frequency, double tolerance) {
        List<DataPoint> resultArray = new ArrayList<>();
        for (DataPoint data : dataFrequency) {
            if (Math.abs(data.getX() - frequency) <= tolerance)
                resultArray.add(data);
        }
        return resultArray;
    }

    private List<DataPoint> dataFrequencyMultiple(List<DataPoint> dataFrequency, double frequency, double tolerance) {
        List<DataPoint> resultArray = new ArrayList<>();
        for (DataPoint data : dataFrequency) {
            double remainder = Math.abs(data.getX() % frequency);
            if (remainder <= tolerance || Math.abs(remainder - frequency) <= tolerance)
                resultArray.add(data);
        }
        return resultArray;
    }

    public List<Double> cloneTrounce(double[] data, double truncatedCoefficient){
        List<Double> resultData = new ArrayList<>();
        for ( int i = 0; i<data.length*truncatedCoefficient; i++)
            resultData.add(data[i]);
        return resultData;
    }

    public double[] toAc(@NonNull List<Double> data){
        double[] temp = new double[data.size()];
        int i = 0;
        for (double dt : data) {
            temp[i] = dt;
            i++;
        }
        double val = 0;
        for (double datum : temp) val = val + datum;
        val = val/(temp.length);
        for (i = 0;i<temp.length;i++) {
            temp[i] = temp[i] - val;
        }
        return temp;
    }

    private List<DataPoint> getPeakPos(List<Integer> dataIn){
        List<DataPoint>  data= new  ArrayList<>();
        int i = 0;
        for (Integer dataOut:dataIn){
            if (dataOut == 1){
                data.add(new DataPoint(i*samplingFrequency/(2.0*dataIn.size()),dataFftArray.get(i)));
            }
            i++;
        }
        return data;
    }
}
