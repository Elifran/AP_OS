package aina.elifran.um5.ensam.ap_os;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.series.DataPoint;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackVelocity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackVelocity extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static RelativeLayout layout1,layout2,layout3;
    private TrackVelocity listener;
    public TrackVelocity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrackVelocity.
     */
    // TODO: Rename and change types and number of parameters
    public static TrackVelocity newInstance(String param1, String param2) {
        TrackVelocity fragment = new TrackVelocity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.track_velocity_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout1 = view.findViewById(R.id.layout1);
        layout2 = view.findViewById(R.id.layout1);
        layout3 = view.findViewById(R.id.layout1);
    }

    public static void velocityTrackResul(List<List<DataPoint>> returnValue){
        int i = 1;
        for (List<DataPoint> resultData: returnValue){
            setButonLayout(resultData,i);
            i++;
        }
    }
    private static void setButonLayout(List<DataPoint> data, int number){
        switch (number){
            case 1:
                putLayout(data,layout1);
                break;
            case 2:
                putLayout(data,layout2);
                break;
            case 3:
                putLayout(data,layout3);
                break;
            default:
                break;
        }
    }
    private static void putLayout(List<DataPoint> dataPointList, RelativeLayout layout){
        Button previousButton = null;
        for (DataPoint data : dataPointList){
            Button button = new Button(layout.getContext());
            button.setText(data.getX() + " : " + data.getY()); // Set button text based on DataPoint label
            layout.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    velocitySelected(data.getX());
                }
            });
            // Set layout parameters for the button
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            // Add rule to position the button below the previous one
            if (previousButton != null) {
                layoutParams.addRule(RelativeLayout.BELOW, previousButton.getId());
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            button.setLayoutParams(layoutParams);
            // Update previousButton reference
            previousButton = button;
        }
    }
    private static void velocitySelected(double selectedVelocity){
        // velocity selected;
    }
}