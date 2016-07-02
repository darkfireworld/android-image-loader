package com.example.imageloader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import org.darkgem.imageloader.DisplayImageOptions;
import org.darkgem.imageloader.ImageLoader;
import org.darkgem.imageloader.render.CombineRender;
import org.darkgem.imageloader.render.SimpleRender;

import java.io.ByteArrayOutputStream;

public class MyActivity extends Activity {
    ListView lv_main;
    String[] strs = new String[]{
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg4ogFIGO-eDnJJG8EErEBAJTfsI",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg54776zG_sKdacOWaDlkujnB91J",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fg6yltXDR7HJzQjkje_65aVVr6CY",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FgCSfXf5XAzEzZtyFzNnMG8NR1LR",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FgDJerFUMPLtrZvJ9xa42A34DntI",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FgMmEHB2bP3VfC6kOKLDODmex9aA",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FgZpfDHf8mCKn-b42OQEAIV--chX",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fgcfo4tmpt3PbBa7ejzcNB384Cam",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fgorpr05C7ukL-JktYfvuFuKBdQw",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FnRwLW_hSNKjRFTi0g9UcKB_DOtI",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fn_O58pOVGaGxaILxODfM2aINf9U",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fnm6VfXCX3qHvrRQG2Q0EkYZgQ2j",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FnmjrbXdqBdCvddyn-9vW4gl9P-T",
            null,
            null,
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fnru8CcqDnIgKeKIVeIZ_ZyxAOoc",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FnykPstW4K6cE74g-QbELmCHtGBk",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fo3QtX1E_-7B7IOviWnc4JYpHWUx",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fo7WTQKIwaQL-Jz8NTZvGa5e7iwc",
            "http://7xjlyc.com1.z0.glb.clouddn.com/Fs6rm3viWbpZxzw7yNmr-sYTHtHI",
            "http://7xjlyc.com1.z0.glb.clouddn.com/FsFX6zmAeoN9AkkPrWuCm2Ee0rS2"
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //组合头像
        final DisplayImageOptions combine = new DisplayImageOptions.Builder(this)
                .setCacheInMemory(true)
                .setRender(new CombineRender())
                .setImgEmptyId(R.drawable.im_notice_doc_icon_able)
                .setImgLoadingId(R.drawable.refresh_black)
                .setImgFailId(R.drawable.chat_fail_resend_normal)
                .build();
        //组合头像
        final DisplayImageOptions normal = new DisplayImageOptions.Builder(this)
                .setCacheInMemory(true)
                .setRender(new SimpleRender())
                .setImgEmptyId(R.drawable.im_notice_doc_icon_able)
                .setImgLoadingId(R.drawable.refresh_black)
                .setImgFailId(R.drawable.chat_fail_resend_normal)
                .build();
        //测试 vender 是否可以自定义
        ExtVendor vendor = ImageLoader.getInstance().getVendor();
        ExtVendor.ExtHttpGrab httpGrab = vendor.getHttpGrab();
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        //重定向指定url到固定的byte[]
        httpGrab.direct("http://7xjlyc.com1.z0.glb.clouddn.com/Fg0s0sWvtUnMKv1FXER6gfKlSrTQ", baos.toByteArray());

        lv_main = (ListView) findViewById(R.id.lv_main);
        lv_main.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 100;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = MyActivity.this.getLayoutInflater().inflate(R.layout.item_image, null);
                }
                ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
                if (position % strs.length == 0) {
                    //使用组合头像
                    ImageLoader.getInstance().displayImage(new String[]{
                            strs[0],
                            strs[1],
                            strs[2]
                    }, iv_avatar, combine);
                } else {
                    ImageLoader.getInstance().displayImage(strs[position % strs.length], iv_avatar, normal);
                }

                return convertView;
            }
        });
    }
}
