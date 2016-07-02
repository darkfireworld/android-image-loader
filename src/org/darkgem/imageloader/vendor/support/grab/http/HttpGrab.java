package org.darkgem.imageloader.vendor.support.grab.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import org.darkgem.imageloader.vendor.LoadListener;
import org.darkgem.imageloader.vendor.support.grab.Grab;
import org.darkgem.imageloader.vendor.support.grab.http.disk.DiskCache;
import org.darkgem.imageloader.vendor.support.grab.http.disk.impl.LruDiskCache;
import org.darkgem.imageloader.vendor.support.grab.http.disk.name.Md5FileNameGenerator;
import org.darkgem.imageloader.vendor.support.util.BitmapUtil;
import org.darkgem.imageloader.vendor.support.util.IoUtil;
import org.darkgem.imageloader.vendor.support.util.StorageUtil;
import org.darkgem.imageloader.vendor.support.util.Tuple;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 从网络加载图片
 */
public class HttpGrab implements Grab {
    /**
     * 可能为空
     */
    DiskCache diskCache;
    /**
     * 失效URL集合,避免重复请求
     */
    Set<String> invalidUrlSet;
    /**
     * URL 锁
     */
    Map<String, Tuple<ReentrantLock, Integer>> lockMap;

    /**
     * 创建Http捕获器
     *
     * @param context       上下文
     * @param diskCacheSize 磁盘缓存大小
     * @param executor      线程池
     */
    public HttpGrab(Context context, int diskCacheSize, ExecutorService executor) {
        this.invalidUrlSet = new HashSet<String>();
        this.lockMap = new HashMap<String, Tuple<ReentrantLock, Integer>>();
        try {
            this.diskCache = new LruDiskCache(StorageUtil.getIndividualCacheDirectory(context),
                    new Md5FileNameGenerator(), diskCacheSize, executor);
        } catch (Exception e) {
            Log.e(HttpGrab.class.getName(), e.getMessage(), e);
            this.diskCache = null;
        }
    }

    /**
     * 锁定某个url
     */
    protected void lock(String url) {
        ReentrantLock lock;
        synchronized (this) {
            //创建URI锁
            Tuple<ReentrantLock, Integer> tuple = lockMap.get(url);
            if (tuple == null) {
                tuple = new Tuple<ReentrantLock, Integer>(new ReentrantLock(), 0);
                lockMap.put(url, tuple);
            }
            lock = tuple._1;
            //引用计数++
            tuple._2++;
        }
        lock.lock();
    }

    /**
     * 解锁
     */
    protected void unlock(String url) {
        synchronized (this) {
            Tuple<ReentrantLock, Integer> tuple = lockMap.get(url);
            if (tuple != null) {
                tuple._1.unlock();
                tuple._2--;
                //如果引用计数=0，则清空
                if (tuple._2 == 0) {
                    lockMap.remove(url);
                }
            }
        }
    }

    /**
     * 记录某个URL失效
     */
    synchronized protected void invalidUrl(String url) {
        if (url != null) {
            invalidUrlSet.add(url);
        }
    }

