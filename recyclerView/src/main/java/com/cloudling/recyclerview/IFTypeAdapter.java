package com.cloudling.recyclerview;

/**
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/10
 */
public interface IFTypeAdapter<T> extends IFAdapter<T> {
    /**
     * 可以根据数据类型来显示不同的item
     */
    int getItemViewType(int position);
}
