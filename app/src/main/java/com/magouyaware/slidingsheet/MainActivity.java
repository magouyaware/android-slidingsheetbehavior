package com.magouyaware.slidingsheet;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.magouyaware.slidingsheet.recyclers.adapters.GenericRecyclerAdapter;
import com.magouyaware.slidingsheet.recyclers.inflaters.IRecyclerViewInflater;
import com.magouyaware.slidingsheet.recyclers.viewholders.GenericViewHolder;
import com.magouyaware.slidingsheetbehavior.behavior.SlidingSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static final String LEFT_SHEET_STATE_TAG = "LeftSheetState";
    private static final String RIGHT_SHEET_STATE_TAG = "RightSheetState";
    private static final String TOP_SHEET_STATE_TAG = "TopSheetState";
    private static final String BOTTOM_SHEET_STATE_TAG = "BottomSheetState";

    private CoordinatorLayout m_sheetParent;
    private FrameLayout m_leftSheet, m_rightSheet, m_topSheet, m_bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_sheetParent = findViewById(R.id.sliding_container);
        m_leftSheet = findViewById(R.id.left_sheet);
        m_rightSheet = findViewById(R.id.right_sheet);
        m_topSheet = findViewById(R.id.top_sheet);
        m_bottomSheet = findViewById(R.id.bottom_sheet);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (outState != null)
        {
            saveSheetState(m_leftSheet, LEFT_SHEET_STATE_TAG, outState);
            saveSheetState(m_rightSheet, RIGHT_SHEET_STATE_TAG, outState);
            saveSheetState(m_topSheet, TOP_SHEET_STATE_TAG, outState);
            saveSheetState(m_bottomSheet, BOTTOM_SHEET_STATE_TAG, outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState)
    {
        if (savedState != null)
        {
            restoreSheetState(m_leftSheet, LEFT_SHEET_STATE_TAG, savedState);
            restoreSheetState(m_rightSheet, RIGHT_SHEET_STATE_TAG, savedState);
            restoreSheetState(m_topSheet, TOP_SHEET_STATE_TAG, savedState);
            restoreSheetState(m_bottomSheet, BOTTOM_SHEET_STATE_TAG, savedState);
        }

        super.onRestoreInstanceState(savedState);
    }

    private void saveSheetState(View sheet, String tag, Bundle outState)
    {
        SlidingSheetBehavior<View> behavior = SlidingSheetBehavior.from(sheet);
        if (behavior == null)
            return;

        Parcelable state = behavior.onSaveInstanceState(m_sheetParent, sheet);
        outState.putParcelable(tag, state);
    }

    private void restoreSheetState(View sheet, String tag, Bundle savedState)
    {
        Parcelable state = savedState.getParcelable(tag);
        if (state == null)
            return;

        SlidingSheetBehavior<View> behavior = SlidingSheetBehavior.from(sheet);
        if (behavior != null)
            behavior.onRestoreInstanceState(m_sheetParent, sheet, state);
    }
}
