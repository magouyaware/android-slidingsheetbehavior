package com.magouyaware.slidingsheetbehavior.callbacks;

import android.support.annotation.NonNull;
import android.view.View;

import com.magouyaware.slidingsheetbehavior.enums.SlideState;

/**
 * Callback for monitoring events about bottom sheets.
 * Google had this as an abstract class... but that unnecessarily limits flexibility in its use.
 */
public interface ISlidingSheetCallback
{
    /**
     * Called when the bottom sheet changes its state.
     *
     * @param slidingSheet The sliding sheet view.
     * @param newState    The new state.
     */
    void onStateChanged(@NonNull View slidingSheet, SlideState newState);

    /**
     * Called when the bottom sheet is being dragged.
     *
     * @param slidingSheet The sliding sheet view.
     * @param slideOffset The new offset of this sliding sheet within [-1,1] range. Offset
     *                    increases as this sliding sheet is moving upward. From 0 to 1 the sheet
     *                    is between collapsed and expanded states and from -1 to 0 it is
     *                    between hidden and collapsed states.
     */
    void onSlide(@NonNull View slidingSheet, float slideOffset);
}
