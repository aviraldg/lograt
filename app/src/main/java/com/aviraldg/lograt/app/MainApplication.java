package com.aviraldg.lograt.app;

import android.app.Application;

import com.aviraldg.lograt.Lograt;

/**
 * aviraldg - 29/12/16
 */

public class MainApplication extends Application {
    private Lograt lograt;

    @Override
    public void onCreate() {
        super.onCreate();

        this.lograt = new Lograt(this);
    }
}
