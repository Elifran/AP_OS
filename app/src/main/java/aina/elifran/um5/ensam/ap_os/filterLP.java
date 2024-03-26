package aina.elifran.um5.ensam.ap_os;

public class filterLP {
    private final int filterOrder;
    private volatile double[] numBuffer;
    public double[] filterCoefficient;
    private double samplingFrequency;
    private double cutOffFrequency;
    private boolean isCreated = false;
    filterLP(int order, double sampling_frequency, double cut_offFrequency){
        if((int)(order/2.0) == order/2)// order filterLP iis odd
        {
            filterOrder = order+1;
        }
        else{
            filterOrder = order;
        }
        samplingFrequency = sampling_frequency;
        cutOffFrequency = cut_offFrequency;
        filterCoefficient = new double[filterOrder+1];
        numBuffer = new double[filterOrder+1];
        isCreated = getFIRfilterCoef(filterOrder,cutOffFrequency,samplingFrequency);
    }
    // verify if the class is created or not,
    public boolean isCreated(){return isCreated;}
    public boolean isConfigChange(double sampling_frequency, double cut_offFrequency){
        boolean changed = false;
        if (sampling_frequency != samplingFrequency || cut_offFrequency != cutOffFrequency){
            samplingFrequency = sampling_frequency;
            cutOffFrequency = cut_offFrequency;
            isCreated = getFIRfilterCoef(filterOrder,cutOffFrequency,samplingFrequency);
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
    public boolean getFIRfilterCoef(int order, double cutoff_frequency, double sampling_frequency){
        // low pass filterLP desine
        double coeff[];
        double Temp;
        double normalized_freq = 2*Math.PI*cutoff_frequency/sampling_frequency;
        coeff = new double[order + 1];

        for (int i=1;i<=(int)(order/2);i++){
            Temp = (1/(Math.PI*i))* Math.sin(i*normalized_freq);
            coeff[(int)(order/2) + i] = Temp;
            coeff[(int)(order/2) - i] = Temp;
        }
        coeff[(int)(order/2)] = normalized_freq/Math.PI;
        filterCoefficient = coeff.clone();
        return true;
    }
    public double[] getFilterCoeff(){ return filterCoefficient; }
}