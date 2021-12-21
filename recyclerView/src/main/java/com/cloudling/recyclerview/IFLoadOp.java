package com.cloudling.recyclerview;

/**
 * 描述：加载操作，配合RecyclerViewDelegate使用
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/23
 */
public interface IFLoadOp {
    /**
     * 下拉刷新
     */
    void onRefresh();

    /**
     * 上拉加载
     */
    void onLoadMore();

    /**
     * 返回实现IFRefreshLayout接口的refreshView
     */
    IFRefreshLayout getRefreshLayout();

}

