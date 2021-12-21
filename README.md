# RecyclerView

Android封装RecyclerView的使用

先看接入步骤：
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```java
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```java
    dependencies {
            implementation 'com.github.linpeixu:RecyclerView:1.0.0'
	        //或者implementation 'com.gitlab.linpeixu:recyclerview:1.0.0'
	}
```


android的日常开发中经常需要使用到列表控件，最开始的时候我们使用的是ListView，随着业务的复杂性上升，页面布局不再单一，ListView的局限性越发凸显，好在google为我们提供了使用更灵活，扩展性更强大的RecyclerView，相比ListView的布局单一，RecyclerView拥有更多的布局方式，如线性布局（竖直方向或水平方向），表格布局，瀑布流布局，翻页布局（类似ViewPager）等，且支持局部刷新。

为方便我们快速使用，接下来我们对RecyclerView的使用进行简单的封装。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210511112317810.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY2MzQz,size_16,color_FFFFFF,t_70#pic_center)
和ListView一样，RecyclerView也需要创建对应的适配器RecyclerView.Adapter，对于Item视图的创建和使用这里采用ViewDataBinding的方式，避免各种findViewById。我们将适配器关于视图的方法抽象为IFAdapter，代码如下：

```java
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;

/**
 * 作者：Coca-Cola on 2021/5/11 10:23
 * Email : 1966353889@qq.com
 */
public interface IFAdapter<T> {
    /**
     * 显示数据（相当于onBindViewHolder）
     */
    void updateView(T data, ViewDataBinding binding, int position, int type);

    /**
     * 创建视图（相当于onCreateViewHolder）
     */
    ViewDataBinding createView(ViewGroup parent, int type);

}
```

为支持设置多个item样式，我们新增如下Interface：

```java
/**
 * 作者：Coca-Cola on 2021/5/11 10:23
 * Email : 1966353889@qq.com
 */
public interface IFTypeAdapter<T> extends IFAdapter<T> {
    /**
     * 可以根据数据类型来显示不同的item
     */
    int getItemViewType(int position);
}
```

为支持onItemClick（item视图点击事件）和OnItemLongClickListener（item视图长按点击事件），我们需要创建以下两个Interface，代码如下：

```java
/**
 * 作者：Coca-Cola on 2021/5/11 10:34
 * Email : 1966353889@qq.com
 */
public interface OnItemClickListener<T> {
       /**
         * @param data 点击的item视图对应的数据源
         * */
        void onItemClick(T data, int position);
}
/**
 * 作者：Coca-Cola on 2021/5/11 10:34
 * Email : 1966353889@qq.com
 */
public interface OnItemLongClickListener<T> {
       /**
         * @param data 点击的item视图对应的数据源
         * */
        boolean onItemLongClick(T data, int position);
 }
```

新建适配器ViewHolder基类，代码如下：

```java
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：Coca-Cola on 2021/5/11 10:35
 * Email : 1966353889@qq.com
 */
public  class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    public ViewDataBinding mBinding;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public  void setData(T data, int position, int type){

    }
}
```

RecyclerViewAdapter数据源我们采用ArrayList的数据形式，为实现RecyclerView丰富的使用场景，我们的RecyclerViewAdapter还应该有add、addAll、remove、move、clear等方法，直接上代码：

```java
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：Coca-Cola on 2021/5/11 10:42
 * Email : 1966353889@qq.com
 * Describe:RecyclerView适配器（结合ViewDataBinding使用）
 */
public class RecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {
    /**
     * 数据源
     */
    private ArrayList<T> data = new ArrayList<>();
    /**
     * item视图接口
     */
    private IFAdapter<T> mIFAdapter;
    private OnItemClickListener<T> mOnItemClickListener;
    private OnItemLongClickListener<T> mOnItemLongClickListener;

    public RecyclerViewAdapter(IFAdapter<T> mIFAdapter) {
        this.mIFAdapter = mIFAdapter;
    }

