package com.magouyaware.slidingsheet.recyclers.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.magouyaware.slidingsheet.recyclers.GenericRecyclerAdapter;

/**
 * Created by Justin Anderson on 10/24/2017.
 */

public abstract class GenericViewHolder<T> extends RecyclerView.ViewHolder
{
    public GenericViewHolder(View itemView)
    {
        super(itemView);
    }

    public abstract void bindData(GenericRecyclerAdapter<T> adapter, int position);
}