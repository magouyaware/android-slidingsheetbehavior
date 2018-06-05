package com.magouyaware.slidingsheet.recyclers.inflaters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magouyaware.slidingsheet.R;
import com.magouyaware.slidingsheet.recyclers.adapters.GenericRecyclerAdapter;
import com.magouyaware.slidingsheet.recyclers.viewholders.GenericViewHolder;
import com.magouyaware.slidingsheet.recyclers.viewholders.SimpleItemTextViewHolder;

public class SimpleItemTextViewInflater implements IRecyclerViewInflater<String>
{
    @Override
    public View onCreateView(ViewGroup parent, int viewType)
    {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_text_item, parent, false);
    }

    @Override
    public int getItemViewType(int position, GenericRecyclerAdapter adapter)
    {
        return 0;
    }

    @Override
    public GenericViewHolder<String> newViewHolder(View newView, int viewType, GenericRecyclerAdapter adapter)
    {
        return new SimpleItemTextViewHolder(newView);
    }
}
