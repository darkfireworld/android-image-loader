package org.darkgem.imageloader.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * 渲染工具
 */
public interface Render {
    /**
     * 显示图片到ImageView上, 注意ImageView的过期,如果渲染失败，则返回null
     *
     * @param context       上下文
     * @param bitmaps       图片集合, 且保证图片都可用
     * @param suggestWidth  ImageLoader 计算出来的width
     * @param suggestHeight ImageLoader 计算出来的height
     */
    @Nullable
    Bitmap render(Context context, Bitmap[] bitmaps, int suggestWidth, int suggestHeight);
}
