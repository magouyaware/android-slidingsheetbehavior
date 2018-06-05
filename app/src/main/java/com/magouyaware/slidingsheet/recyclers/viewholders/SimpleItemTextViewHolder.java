package com.magouyaware.slidingsheet.recyclers.viewholders;

import android.view.View;
import android.widget.TextView;

import com.magouyaware.slidingsheet.recyclers.adapters.GenericRecyclerAdapter;

public class SimpleItemTextViewHolder extends GenericViewHolder<String>
{
    public SimpleItemTextViewHolder(View itemView)
    {
        super(itemView);
    }

    @Override
    public void bindData(GenericRecyclerAdapter<String> adapter, int position)
    {
        ((TextView)itemView).setText(adapter.getItem(position));
    }
}
