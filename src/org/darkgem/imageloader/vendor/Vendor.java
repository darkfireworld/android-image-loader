package org.darkgem.imageloader.vendor;

import android.graphics.Bitmap;

/**
 * 图片加载器
 */
public interface Vendor {
    /**
     * 加载图片
     *
     * @param uri           资源文件位置, 如 http:// ,https://, drawable:// file://
     * @param width         加载图片width
     * @param height        加载图片height
     * @param config        图片色彩配置
     * @param cacheInMemory 是否保存到内存中
     * @param listener      加载的回调函数, 不能为空, 进行回调的线程不确定
     */
    void loadImage(final String uri, final int width, final int height, final Bitmap.Config config,
                   final boolean cacheInMemory, final LoadListener listener);
}
