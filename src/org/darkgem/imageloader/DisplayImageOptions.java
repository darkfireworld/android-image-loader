package org.darkgem.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import org.darkgem.imageloader.render.Render;
import org.darkgem.imageloader.render.SimpleRender;

/**
 * 图片展示选项
 */
public class DisplayImageOptions {
    //uri 为null的时候使用的背景图片, 如果为0，则表示使用null
    int imgEmptyId;
    //正在加载图片的时候使用的背景图片，如果为0，则表示使用null
    int imgLoadingId;
    //图片加载失败的时候使用的背景图片, 如果为0，则表示使用null
    int imgFailId;
    //是否保存到内存中
    boolean cacheInMemory;
    //图片大小
    int width;
    //图片大小
    int height;
    //默认图片质量
    Bitmap.Config config;
    //展示器
    Render render;

    private DisplayImageOptions(int imgEmptyId, int imgLoadingId, int imgFailId, boolean cacheInMemory, int width, int height, Bitmap.Config config, Render render) {
        this.imgEmptyId = imgEmptyId;
        this.imgLoadingId = imgLoadingId;
        this.imgFailId = imgFailId;
        this.cacheInMemory = cacheInMemory;
        this.width = width;
        this.height = height;
        this.config = config;
        this.render = render;
    }

    /**
     * uri 为null的时候使用的背景图片, 如果为0，则表示使用null
     */
    public int getImgEmptyId() {
        return imgEmptyId;
    }

    /**
     * 正在加载图片的时候使用的背景图片，如果为0，则表示使用null
     */
    public int getImgLoadingId() {
        return imgLoadingId;
    }

    /**
     * 图片加载失败的时候使用的背景图片, 如果为0，则表示使用null
     */
    public int getImgFailId() {
        return imgFailId;
    }

    /**
     * 该图片是否进行缓存到内存中
     */
    public boolean isCacheInMemory() {
        return cacheInMemory;
    }

    /**
     * 该图片需要被渲染的大小, 不能<=0
     */
    public int getWidth() {
        return width;
    }

    /**
     * 该图片需要被渲染的大小, 不能<=0
     */
    public int getHeight() {
        return height;
    }

    /**
     * 图片的质量
     */
    public Bitmap.Config getConfig() {
        return config;
    }

    /**
     * 获得图片本次展示的显示器
     */
    public Render getRender() {
        return render;
    }

    /**
     * 构造类
     */
    public static class Builder {
        //上下文
        Context context;
        //uri 为空的时候
        int imgEmptyId;
        //imageView 正在加载图片的时候
        int imgLoadingId;
        //uri 图片加载失败
        int imgFailId;
        //是否保存到内存中
        boolean cacheInMemory;
        //图片大小
        int width;
        //图片大小
        int height;
        //默认图片质量
        Bitmap.Config config;
        //展示器
        Render render;

        public Builder(Context context) {
            this.context = context;
            this.imgEmptyId = 0;
            this.imgLoadingId = 0;
            this.imgFailId = 0;
            this.cacheInMemory = false;
            this.width = 0;
            this.height = 0;
            this.config = null;
            this.render = null;
        }

        /**
         * 构造
         */
        public DisplayImageOptions build() {
            //参数矫正
            {
                //状态图片设置
                {
                    imgEmptyId = (imgEmptyId < 0 ? 0 : imgEmptyId);
                    imgLoadingId = (imgLoadingId < 0 ? 0 : imgLoadingId);
                    imgFailId = (imgFailId < 0 ? 0 : imgFailId);
                }
                if (width <= 0 || height <= 0) {
                    //使用屏幕大小
                    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                    width = displayMetrics.widthPixels;
                    height = displayMetrics.heightPixels;
                }
                if (config == null) {
                    //选择质量最差的配置
                    config = Bitmap.Config.RGB_565;
                }
                if (render == null) {
                    //选择一个简单的适配器
                    render = new SimpleRender();
                }
            }
            //创建一个对象
            return new DisplayImageOptions(
                    imgEmptyId,
                    imgLoadingId,
                    imgFailId,
                    cacheInMemory,
                    width,
                    height,
                    config,
                    render);
        }

        /**
         * 设置默认图片
         */
        public Builder setImgDefaultId(int imgDefault) {
            this.imgEmptyId = this.imgLoadingId = this.imgFailId = imgDefault;
            return this;
        }

        /**
         * 设置url为空的时候图片
         */
        public Builder setImgEmptyId(int imgEmptyId) {
            this.imgEmptyId = imgEmptyId;
            return this;
        }

        /**
         * 正在加载的图片
         */
        public Builder setImgLoadingId(int imgLoadingId) {
            this.imgLoadingId = imgLoadingId;
            return this;
        }

        /**
         * 加载失败的图片
         */
        public Builder setImgFailId(int imgFailId) {
            this.imgFailId = imgFailId;
            return this;
        }

        /**
         * 本图片是否缓存到内存中
         */
        public Builder setCacheInMemory(boolean cacheInMemory) {
            this.cacheInMemory = cacheInMemory;
            return this;
        }

        /**
         * 设置图片大小, 不能<=0
         */
        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        /**
         * 设置图片大小, 不能<=0
         */
        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        /**
         * 设置图片的质量
         */
        public Builder setConfig(Bitmap.Config config) {
            this.config = config;
            return this;
        }

        /**
         * 设置图片的显示器
         */
        public Builder setRender(Render render) {
            this.render = render;
            return this;
        }
    }
}
