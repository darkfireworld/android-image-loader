package org.darkgem.imageloader.render;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.Log;

public class RoundedRender implements Render {

    protected final int cornerRadius;
    protected final int margin;

    public RoundedRender(int cornerRadiusPixels) {
        this(cornerRadiusPixels, 0);
    }

    public RoundedRender(int cornerRadiusPixels, int marginPixels) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
    }

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
        try {
            Bitmap targetBitmap = Bitmap.createBitmap(bitmap[0].getWidth(), bitmap[0].getHeight(), Bitmap.Config.ARGB_8888);
            // 得到画布
            Canvas canvas = new Canvas(targetBitmap);
            // 创建画笔
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Rect rect = new Rect(0, 0, bitmap[0].getWidth(), bitmap[0].getHeight());
            RectF rectF = new RectF(rect);
            // 绘制
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap[0], rect, rect, paint);
            return targetBitmap;
        } catch (Exception e) {
            Log.e(RoundedRender.class.getName(), e.getMessage(), e);
            return null;
        }
    }
}
