package com.cloudling.recyclerview;

/**
 * 描述：带下拉刷新效果的recyclerView的下拉刷新控件需实现此接口
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/23
 */
public interface IFRefreshLayout {
    void setOnRefreshListener(OnRefreshListener listener);

    void finishRefresh();
}
