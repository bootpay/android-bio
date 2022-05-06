package kr.co.bootpay.core.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreferenceHelper {
    public static void setValue(Context context, String key, String value) {
        if (value == null) return;
        if ("null".equals(value)) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor ed = prefs.edit();
        ed.putString(key, value);
        ed.commit();
    }

    public static String getValue(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, "");
    }

    public static void removeValue(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor ed = prefs.edit();
        ed.remove(key);
        ed.commit();
    }

    public static void allRemove(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
