package com.aviraldg.lograt;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * aviraldg - 29/12/16
 */

public class LogratService extends IntentService {
    private static final String TAG = "LogratService";
    private Handler handler;
    private NotificationManagerCompat notificationManager;
    private Notification notification;
    private Pattern logcatPattern = Pattern.compile("\\[\\]");
    private static HashMap<Character, String> colorMap = new HashMap<>();

    static {
        colorMap.put('V', "#009109");
        colorMap.put('D', "#d12200");
        colorMap.put('I', "#0173ba");
        colorMap.put('W', "#f7ad00");
        colorMap.put('E', "#dd3300");
        colorMap.put('A', "#ff6100");
    }

    public static @NonNull Intent getIntent(@NonNull Context context) {
        return new Intent(context, LogratService.class);
    }

    public LogratService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, LogActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        handler = new Handler(Looper.getMainLooper());
        notificationManager = NotificationManagerCompat.from(this);
        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_lograt)
                .setContentTitle("Lograt is running...")
                .setContentText("Tap to view logcat.")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .getNotification();

//        notificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        notificationManager.cancel(0);
    }

    private void handleLog(final String line) {
        Matcher matcher = logcatPattern.matcher(line);
        if(matcher.matches()) {
            String date = matcher.group(1);
            String time = matcher.group(2);
            String pidApp = matcher.group(3);
            String level = matcher.group(4);
            String tag = matcher.group(5);
            String text = matcher.group(6);
            Log.d(TAG, level);
        }

        if(line.length() == 0) return;
        if(line.charAt(0) != '[') return;

        final String []lines = line.split("\n");
        final String status = lines[0];
        String []parts = status.split(" ");
        int i = line.indexOf('\n');
        String tmp = "";
        final char level = parts[4].charAt(0);
        if(i>=0) {
            tmp = line.substring(i);
        }
        final String text = tmp;

        handler.post(new Runnable() {
            @Override
            public void run() {
                LayoutInflater li = LayoutInflater.from(LogratService.this);
                Toast toast = new Toast(LogratService.this);
                View view = li.inflate(R.layout.toast, null);
                ((TextView) view.findViewById(R.id.text)).setText(text);
                view.findViewById(R.id.color_bar).setBackgroundColor(Color.parseColor(colorMap.containsKey(level) ? colorMap.get(level) : "#000000"));
                view.setAlpha(0.9f);
                toast.setView(view);
                toast.setMargin(0, 0);
                toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
                toast.setDuration(lines.length > 1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat -v long");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(true) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                    Thread.sleep(100);

//                    handleLog(line);

                    if(line.trim().equals("")) {
                        handleLog(stringBuilder.toString());
                        stringBuilder.setLength(0);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            handleLog(stringBuilder.toString());
        }
    }
}
