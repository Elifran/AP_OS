package aina.elifran.um5.ensam.ap_os;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;

public class velocityTracking{
    private long counter = 0;
    private double estimatedVelocity;
    private double[][] dataArrayFft;
    private double samplingFrequency;
    private velocityTrackingInterface listener;
    public velocityTracking(velocityTrackingInterface listener){
        this.listener = listener;
    }

    public interface velocityTrackingInterface{
        void velocityResult(List<List<DataPoint>> returnValue);
    }
    public void setEstimatedVelocity(double estimated_Velocity){
        estimatedVelocity = estimated_Velocity/60;
    }
    public void addDataTrack(double[] data1,double[] data2,double[] data3, double sampling_Frequency){
        dataArrayFft = new double[][]{data1, data2, data3};
        samplingFrequency = sampling_Frequency;
        track();
    }
    private void track(){
        List<List<DataPoint>> listData = new ArrayList<>();
        int velocityHigh = (int)(estimatedVelocity*dataArrayFft[0].length/samplingFrequency + estimatedVelocity*dataArrayFft[0].length*0.15/samplingFrequency); // +/-15% from estimated RPM
        int velocityLow = (int)(estimatedVelocity*dataArrayFft[0].length/samplingFrequency - estimatedVelocity*dataArrayFft[0].length*0.15/samplingFrequency);
        for (double[] data : dataArrayFft){
            listData.add(getMax(data, velocityLow,velocityHigh ));
        }
        listener.velocityResult(listData);
    }
    private List<DataPoint> getMax(@NonNull double[] dataArray , int posMin, int posMax){
        double[] data = new double[(int)(posMax-posMin + 1)];
        System.arraycopy(dataArray, posMin, data, 0, data.length);
        List<DataPoint> resultList = new ArrayList<>();
        for (int j = 0; j < 5; j++ ){
            resultList.add(getMaxInside(data,posMin,dataArray.length));
        }
        return resultList;
    }
    private  DataPoint getMaxInside(double[] dataPointList, int posMin, int length){
        double max = -1E100;
        int pos = 0;
        for(int i = 0; i < dataPointList.length;i++){
            if(max < dataPointList[i]){
                max = dataPointList[i];
                pos = i;
            }
        }
        int alpha = (int) (dataPointList.length*0.1/samplingFrequency);
        for (int i = pos - alpha ; i <= pos + alpha; i++){
            try{ dataPointList[i] = -1.0E100;} catch (Exception ignored){}
        }
        return new DataPoint((double)((pos+posMin)*samplingFrequency*60.0/length),max);
    }

}
