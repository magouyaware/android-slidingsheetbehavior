package com.magouyaware.slidingsheet.recyclers.inflaters;

import android.view.View;
import android.view.ViewGroup;

import com.magouyaware.slidingsheet.recyclers.GenericRecyclerAdapter;
import com.magouyaware.slidingsheet.recyclers.viewholders.GenericViewHolder;

/**
 * Created by Justin Anderson on 10/24/2017.
 */

public interface IRecyclerViewInflater<T>
{
    View onCreateView(ViewGroup parent, int viewType);
    int getItemViewType(int position, GenericRecyclerAdapter<T> adapter);
    GenericViewHolder<T> newViewHolder(View newView, int viewType, GenericRecyclerAdapter<T> adapter);
}