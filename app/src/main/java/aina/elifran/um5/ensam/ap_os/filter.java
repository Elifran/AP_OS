package aina.elifran.um5.ensam.ap_os;
public class filter {
    private static int filterOrder;
    private volatile double[] numBuffer;
    public static double[] filterCoefficient;
    private static double samplingfrequency;
    private static double cutofffrequency;
    private volatile boolean isCreated = false;
    filter(int ordre, double sampling_frequency,double cut_offFrequency){
        if((int)(ordre/2) == ordre/2)// order filter iis odd
            filterOrder = ordre+1;
        else
            filterOrder = ordre;
        samplingfrequency = sampling_frequency;
        cutofffrequency = cut_offFrequency;
        filterCoefficient = new double[filterOrder+1];
        numBuffer = new double[filterOrder+1];
        isCreated = getFIRfilterCoef(filterOrder,cutofffrequency,samplingfrequency);
    }

    // verify if the class is created or not,
    public boolean isCreated(){return isCreated;}
    public boolean isConfigChange( double sampling_frequency,double cut_offFrequency){
        boolean changed = false;
        if (sampling_frequency != samplingfrequency || cut_offFrequency != cutofffrequency){
            samplingfrequency = sampling_frequency;
            cutofffrequency = cut_offFrequency;
            isCreated = getFIRfilterCoef(filterOrder,cutofffrequency,samplingfrequency);
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
    public boolean getFIRfilterCoef(int ordre, double cutoff_frequency, double sampling_frequency){
        // low pass filter desine
        double coeff[];
        double Temp;
        double normalized_freq = 2*Math.PI*cutoff_frequency/sampling_frequency;
        coeff = new double[ordre + 1];

        for (int i=1;i<=(int)(ordre/2);i++){
            Temp = (1/(Math.PI*i))* Math.sin(i*normalized_freq);
            coeff[(int)(ordre/2) + i] = Temp;
            coeff[(int)(ordre/2) - i] = Temp;
        }
        coeff[(int)(ordre/2)] = normalized_freq/Math.PI;
        filterCoefficient = coeff.clone();
        return true;
    }
    public double[] getFilterCoeff(){ return filterCoefficient; }
}