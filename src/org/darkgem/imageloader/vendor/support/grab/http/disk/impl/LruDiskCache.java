/**
 * ****************************************************************************
 * Copyright 2014 Sergey Tarasevich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package org.darkgem.imageloader.vendor.support.grab.http.disk.impl;

import android.graphics.Bitmap;
import android.util.Log;
import org.darkgem.imageloader.vendor.support.grab.http.disk.DiskCache;
import org.darkgem.imageloader.vendor.support.grab.http.disk.name.FileNameGenerator;
import org.darkgem.imageloader.vendor.support.util.IoUtil;

import java.io.*;
import java.util.concurrent.ExecutorService;

/**
 * Disk cache based on "Least-Recently Used" principle. Adapter pattern, adapts
 * {@link DiskLruCache DiskLruCache} to
 * {@link DiskCache}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see FileNameGenerator
 * @since 1.9.2
 */
public class LruDiskCache implements DiskCache {
    /**
     * {@value
     */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 Kb
    /**
     * {@value
     */
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    /**
     * {@value
     */
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final String ERROR_ARG_NULL = " argument must be not null";
    private static final String ERROR_ARG_NEGATIVE = " argument must be positive number";
    protected final FileNameGenerator fileNameGenerator;
    protected DiskLruCache cache;
    protected int bufferSize = DEFAULT_BUFFER_SIZE;
    protected Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = DEFAULT_COMPRESS_QUALITY;
    /**
     * 线程池
     */
    ExecutorService executor;
    private File reserveCacheDir;

    /**
     * @param cacheDir          Directory for file caching
     * @param fileNameGenerator {@linkplain FileNameGenerator
     *                          Name generator} for cached files. Generated names must match the regex
     *                          <strong>[a-z0-9_-]{1,64}</strong>
     * @param cacheMaxSize      Max cache size in bytes. <b>0</b> means cache size is unlimited.
     * @param executor          线程池, 可以为空
     * @throws IOException if cache can't be initialized (e.g. "No space left on device")
     */
    public LruDiskCache(File cacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize, ExecutorService executor) throws IOException {
        this(cacheDir, null, fileNameGenerator, cacheMaxSize, 0, executor);
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param reserveCacheDir   null-ok; Reserve directory for file caching. It's used when the primary directory isn't available.
     * @param fileNameGenerator {@linkplain FileNameGenerator
     *                          Name generator} for cached files. Generated names must match the regex
     *                          <strong>[a-z0-9_-]{1,64}</strong>
     * @param cacheMaxSize      Max cache size in bytes. <b>0</b> means cache size is unlimited.
     * @param cacheMaxFileCount Max file count in cache. <b>0</b> means file count is unlimited.
     * @param executor          执行器, 可以为空
     * @throws IOException if cache can't be initialized (e.g. "No space left on device")
     */
    public LruDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize,
                        int cacheMaxFileCount, ExecutorService executor) throws IOException {
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
        }
        if (cacheMaxSize < 0) {
            throw new IllegalArgumentException("cacheMaxSize" + ERROR_ARG_NEGATIVE);
        }
        if (cacheMaxFileCount < 0) {
            throw new IllegalArgumentException("cacheMaxFileCount" + ERROR_ARG_NEGATIVE);
        }
        if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
        }

        if (cacheMaxSize == 0) {
            cacheMaxSize = Long.MAX_VALUE;
        }
        if (cacheMaxFileCount == 0) {
            cacheMaxFileCount = Integer.MAX_VALUE;
        }

        this.reserveCacheDir = reserveCacheDir;
        this.fileNameGenerator = fileNameGenerator;
        this.executor = executor;
        initCache(cacheDir, reserveCacheDir, cacheMaxSize, cacheMaxFileCount);
    }

    /**
     * 初始化缓存
     *
     * @param cacheDir          缓存目录
     * @param reserveCacheDir   保留缓存目录
     * @param cacheMaxSize      缓存大小
     * @param cacheMaxFileCount 缓存文件数量
     */
    private void initCache(File cacheDir, File reserveCacheDir, long cacheMaxSize, int cacheMaxFileCount)
            throws IOException {
        try {
            cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount, executor);
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
            if (reserveCacheDir != null) {
                initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
            }
            if (cache == null) {
                throw e; //new RuntimeException("Can't initialize disk cache", e);
            }
        }
    }

    @Override
    public File getDirectory() {
        return cache.getDirectory();
    }

    @Override
    public File get(String imageUri) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = cache.get(getKey(imageUri));
            return snapshot == null ? null : snapshot.getFile(0);
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
            return null;
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    @Override
    public boolean save(String imageUri, InputStream imageStream, IoUtil.CopyListener listener) throws IOException {
        DiskLruCache.Editor editor = cache.edit(getKey(imageUri));
        if (editor == null) {
            return false;
        }

        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), bufferSize);
        boolean copied = false;
        try {
            copied = IoUtil.copyStream(imageStream, os, listener, bufferSize);
        } finally {
            IoUtil.closeSilently(os);
            if (copied) {
                editor.commit();
            } else {
                editor.abort();
            }
        }
        return copied;
    }

    @Override
    public boolean remove(String imageUri) {
        try {
            return cache.remove(getKey(imageUri));
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void close() {
        try {
            cache.close();
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
        }
        cache = null;
    }

    @Override
    public void clear() {
        try {
            cache.delete();
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
        }
        try {
            initCache(cache.getDirectory(), reserveCacheDir, cache.getMaxSize(), cache.getMaxFileCount());
        } catch (IOException e) {
            Log.e(LruDiskCache.class.getName(), e.getMessage(), e);
        }
    }

    private String getKey(String imageUri) {
        return fileNameGenerator.generate(imageUri);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }
}
