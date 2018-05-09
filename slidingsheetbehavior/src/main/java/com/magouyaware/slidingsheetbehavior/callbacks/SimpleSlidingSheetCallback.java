package com.magouyaware.slidingsheetbehavior.callbacks;

import android.support.annotation.NonNull;
import android.view.View;

import com.magouyaware.slidingsheetbehavior.enums.SlideState;

/**
 * Simple class implementing ISlidingSheetCallback so that you only need to implement the methods
 * you care about.  Default implementations do nothing.
 */
public class SimpleSlidingSheetCallback implements ISlidingSheetCallback
{
    @Override
    public void onStateChanged(@NonNull View slidingSheet, SlideState newState) {}

    @Override
    public void onSlide(@NonNull View slidingSheet, float slideOffset) {}
}
