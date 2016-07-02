package org.darkgem.imageloader;

import android.graphics.Bitmap;

/**
 * 加载器监听器
 */
public interface ImageLoadListener {
    /**
     * 开始加载
     */
    void onLoadStart();

    /**
     * @param bitmap 加载成功的Bitmap对象
     */
    void onLoadSuccess(Bitmap[] bitmap);

    /**
     * @param e 加载失败的异常
     */
    void onLoadFailed(Exception e);

    /**
     * 加载进度
     *
     * @param pg 进度
     */
    void onLoadProgress(int pg);

    /**
     * 被取消
     */
    void onLoadCancel();
}
