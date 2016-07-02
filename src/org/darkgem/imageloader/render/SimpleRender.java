package org.darkgem.imageloader.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2015/7/7.
 */
public class SimpleRender implements Render {
    /**
     * 显示图片到ImageView上, 注意ImageView的过期
     *
     * @param context       上下文
     * @param bitmap        图片集合, 且保证图片都可用
     * @param suggestWidth  ImageLoader 计算出来的width
     * @param suggestHeight ImageLoader 计算出来的height
     */
    @Override
    @Nullable
    public Bitmap render(Context context, Bitmap[] bitmap, int suggestWidth, int suggestHeight) {
        return bitmap[0];
    }
}
