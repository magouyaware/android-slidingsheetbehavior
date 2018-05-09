package com.magouyaware.slidingsheet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.magouyaware.slidingsheet.recyclers.GenericRecyclerAdapter;
import com.magouyaware.slidingsheet.recyclers.inflaters.IRecyclerViewInflater;
import com.magouyaware.slidingsheet.recyclers.viewholders.GenericViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recycler = findViewById(R.id.recycled_list);
        if (recycler != null)
        {
            GenericRecyclerAdapter<String> adapter = new GenericRecyclerAdapter<>(new IRecyclerViewInflater<String>()
            {
                @Override
                public View onCreateView(ViewGroup parent, int viewType)
                {
                    return LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                }

                @Override
                public int getItemViewType(int position, GenericRecyclerAdapter<String> adapter)
                {
                    return 0;
                }

                @Override
                public GenericViewHolder<String> newViewHolder(View newView, int viewType, GenericRecyclerAdapter<String> adapter)
                {
                    return new TextViewHolder(newView);
                }
            });

            adapter.addItems(getItems(), false);

            recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recycler.setAdapter(adapter);
        }
    }

    private List<String> getItems()
    {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 50; i++)
            items.add("Item ");

        return items;
    }

    public class TextViewHolder extends GenericViewHolder<String>
    {

        public TextViewHolder(View itemView)
        {
            super(itemView);
        }

        @Override
        public void bindData(GenericRecyclerAdapter<String> adapter, int position)
        {
            ((TextView)itemView).setText(adapter.getItem(position) + position);
        }
    }

}
