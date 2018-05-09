package com.magouyaware.slidingsheetbehavior.behavior;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;

import com.magouyaware.slidingsheetbehavior.enums.SlideState;

public class SavedState extends AbsSavedState
{
    final SlideState state;

    public SavedState(Parcel source)
    {
        this(source, null);
    }

    public SavedState(Parcel source, ClassLoader loader)
    {
        super(source, loader);
        state = SlideState.fromIntValue(source.readInt());
    }

    public SavedState(Parcelable superState, SlideState state)
    {
        super(superState);
        this.state = state;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        out.writeInt(state.getIntValue());
    }

    public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>()
    {
        @Override
        public SavedState createFromParcel(Parcel in, ClassLoader loader)
        {
            return new SavedState(in, loader);
        }

        @Override
        public SavedState[] newArray(int size)
        {
            return new SavedState[size];
        }
    });
}
