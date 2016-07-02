package org.darkgem.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import org.darkgem.imageloader.vendor.LoadListener;
import org.darkgem.imageloader.vendor.Vendor;
import org.darkgem.imageloader.vendor.support.DefaultVendor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageLoader {
    static volatile ImageLoader instance;
    //是否已经初始化
    boolean initialized = false;
    //上下文
    Context context;
    //任务管理器
    Map<Integer, Object> taskMap = null;
    //默认图片显示配置
    DisplayImageOptions defaultDisplayImageOptions = null;
    //资源提供者
    Vendor vendor = null;
    //默认的图片加载监听器
    ImageLoadListener defaultImageLoadListener = null;
    //handler
    Handler handler;

    /**
     * 单例模式
     */
    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化ImageLoader对象, 且只能调用一次
     */
    synchronized public void init(Context context, DisplayImageOptions options, Vendor vendor) {
        if (this.initialized) {
            throw new RuntimeException("init() method is called");
        }
        //表示已经初始化
        this.initialized = true;
        this.context = context;
        this.taskMap = new ConcurrentHashMap<Integer, Object>();
        this.defaultDisplayImageOptions = (options == null ? new DisplayImageOptions.Builder(context).build() : options);
        this.vendor = (vendor == null ? new DefaultVendor(context) : vendor);
        this.defaultImageLoadListener = new ImageLoadListener() {
            @Override
            public void onLoadStart() {

            }

            @Override
            public void onLoadSuccess(Bitmap[] bitmap) {

            }

            @Override
            public void onLoadFailed(Exception e) {

            }

            @Override
            public void onLoadProgress(int pg) {

            }

            @Override
            public void onLoadCancel() {

            }
        };
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 指定uri的图片显示到view中, 使用默认 显示选项
     *
     * @param uri  图片地址
     * @param view ImageView对象
     */
    public void displayImage(String uri, ImageView view) {
        displayImage(new String[]{uri}, view, null, null);
    }

    /**
     * 指定uri的图片显示到view中, 使用默认 显示选项
     *
     * @param uri      图片地址
     * @param view     ImageView对象
     * @param listener 加载监听器
     */
    public void displayImage(String uri, ImageView view, ImageLoadListener listener) {
        displayImage(new String[]{uri}, view, null, listener);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uri      图片地址
     * @param view     ImageView对象
     * @param options0 显示选项
     */
    public void displayImage(String uri, final ImageView view, DisplayImageOptions options0) {
        displayImage(new String[]{uri}, view, options0, null);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uri      图片地址
     * @param view     ImageView对象
     * @param options0 显示选项
     * @param listener 加载监听器
     */
    public void displayImage(String uri, final ImageView view, DisplayImageOptions options0, ImageLoadListener listener) {
        displayImage(new String[]{uri}, view, options0, listener);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uris 图片地址
     * @param view ImageView对象
     */
    public void displayImage(String[] uris, final ImageView view) {
        displayImage(uris, view, null, null);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uris     图片地址
     * @param view     ImageView对象
     * @param listener 加载监听器
     */
    public void displayImage(String[] uris, final ImageView view, ImageLoadListener listener) {
        displayImage(uris, view, null, listener);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uris      图片地址
     * @param imageView ImageView对象
     * @param options   显示选项
     */
    public void displayImage(String[] uris, final ImageView imageView, DisplayImageOptions options) {
        displayImage(uris, imageView, options, null);
    }

    /**
     * 指定uri的图片显示到view中
     *
     * @param uris      图片地址
     * @param imageView ImageView对象
     * @param options0  显示选项
     * @param listener0 加载监听器
     */
    public void displayImage(final @Nullable String[] uris, @Nullable final ImageView imageView,
                             final @Nullable DisplayImageOptions options0, final @Nullable ImageLoadListener listener0) {
        //前置处理
        {
            if (!initialized) {
                throw new RuntimeException("init() method Don't called");
            }
            //参数检测
            if (imageView == null) {
                return;
            }
            //取消之前的task,避免之前的任务加载完毕，继续操作
            taskMap.remove(System.identityHashCode(imageView));
        }
        //整理参数
        //图片加载选项
        final DisplayImageOptions options = (options0 == null ? defaultDisplayImageOptions : options0);
        //图片加载监听器
        final ImageLoadListener listener = (listener0 == null ? defaultImageLoadListener : listener0);
        //要显示的图片
        final List<String> uriList = new ArrayList<String>();
        //整理uri
        {
            if (uris != null) {
                for (String uri : uris) {
                    if (uri == null) {
                        //如果存在空资源图片，则使用该图片
                        if (options.getImgEmptyId() > 0) {
                            uriList.add("drawable://" + options.getImgEmptyId());
                        }
                    } else {
                        uriList.add(uri);
                    }
                }
            }
            //不存在任何图片,则给定一个空的图片资源
            if (uriList.size() == 0) {
                //如果存在空资源图片，则使用该图片
                if (options.getImgEmptyId() > 0) {
                    uriList.add("drawable://" + options.getImgEmptyId());
                }
            }
        }
        //如果URI List为空, 则把对应ImageView 置空, 并且返回
        if (uriList.size() == 0) {
            imageView.setImageBitmap(null);
            return;
        }
        //处理加载图片前奏
        {
            //如果给定了加载图片，则使用它，否则清空之前的图片，
            //这个主要是为了避免ListView滑动的时候，图片加载过慢导致出现图片位置错误
            if (options.getImgLoadingId() > 0) {
                imageView.setImageResource(options.getImgLoadingId());
            } else {
                imageView.setImageDrawable(null);
            }
        }
        //进行加载图片
        {
            //目前为止, 所有的参数都有效, 并且关闭了 之前在这个ImageView上的任务
            //配置图片大小参数
            final int suggestWidth = SizeUtils.getImageViewWidth(imageView, options.getWidth());
            final int suggestHeight = SizeUtils.getImageViewHeight(imageView, options.getHeight());
            //设计加载监听器
            final LoadListener loadListener = new LoadListener() {
                //该任务是否已经取消
                boolean cancel = false;
                //当前已经加载/失败的图片数量
                int triggerCount = 0;
                //可以使用的图片, 注意当任务失败/完成后, 清理资源
                List<Bitmap> bitmapList = new LinkedList<Bitmap>();

                //执行器
                class Execute implements Runnable {
                    Object task;
                    Bitmap bitmap;

                    public Execute(Object task, Bitmap bitmap) {
                        this.task = task;
                        this.bitmap = bitmap;
                    }

                    @Override
                    public void run() {
                        try {
                            //检测该任务是否已经被取消
                            {
                                //如果现在任务表中的任务对象，不为当前操作对象，则表示该任务被取消了
                                if (task != taskMap.get(System.identityHashCode(imageView))) {
                                    //如果任务之前没有被取消，则现在进行取消，并且进行通知
                                    if (!cancel) {
                                        cancel = true;
                                        //任务取消
                                        listener.onLoadCancel();
                                    }
                                    return;
                                }
                            }
                            //加入图片集合
                            {
                                if (bitmap != null) {
                                    bitmapList.add(bitmap);
                                }
                                triggerCount++;
                            }
                            //进度通知，仅仅当要加载的图片数量大于1的时候
                            {
                                if (uriList.size() > 1) {
                                    listener.onLoadProgress((triggerCount * 100) / uriList.size());
                                }
                            }
                            //检测是否到达目标数量
                            {
                                if (uriList.size() <= triggerCount) {
                                    //加载的数量符合条件,从任务队列删除
                                    taskMap.remove(System.identityHashCode(imageView));
                                    boolean ok = false;
                                    //处理图片
                                    {
                                        Bitmap[] bitmapArray = bitmapList.toArray(new Bitmap[0]);
                                        if (bitmapArray.length != 0) {
                                            Bitmap bitmap = options.getRender().render(context, bitmapArray, suggestWidth, suggestHeight);
                                            if (imageView != null && bitmap != null) {
                                                imageView.setImageBitmap(bitmap);
                                            }
                                            ok = true;
                                            //加载成功
                                            listener.onLoadSuccess(bitmapArray);
                                        }
                                    }
                                    //如果没有加载成功，则使用失败的图片
                                    {
                                        if (!ok) {
                                            if (imageView != null) {
                                                if (options.getImgFailId() > 0) {
                                                    imageView.setImageResource(options.getImgFailId());
                                                } else {
                                                    imageView.setImageDrawable(null);
                                                }
                                            }
                                            //抛出异常
                                            throw new Exception("No Collect Bitmap");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(ImageLoader.class.getName(), e.getMessage(), e);
                            //如果发生任何异常, 则剔除这个任务
                            taskMap.remove(System.identityHashCode(imageView));
                            try {
                                listener.onLoadFailed(e);
                            } catch (Exception e0) {
                                //静默处理
                                Log.e(ImageLoader.class.getName(), e0.getMessage(), e0);
                            }

                        }
                    }
                }

                @Override
                public void onLoadSuccess(final Bitmap bitmap) {
                    Execute execute = new Execute(this, bitmap);
                    //判断线程是否为主线程
                    if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                        execute.run();
                    } else {
                        //在主线程中运行
                        handler.post(execute);
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    Execute execute = new Execute(this, null);
                    //判断线程是否为主线程
                    if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                        execute.run();
                    } else {
                        //在主线程中运行
                        handler.post(execute);
                    }
                }

                @Override
                public void onLoadProgress(final int current, final int total) {
                    //只有一张图片的时候，才进行进度通知，否则按照张数完成度通知
                    if (uriList.size() == 1) {
                        //判断线程是否为主线程
                        if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                            listener.onLoadProgress(current * 100 / total);
                        } else {
                            //在主线程中运行
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onLoadProgress((current * 100) / total);
                                }
                            });
                        }
                    }
                }
            };
            //加入任务管理器
            taskMap.put(System.identityHashCode(imageView), loadListener);
            //开始加载
            listener.onLoadStart();
            //批量加载图片
            for (final String uri : uriList) {
                //加载
                vendor.loadImage(uri, suggestWidth, suggestHeight, options.getConfig(), options.isCacheInMemory(), loadListener);
            }
        }
    }

    /**
     * 获取资源提供者
     */
    public <T extends Vendor> T getVendor() {
        return (T) vendor;
    }


    /**
     * 图片大小计算帮助
     */
    static class SizeUtils {
        /**
         * 读取ImageView的属性
         */
        static int getImageViewFieldValue(Object object, String fieldName) {
            int value = 0;
            try {
                Field field = ImageView.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                int fieldValue = (Integer) field.get(object);
                if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                    value = fieldValue;
                }
            } catch (Exception e) {
                Log.e(ImageLoader.class.getName(), e.getMessage(), e);
            }
            return value;
        }

        /**
         * 读取图片的大小
         */
        static int getImageViewWidth(ImageView imageView, int defaultWidth) {

            int width = 0;
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                width = imageView.getWidth(); // Get actual image width
            }
            if (width <= 0 && params != null) {
                width = params.width; // Get layout width parameter
            }
            if (width <= 0) {
                width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check maxWidth parameter
            }
            if (width <= 0) {
                width = imageView.getMeasuredWidth();
            }
            if (width <= 0) {
                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                imageView.measure(w, h);
                width = imageView.getMeasuredWidth();
            }
            if (width <= 0) {
                width = defaultWidth;
            }
            return width;
        }

        /**
         * 读取图片的大小
         */
        static int getImageViewHeight(ImageView imageView, int defaultHeight) {
            int height = 0;
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            if (params != null && params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = imageView.getHeight(); // Get actual image height
            }
            if (height <= 0 && params != null) {
                height = params.height; // Get layout height parameter
            }
            if (height <= 0) {
                height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check maxHeight parameter
            }
            if (height <= 0) {
                height = imageView.getMeasuredHeight();
            }
            if (height <= 0) {
                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                imageView.measure(w, h);
                height = imageView.getMeasuredHeight();
            }
            if (height <= 0) {
                height = defaultHeight;
            }
            return height;

        }
    }
}
