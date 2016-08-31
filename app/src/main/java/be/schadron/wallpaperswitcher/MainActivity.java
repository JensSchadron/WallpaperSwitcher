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
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private final static String PREF_SERV_ENABLED_KEY = "serv_enabled";
    private final static String PREF_SERV_STATUS_KEY = "serv_status";

    private AlarmManager alarmMgr;

    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        pref = getSharedPreferences("ServiceData", MODE_PRIVATE);

        setupStatus();
        setupOnClickListeners();
    }

    private void setupStatus() {
        final TextView tvServStatus = (TextView) findViewById(R.id.tv_service_status);

        String strStatus;

        switch (checkServiceStatus()) {
            case 0:
                strStatus = "Up and running";
                break;
            case 1:
                strStatus = "Shutdown";
                break;
            case 2:
                strStatus = "An error occured";
                break;
            default:
                strStatus = "";
        }

        tvServStatus.setText(strStatus);

    }

    public void setupOnClickListeners() {
        final Button btnStart = (Button) findViewById(R.id.btnStart);
        final Button btnStop = (Button) findViewById(R.id.btnStop);


        if (checkServiceStatus() == 0) {
            btnStart.setEnabled(false);
        } else {
            btnStop.setEnabled(false);
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleAlarms();
                switchServiceEnabled();
                switchServiceStatus(false);

                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarms();
                switchServiceEnabled();
                switchServiceStatus(false);

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

    private int checkServiceStatus() {
        return pref.getInt(PREF_SERV_STATUS_KEY, 1);
    }

    private void switchServiceStatus(boolean error) {
        if (error) {
            pref.edit().putInt(PREF_SERV_STATUS_KEY, 2); //ERROR
        } else if (checkServiceStatus() == 0) {
            pref.edit().putInt(PREF_SERV_STATUS_KEY, 1).apply(); //User shutdown
        } else {
            pref.edit().putInt(PREF_SERV_STATUS_KEY, 0).apply(); //User start
        }
        setupStatus();
    }

    private boolean isServiceEnabled() {
        return pref.getBoolean(PREF_SERV_ENABLED_KEY, false);
    }

    private void switchServiceEnabled() {
        pref.edit().putBoolean(PREF_SERV_ENABLED_KEY, (!isServiceEnabled())).apply();
    }
}
