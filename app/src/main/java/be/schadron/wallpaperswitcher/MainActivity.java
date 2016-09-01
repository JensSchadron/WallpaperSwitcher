package be.schadron.wallpaperswitcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import be.schadron.wallpaperswitcher.adapters.TimeSettingsAdapter;
import be.schadron.wallpaperswitcher.util.ViewSizeHelper;

public class MainActivity extends AppCompatActivity {
    private final static String PREF_SERV_ENABLED_KEY = "serv_enabled";
    private final static String PREF_SERV_STATUS_KEY = "serv_status";
    private final static String[] PREF_REQUESTCODE_TIME = {"20700", "20701", "20702", "20703", "20704"};

    private final static int green = Color.rgb(76, 175, 80);
    private final static int red = Color.rgb(255, 70, 0);

    private AlarmManager alarmMgr;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        pref = getSharedPreferences("ServiceData", MODE_PRIVATE);

        setupStatus();
        setupOnClickListeners();
        setupTimeSettings();
    }

    private void setupStatus() {
        final TextView tvServStatus = (TextView) findViewById(R.id.tv_service_status);

        switch (checkServiceStatus()) {
            case 0:
                tvServStatus.setText("Up and running");
                tvServStatus.setTextColor(green);
                break;
            case 1:
                tvServStatus.setText("Shutdown");
                tvServStatus.setTextColor(red);
                break;
            case 2:
                tvServStatus.setText("An error occured");
                tvServStatus.setTextColor(red);
                break;
            default:
                tvServStatus.setText("");
                tvServStatus.setTextColor(Color.GRAY);
        }
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

    private void setupTimeSettings() {
        final ListView listView = (ListView) findViewById(R.id.lv_timestap_settings);

        listView.setAdapter(new TimeSettingsAdapter(MainActivity.this, new ArrayList<String>(){{
            for (String aPREF_REQUESTCODE_TIME : PREF_REQUESTCODE_TIME) {
                add(String.format(Locale.ROOT, "Wallpaper %d-%s", (Integer.parseInt(aPREF_REQUESTCODE_TIME.substring(4))) + 1, getRequestcodeTime(aPREF_REQUESTCODE_TIME)));
            }
        }}));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minutes) {
                        setRequestcodeTime("2070" + pos, String.format(Locale.ROOT, "%02d:%02d", hour, minutes));
                        setupTimeSettings();
                    }
                }, Integer.parseInt(getRequestcodeTime("2070" + pos).split(":")[0]), Integer.parseInt(getRequestcodeTime("2070" + pos).split(":")[1]), true).show();
            }
        });
        ViewSizeHelper.setListViewSize(listView);
    }

    private void scheduleAlarms() {
        Log.i("Wallpaper Switcher", "Service is being started.");
        for (String requestcode : PREF_REQUESTCODE_TIME) {
            String time = getRequestcodeTime(requestcode);

            Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
            i.putExtra("request code", requestcode);
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, Integer.parseInt(requestcode), i, PendingIntent.FLAG_UPDATE_CURRENT);

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
        for (String requestcode : PREF_REQUESTCODE_TIME) {
            Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, Integer.parseInt(requestcode), i, PendingIntent.FLAG_UPDATE_CURRENT);

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

    private String getRequestcodeTime(String requestcode){
        return pref.getString(requestcode, "08:30");
    }

    private void setRequestcodeTime(String requestcode, String time){
        pref.edit().putString(requestcode, time).apply();
    }


}
