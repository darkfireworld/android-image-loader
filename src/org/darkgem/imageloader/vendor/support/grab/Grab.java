package org.darkgem.imageloader.vendor.support.grab;

import android.graphics.Bitmap;
import org.darkgem.imageloader.vendor.LoadListener;

/**
 * 读取给定URI的图片文件
 */
public interface Grab {
    /**
     * 读取给定URI的图片
     *
     * @param uri      图片地址
     * @param width    加载图片宽度
     * @param height   加载图片长度
     * @param config   图片的配置
     * @param listener 加载监听器
     */
    void grab(String uri, int width, int height, Bitmap.Config config, LoadListener listener);

    /**
     * 判断是否支持该URI, 如果支持，则调用该grab
     *
     * @param uri 待测试的uri
     * @return 该grab是否支持解析指定uri
     */
    boolean support(String uri);
}
