package org.darkgem.imageloader.vendor.support.mc;

import android.graphics.Bitmap;

public interface MemoryCache {
    /**
     * 缓存中添加一个指定的bitmap对象
     *
     * @param uri    图片资源位置
     * @param width  图片width
     * @param height 图片height
     * @param bitmap 图片bitmap对象
     * @return 是否保存成功
     */
    boolean put(String uri, int width, int height, Bitmap bitmap);

    /**
     * 获取图片
     *
     * @param uri    图片资源位置
     * @param width  图片width
     * @param height 图片height
     * @return 图片bitmap对象
     */
    Bitmap get(String uri, int width, int height);

    /**
     * 移除对应图片的缓存
     *
     * @param uri    图片资源位置
     * @param width  图片width
     * @param height 图片height
     * @return 图片bitmap对象
     */
    Bitmap remove(String uri, int width, int height);

    /**
     * 清空缓存
     */
    void clear();
}
