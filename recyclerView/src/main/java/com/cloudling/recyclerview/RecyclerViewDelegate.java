package com.cloudling.recyclerview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

/**
 * 描述：RecyclerView委托者（不支持下拉刷新和上拉加载回调）
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/23
 */
public class RecyclerViewDelegate<T> {
    private final String TAG = RecyclerViewDelegate.class.getSimpleName();
    /**
     * 列表控件
     */
    private RecyclerView mRecyclerView;
    /**
     * recyclerView创建/更新视图操作
     */
    private IFAdapter<T> mIFAdapter;
    private HeadRecyclerAdapter mHeadAdapter;
    private RecyclerViewAdapter<T> mAdapter;


    public RecyclerViewDelegate(IFAdapter<T> Adapter, RecyclerView mRecyclerView) {
        this.mIFAdapter = Adapter;
        this.mRecyclerView = mRecyclerView;
    }

    public RecyclerViewDelegate<T> initLinear() {
        initLinearLayoutManager(LinearLayoutManager.VERTICAL);
        return this;
    }

    public RecyclerViewDelegate<T> initLinear(int orientation) {
        initLinearLayoutManager(orientation);
        return this;
    }

    public RecyclerViewDelegate<T> initGrid(int spanCount) {
        initGridLayoutManager(spanCount);
        return this;
    }

    public RecyclerViewDelegate<T> initStaggeredGrid(int spanCount, int orientation) {
        initStaggeredGridLayoutManager(spanCount, orientation);
        return this;
    }

    public RecyclerViewDelegate<T> initPaging(int orientation) {
        initPagingLayoutManager(orientation);
        return this;
    }

    public RecyclerViewDelegate<T> addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
        return this;
    }

    public RecyclerViewDelegate<T> addItemDecoration(@NonNull RecyclerView.ItemDecoration decor, int index) {
        mRecyclerView.addItemDecoration(decor, index);
        return this;
    }

    public RecyclerViewDelegate<T> build() {
        mAdapter = new RecyclerViewAdapter<>(mIFAdapter);
        mHeadAdapter = new HeadRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeadAdapter);
        return this;
    }


    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(ArrayList<T> list) {
        mAdapter.add(list);
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(T single, int index) {
        mAdapter.add(single, index);
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(ArrayList<T> list, int index) {
        mAdapter.add(list, index);
    }

    /**
     * 添加数据，会覆盖原有的数据
     */
    public void addAll(ArrayList<T> list) {
        mAdapter.addAll(list);
    }

    /**
     * 添加HeaderView
     *
     * @param view
     */
    public void addHeaderView(View view) {
        mHeadAdapter.addHeaderView(view);
    }

    /**
     * 移除HeaderView
     *
     * @param view 待移除的view
     */
    public void removeHeaderView(View view) {
        mHeadAdapter.removeHeaderView(view);
    }

    /**
     * 获取HeaderView的个数
     *
     * @return
     */
    public int getHeadersCount() {
        return mHeadAdapter.getHeadersCount();
    }

    /**
     * 添加FooterView
     *
     * @param view
     */
    public void addFooterView(View view) {
        mHeadAdapter.addFooterView(view);
    }

    /**
     * 移除FooterView
     *
     * @param view 待移除的view
     */
    public void removeFooterView(View view) {
        mHeadAdapter.removeFooterView(view);
    }

    /**
     * 获取FooterView的个数
     *
     * @return
     */
    public int getFootersCount() {
        return mHeadAdapter.getFootersCount();
    }

    /**
     * 移除Item（非headerView或footerView）
     *
     * @param index 待移除的item的位置（不算headerView或footerView的位置）
     */
    public void removeItem(int index) {
        mAdapter.remove(index);
    }

    /**
     * 把fromPosition位置的item移到toPosition位置
     *
     * @param fromPosition 待移动的item的位置
     * @param toPosition   移到的位置
     */
    public void moveItem(int fromPosition, int toPosition) {
        mAdapter.move(fromPosition, toPosition);
    }

    /**
     * 清除数据
     */
    public void clear() {
        mAdapter.clear();
    }

    /**
     * 获取全部的item数（包括headerView和footerView）
     */
    public int getItemCount() {
        return mHeadAdapter != null ? mHeadAdapter.getItemCount() : 0;
    }

    /**
     * 获取全部的item数（不包括headerView和footerView）
     */
    public int getContentItemCount() {
        return mAdapter != null ? mAdapter.getItemCount() : 0;
    }

    public RecyclerViewAdapter<T> getAdapter() {
        return this.mAdapter;
    }

    public void setOnItemClickListener(RecyclerViewAdapter.OnItemClickListener<T> l) {
        if (mAdapter != null) {
            mAdapter.setOnItemClickListener(l);
        }
    }

    public void setOnItemLongClickListener(RecyclerViewAdapter.OnItemLongClickListener<T> l) {
        if (mAdapter != null) {
            mAdapter.setOnItemLongClickListener(l);
        }
    }

    private void initLinearLayoutManager(int orientation) {
        //默认的layoutManager
        LinearLayoutManager mManager = new LinearLayoutManager(mRecyclerView.getContext());
        mManager.setOrientation(orientation);
        //设置layoutManager
        mRecyclerView.setLayoutManager(mManager);
        //如果是横向布局的话
        if (orientation == LinearLayoutManager.HORIZONTAL) {
            FixLinearSnapHelper snapHelper = new FixLinearSnapHelper();
            snapHelper.attachToRecyclerView(mRecyclerView);
        }
    }


    /**
     * 网格列表
     *
     * @param spanCount
     */
    private void initGridLayoutManager(int spanCount) {
        //GridLayoutManager
        GridLayoutManager mManager = new GridLayoutManager(mRecyclerView.getContext(), spanCount);
        //设置layoutManager
        mRecyclerView.setLayoutManager(mManager);
    }

    /**
     * 交错网格
     *
     * @param spanCount
     */
    private void initStaggeredGridLayoutManager(int spanCount, int orientation) {
        // 交错网格布局管理器
        StaggeredGridLayoutManager mManager = new StaggeredGridLayoutManager(spanCount, orientation);
        /*mManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);*/
        mRecyclerView.setLayoutManager(mManager);
        /*mRecyclerView.setItemAnimator(null);*/
    }

    /**
     * 分页（类似ViewPager）
     *
     * @param orientation 方向（传RecyclerView.HORIZONTAL或RecyclerView.VERTICAL）
     */
    private void initPagingLayoutManager(int orientation) {
        LinearLayoutManager mManager = new LinearLayoutManager(mRecyclerView.getContext());
        mManager.setOrientation(orientation);
        mRecyclerView.setLayoutManager(mManager);
        /*使RecyclerView根据设定的orientation，像ViewPager一样的效果，每次只能滑动一页，上下或左右滑动*/
        PagerSnapHelper snapHelper = new PagerSnapHelper() {
            /*在Adapter的onBindViewHolder之后执行*/
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX,
                                              int velocityY) {
                return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
            }
        };
        snapHelper.attachToRecyclerView(mRecyclerView);
    }

}
