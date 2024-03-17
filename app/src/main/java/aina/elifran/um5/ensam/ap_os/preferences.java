package aina.elifran.um5.ensam.ap_os;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class preferences {

    private static final String PREF_NAME = "accelerometer_data_preference";
    public static void writePreferences(Context context, String key, Object value){
        SharedPreferences preference = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        switch (value.getClass().getSimpleName()) {
            case "Boolean":
                editor.putBoolean(key, (boolean) value);
                break;
            case "String":
                editor.putString(key, (String) value);
                break;
            case "Double":
                editor.putFloat(key, ((Double) value).floatValue());
                break;
            case "Float":
                editor.putFloat(key, (float) value);
                break;
            case "Integer":
            case "Int":
                editor.putInt(key, (int) value);
                break;
            default:
                // Handle default case
                break;
        }
        editor.commit();
    }
    public static Map<String, ?> readPreferences(Context context){
        SharedPreferences preference = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return  preference.getAll();
    }

}
