package org.darkgem.imageloader.vendor.support.grab.file;

import android.graphics.Bitmap;
import org.darkgem.imageloader.vendor.LoadListener;
import org.darkgem.imageloader.vendor.support.grab.Grab;
import org.darkgem.imageloader.vendor.support.util.BitmapUtil;

/**
 * Created by Administrator on 2015/7/7.
 */
public class FileGrab implements Grab {
    static final String SCHEME = "file://";

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

        //如果存在图片文件, 则标记Ok
        Bitmap bitmap = BitmapUtil.getBitmap(uri.substring(SCHEME.length()), width, height, config);
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
