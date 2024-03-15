package aina.elifran.um5.ensam.ap_os;
import static java.lang.Math.pow;
import java.lang.Math;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;


public class fft {
    private static int length_data; //data length
    private static int[] BitReversData;
    private static double[] DataReversData;
    private static Complex[][] fftCoefficient;
    private static int log2_data;
    private static double[] data_input;
    private static Complex[] data_Output_Complex;
    private static double[] data_Output_Abs;
    private static double[] data_Output_Abs_Shift;
    private static double[] data_Output_Abs_Log;
    private static double[] data_Output_Abs_Log_Shift;



    private static Handler fft_handler;
    public fft(int length) {
        fft_handler = new Handler(Looper.myLooper());
        log2_data = (int)(Math.abs(Math.log(length) / Math.log(2)));
        length_data = length;
        DataReversData = new double[length];
        BitReversData = new int[length_data];
        fftCoefficient = new Complex[log2_data][length_data];

        bitrevers();
        getFftCoefficient();
    }

    public static final Runnable getLogfft = new Runnable() {
        @Override
        public void run() {
            data_Output_Abs_Log = getLogtfft(data_input);
        }
    };
    public static final Runnable getLogShifttfft = new Runnable() {
        @Override
        public void run() {
            data_Output_Abs_Log_Shift = getLogShifttfft(data_input);
        }
    };
    public static final Runnable fftShift = new Runnable() {
        @Override
        public void run() {
            data_Output_Abs_Shift = fftShift(data_input);
        }
    };
    public static final Runnable getAbsfft = new Runnable() {
        @Override
        public void run() {
            data_Output_Abs = getAbsfft(data_input);
        }
    };
    public static final Runnable getfft = new Runnable() {
        @Override
        public void run() {
            data_Output_Complex = getfft(data_input);
        }
    };


    @NonNull
    public static double[] getLogtfft(double data[]){
        double[] data_norm = getAbsfft(data);
        for (int i = 0; i<length_data;i++){
            data_norm[i] = 20*Math.log(data_norm[i]);
        }
        return data_norm;
    }
    @NonNull
    public static double[] getLogShifttfft(double data[]){
        double[] data_norm = fftShift(data);
        for (int i = 0; i<length_data;i++){
            data_norm[i] = 20*Math.log(data_norm[i]);
        }
        return data_norm;
    }
    @NonNull
    public static double[] fftShift(double data[]){
        double[] shifted = getAbsfft(data);
        double[] shifteddata = shifted.clone();
        for (int i = 0; i<length_data/2;i++){
            shifteddata[i] = shifted[i+length_data/2];
        }
        for (int i = data.length/2; i<length_data;i++){
            shifteddata[i] = shifted[i-length_data/2];
        }
        return shifteddata;
    }
    @NonNull
    public static double[] getAbsfft(double data[]){
       Complex[] absfft = new Complex[length_data];
        double[] dataOut = new double[length_data];
        absfft = getfft(data);
        for (int i = 0; i<length_data;i++){
            dataOut[i] = absfft[i].abs()/length_data;
        }
        return dataOut;
    }
    @NonNull
    public static Complex[] getfft(double data[]){
        datarevers(data);
        Complex[][] tempo_data = new Complex[log2_data + 1][length_data];
        for (int i = 0; i<length_data; i++){
            tempo_data[0][i] = Complex.toComplex(DataReversData[i]);
        }
            for (int i = 0; i<log2_data;i++){
                int x =(int)pow(2,i);
                for (int j = 0; j<(length_data);j+=x*2){ // i = 1
                    for (int k=0; k < x ; k++){
                        tempo_data[i+1][j + k + 0] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient[i][j + k + 0]));
                        tempo_data[i+1][j + k + x] = tempo_data[i][j + k].add(tempo_data[i][j + k + x].multiply(fftCoefficient[i][j + k + x]));
                    }
                }
            }
        return tempo_data[log2_data];
    }


    private static void getFftCoefficient(){
        for (int i = 0; i<log2_data;i++){
            for (int k = 0; k<length_data;k++){
                fftCoefficient[i][k] = Complex.exponential(-(2*Math.PI*k)/(pow(2,(i+1))));
            }
        }
    }
    private static double[] datarevers(double[] data){
            for (int i = 0; i<length_data - 1; i+=2){
                DataReversData[i]  = data[BitReversData[i]];
            }
            return DataReversData;
    }
    private static void bitrevers(){
        BitReversData = bitrevers(length_data);
    }
    private static int[] bitrevers(int length){
        double log2 = Math.abs(Math.log(length) / Math.log(2));
        int[] array = new int[length];
        if (log2 - (int)(log2) > 1e-10) {
            // not type
        }else
        {
            int log2_int = (int)(log2);
            for (int k = 0; k<length; k++)
                array[k] = bitrevers(k,log2_int);
        }
        return array;
    }
    private static int bitrevers(int data, int numBits) {
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



