package aina.elifran.um5.ensam.ap_os;
import static java.lang.Math.pow;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;


public class fft {
    private int length_data; //data length
    private  List<Integer> BitReversData;
    private  List<Double> DataReversData;
    private  List<List<Complex>>  fftCoefficient;

    private  Handler fft_handler;
    public fft(int length) {
        fft_handler = new Handler(Looper.myLooper());
        fftInit(length);
    }
    private void fftInit(int length){
        length_data = length;
        try {DataReversData.clear();}catch (Exception e){}
        try {BitReversData.clear();}catch (Exception e){}
        try {fftCoefficient.clear();}catch (Exception e){}
        //DataReversData = new double[length];
        DataReversData = new ArrayList<Double>(Collections.nCopies(length_data, 0d));
        //BitReversData = new int[length_data];
        BitReversData = new ArrayList<Integer>(Collections.nCopies(length_data, 0));
        //fftCoefficient = new Complex[(int)(Math.abs(Math.log(length) / Math.log(2)))][length_data];
        fftCoefficient = new ArrayList<>();
        for (int i=0; i<= (int)(Math.abs(Math.log(length_data) / Math.log(2)));i++){
            fftCoefficient.add(new ArrayList<Complex>(Collections.nCopies(length_data, new Complex(0.0,0.0))));
        }
        bitRevers();
        getFftCoefficient(length);
    }
    public void chancheResolution(int lengh){
        fftInit(lengh);

    }
    @NonNull
    public  double[] getLogtfft(double data[]){
        double[] data_norm = getAbsFft(data);
        for (int i = 0; i<length_data;i++){
            data_norm[i] = 20*Math.log(data_norm[i]);
        }
        return data_norm;
    }
    @NonNull
    public  double[] getLogShifttfft(double data[]){
        double[] data_norm = fftShift(data);
        for (int i = 0; i<data.length;i++){
            data_norm[i] = 20*Math.log(data_norm[i]);
        }
        return data_norm;
    }
    @NonNull
    public  double[] fftShift(double data[]){
        double[] shifted = getAbsFft(data);
        double[] shiftedData = shifted.clone();
        for (int i = 0; i<data.length/2;i++){
            shiftedData[i] = shifted[i+data.length/2];
        }
        for (int i = data.length/2; i<data.length;i++){
            shiftedData[i] = shifted[i-data.length/2];
        }
        return shiftedData;
    }
    @NonNull
    public  double[] getAbsFft(double data[]){
        Complex[] absfft = new Complex[length_data];
        double[] dataOut = new double[data.length];
        absfft = getFft(data);
        for (int i = 0; i<data.length;i++){
            dataOut[i] = absfft[i].abs()/data.length;
        }
        return dataOut;
    }
    @NonNull
    public  Complex[] getFft(double data[]){
        int log2_data = (int)(Math.abs(Math.log(data.length) / Math.log(2)));
        dataRevers(data);
        Complex[][] tempo_data = new Complex[log2_data + 1][data.length];
        for (int i = 0; i<data.length; i++){
            //tempo_data[0][i] = Complex.toComplex(DataReversData[i]);
            tempo_data[0][i] = Complex.toComplex(DataReversData.get(i));
        }
            for (int i = 0; i<log2_data;i++){
                int x =(int)pow(2,i);
                for (int j = 0; j<(data.length);j+=x*2){ // i = 1
                    for (int k=0; k < x ; k++){
                       //tempo_data[i+1][j + k + 0] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient[i][j + k + 0]));
                       //tempo_data[i+1][j + k + x] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient[i][j + k + x]));

                        tempo_data[i+1][j + k + 0] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient.get(i).get(j + k + 0)));
                        tempo_data[i+1][j + k + x] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient.get(i).get(j + k + x)));

                    }
                }
            }
        return tempo_data[log2_data];
    }
    private  void getFftCoefficient(int length){
        int log2_data = (int)(Math.abs(Math.log(length) / Math.log(2)));
        for (int i = 0; i<log2_data;i++){
            for (int k = 0; k<length;k++){
                //fftCoefficient[i][k] = Complex.exponential(-(2*Math.PI*k)/(pow(2,(i+1))));
                fftCoefficient.get(i).set(k,Complex.exponential(-(2*Math.PI*k)/(pow(2,(i+1)))));
            }
        }
    }
    private void dataRevers(double[] data){
            for (int i = 0; i<data.length - 1; i+=2){
                DataReversData.set(i,data[BitReversData.get(i)]);
            }
    }
    private  void bitRevers(){
        BitReversData = bitRevers(length_data);
    }
    private  List<Integer> bitRevers(int length){
        double log2 = Math.abs(Math.log(length) / Math.log(2));
        List<Integer> array = new ArrayList<Integer>(Collections.nCopies(length_data, 0));
        if (log2 - (int)(log2) < 1e-10) {
            int log2_int = (int)(log2);
            for (int k = 0; k<length; k++)
                array.set(k,bitRevers(k,log2_int));
        }
        return array;
    }
    private int bitRevers(int data, int numBits) {
        int reversedData = 0;
        for (int i = 0; i < numBits; i++) {
            int leftBit = (data >> i) & 1; // Get the bit at position i from the left
            int rightBit = (data >> (numBits - i)) & 1; // Get the bit at position i from the right
            reversedData |= (leftBit << (numBits - i - 1));
            reversedData |= (rightBit << (i - 1));
        }
        return reversedData;
    }
}



