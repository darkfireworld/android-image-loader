package org.darkgem.imageloader.vendor.support.grab.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.darkgem.imageloader.vendor.LoadListener;
import org.darkgem.imageloader.vendor.support.grab.Grab;

/**
 * Created by Administrator on 2015/7/7.
 */
public class DrawableGrab implements Grab {
    final static String SCHEME = "drawable://";
    Resources resources;

    public DrawableGrab(Context context) {
        resources = context.getResources();
    }

    /**
     * 读取给定URI的图片
     *
     * @param uri      图片地址
     * @param width    加载图片宽度
     * @param height   加载图片长度
     * @param config   图片的配置
     * @param listener 加载监听器
     */
    @Override
    public void grab(String uri, int width, int height, Bitmap.Config config, LoadListener listener) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(resources, Integer.parseInt(uri.substring(SCHEME.length())));
        } catch (Exception e) {
            Log.e(DrawableGrab.class.getName(), e.getMessage(), e);
        }
        if (bitmap != null) {
            listener.onLoadSuccess(bitmap);
        } else {
            //加载失败, 不存在该图片
            listener.onLoadFailed(new Exception("Can't Load Image : " + uri));
        }
    }

    @Override
    public boolean support(String uri) {
        return uri.toLowerCase().startsWith(SCHEME);
    }
}
