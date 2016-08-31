package be.schadron.wallpaperswitcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private final static String PREF_SERV_ENABLED_KEY = "serv_enabled";

    private AlarmManager alarmMgr;

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        pref = getSharedPreferences("ServiceData", MODE_PRIVATE);

        setupOnClickListeners();
    }

    public void setupOnClickListeners() {
        final Button btnStart = (Button) findViewById(R.id.btnStart);
        final Button btnStop = (Button) findViewById(R.id.btnStop);


        if(isServiceEnabled()) {
            btnStart.setEnabled(false);
        } else {
            btnStop.setEnabled(false);
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleAlarms();
                switchServiceEnabled();
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarms();
                switchServiceEnabled();
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);

            }
        });
    }

    private void scheduleAlarms() {
        Log.i("Wallpaper Switcher", "Service is being started.");
        for (Integer requestcode : AlarmReceiver.REQUEST_CODES.keySet()) {
            String time = AlarmReceiver.REQUEST_CODES.get(requestcode);

            Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
            i.putExtra("request code", requestcode);
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, requestcode, i, PendingIntent.FLAG_UPDATE_CURRENT);

            int hour = Integer.parseInt(time.split(":")[0]);
            int minute = Integer.parseInt(time.split(":")[1]);

            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) > hour || (calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) > minute)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
            calendar.set(GregorianCalendar.MINUTE, minute);

            Log.i("Wallpaper Switcher", String.format("%02d/%02d/%02d - %02d:%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);
            alarmMgr.setWindow(AlarmManager.RTC, calendar.getTimeInMillis(), 10000, pIntent);

            startService(i);
        }
    }

    private void cancelAlarms() {
        Log.i("Wallpaper Switcher", "Service is being cancelled.");
        for (Integer requestcode : AlarmReceiver.REQUEST_CODES.keySet()) {
            Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, requestcode, i, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmMgr.cancel(pIntent);
        }
    }

    private boolean isServiceEnabled(){
        return pref.getBoolean(PREF_SERV_ENABLED_KEY, false);
    }

    private void switchServiceEnabled(){
        pref.edit().putBoolean(PREF_SERV_ENABLED_KEY, (!isServiceEnabled())).apply();
    }
}