    @NonNull
    @Override
    public BaseViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = mIFAdapter.createView(parent, viewType);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(binding.getRoot());
        viewHolder.mBinding = binding;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<T> holder, final int position) {
        holder.setData(getItem(position), position, getItemViewType(position));
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (position < data.size()) {
                                                           mOnItemClickListener.onItemClick(getItem(position), position);
                                                       }
                                                   }
                                               }
            );
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                                       @Override
                                                       public Boolean onLongClick(View v) {
                                                           return mOnItemLongClickListener.onItemLongClick(getItem(position), position);
                                                       }
                                                   }
            );
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mIFAdapter instanceof IFTypeAdapter) {
            return ((IFTypeAdapter) mIFAdapter).getItemViewType(position);
        }
        return super.getItemViewType(position);
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(ArrayList<T> list) {
        if (list != null) {
            data.addAll(data.size(), list);
            notifyItemRangeInserted(data.size() - list.size(), list.size());
        }
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(T single, int index) {
        if (single != null) {
            data.add(index, single);
            notifyItemInserted(index);
            /*保证position的正确性*/
            notifyItemRangeChanged(index, getItemCount() - index);
        }
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(ArrayList<T> list, int index) {
        if (list != null && index < data.size()) {
            if (index < 0) {
                index = 0;
            }
            data.addAll(index, list);
            notifyItemRangeInserted(index, list.size());
        }
    }

    /**
     * 添加数据，会覆盖原有的数据
     */
    public void addAll(ArrayList<T> list) {
        if (list != null) {
            if (!data.isEmpty()) {
                data.clear();
            }
            data.addAll(list);
            notifyDataSetChanged();
        }
    }

    /**
     * 获取数据源
     */
    public ArrayList<T> getData() {
        return data;
    }

    /**
     * 删除数据
     *
     * @param index 待删除数据的位置
     */
    public void remove(int index) {
        if (index >= 0 && data != null && index < data.size()) {
            data.remove(index);
            notifyItemRemoved(index);
			/*传入的参数position，它是在进行onBind操作时确定的，在删除单项后，
              已经出现在画面里的项不会再有调用onBind机会，这样它保留的position
              一直是未进行删除操作前的postion值。对于尚未进入画面的单项来说，它
              会使用新的position值，这个值是正确的。所以需要调用notifyItemRangeChanged刷新下当前页面中的item*/
            notifyItemRangeChanged(index, getItemCount() - index);
        }
    }

    /**
     * 把fromPosition位置的item移到toPosition位置
     *
     * @param fromPosition 待移动的item的位置
     * @param toPosition   移到的位置
     */
    public void move(int fromPosition, int toPosition) {
        if (fromPosition < 0) {
            throw new RuntimeException("fromPosition can't be" + fromPosition);
        } else if (toPosition < 0) {
            throw new RuntimeException("toPosition can't be" + toPosition);
        } else if (fromPosition < data.size() && toPosition < data.size()) {
            /*数据更换*/
            data.add(toPosition, data.remove(fromPosition));
            /*移动item*/
            notifyItemMoved(fromPosition, toPosition);
            /*受影响的item都刷新下*/
            notifyItemRangeChanged(Math.min(fromPosition, toPosition), Math.abs(fromPosition - toPosition) + 1);
        }
    }

    /**
     * 清除数据
     */
    public void clear() {
        if (data != null && data.size() > 0) {
            data.clear();
            notifyDataSetChanged();
        }
    }

    public T getItem(int position) {
        return position < data.size() ? data.get(position) : null;
    }

    public void setOnItemClickListener(OnItemClickListener<T> l) {
        mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> l) {
        mOnItemLongClickListener = l;
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T data, int position);
    }

    public interface OnItemLongClickListener<T> {
        Boolean onItemLongClick(T data, int position);
    }

    private class RecyclerViewHolder extends BaseViewHolder<T> {
        private ViewDataBinding mBinding;

        private RecyclerViewHolder(View view) {
            super(view);
        }

        public void setData(T data, int position, int type) {
            /*交回到IFAdapter使用*/
            mIFAdapter.updateView(data, mBinding, position, type);
        }
    }
}
```

有相当多的业务场景技术上的实现需要在列表控件上添加头部或底部视图，类似ListView的addHeaderView()和addFooterView()，那么RecyclerView支不支持呢？官方是没提供现成可调用的方法的，不过前面说了RecyclerView强大的可扩展性，我们可以自行实现。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210511112615343.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY2MzQz,size_16,color_FFFFFF,t_70#pic_center)

主要的也是写支持添加头部或底部视图的适配器，代码如下：

```java
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：Coca-Cola on 2021/5/11 10:23
 * Email : 1966353889@qq.com
 * Describe:支持增加头部和尾部的RecyclerView适配器
 */
