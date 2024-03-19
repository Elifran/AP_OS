package aina.elifran.um5.ensam.ap_os.placeholder;

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
    private Context myContext;
    private velocityTrackingInterface listener;
    public velocityTracking(Context context){
        this.listener =(velocityTrackingInterface) context;
    }

    public interface velocityTrackingInterface{
        void velocityResult(List<List<DataPoint>> returnValue);
    }
    public void setEstimatedVelocity(double estimated_Velocity){
        estimatedVelocity = estimated_Velocity;
    }
    public void addDataTrack(double[] data,double[] data2,double[] data3, double sampling_Frequency){
        dataArrayFft = new double[][]{data, data2, data3};
        samplingFrequency = sampling_Frequency;

    }
    private void track(){
        List<List<DataPoint>> listData = new ArrayList<>();
        int velocityHigh = (int)(estimatedVelocity*dataArrayFft[0].length/samplingFrequency + estimatedVelocity*dataArrayFft[0].length*0.1/samplingFrequency); // +/- 10% from estimated RPM
        int velocityLow = (int)(estimatedVelocity*dataArrayFft[0].length/samplingFrequency - estimatedVelocity*dataArrayFft[0].length*0.1/samplingFrequency);
        for (double[] data : dataArrayFft){
            listData.add(getMax(data, velocityLow,velocityHigh ));
        }
        listener.velocityResult(listData);
    }
    private List<DataPoint> getMax(@NonNull double[] dataArray , int posMin, int posMax){
        double max =-1.0E100;
        double[] data = dataArray;
        int alpha = (int) (data.length * 0.5/samplingFrequency);
                List<DataPoint> resultList = new ArrayList<>();
        int pos = 0;
        for (int j = 0; j < 5; j++ ){
            for(int i = posMin; i <posMax;i++){
                if(max < data[i]){
                    max = data[i];
                    pos = i;
                }
            }
            resultList.add(new DataPoint(pos,max));
            for (int i = pos - alpha ; i < pos + alpha; i++){
                try{ data[i] = -1.0E100;} catch (Exception ignored){}
            }
        }
        return resultList;
    }

}
