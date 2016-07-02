package org.darkgem.imageloader.vendor.support;

import android.content.Context;
import android.support.annotation.Nullable;
import org.darkgem.imageloader.vendor.support.grab.Grab;
import org.darkgem.imageloader.vendor.support.grab.drawable.DrawableGrab;
import org.darkgem.imageloader.vendor.support.grab.file.FileGrab;
import org.darkgem.imageloader.vendor.support.grab.http.HttpGrab;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 默认Vendor，支持http://，drawable://, file://
 */
public class DefaultVendor extends MemoryCacheVendor {
    List<Grab> grabList = new ArrayList<Grab>(3);

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public DefaultVendor(Context context) {
        this(context, 16 * 1024 * 1024, 32 * 1024 * 1024, Executors.newCachedThreadPool());
    }

    /**
     * 构造函数
     *
     * @param context         上下文
     * @param memoryCacheSize 内存缓存大小
     * @param diskCacheSize   磁盘缓存大小
     * @param executorService 线程池
     */
    private DefaultVendor(Context context, int memoryCacheSize, int diskCacheSize, ExecutorService executorService) {
        super(context, memoryCacheSize, executorService);
        grabList.add(new HttpGrab(context, diskCacheSize, executorService));
        grabList.add(new DrawableGrab(context));
        grabList.add(new FileGrab());
    }

    @Nullable
    @Override
    public Grab getGrab(String uri) {
        for (Grab grab : grabList) {
            if (grab.support(uri)) {
                return grab;
            }
        }
        return null;
    }
}
