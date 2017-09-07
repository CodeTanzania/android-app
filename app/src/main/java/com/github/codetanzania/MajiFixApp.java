package com.github.codetanzania;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

public class MajiFixApp extends Application {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
