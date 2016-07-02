package org.darkgem.imageloader.render;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * 组合头像
 */
public class CombineRender implements Render {

    private static Bitmap combine(int width, int height, Bitmap[] bitmaps) {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bm);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        int r = (int) (width / 3.5f);
        if (bitmaps.length >= 1) {
            cv.drawBitmap(toRoundBitmap(bitmaps[0], 2 * r), width / 2f - r, 0, paint);
        }
        if (bitmaps.length >= 2) {
            cv.drawBitmap(toRoundBitmap(bitmaps[1], 2 * r), 0, width - 2 * r, paint);
        }
        if (bitmaps.length >= 3) {
            cv.drawBitmap(toRoundBitmap(bitmaps[2], 2 * r), width - 2 * r, width - 2 * r, paint);
        }
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        return bm;
    }

    /**
     * 圆形头像
     */
    private static Bitmap toRoundBitmap(Bitmap bitmap, int w) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float left, top, right, bottom;
        if (width <= height) {
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
        } else {
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
        }

        final int sideW = 2;//描边
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        //画小圆的原始图
        paint.setAntiAlias(true);                       //设置画笔为无锯齿
        paint.setColor(Color.WHITE);
        paint.setFilterBitmap(true);
        Bitmap output1 = Bitmap.createBitmap(w - sideW * 2,
                w - sideW * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output1);
        canvas.drawRoundRect(new RectF(new Rect(0, 0, w - sideW * 2, w - sideW * 2)), w / 2f - sideW, w / 2f - sideW, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, new Rect(0, 0, w - sideW * 2, w - sideW * 2), paint);//截图

        //画大圆
        final Paint paint2 = new Paint();
        paint2.setAntiAlias(true);                       //设置画笔为无锯齿
        paint2.setColor(Color.WHITE);
        paint2.setFilterBitmap(true);
        Bitmap output2 = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(output2);
        canvas2.drawRoundRect(new RectF(new Rect(0, 0, w, w)), w / 2f, w / 2f, paint2);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas2.drawBitmap(output1, sideW, sideW, paint2);//小圆放大圆里面
        return output2;
    }

    /**
     * 显示图片到ImageView上, 注意ImageView的过期
     *
     * @param bitmaps       图片集合, 且保证图片都可用
     * @param suggestWidth  ImageLoader 计算出来的width
     * @param suggestHeight ImageLoader 计算出来的height
     */
    @Override
    @Nullable
    public Bitmap render(Context context, Bitmap[] bitmaps, int suggestWidth, int suggestHeight) {
        return combine(suggestWidth, suggestHeight, bitmaps);
    }
}
