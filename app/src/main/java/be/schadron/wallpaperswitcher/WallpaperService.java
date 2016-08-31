package be.schadron.wallpaperswitcher;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;

public class WallpaperService extends IntentService {
    private WallpaperManager wallpaperMgr;

    public WallpaperService() {
        super("WallpaperService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wallpaperMgr = WallpaperManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("Wallpaper Switcher", "Switching wallpaper...");
        try {
            if (intent.getExtras().getInt("request code") != 0) {
                int resId = getResources().getIdentifier("w".concat(String.valueOf(intent.getExtras().getInt("request code"))), "drawable", "be.schadron.wallpaperswitcher");

                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inScaled = true;

                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                //Bitmap tempbitMap = BitmapFactory.decodeResource(getApplicationContext().getResources(), resId, options);
                //Bitmap bitmap = Bitmap.createScaledBitmap(tempbitMap, metrics.widthPixels, metrics.heightPixels, true);


                wallpaperMgr.setWallpaperOffsetSteps(0.5f, 1f);
                wallpaperMgr.suggestDesiredDimensions(metrics.widthPixels, metrics.heightPixels);
                wallpaperMgr.setResource(resId);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}