package aina.elifran.um5.ensam.ap_os;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.series.DataPoint;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static Button l1B1,l1B2,l1B3,l1B4,l1B5;
    private static Button l2B1,l2B2,l2B3,l2B4,l2B5;
    private static Button l3B1,l3B2,l3B3,l3B4,l3B5;
    private static Button exit;
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
        l1B1 = view.findViewById(R.id.layout1_button1);
        l1B2 = view.findViewById(R.id.layout1_button2);
        l1B3 = view.findViewById(R.id.layout1_button3);
        l1B4 = view.findViewById(R.id.layout1_button4);
        l1B5 = view.findViewById(R.id.layout1_button5);
        l2B1 = view.findViewById(R.id.layout2_button1);
        l2B2 = view.findViewById(R.id.layout2_button2);
        l2B3 = view.findViewById(R.id.layout2_button3);
        l2B4 = view.findViewById(R.id.layout2_button4);
        l2B5 = view.findViewById(R.id.layout2_button5);
        l3B1 = view.findViewById(R.id.layout3_button1);
        l3B2 = view.findViewById(R.id.layout3_button2);
        l3B3 = view.findViewById(R.id.layout3_button3);
        l3B4 = view.findViewById(R.id.layout3_button4);
        l3B5 = view.findViewById(R.id.layout3_button5);
        exit = view.findViewById(R.id.exit);

        buttonConfiguration();
    }
    public void buttonConfiguration(){
        l1B1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l1B2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l1B3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l1B4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l1B5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });

        l2B1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l2B2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l2B3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l2B4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l2B5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });

        l3B1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l3B2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l3B3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l3B4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });
        l3B5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                double velocity = 0.0;
                // Cast the view to Button and get its text
                String buttonText = ((Button) view).getText().toString();
                Pattern pattern = Pattern.compile("^([\\d.-]+)\\|");
                Matcher velocityText = pattern.matcher(buttonText);
                if (velocityText.find()) {
                    velocity = Double.parseDouble(velocityText.group(1));
                } else {
                    velocity = Double.NaN;
                }
                velocitySelected(velocity);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSetting(getContext());
            }
        });
    }
    public static void velocityTrackResul(List<List<DataPoint>> returnValue){
        int i = 1;
        for (List<DataPoint> resultData: returnValue){
            setButtonLayout(resultData,i);
            i++;
        }
    }
    private static void setButtonLayout(List<DataPoint> data, int number){
        switch (number){
            case 1:
                putLayout1(data);
                break;
            case 2:
                putLayout2(data);
                break;
            case 3:
                putLayout3(data);
                break;
            default:
                break;
        }
    }
    private static void putLayout1(List<DataPoint> dataPointList){
        try {
            l1B1.setText(String.valueOf(dataPointList.get(0).getX()) + "|" + dataPointList.get(0).getY());
        }catch (Exception e){
            l1B1.setText("Not defined");}
        try {
            l1B2.setText(String.valueOf(dataPointList.get(1).getX()) + "|" + dataPointList.get(1).getY());
        }catch (Exception e){
            l1B2.setText("Not defined");}
        try {
            l1B3.setText(String.valueOf(dataPointList.get(2).getX()) + "|" + dataPointList.get(2).getY());
        }catch (Exception e){
            l1B3.setText("Not defined");}
        try {
            l1B4.setText(String.valueOf(dataPointList.get(3).getX()) + "|" + dataPointList.get(3).getY());
        }catch (Exception e){
            l1B4.setText("Not defined");}
        try {
            l1B5.setText(String.valueOf(dataPointList.get(4).getX()) + "|" + dataPointList.get(4).getY());
        }catch (Exception e){
            l1B4.setText("Not defined");}
    }
    private static void putLayout2(List<DataPoint> dataPointList){
        try {
            l2B1.setText(String.valueOf(dataPointList.get(0).getX()) + "|" + dataPointList.get(0).getY());
        }catch (Exception e){
            l2B1.setText("Not defined");}
        try {
            l2B2.setText(String.valueOf(dataPointList.get(1).getX()) + "|" + dataPointList.get(1).getY());
        }catch (Exception e){
            l2B2.setText("Not defined");}
        try {
            l2B3.setText(String.valueOf(dataPointList.get(2).getX()) + "|" + dataPointList.get(2).getY());
        }catch (Exception e){
            l2B3.setText("Not defined");}
        try {
            l2B4.setText(String.valueOf(dataPointList.get(3).getX()) + "|" + dataPointList.get(3).getY());
        }catch (Exception e){
            l2B4.setText("Not defined");}
        try {
            l2B5.setText(String.valueOf(dataPointList.get(4).getX()) + "|" + dataPointList.get(4).getY());
        }catch (Exception e){
            l2B4.setText("Not defined");}
    }
    private static void putLayout3(List<DataPoint> dataPointList){
        try {
            l3B1.setText(String.valueOf(dataPointList.get(0).getX()) + "|" + dataPointList.get(0).getY());
        }catch (Exception e){
            l3B1.setText("Not defined");}
        try {
            l3B2.setText(String.valueOf(dataPointList.get(1).getX()) + "|" + dataPointList.get(1).getY());
        }catch (Exception e){
            l3B2.setText("Not defined");}
        try {
            l3B3.setText(String.valueOf(dataPointList.get(2).getX()) + "|" + dataPointList.get(2).getY());
        }catch (Exception e){
            l3B3.setText("Not defined");}
        try {
            l3B4.setText(String.valueOf(dataPointList.get(3).getX()) + "|" + dataPointList.get(3).getY());
        }catch (Exception e){
            l3B4.setText("Not defined");}
        try {
            l3B5.setText(String.valueOf(dataPointList.get(4).getX()) + "|" + dataPointList.get(4).getY());
        }catch (Exception e){
            l3B4.setText("Not defined");}
    }
    private void velocitySelected(double selectedVelocity) {
        // velocity selected;
        MainActivity myActivity = (MainActivity) getActivity();
        myActivity.getFromTracking(selectedVelocity);
        closeSetting(getContext());
    }

    private void closeSetting(Context context) {
        // Remplacer le fragment
        TrackVelocity velocityFragment = (TrackVelocity) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragmentContainerVelocity);
        if (velocityFragment != null) {
            // Begin the transaction
            FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            // Remove the fragment
            transaction.remove(velocityFragment);
            // Commit the transaction
            transaction.commit();
        }
    }
}