package org.darkgem.imageloader.vendor.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import org.darkgem.imageloader.vendor.LoadListener;
import org.darkgem.imageloader.vendor.Vendor;
import org.darkgem.imageloader.vendor.support.grab.Grab;
import org.darkgem.imageloader.vendor.support.mc.LruMemoryCache;
import org.darkgem.imageloader.vendor.support.mc.MemoryCache;

import java.util.concurrent.ExecutorService;

/**
 * 带有基本缓存的Vendor
 */
abstract public class MemoryCacheVendor implements Vendor {
    MemoryCache memoryCache;
    ExecutorService pool;
    Context context;

    /**
     * 构造函数
     *
     * @param context         上下文
     * @param memoryCacheSize 内存缓存大小
     * @param executorService 线程池
     */
    public MemoryCacheVendor(Context context, int memoryCacheSize, ExecutorService executorService) {
        this.context = context;
        this.memoryCache = new LruMemoryCache(memoryCacheSize);
        this.pool = executorService;
    }

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
    public void loadImage(final String uri, final int width, final int height, final Bitmap.Config config,
                          final boolean cacheInMemory, final LoadListener listener) {
        //查询缓存是否存在
        Bitmap bitmap = memoryCache.get(uri, width, height);
        if (bitmap != null) {
            listener.onLoadSuccess(bitmap);
        } else {
            //抓取图片
            try {
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        Grab grab = getGrab(uri);
                        if (grab == null) {
                            listener.onLoadFailed(new Exception(uri + "不能被支持"));
                        } else {
                            LoadListener loadListener = listener;
                            if (cacheInMemory) {
                                //如果加载成功, 则需要保存到缓存当中
                                loadListener = new LoadListener() {
                                    @Override
                                    public void onLoadSuccess(Bitmap bitmap) {
                                        memoryCache.put(uri, width, height, bitmap);
                                        listener.onLoadSuccess(bitmap);
                                    }

                                    @Override
                                    public void onLoadFailed(Exception e) {
                                        listener.onLoadFailed(e);
                                    }

                                    @Override
                                    public void onLoadProgress(int current, int total) {
                                        listener.onLoadProgress(current, total);
                                    }
                                };
                            }
                            try {
                                //抓取
                                grab.grab(uri, width, height, config, loadListener);
                            } catch (Exception e) {
                                listener.onLoadFailed(e);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                listener.onLoadFailed(e);
            }
        }
    }

    /**
     * 获取一个具体的Grab对象, 如果不支持该URI，则返回null
     */
    @Nullable
    abstract protected Grab getGrab(String uri);
}
