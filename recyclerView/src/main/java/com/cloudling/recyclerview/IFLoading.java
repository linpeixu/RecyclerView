package com.cloudling.recyclerview;

import android.view.View;

/**
 * 描述：recyclerView底部加载更多视图需实现此接口
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/23
 */
public interface IFLoading {
    /**
     * 正在加载
     *
     * @param hint 提示文本
     */
    void loading(String hint);

    /**
     * 无更多数据
     *
     * @param hint 提示文本
     */
    void hasNotMore(String hint);

    /**
     * 不可见
     */
    void gone();

    /**
     * 返回自己
     */
    View getView();
}
