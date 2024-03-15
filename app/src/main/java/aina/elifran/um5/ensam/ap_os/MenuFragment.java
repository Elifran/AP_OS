package aina.elifran.um5.ensam.ap_os;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ViewGroup menu_layout;
    private FloatingActionButton setting_button;
    private Button confirm_button;
    private Switch SW1,SW2,SW3,SW4,SW5;
    private EditText RPM,POWER;
    private String mParam1;
    private String mParam2;
    private boolean[] SwitchValue = new boolean[5];
    private double rpmValue,powerValue;
    private OnDataChangeListener mListener;

    public MenuFragment() {
        // Required empty public constructor
    }
    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu_fragment, container, false);
        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        menu_layout = (ViewGroup) view.findViewById(R.id.menu_layout);
        setting_button = view.findViewById(R.id.setting_button);
        confirm_button = view.findViewById(R.id.confirm_button);

        RPM = view.findViewById(R.id.velocity_value);
        POWER = view.findViewById(R.id.power_value);

        SW1 = view.findViewById(R.id.switch1);
        SW2 = view.findViewById(R.id.switch2);
        SW3 = view.findViewById(R.id.switch3);
        SW4 = view.findViewById(R.id.switch4);
        SW5 = view.findViewById(R.id.switch5);

        onClick();
        onChange();
        setConfiguration();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);try {
            mListener = (OnDataChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDataChangeListener");
        }
    }
    public interface OnDataChangeListener {
        void onDataChanged(Object newData);
    }
    private void sendData(Object data) {
        if (mListener != null) {
            mListener.onDataChanged(data);
        }
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setConfiguration();
    }
    private void setConfiguration(){

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;

        ViewGroup.LayoutParams menu_fragment_params = menu_layout.getLayoutParams();


        //fragment setting params
       if(height > width){
           menu_fragment_params.width = width - (int)(width*0.05);
       }else{
           menu_fragment_params.width
                   = width/2 - (int)(width*0.02);
       }
       menu_layout.setLayoutParams(menu_fragment_params);
       menu_layout.setBackgroundColor(Color.parseColor("#CCCCCCCC"));
    }
    private void onClick(){
        setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remplacer FragmentA par FragmentB
                BlankFragment blank_fragment = new BlankFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, blank_fragment);
                transaction.addToBackStack(null);  // Permet de revenir en arrière
                transaction.commit();
            }
        });
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(new data("SWITCH",SwitchValue));
                sendData(new data("RPM",rpmValue));
                sendData(new data("POWER",powerValue));
            }
        });
        SW1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SwitchValue[0]= isChecked;
            }});
        SW2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SwitchValue[1]= isChecked;
            }});
        SW3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SwitchValue[2]= isChecked;
            }});
        SW4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SwitchValue[3]= isChecked;
            }});
        SW5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SwitchValue[4]= isChecked;
            }});

    }
    private void onChange() {
        RPM.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String powerText = RPM.getText().toString(); // Get the text entered in POWER view
                    if (!powerText.isEmpty()) { // Check if the text is not empty
                        try {
                            rpmValue = Double.parseDouble(powerText); // Convert text to float
                        } catch (NumberFormatException e) {
                            rpmValue = 0.0; // Convert text to float
                        }
                    }
                }
            }
        });
        POWER.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String powerText = POWER.getText().toString(); // Get the text entered in POWER view
                    if (!powerText.isEmpty()) { // Check if the text is not empty
                        try {
                            powerValue = Double.parseDouble(powerText); // Convert text to float
                        } catch (NumberFormatException e) {
                            powerValue = 0.0;
                        }
                    }
                }
            }
        });
    }
}