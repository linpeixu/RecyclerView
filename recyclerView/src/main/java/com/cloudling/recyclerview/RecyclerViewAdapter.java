package com.cloudling.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * 描述：RecyclerView适配器（结合ViewDataBinding使用）
 * 联系: 1966353889@qq.com
 * 日期: 2019/7/5
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

    public RecyclerViewAdapter(IFAdapter<T> mIFAdapter) {
        this.mIFAdapter = mIFAdapter;
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

    public ArrayList<T> getData() {
        return this.data;
    }

    /**
     * 添加数据，不会覆盖原有的数据
     */
    public void add(T single, int index) {
        if (single != null) {
            data.add(single);
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
                    if (position < getItemCount()) {
                        mOnItemClickListener.onItemClick(getItem(position), position);
                    }
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (position < getItemCount()) {
                        return mOnItemLongClickListener.onItemLongClick(getItem(position), position);
                    }
                    return false;
                }
            });
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

    public T getItem(int position) {
        return position < data.size() ? data.get(position) : null;
    }

    private OnItemClickListener<T> mOnItemClickListener;
    private OnItemLongClickListener<T> mOnItemLongClickListener;

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
        boolean onItemLongClick(T data, int position);
    }
}
