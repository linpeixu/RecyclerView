package com.cloudling.recyclerview;

import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;

/**
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/2
 */
public interface IFAdapter<T> {
    /**
     * 显示数据
     */
    void updateView(T data, ViewDataBinding binding, int position, int type);

    /**
     * 创建视图
     */
    ViewDataBinding createView(ViewGroup parent, int type);

}
