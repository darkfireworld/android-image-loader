package org.darkgem.imageloader.vendor.support.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

public class BitmapUtil {
    /**
     * 通过文件地址, 获取某一个图片的bitmap
     *
     * @param file   图片地址
     * @param width  图片宽度
     * @param height 图片的高度
     * @param config 图片格式
     * @return 经过大小处理的bitmap
     */
    @Nullable
    static public Bitmap getBitmap(String file, int width, int height, Bitmap.Config config) {
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            //开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(file, newOpts);//此时返回bm为空
            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
            float hh = height;
            float ww = width;
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            int be = 1;//be=1表示不缩放
            if (w >= h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) Math.rint(w / ww);
            } else if (w <= h && h > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (int) Math.rint(h / hh);
            }
            if (be <= 0)
                be = 1;
            newOpts.inSampleSize = be;//设置缩放比例
            newOpts.inPreferredConfig = config;//设置图片格式
            //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
            bitmap = BitmapFactory.decodeFile(file, newOpts);
            return bitmap;
        } catch (Exception e) {
            Log.e(BitmapUtil.class.getName(), e.getMessage(), e);
        }
        return null;

    }
}
