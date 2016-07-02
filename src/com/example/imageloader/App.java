package com.example.imageloader;

import android.app.Application;
import org.darkgem.imageloader.ImageLoader;

/**
 * Created by Administrator on 2015/7/7.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.getInstance().init(this, null, new ExtVendor(this));
    }
}
