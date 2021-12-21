package com.cloudling.recyclerview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

/**
 * 描述：RecyclerView委托者（支持下拉刷新和上拉加载回调）
 * 联系: 1966353889@qq.com
 * 日期: 2020/4/1
 */
public class SwipeRecyclerViewDelegate<T> {
    private final String TAG = SwipeRecyclerViewDelegate.class.getSimpleName();
    /**
     * 列表控件
     */
    private RecyclerView mRecyclerView;
    /**
     * 列表RecyclerView底部的加载更多视图
     */
    private IFLoading mIFLoading;
    /**
     * 是否有更多，默认为true（用于上拉加载更多）
     */
    private boolean mHasMore = true;
    /**
     * 加载操作（下拉刷新，上拉加载等）
     */
    private IFLoadOp mIFLoadOp;
    /**
     * recyclerView创建/更新视图操作
     */
    private IFAdapter<T> mIFAdapter;
    private HeadRecyclerAdapter mHeadAdapter;
    private RecyclerViewAdapter<T> mAdapter;
    /**
     * 当前的状态（默认为普通状态）
     */
    private TYPE type = TYPE.NORMAL;
    /**
     * 加载提示
     */
    private String mLoadHint = "加载中...";
    /**
     * 无更多数据提示
     */
    private String mHasNoMoreHint = "已无更多数据";

    public enum TYPE {
        /**
         * 普通状态
         */
        NORMAL,
        /**
         * 正在刷新
         */
        REFRESH,
        /**
         * 正在加载更多
         */
        LOADMORE
    }

    /**
     * 支持下拉刷新和上拉加载
     */
    public SwipeRecyclerViewDelegate(IFLoadOp mIFLoadOp, IFAdapter<T> Adapter, RecyclerView mRecyclerView) {
        this.mIFLoadOp = mIFLoadOp;
        this.mIFAdapter = Adapter;
        this.mRecyclerView = mRecyclerView;
    }

    /**
     * 不支持下拉刷新和上拉加载
     */
    public SwipeRecyclerViewDelegate(IFAdapter<T> Adapter, RecyclerView mRecyclerView) {
        this.mIFAdapter = Adapter;
        this.mRecyclerView = mRecyclerView;
    }

    public SwipeRecyclerViewDelegate<T> initLinear() {
        initLinearLayoutManager(LinearLayoutManager.VERTICAL);
        return this;
    }

    public SwipeRecyclerViewDelegate<T> initLinear(int orientation) {
        initLinearLayoutManager(orientation);
        return this;
    }

    public SwipeRecyclerViewDelegate<T> initGrid(int spanCount) {
        initGridLayoutManager(spanCount);
        return this;
    }

    public SwipeRecyclerViewDelegate<T> initStaggeredGrid(int spanCount, int orientation) {
        initStaggeredGridLayoutManager(spanCount, orientation);
        return this;
    }

    public SwipeRecyclerViewDelegate<T> loadHint(String hint) {
        this.mLoadHint = hint;
        return this;
    }

    public SwipeRecyclerViewDelegate<T> noMoreHint(String hint) {
        this.mHasNoMoreHint = hint;
        return this;
    }

    public SwipeRecyclerViewDelegate<T> build() {
        mAdapter = new RecyclerViewAdapter<>(mIFAdapter);
        mHeadAdapter = new HeadRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeadAdapter);
        if (mIFLoadOp != null) {
            mIFLoadOp.getRefreshLayout().setOnRefreshListener(() -> {
                type = TYPE.REFRESH;
                mIFLoadOp.onRefresh();
            });
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        /*滑动停止*/
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    /*得到当前显示的最后一个item的view*/
                    View lastChildView = recyclerView.getLayoutManager().getChildAt(recyclerView.getLayoutManager().getChildCount() - 1);
                    /*得到lastChildView的bottom坐标值*/
                    int lastChildBottom = lastChildView.getBottom();
                    /*得到Recyclerview的底部坐标减去底部padding值，也就是显示内容最底部的坐标*/
                    int recyclerBottom = recyclerView.getBottom() - recyclerView.getPaddingBottom();
                    /*通过这个lastChildView得到这个view当前的position值*/
                    int lastPosition = recyclerView.getLayoutManager().getPosition(lastChildView);
                    /*判断lastChildView的bottom值跟recyclerBottom
                    判断lastPosition是不是最后一个position
                    如果两个条件都满足则说明是真正的滑动到了底部*/
                    if (lastChildBottom == recyclerBottom && lastPosition == recyclerView.getLayoutManager().getItemCount() - 1) {
                        /*滑动到底了*/
                        if (type == TYPE.NORMAL && mHasMore) {
                            type = TYPE.LOADMORE;
                            if (mIFLoading != null) {
                                mIFLoading.loading(mLoadHint);
                            }
                            mIFLoadOp.onLoadMore();
                        }
                    }

                }
            });
        }
        return this;
    }

    public void reset() {
        type = TYPE.NORMAL;
        mHasMore = true;
        if (mIFLoadOp != null) {
            mIFLoadOp.getRefreshLayout().finishRefresh();
        }
    }

    /**
     * 无更多数据（实现IFLoading接口的View应在hasNotMore方法中让自己可见）
     */
    public void hasNoMore() {
        type = TYPE.NORMAL;
        mHasMore = false;
        if (mIFLoading != null) {
            mIFLoading.hasNotMore(mHasNoMoreHint);
        }
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

    public void setLoadingView(IFLoading mIFLoading) {
        this.mIFLoading = mIFLoading;
        this.mIFLoading.gone();
        addFooterView(this.mIFLoading.getView());
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
    protected void initStaggeredGridLayoutManager(int spanCount, int orientation) {
        // 交错网格布局管理器
        StaggeredGridLayoutManager mManager = new StaggeredGridLayoutManager(spanCount, orientation);
        /*mManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);*/
        mRecyclerView.setLayoutManager(mManager);
        /*mRecyclerView.setItemAnimator(null);*/
    }

    /**
     * 获取全部的item数（包括headerView和footerView）
     */
    public int getItemCount() {
        return mHeadAdapter != null ? mHeadAdapter.getItemCount() : 0;
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

}
