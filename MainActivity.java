package pucudenhos;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.os.Handler;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import android.provider.Settings;
import android.content.Context;

// 1. A MainActivity (Classe Principal)
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        } else {
            // Tenta iniciar o serviço
            startService(new Intent(this, EffectService.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, EffectService.class));
            }
        }
        finish();
    }
}

// 2. O EffectService (DEVE FICAR FORA DAS CHAVES DA MAINACTIVITY)
// Note que não há "public" aqui
class EffectService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private Handler handler = new Handler();
    private int state = 0; 

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayView = new View(this);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        windowManager.addView(overlayView, params);
        startColorLoop();
    }

    private void startColorLoop() {
        handler.postDelayed(new Runnable() {
            int secondsPassed = 0;
            @Override
            public void run() {
                if (secondsPassed >= 5) {
                    state = (state + 1) % 3;
                    secondsPassed = 0;
                }

                int color;
                if (state == 0) color = Color.argb(100, 255, 0, 0); 
                else if (state == 1) color = Color.argb(100, 0, 255, 0);
                else color = Color.argb(100, 0, 0, 255);

                overlayView.setBackgroundColor(color);
                
                secondsPassed++;
                handler.postDelayed(this, 1000); 
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
    }
}
