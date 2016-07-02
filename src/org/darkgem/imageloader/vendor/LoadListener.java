package org.darkgem.imageloader.vendor;

import android.graphics.Bitmap;

public interface LoadListener {

    /**
     * @param bitmap 加载成功的Bitmap对象
     */
    void onLoadSuccess(Bitmap bitmap);

    /**
     * @param e 加载失败的异常
     */
    void onLoadFailed(Exception e);

    /**
     * 加载进度
     *
     * @param current 当前加载字节数
     * @param total   总共字节数
     */
    void onLoadProgress(int current, int total);


}
