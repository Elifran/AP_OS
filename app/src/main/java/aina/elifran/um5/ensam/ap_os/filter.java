package aina.elifran.um5.ensam.ap_os;

public class filter {
    private final int filterOrder;
    private volatile double[] numBuffer;
    public double[] filterCoefficient;
    private double samplingFrequency;
    private double cut_offFrequency_Low;
    private double cut_offFrequency_High;
    private boolean isCreated = false;
    filter(int order, double sampling_frequency,double cut_offFrequencyLow, double cut_offFrequencyHigh){
        if((int)(order/2.0) == order/2)// order filterLP iis odd
        {
            filterOrder = order+1;
        }
        else{
            filterOrder = order;
        }
        samplingFrequency = sampling_frequency;
        cut_offFrequency_Low = cut_offFrequencyLow;
        cut_offFrequency_High = cut_offFrequencyHigh;
        filterCoefficient = new double[filterOrder+1];
        numBuffer = new double[filterOrder+1];
        isCreated = getFIRfilterCoef(filterOrder,cut_offFrequency_Low,cut_offFrequency_High,samplingFrequency);
    }
    // verify if the class is created or not,
    public boolean isCreated(){return isCreated;}
    public boolean isConfigChange(double sampling_frequency){
        boolean changed = false;
        if (sampling_frequency != samplingFrequency){
            samplingFrequency = sampling_frequency;
            isCreated = getFIRfilterCoef(filterOrder,cut_offFrequency_Low,cut_offFrequency_High,samplingFrequency);
            changed = true;
        }
        return changed;
    }
    public double filterData( double actualData){
        double Temp_res = 0;
        for (int i = filterOrder;i>0 ;i--){
            Temp_res += numBuffer[i]*filterCoefficient[i];
            numBuffer[i] = numBuffer[i-1];
        }
        numBuffer[0] = actualData;
        return Temp_res + numBuffer[0]*filterCoefficient[0];
    }
    public boolean getFIRfilterCoef(int order, double cutoff_frequencyLow,double cutoff_frequencyHigh, double sampling_frequency){
        // low pass filterLP desine
        double[] coefficient;
        double Temp;
        double normalized_freqL = 2*Math.PI*cutoff_frequencyLow/sampling_frequency;
        double normalized_freqH = 2*Math.PI*cutoff_frequencyHigh/sampling_frequency;
        coefficient = new double[order + 1];
        for (int i=1;i<=(int)(order/2);i++){
            Temp = (1/(Math.PI*i))* (Math.sin(i*normalized_freqH) - Math.sin(i*normalized_freqL));
            coefficient[(int)(order/2) + i] = Temp;
            coefficient[(int)(order/2) - i] = Temp;
        }
        coefficient[(int)(order/2)] = normalized_freqH/Math.PI - normalized_freqL/Math.PI;
        filterCoefficient = coefficient.clone();
        return true;
    }
    public double[] getFilterCoeff(){ return filterCoefficient; }
}