package be.schadron.wallpaperswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

public class AlarmReceiver extends BroadcastReceiver {
    public final static TreeMap<Integer, String> REQUEST_CODES;
    static {
        REQUEST_CODES = new TreeMap<>(Collections.reverseOrder());
        REQUEST_CODES.put(20700, "06:00");
        REQUEST_CODES.put(20701, "08:30");
        REQUEST_CODES.put(20702, "12:00");
        REQUEST_CODES.put(20703, "20:00");
        REQUEST_CODES.put(20704, "21:00");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, WallpaperService.class);
        i.putExtras(intent.getExtras());
        Log.i("Wallpaper Switcher", "Testing somehow...");
        context.startService(i);

    }
}
