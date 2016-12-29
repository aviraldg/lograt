package com.aviraldg.lograt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

/**
 * aviraldg - 29/12/16
 */

public class Lograt {
    private final Context context;

    public Lograt(@NonNull Context context) {
        this.context = context;

        Intent intent = LogratService.getIntent(context);
        context.startService(intent);
    }

    public static @NonNull Lograt start(@NonNull Context context) {
        return new Lograt(context);
    }
}
