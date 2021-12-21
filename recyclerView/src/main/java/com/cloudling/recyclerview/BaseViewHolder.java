package com.cloudling.recyclerview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/16
 */
public class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    public ViewDataBinding mBinding;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setData(T data, int position, int type) {

    }


}
