package com.magouyaware.slidingsheet.recyclers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.magouyaware.slidingsheet.recyclers.inflaters.IRecyclerViewInflater;
import com.magouyaware.slidingsheet.recyclers.viewholders.GenericViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Justin Anderson on 10/24/2017.
 */

public class GenericRecyclerAdapter<T> extends RecyclerView.Adapter<GenericViewHolder<T>>
{
    private IRecyclerViewInflater<T> m_recycleViewInflater;
    private List<T> m_data = new ArrayList<T>();

    public GenericRecyclerAdapter(IRecyclerViewInflater<T> inflater)
    {
        m_recycleViewInflater = inflater;
    }

    public void setViewInflater(IRecyclerViewInflater<T> inflater, boolean notify)
    {
        m_recycleViewInflater = inflater;
        if (notify)
            notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position)
    {
        return m_recycleViewInflater.getItemViewType(position, this);
    }

    public IRecyclerViewInflater<T> getViewInflater()
    {
        return m_recycleViewInflater;
    }

    public void setData(List<T> data, boolean notify)
    {
        m_data = data;

        if (notify)
            notifyDataSetChanged();
    }

    public void addItem(T data, boolean notify)
    {
        m_data.add(data);

        if (notify)
            notifyDataSetChanged();
    }

    public void addItems(Collection<T> data, boolean notify)
    {
        m_data.addAll(data);

        if (notify)
            notifyDataSetChanged();
    }

    public void clear(boolean notify)
    {
        m_data.clear();

        if (notify)
            notifyDataSetChanged();
    }

    @Override
    public GenericViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View rootView = m_recycleViewInflater.onCreateView(parent, viewType);
        return m_recycleViewInflater.newViewHolder(rootView, viewType, this);
    }

    @Override
    public void onBindViewHolder(GenericViewHolder<T> holder, int position)
    {
        holder.bindData(this, position);
    }

    @Override
    public int getItemCount()
    {
        return m_data.size();
    }

    public T getItem(int position)
    {
        return m_data.get(position);
    }

    public List<T> getAllItems()
    {
        return m_data;
    }
}
