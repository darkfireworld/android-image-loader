package com.example.imageloader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import org.darkgem.imageloader.vendor.support.MemoryCacheVendor;
import org.darkgem.imageloader.vendor.support.grab.Grab;
import org.darkgem.imageloader.vendor.support.grab.drawable.DrawableGrab;
import org.darkgem.imageloader.vendor.support.grab.file.FileGrab;
import org.darkgem.imageloader.vendor.support.grab.http.HttpGrab;
import org.darkgem.imageloader.vendor.support.grab.http.disk.DiskCache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtVendor extends MemoryCacheVendor {
    List<Grab> grabList = new ArrayList<Grab>(3);
    ExtHttpGrab httpGrab;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public ExtVendor(Context context) {
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
    private ExtVendor(Context context, int memoryCacheSize, int diskCacheSize, ExecutorService executorService) {
        super(context, memoryCacheSize, executorService);
        httpGrab = new ExtHttpGrab(context, diskCacheSize, executorService);
        grabList.add(httpGrab);
        grabList.add(new DrawableGrab(context));
        grabList.add(new FileGrab());
    }

    @Nullable
    @Override
    protected Grab getGrab(String uri) {
        for (Grab grab : grabList) {
            if (grab.support(uri)) {
                return grab;
            }
        }
        return null;
    }

    public ExtHttpGrab getHttpGrab() {
        return httpGrab;
    }

    /**
     * 自定义HttpGrab, 支持直接设置url指向的资源
     */
    public static class ExtHttpGrab extends HttpGrab {

        /**
         * 创建Http捕获器
         *
         * @param context       上下文
         * @param diskCacheSize 磁盘缓存大小
         * @param executor      线程池
         */
        public ExtHttpGrab(Context context, int diskCacheSize, ExecutorService executor) {
            super(context, diskCacheSize, executor);
        }

        /**
         * 直接指定url指向data
         *
         * @param url  http地址
         * @param data 图片二进制
         */
        public void direct(String url, byte[] data) {

            try {
                lock(url);
                DiskCache cache = getDiskCache();
                if (cache != null) {
                    cache.save(url, new ByteArrayInputStream(data), null);
                }
            } catch (Exception e) {
                Log.e(ExtVendor.class.getName(), e.getMessage(), e);
            } finally {
                unlock(url);
            }
        }

        /**
         * 直接指定url指向file
         *
         * @param url  http地址
         * @param file 本地文件图片
         */
        public void direct(String url, File file) {
            try {
                lock(url);
                DiskCache cache = getDiskCache();
                if (cache != null) {
                    cache.save(url, new FileInputStream(file), null);
                }
            } catch (Exception e) {
                Log.e(ExtVendor.class.getName(), e.getMessage(), e);
            } finally {
                unlock(url);
            }
        }

        /**
         * 直接指定url指向path
         *
         * @param url  http 地址
         * @param path 文件路径，eg: /sdcard/xxx/aa.jpg
         */
        public void direct(String url, String path) {
            try {
                lock(url);
                DiskCache cache = getDiskCache();
                if (cache != null) {
                    cache.save(url, new FileInputStream(new File(path)), null);
                }
            } catch (Exception e) {
                Log.e(ExtVendor.class.getName(), e.getMessage(), e);
            } finally {
                unlock(url);
            }
        }
    }
}