public class HeadRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 被包装的Adapter
     */
    private RecyclerView.Adapter mAdapter;
    /**
     * 存放HeaderView
     */
    private final List<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();
    /**
     * 存放FooterView
     */
    private final List<FixedViewInfo> mFooterViewInfos = new ArrayList<>();
    /**
     * 用于监听被包装的Adapter的数据变化的监听器。它将被包装的Adapter的数据变化映射成HeaderViewAdapter的变化。
     */
    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(getHeadersCount() + positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(getHeadersCount() + positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(getHeadersCount() + positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(getHeadersCount() + fromPosition, getHeadersCount() + toPosition);
        }
    };

    public HeadRecyclerAdapter(RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter != null) {
            //注册mAdapter的数据变化监听
            mAdapter.registerAdapterDataObserver(mObserver);
        }
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter instanceof HeadRecyclerAdapter) {
            //被包装的adapter不能是HeaderViewAdapter。
            throw new IllegalArgumentException("Cannot wrap a HeadRecyclerAdapter");
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            /*注册mAdapter的数据变化监听*/
            mAdapter.registerAdapterDataObserver(mObserver);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 根据viewType查找对应的HeaderView 或 FooterView。如果没有找到则表示该viewType是普通的列表项。
        View view = findViewForInfos(viewType);
        if (view != null) {
            return new ViewHolder(view);
        } else {
            //交由mAdapter处理。
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /*如果是HeaderView或者是FooterView则不绑定数据,因为HeaderView和FooterView是由外部传进来的，它们不由列表去更新。*/
        if (isHeader(position) || isFooter(position)) {
            return;
        }
        /*将列表实际的position调整成mAdapter对应的position,交由mAdapter处理*/
        int adjPosition = position - getHeadersCount();
        mAdapter.onBindViewHolder(holder, adjPosition);
    }

    @Override
    public int getItemCount() {
        return mHeaderViewInfos.size() + mFooterViewInfos.size()
                + (mAdapter == null ? 0 : mAdapter.getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        /*如果当前item是HeaderView，则返回HeaderView对应的itemViewType。*/
        if (isHeader(position)) {
            return mHeaderViewInfos.get(position).itemViewType;
        }
        /*如果当前item是FooterView，则返回FooterView对应的itemViewType。*/
        if (isFooter(position)) {
            return mFooterViewInfos.get(position - mHeaderViewInfos.size() - mAdapter.getItemCount()).itemViewType;
        }
        /*将列表实际的position调整成mAdapter对应的position,交由mAdapter处理。*/
        int adjPosition = position - getHeadersCount();
        return mAdapter.getItemViewType(adjPosition);
    }

    /**
     * 判断当前位置是否是头部View。
     *
     * @param position 这里的position是整个列表(包含HeaderView和FooterView)的position。
     * @return
     */
    public boolean isHeader(int position) {
        return position < getHeadersCount();
    }

    /**
     * 判断当前位置是否是尾部View。
     *
     * @param position 这里的position是整个列表(包含HeaderView和FooterView)的position。
     * @return
     */
    public boolean isFooter(int position) {
        return getItemCount() - position <= getFootersCount();
    }

    /**
     * 获取HeaderView的个数
     *
     * @return
     */
    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    /**
     * 获取FooterView的个数
     *
     * @return
     */
    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    /**
     * 添加HeaderView
     *
     * @param view
     */
    public void addHeaderView(View view) {
        addHeaderView(view, generateUniqueViewType());
    }

    private void addHeaderView(View view, int viewType) {
        //包装HeaderView数据并添加到列表
        FixedViewInfo info = new FixedViewInfo();
        info.view = view;
        info.itemViewType = viewType;
        mHeaderViewInfos.add(info);
        notifyItemInserted(mHeaderViewInfos.size() - 1);
    }

    /**
     * 移除HeaderView
     *
     * @param view 待移除的view
     */
    public void removeHeaderView(View view) {
        for (int i = 0; i < mHeaderViewInfos.size(); i++) {
            if (mHeaderViewInfos.get(i).view == view) {
                mHeaderViewInfos.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * 添加FooterView
     *
     * @param view
     */
    public void addFooterView(View view) {
        addFooterView(view, generateUniqueViewType());
    }

    private void addFooterView(View view, int viewType) {
        // 包装FooterView数据并添加到列表
        FixedViewInfo info = new FixedViewInfo();
        info.view = view;
        info.itemViewType = viewType;
        mFooterViewInfos.add(info);
        notifyItemInserted(getItemCount() - 1);
    }

    /**
     * 移除FooterView
     *
     * @param view 待移除的view
     */
    public void removeFooterView(View view) {
        for (int i = 0; i < mFooterViewInfos.size(); i++) {
            if (mFooterViewInfos.get(i).view == view) {
                int position = getItemCount() - mFooterViewInfos.size() + i;
                mFooterViewInfos.remove(i);
                notifyItemRemoved(position);
                break;
            }
        }
    }

    /**
     * 生成一个唯一的数，用于标识HeaderView或FooterView的type类型，并且保证类型不会重复。
     *
     * @return
     */
    private int generateUniqueViewType() {
        int count = getItemCount();
        while (true) {
            /*生成一个随机数*/
            int viewType = (int) (Math.random() * Integer.MAX_VALUE) + 1;
            /*判断该viewType是否已使用*/
            boolean isExist = false;
            for (int i = 0; i < count; i++) {
                if (viewType == getItemViewType(i)) {
                    isExist = true;
                    break;
                }
            }
            /*判断该viewType还没被使用，则返回。否则进行下一次循环，重新生成随机数*/
            if (!isExist) {
                return viewType;
            }
        }
    }

    /**
     * 根据viewType查找对应的HeaderView 或 FooterView。没有找到则返回null。
     *
     * @param viewType 查找的viewType
     * @return
     */
    private View findViewForInfos(int viewType) {
        for (FixedViewInfo info : mHeaderViewInfos) {
            if (info.itemViewType == viewType) {
                return info.view;
            }
        }

        for (FixedViewInfo info : mFooterViewInfos) {
            if (info.itemViewType == viewType) {
                return info.view;
            }
        }

        return null;
    }

    /**
     * 用于包装HeaderView和FooterView的数据类
     */
    private class FixedViewInfo {
        /*保存HeaderView或FooterView*/
        View view;
        /*保存HeaderView或FooterView对应的viewType*/
        int itemViewType;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
```

下面我们通过一个Delegate来方便快捷地使用RecyclerView，代码中都有详细的注释，直接看代码吧：

```java
import android.view.View;

import com.huitouche.android.kit.adapter.HeadRecyclerAdapter;
import com.huitouche.android.kit.adapter.IFAdapter;
import com.huitouche.android.kit.adapter.RecyclerViewAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * 作者：Coca-Cola on 2021/5/11 10:58
 * Email : 1966353889@qq.com
 * Describe:RecyclerView委托者（不支持下拉刷新和上拉加载回调）
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

    /**
     * 线性布局（默认竖直方向）
     */
    public RecyclerViewDelegate<T> initLinear() {
        initLinearLayoutManager(LinearLayoutManager.VERTICAL);
        return this;
    }

    /**
     * 线性布局
     *
     * @param orientation 布局的方向（水平或竖直）
     */
    public RecyclerViewDelegate<T> initLinear(int orientation) {
        initLinearLayoutManager(orientation);
        return this;
    }

    /**
     * 表格布局
     *
     * @param spanCount 列数
     */
    public RecyclerViewDelegate<T> initGrid(int spanCount) {
        initGridLayoutManager(spanCount);
        return this;
    }

    /**
     * 瀑布流布局
     *
     * @param spanCount   列数
     * @param orientation 方向
     */
    public RecyclerViewDelegate<T> initStaggeredGrid(int spanCount, int orientation) {
        initStaggeredGridLayoutManager(spanCount, orientation);
        return this;
    }

    /**
     * 翻页布局（类似ViewPager）
     *
     * @param orientation 方向
     */
    public RecyclerViewDelegate<T> initPaging(int orientation) {
        initPagingLayoutManager(orientation);
        return this;
    }

    /**
     * 添加分割线
     */
    public RecyclerViewDelegate<T> addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
        return this;
    }

    /**
     * 添加分割线
     */
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

    public RecyclerViewAdapter<T> getAdapter() {
        return mAdapter;
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
        mRecyclerView.setLayoutManager(mManager);
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
                int targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                /*"滑到到"+targetPos+"位置"*/
                /*Log.e("RecyclerViewDelegate", "滑到到" + targetPos + "位置");*/
                return targetPos;
            }
        };
        snapHelper.attachToRecyclerView(mRecyclerView);
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
```

initLinearLayoutManager(int orientation)方法中的FixLinearSnapHelper代码如下：

```java
import android.view.View;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：Coca-Cola on 2021/5/11 10:58
 * Email : 1966353889@qq.com
 */
public class FixLinearSnapHelper extends LinearSnapHelper {

    private OrientationHelper mVerticalHelper;

    private OrientationHelper mHorizontalHelper;

    private RecyclerView mRecyclerView;

    @Override
    public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager,
                                              View targetView) {
        int[] out = new int[2];

        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(targetView, getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(targetView, getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }

    @Override
    public void attachToRecyclerView(RecyclerView recyclerView) throws IllegalStateException {
        this.mRecyclerView = recyclerView;
        super.attachToRecyclerView(recyclerView);
    }

    private int distanceToCenter(View targetView, OrientationHelper helper) {
        //如果已经滚动到尽头 并且判断是否是第一个item或者是最后一个，直接返回0，不用多余的滚动了
        if ((helper.getDecoratedStart(targetView) == 0 && mRecyclerView.getChildAdapterPosition(targetView) == 0)
                || (helper.getDecoratedEnd(targetView) == helper.getEndAfterPadding()
                && mRecyclerView.getChildAdapterPosition(targetView) == mRecyclerView.getAdapter().getItemCount() - 1))
            return 0;

        int viewCenter = helper.getDecoratedStart(targetView) + (helper.getDecoratedEnd(targetView) - helper.getDecoratedStart(targetView)) / 2;
        int correctCenter = (helper.getEndAfterPadding() - helper.getStartAfterPadding()) / 2;
        return viewCenter - correctCenter;
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

}
```

封装到这里便完成了，使用方法如下：

```java
/*ItemBean（泛型）为每个item对应的数据源，不固定，可自行调整*/
private RecyclerViewDelegate<ItemBean> mDelegate;
/*初始化RecyclerViewDelegate（布局方式自行调用，这里调用了initLinear()）*/
mDelegate = new RecyclerViewDelegate<>(new IFAdapter<ItemBean>() {
            @Override
            public void updateView(ItemBean itemData, ViewDataBinding viewDataBinding, int position, int type) {
               /*ItemXXXBinding为item布局对应的ViewDataBinding*/ 
               ItemXXXBinding mBinding = (ItemXXXBinding) viewDataBinding;
                /*设置数据*/
                ...
                mBinding.tvContent.setText
            }

            @Override
            public ViewDataBinding createView(ViewGroup parent, int type) {
               /*item_xxx为item布局对应的layoutId*/ 
                return DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_xxx, parent, false);
            }
        }, recyclerView).initLinear().build();
/*设置item点击事件*/
mDelegate.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener<ItemBean>() {
            @Override
            public void onItemClick(ItemBean itemBean, int position) {
                

            }
 });
/*数据源*/
ArrayList<ItemBean> datas = xxxxxx;
/*设置完数据源后绑定到适配器*/
mDelegate.addAll(array);
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210511112809574.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzODY2MzQz,size_16,color_FFFFFF,t_70#pic_center)
怎么样？是不是很方便使用了。

关注我，下篇文章我们来聊聊如何为RecyclerView增加下拉刷新和上拉加载的功能。

希望本文可以帮助到您，也希望各位不吝赐教，提出您在使用中的宝贵意见，谢谢。
