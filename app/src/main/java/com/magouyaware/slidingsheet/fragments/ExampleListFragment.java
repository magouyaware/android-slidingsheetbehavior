package com.magouyaware.slidingsheet.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magouyaware.slidingsheet.R;
import com.magouyaware.slidingsheet.recyclers.adapters.GenericRecyclerAdapter;
import com.magouyaware.slidingsheet.recyclers.inflaters.SimpleItemTextViewInflater;

import java.util.ArrayList;
import java.util.List;

public class ExampleListFragment extends Fragment
{
    private RecyclerView m_list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_list_example, container, false);
        m_list = view.findViewById(R.id.example_sheets);

        GenericRecyclerAdapter<String> adapter =
                new GenericRecyclerAdapter<>(new SimpleItemTextViewInflater());

        adapter.addItems(getItems(), false);
        m_list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        m_list.setAdapter(adapter);

        return view;
    }

    private List<String> getItems()
    {
        List<String> items = new ArrayList<>();
        for (int i = 1; i < 51; i++)
            items.add("Item " + i);

        return items;
    }
}
