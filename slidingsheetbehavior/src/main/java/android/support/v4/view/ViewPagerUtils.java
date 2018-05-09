package android.support.v4.view;

import android.view.View;

/**
 * Code based on ViewPagerBottomSheet github repo
 * https://github.com/laenger/ViewPagerBottomSheet/blob/master/vpbs/src/main/java/android/support/v4/view/ViewPagerUtils.java
 */
public class ViewPagerUtils
{
    public static View getCurrentView(ViewPager viewPager)
    {
        final int currentItem = viewPager.getCurrentItem();
        final int childCount = viewPager.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            View child = viewPager.getChildAt(i);
            ViewPager.LayoutParams params = (ViewPager.LayoutParams) child.getLayoutParams();
            if (!params.isDecor && currentItem == params.position)
                return child;
        }

        return null;
    }
}
