package org.darkgem.imageloader.vendor.support.util;

/**
 * Created by Administrator on 2015/4/24.
 */
public class Tuple<T, K> {
    public T _1;
    public K _2;

    public Tuple(T t, K k) {
        this._1 = t;
        this._2 = k;
    }

    public Tuple() {
    }
}