    /**
     * 判断URL是否失效
     */
    synchronized protected boolean isInvalidUrl(String url) {
        if (url == null || invalidUrlSet.contains(url)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 读取给定URI的图片
     *
     * @param uri      图片地址
     * @param width    加载图片宽度
     * @param height   加载图片长度
     * @param config   图片配置
     * @param listener 加载监听器
     */
    @Override
    public void grab(String uri, int width, int height, Bitmap.Config config, final LoadListener listener) {
        if (getDiskCache() == null) {
            listener.onLoadFailed(new Exception("Can't Create DiskCache"));
            return;
        }
        if (isInvalidUrl(uri)) {
            listener.onLoadFailed(new Exception("INVALID URL"));
            return;
        }
        File imageFile = null;
        doJob:
        {
            //缓存中还是不存在URI对应的图片，则开始下载图片
            InputStream is = null;
            try {
                //对该URI进行枷锁, 避免多个HTTP同时下载
                lock(uri);
                //检测图片是否已经被下载完成
                imageFile = getDiskCache().get(uri);
                //如果存在图片, 则跳过
                if (imageFile != null) {
                    break doJob;
                }
                //未被标记为无效URL，则继续操作
                //创建网络IO
                is = getStreamFromNetwork(uri);
                //如果 diskCache 为空, 则直接失败
                boolean ok = getDiskCache().save(uri, is, new IoUtil.CopyListener() {
                    @Override
                    public boolean onBytesCopied(int current, int total) {
                        listener.onLoadProgress(current, total);
                        return true;
                    }
                });
                //如果缓存文件创建成功, 则给定image对象
                if (ok) {
                    imageFile = getDiskCache().get(uri);
                }
            } catch (Exception e) {
                Log.e(HttpGrab.class.getName(), e.getMessage(), e);
            } finally {
                //关闭流
                IoUtil.closeSilently(is);
                //解锁
                unlock(uri);
            }
        }

        if (imageFile == null) {
            //加载失败, 不存在该图片
            listener.onLoadFailed(new Exception("Can't Load Image : " + uri));
            return;
        }
        //如果存在图片文件, 则标记Ok
        Bitmap bitmap = BitmapUtil.getBitmap(imageFile.getAbsolutePath(), width, height, config);
        if (bitmap != null) {
            listener.onLoadSuccess(bitmap);
        } else {
            //加载失败, 不存在该图片
            listener.onLoadFailed(new Exception("Can't Load Image : " + uri));
        }
    }

    @Override
    public boolean support(String uri) {
        return Scheme.getScheme(uri) != null;
    }

    @Nullable
    public DiskCache getDiskCache() {
        return diskCache;
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in the network).
     *
     * @param imageUri Image URI
     * @return {@link InputStream} of image
     * @throws IOException if some I/O error occurs during network request or if no InputStream could be created for
     *                     URL.
     */
    protected InputStream getStreamFromNetwork(String imageUri) throws IOException {
        HttpURLConnection conn = createConnection(imageUri);

        InputStream imageStream = null;
        try {
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            //如果查找不到图片，则直接记录失效记录表中，避免进行额外的网络请求
            if (conn.getResponseCode() == 404) {
                invalidUrl(imageUri);
            }
            IoUtil.readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        if (conn.getResponseCode() != 200) {
            IoUtil.closeSilently(imageStream);
            throw new IOException("Image request failed with response code " + conn.getResponseCode());
        }
        return new ContentLengthInputStream(new BufferedInputStream(imageStream, 32 * 1024), conn.getContentLength());
    }

    /**
     * Create {@linkplain HttpURLConnection HTTP connection} for incoming URL
     *
     * @param url URL to connect to
     * @return {@linkplain HttpURLConnection Connection} for incoming URL. Connection isn't established so it still configurable.
     * @throws IOException if some I/O error occurs during network request or if no InputStream could be created for
     *                     URL.
     */
    protected HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~'%");
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(1000 * 5);
        conn.setReadTimeout(1000 * 10);
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        return conn;
    }

    /**
     * 路径协议
     */
    public enum Scheme {
        HTTP("http://"),
        HTTPS("https://");
        String scheme;

        Scheme(String scheme) {
            this.scheme = scheme;
        }

        @Nullable
        public static Scheme getScheme(String uri) {
            if (uri == null) {
                return null;
            }
            if (uri.toLowerCase().startsWith(HTTP.scheme)) {
                return HTTP;
            }
            if (uri.toLowerCase().startsWith(HTTPS.scheme)) {
                return HTTPS;
            }
            return null;
        }
    }

    /**
     * Decorator for {@link InputStream InputStream}. Provides possibility to return defined stream length by
     * {@link #available()} method.
     *
     * @author Sergey Tarasevich (nostra13[at]gmail[dot]com), Mariotaku
     * @since 1.9.1
     */
    public static class ContentLengthInputStream extends InputStream {

        private final InputStream stream;
        private final int length;

        public ContentLengthInputStream(InputStream stream, int length) {
            this.stream = stream;
            this.length = length;
        }

        @Override
        public int available() {
            return length;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        @Override
        public void mark(int readLimit) {
            stream.mark(readLimit);
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return stream.read(buffer);
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            return stream.read(buffer, byteOffset, byteCount);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public long skip(long byteCount) throws IOException {
            return stream.skip(byteCount);
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }
    }
}
