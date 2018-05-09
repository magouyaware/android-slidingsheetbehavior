package com.magouyaware.slidingsheetbehavior.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPagerUtils;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.magouyaware.slidingsheetbehavior.R;
import com.magouyaware.slidingsheetbehavior.callbacks.ISlidingSheetCallback;
import com.magouyaware.slidingsheetbehavior.enums.SlideEdge;
import com.magouyaware.slidingsheetbehavior.enums.SlideState;

import java.lang.ref.WeakReference;


/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as
 * a sliding sheet.
 */
public class SlidingSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
{
    /**
     * Peek at the 16:9 ratio of the size difference between the parent's height and width.
     *
     * <p>This can be used as a parameter for {@link #setPeekSize(int)}.
     * {@link #getPeekSize()} will return this when the value is set.</p>
     */
    public static final int PEEK_SIZE_AUTO = -1;

    private static final int NO_EDGE_DRAG = 0;
    private static final float HIDE_THRESHOLD = 0.5f;
    private static final float HIDE_FRICTION = 0.1f;

    private static final int SCROLL_LEFT_OR_UP = -1;
    private static final int SCROLL_RIGHT_OR_DOWN = 1;

    private float m_maximumVelocity;
    private int m_peekSize;
    private boolean m_peekSizeAuto;
    private int m_peekSizeMin;
    private int m_minOffset;
    private int m_maxOffset;
    private boolean m_hideable;
    private boolean m_skipCollapsed;
    private SlideState m_state = SlideState.Collapsed;
    private ViewDragHelper m_viewDragHelper;
    private boolean m_ignoreEvents;
    private int m_lastNestedScrollDelta;
    private boolean m_nestedScrolled;
    private int m_parentSize;
    private WeakReference<CoordinatorLayout> m_parentRef;
    private WeakReference<V> m_childRef;
    private WeakReference<View> m_nestedScrollingChildRef;
    private ISlidingSheetCallback m_callback;
    private VelocityTracker m_velocityTracker;
    private int m_activePointerId;
    private int m_initialX;
    private int m_initialY;
    private boolean m_touchingScrollingChild;
    private SlideEdge m_slideEdge;

    //TODO: Come up with a better name for m_slideIsReversed.
    //      Since this is based on Google's BottomSheetBehavior class, this value would be true for
    //      sliding from the top.  The calculations are the same for bottom and right edges, so this
    //      is also true when sliding from the left edge.
    private boolean m_slideIsReversed;
    private boolean m_slideIsVertical;
    private boolean m_edgeDragEnabled;

    private boolean m_needsOffsetUpdate = false;
    private ViewDragHelper.Callback m_dragCallback = new ViewDragHelperCallback();

    /**
     * A utility function to get the {@link SlidingSheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link SlidingSheetBehavior}.
     * @return The {@link SlidingSheetBehavior} associated with the {@code view}, or null if there isn't one
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> SlidingSheetBehavior<V> from(V view)
    {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams))
            return null; //The view is not a child of CoordinatorLayout

        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
        if (!(behavior instanceof SlidingSheetBehavior))
            return null; //The view is not associated with SlidingSheetBehavior

        return (SlidingSheetBehavior<V>) behavior;
    }

    /**
     * Default constructor for instantiating SlidingSheetBehaviors.  It uses PEEK_SIZE_AUTO for the
     * peek size, is not hideable, does not skip collapsed state, and doesn't use edge dragging. To
     * change these values use the appropriate setter methods.
     */
    public SlidingSheetBehavior(Context context, SlideEdge edge)
    {
        initializeInternalState(context);
        setPeekSize(PEEK_SIZE_AUTO);
        setHideable(false);
        setSkipCollapsed(false);
        enableEdgeDrag(false);
        setSlideEdge(edge);
    }

    /**
     * Default constructor for inflating SlidingSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public SlidingSheetBehavior(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initializeInternalState(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingSheetBehavior_Layout);

        TypedValue value = array.peekValue(R.styleable.SlidingSheetBehavior_Layout_behavior_peekSize);
        if (value != null && value.data == PEEK_SIZE_AUTO)
            setPeekSize(value.data);
        else
            setPeekSize(array.getDimensionPixelSize(R.styleable.SlidingSheetBehavior_Layout_behavior_peekSize, PEEK_SIZE_AUTO));

        setHideable(array.getBoolean(R.styleable.SlidingSheetBehavior_Layout_behavior_hideable, false));
        setSkipCollapsed(array.getBoolean(R.styleable.SlidingSheetBehavior_Layout_behavior_skipCollapsed, false));
        enableEdgeDrag(array.getBoolean(R.styleable.SlidingSheetBehavior_Layout_behavior_enableEdgeDrag, false));

        int slideEdge = array.getInt(R.styleable.SlidingSheetBehavior_Layout_behavior_slideEdge, SlideEdge.Bottom.getDragEdge());
        setSlideEdge(SlideEdge.fromIntValue(slideEdge));
        
        array.recycle();
    }

    private void initializeInternalState(Context context)
    {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        m_maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        m_peekSizeMin = context.getResources().getDimensionPixelSize(R.dimen.slidingsheet_min_peek_size);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child)
    {
        return new SavedState(super.onSaveInstanceState(parent, child), m_state);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state)
    {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());

        // Intermediate states are restored as collapsed state
        if (ss.state == SlideState.Dragging || ss.state == SlideState.Settling)
            m_state = SlideState.Collapsed;
        else
            m_state = ss.state;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection)
    {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child))
            ViewCompat.setFitsSystemWindows(child, true);

        m_parentRef = new WeakReference<>(parent);

        // First let the parent lay it out
        int savedChildPosition = getChildPosition(child);
        parent.onLayoutChild(child, layoutDirection);

        // Offset the sliding sheet
        m_parentSize = getViewSize(parent);
        int actualPeekSize = m_peekSizeAuto ? Math.max(m_peekSizeMin, calculateAutoPeekSize(parent, child)) : m_peekSize;

        setChildOffsetRange(child, actualPeekSize);

        if (m_state.isMoving())
            offsetChildView(child, savedChildPosition - getChildPosition(child));
        else
            offsetChildView(child, getPositionForState(m_state, child));

        if (m_viewDragHelper == null)
            m_viewDragHelper = ViewDragHelper.create(parent, m_dragCallback);

        enableEdgeDragInternal(m_edgeDragEnabled);

        // TODO: We should see if there is a better way to get around this issue
        // For some strange reason, if there are multiple sliding sheets with the same parent layout,
        // we get weird touch/sliding behavior unless layout is set to be clickable. Rather than
        // require the developer to remember to set that attribute, we'll just take care of that here
        child.setClickable(true);

        m_childRef = new WeakReference<>(child);
        m_nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        if (!child.isShown())
        {
            m_ignoreEvents = true;
            return false;
        }

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN)
            reset();

        if (m_velocityTracker == null)
            m_velocityTracker = VelocityTracker.obtain();

        m_velocityTracker.addMovement(event);

        switch (action)
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                m_touchingScrollingChild = false;
                m_activePointerId = MotionEvent.INVALID_POINTER_ID;

                // Reset the ignore flag
                if (m_ignoreEvents)
                {
                    m_ignoreEvents = false;
                    return false;
                }
                break;
            }
            case MotionEvent.ACTION_DOWN:
            {
                m_initialX = (int) event.getX();
                m_initialY = (int) event.getY();
                View scroll = getNestedScrollingChild();

                if (scroll != null && parent.isPointInChildBounds(scroll, m_initialX, m_initialY))
                {
                    m_activePointerId = event.getPointerId(event.getActionIndex());
                    m_touchingScrollingChild = true;
                }

                m_ignoreEvents = m_activePointerId == MotionEvent.INVALID_POINTER_ID &&
                                 !parent.isPointInChildBounds(child, m_initialX, m_initialY);
                break;
            }
        }

        if (!m_ignoreEvents && m_viewDragHelper.shouldInterceptTouchEvent(event))
            return true;

        // We have to handle cases that the ViewDragHelper does not capture the sliding sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = getNestedScrollingChild();

        // Determining if we should intercept is super confusing... pulling everything out to individual
        // variables to make it a little easier to read by pulling each part out into its own variable
        boolean isMoveEvent = action == MotionEvent.ACTION_MOVE;
        boolean nestedScrollNotNull = scroll != null;
        boolean notDragging = m_state != SlideState.Dragging;
        boolean pointInChildBounds = nestedScrollNotNull && parent.isPointInChildBounds(scroll, (int)event.getX(), (int)event.getY());
        boolean touchEventIsDrag = touchEventIndicatesDrag(event);
        boolean needsChildBoundsCheck = (canScrollInEventDirection(scroll, event));

        return isMoveEvent &&
               nestedScrollNotNull &&
               !m_ignoreEvents &&
               notDragging &&
               (!needsChildBoundsCheck || (needsChildBoundsCheck && !pointInChildBounds)) &&
               touchEventIsDrag;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        if (!child.isShown())
            return false;

        int action = event.getActionMasked();
        if (m_state == SlideState.Dragging && action == MotionEvent.ACTION_DOWN)
            return true;

        if (m_viewDragHelper != null)
            m_viewDragHelper.processTouchEvent(event);

        if (action == MotionEvent.ACTION_DOWN)
            reset();

        if (m_velocityTracker == null)
            m_velocityTracker = VelocityTracker.obtain();

        m_velocityTracker.addMovement(event);

        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the sliding sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !m_ignoreEvents && touchEventIndicatesDrag(event))
            m_viewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));

        return m_hideable && m_edgeDragEnabled && m_viewDragHelper.isEdgeTouched(m_slideEdge.getDragEdge());
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type)
    {
        m_lastNestedScrollDelta = 0;
        m_nestedScrolled = false;

        return (axes & (m_slideIsVertical ? ViewCompat.SCROLL_AXIS_VERTICAL : ViewCompat.SCROLL_AXIS_HORIZONTAL)) != 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type)
    {
        if (target != getNestedScrollingChild())
            return;

        int delta = m_slideIsVertical ? dy : dx;
        int currentPosition = getChildPosition(child);
        int newPosition = currentPosition - delta;

        if (m_needsOffsetUpdate)
            setChildOffsetRange(child, m_peekSize);

        int expandedPostion = getPositionForState(SlideState.Expanded, child);
        int collapsedPosition = getPositionForState(SlideState.Collapsed, child);

        if (delta > 0 && m_slideIsReversed && !canScroll(target, SCROLL_RIGHT_OR_DOWN, true))
        {
            if (newPosition < collapsedPosition || m_hideable)
            {
                consumed[1] = delta;
                offsetChildView(child, -delta);
                setStateInternal(SlideState.Dragging);
            }
            else
            {
                consumed[1] = currentPosition - expandedPostion;
                offsetChildView(child, -consumed[1]);
                setStateInternal(SlideState.Collapsed);
            }
        }
        else if (delta > 0 && !m_slideIsReversed)
        {
            if (newPosition < expandedPostion)
            {
                consumed[1] = currentPosition - expandedPostion;
                offsetChildView(child, -consumed[1]);
                setStateInternal(SlideState.Expanded);
            }
            else
            {
                consumed[1] = delta;
                offsetChildView(child, -delta);
                setStateInternal(SlideState.Dragging);
            }
        }
        else if (delta < 0 && m_slideIsReversed)
        {
            if (newPosition < expandedPostion)
            {
                consumed[1] = currentPosition - expandedPostion;
                offsetChildView(child, -consumed[1]);
                setStateInternal(SlideState.Expanded);
            }
            else
            {
                consumed[1] = delta;
                offsetChildView(child, -delta);
                setStateInternal(SlideState.Dragging);
            }
        }
        else if (delta < 0 && !canScroll(target, SCROLL_LEFT_OR_UP, true))
        {
            if (newPosition < collapsedPosition || m_hideable)
            {
                consumed[1] = delta;
                offsetChildView(child, -delta);
                setStateInternal(SlideState.Dragging);
            }
            else
            {
                consumed[1] = currentPosition - expandedPostion;
                offsetChildView(child, -consumed[1]);
                setStateInternal(SlideState.Collapsed);
            }
        }

        dispatchOnSlide(getChildPosition(child));
        m_lastNestedScrollDelta = delta;
        m_nestedScrolled = true;
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int type)
    {
        if (m_needsOffsetUpdate)
            setChildOffsetRange(child, m_peekSize);

        if (getChildPosition(child) == getPositionForState(SlideState.Expanded, child))
        {
            setStateInternal(SlideState.Expanded);
            return;
        }

        if (!m_nestedScrolled || getNestedScrollingChild() == null)
            return;

        SlideState targetState = SlideState.Collapsed;

        if ((m_lastNestedScrollDelta > 0 && !m_slideIsReversed) || m_lastNestedScrollDelta < 0 && m_slideIsReversed)
            targetState = SlideState.Expanded;
        else if (shouldHide(child, getVelocity()))
            targetState = SlideState.Hidden;
        else if (m_lastNestedScrollDelta == 0 && childCloserToExpandedState(child)) // Not moving
            targetState = SlideState.Expanded;

        int position = getPositionForState(targetState, child);

        int finalLeft = m_slideIsVertical ? child.getLeft() : position;
        int finalTop = m_slideIsVertical ? position : child.getTop();

        if (m_viewDragHelper.smoothSlideViewTo(child, finalLeft, finalTop))
        {
            setStateInternal(SlideState.Settling);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
        }
        else
        {
            setStateInternal(targetState);
        }

        m_nestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY)
    {
        //TODO: Is this behavior correct? We need to thorougly test this out
        return target == getNestedScrollingChild() &&
                (m_state != SlideState.Expanded || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }

    /**
     * Sets the height of the sliding sheet when it is collapsed.
     *
     * @param peekSize The size of the collapsed sheet in pixels, or
     *                   {@link #PEEK_SIZE_AUTO} to configure the sheet to peek automatically
     */
    public final void setPeekSize(int peekSize)
    {
        boolean needsLayout = false;
        if (peekSize == PEEK_SIZE_AUTO)
        {
            if (!m_peekSizeAuto)
            {
                m_peekSizeAuto = true;
                needsLayout = true;
            }
        }
        else if (m_peekSizeAuto || m_peekSize != peekSize)
        {
            m_peekSizeAuto = false;
            m_peekSize = Math.max(0, peekSize);

            V child = getChild();
            setChildOffsetRange(child, peekSize);
            needsLayout = true;
        }

        if (needsLayout && m_state == SlideState.Collapsed)
        {
            V child = getChild();
            if (child != null)
                child.requestLayout();
        }
    }

    /**
     * Gets the size of the sheet when it is collapsed.
     *
     * @return The size of the collapsed sheet in pixels, or {@link #PEEK_SIZE_AUTO}
     *         if the sheet is configured to peek with an automatic value
     */
    public final int getPeekSize()
    {
        return m_peekSizeAuto ? PEEK_SIZE_AUTO : m_peekSize;
    }

    /**
     * Sets whether this sliding sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this sliding sheet hideable.
     */
    public void setHideable(boolean hideable)
    {
        m_hideable = hideable;
    }

    /**
     * Gets whether this sheet can hide when it is swiped down.
     *
     * @return {@code true} if this sliding sheet can hide.
     */
    public boolean isHideable()
    {
        return m_hideable;
    }

    /**
     * Sets whether this sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the sheet should skip the collapsed state.
     */
    public void setSkipCollapsed(boolean skipCollapsed)
    {
        m_skipCollapsed = skipCollapsed;
    }

    /**
     * Gets whether this sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the sheet should skip the collapsed state.
     */
    public boolean getSkipCollapsed()
    {
        return m_skipCollapsed;
    }

    /**
     * @return The edge that the child is anchored to
     */
    public SlideEdge getSlideEdge()
    {
        return m_slideEdge;
    }

    /**
     * Enable or disable edge dragging.  Setting this has no effect unless sheet is hideable.
     * @param enabled True if edge dragging should be enabled, false otherwise
     */
    public void enableEdgeDrag(boolean enabled)
    {
        m_edgeDragEnabled = enabled;
        enableEdgeDragInternal(enabled);
    }

    /**
     * @return True if edge dragging is enabled, False otherwise
     */
    public boolean edgeDragEnabled()
    {
        return m_edgeDragEnabled;
    }

    /**
     * Sets a callback to be notified of sliding sheet events.
     *
     * @param callback The callback to notify when sliding sheet events occur.
     */
    public void setSlidingSheetCallback(ISlidingSheetCallback callback)
    {
        m_callback = callback;
    }

    /**
     * Sets the state of the sheet. The sheet will transition to that state with
     * animation.
     */
    public final void setState(final SlideState stateRequest)
    {
        if (stateRequest == m_state)
            return;

        //Quick sanitation check to make sure we never set to a bad state
        final SlideState finalState = (stateRequest == SlideState.Hidden && !m_hideable) ? SlideState.Collapsed : stateRequest;

        if (m_childRef == null)
        {
            // The view is not laid out yet; modify m_state and let onLayoutChild handle it later
            if (finalState == SlideState.Collapsed || finalState == SlideState.Expanded || finalState == SlideState.Hidden)
                m_state = finalState;

            return;
        }

        final V child = getChild();
        if (child == null)
            return;

        // Start the animation; wait until a pending layout if there is one.
        ViewParent parent = child.getParent();
        if (parent == null || !parent.isLayoutRequested() || !ViewCompat.isAttachedToWindow(child))
        {
            startSettlingAnimation(child, finalState);
            return;
        }

        child.post(new Runnable()
        {
            @Override
            public void run()
            {
                startSettlingAnimation(child, finalState);
            }
        });
    }

    /**
     * Gets the current state of the sliding sheet.
     */
    public final SlideState getState()
    {
        return m_state;
    }

    private void enableEdgeDragInternal(boolean enabled)
    {
        if (m_viewDragHelper != null)
            m_viewDragHelper.setEdgeTrackingEnabled(enabled ? m_slideEdge.getDragEdge() : NO_EDGE_DRAG);
    }

    private V getChild()
    {
        return m_childRef != null ? m_childRef.get() : null;
    }

    private View getNestedScrollingChild()
    {
        return m_nestedScrollingChildRef != null ? m_nestedScrollingChildRef.get() : null;
    }

    private CoordinatorLayout getParent()
    {
        return m_parentRef != null ? m_parentRef.get() : null;
    }

    private int getChildPosition(View childView)
    {
        return m_slideIsVertical ? childView.getTop() : childView.getLeft();
    }

    private int getChildEndPosition(View childView)
    {
        return m_slideIsVertical ? childView.getBottom() : childView.getRight();
    }

    private void offsetChildView(View childView, int offset)
    {
        if (childView == null)
            return;

        if (m_slideIsVertical)
            ViewCompat.offsetTopAndBottom(childView, offset);
        else
            ViewCompat.offsetLeftAndRight(childView, offset);
    }

    private int getViewSize(View view)
    {
        return m_slideIsVertical ? view.getHeight() : view.getWidth();
    }

    private int calculateAutoPeekSize(CoordinatorLayout parent, V child)
    {
        // I didn't like the original implementation for calculating an auto peek size. In certain
        // situations (like when in landscape mode) the calculated value would be negative and then
        // default to the min, so I created my own...
        int parentSize = getViewSize(parent);
        int peekSize = parentSize - (parentSize * 9 / 16);

        //Make sure the peek is not larger than the child
        int childSize = getViewSize(child);
        return peekSize < childSize ? peekSize : childSize;
    }

    private void setStateInternal(SlideState stateRequest)
    {
        if (m_state == stateRequest)
            return;

        //Prevent invalid states
        m_state = (stateRequest == SlideState.Hidden && !m_hideable) ? SlideState.Collapsed : stateRequest;

        View child = getChild();
        if (child != null && m_callback != null)
            m_callback.onStateChanged(child, m_state);
    }

    private void setSlideEdge(SlideEdge edge)
    {
        m_slideEdge = edge;
        m_slideIsReversed = m_slideEdge == SlideEdge.Left || m_slideEdge == SlideEdge.Top;
        m_slideIsVertical = m_slideEdge == SlideEdge.Bottom || m_slideEdge == SlideEdge.Top;
    }

    private void reset()
    {
        m_activePointerId = ViewDragHelper.INVALID_POINTER;
        if (m_velocityTracker != null)
        {
            m_velocityTracker.recycle();
            m_velocityTracker = null;
        }
    }

    private boolean shouldHide(View child, float velocity)
    {
        if (!m_hideable)
            return false;

        //If the velocity indicates we are expanding, then we also don't want to hide
        if ((velocity < 0 && !m_slideIsReversed) || (velocity > 0 && m_slideIsReversed) || (velocity == 0 && childCloserToExpandedState(child)))
        {
            return false;
        }

        // Now that we've established we aren't opening the drawer, we can always return true if
        // we are supposed to skip the collapsed state
        if (m_skipCollapsed)
            return true;

        if (!m_slideIsReversed)
        {
            // Do not hide if the child is not in the peek area
            int childPosition = getChildPosition(child);
            if (childPosition < m_maxOffset)
                return false;

            final float newPosition = childPosition + velocity * HIDE_FRICTION;
            return Math.abs(newPosition - m_maxOffset) / (float) m_peekSize > HIDE_THRESHOLD;
        }
        else
        {
            // Do not hide if the child is not in the peek area
            int childEndPosition = getChildEndPosition(child);
            if (childEndPosition > m_peekSize)
                return false;

            final float newEndPosition = childEndPosition + velocity * HIDE_FRICTION;
            return (newEndPosition / (float) m_peekSize) < HIDE_THRESHOLD;
        }
    }

    private View findScrollingChild(View view)
    {
        if (view instanceof NestedScrollingChild)
            return view;

        if (view instanceof ViewPager)
        {
            ViewPager viewPager = (ViewPager) view;
            View currentViewPagerChild = ViewPagerUtils.getCurrentView(viewPager);
            View scrollingChild = findScrollingChild(currentViewPagerChild);
            if (scrollingChild != null)
                return scrollingChild;
        }
        else if (view instanceof ViewGroup)
        {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++)
            {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null)
                    return scrollingChild;
            }
        }

        return null;
    }

    private float getVelocity()
    {
        m_velocityTracker.computeCurrentVelocity(1000, m_maximumVelocity);
        if (m_slideIsVertical)
            return VelocityTrackerCompat.getYVelocity(m_velocityTracker, m_activePointerId);

        return VelocityTrackerCompat.getXVelocity(m_velocityTracker, m_activePointerId);
    }

    private void startSettlingAnimation(View child, SlideState state)
    {
        if (state != SlideState.Collapsed && state != SlideState.Expanded && state != SlideState.Hidden)
            throw new IllegalArgumentException("Illegal state argument: " + state);

        int newPosition = getPositionForState(state, child);
        setStateInternal(SlideState.Settling);

        int finalLeft = m_slideIsVertical ? child.getLeft() : newPosition;
        int finalTop = m_slideIsVertical ? newPosition : child.getTop();

        if (m_viewDragHelper.smoothSlideViewTo(child, finalLeft, finalTop))
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
    }

    /**
     * Sets the minimum and maximum sliding offsets for the child, taking peek size into account
     * @param child The sliding child
     * @param peekSize The peek size for the child, in pixels
     */
    private void setChildOffsetRange(V child, int peekSize)
    {
        if (child == null)
        {
            m_needsOffsetUpdate = true;
            return;
        }

        m_needsOffsetUpdate = false;
        int childSize = getViewSize(child);

        if (m_slideIsReversed)
        {
            m_minOffset = peekSize - childSize;;
            m_maxOffset = 0;
        }
        else
        {
            m_minOffset = Math.max(0, m_parentSize - childSize);
            m_maxOffset = Math.max(m_parentSize - peekSize, m_minOffset);
        }
    }

    private boolean childCloserToExpandedState(View child)
    {
        int currentPosition = getChildPosition(child);
        int expandedPosition = getPositionForState(SlideState.Expanded, child);
        int collapsedPosition = getPositionForState(SlideState.Collapsed, child);
        int expandedThreshold = (collapsedPosition - expandedPosition) / 2;

        return m_slideIsReversed ? currentPosition > expandedThreshold : currentPosition < expandedThreshold;
    }

    private boolean touchEventIndicatesDrag(MotionEvent event)
    {
        int touchDifference = (int)(m_slideIsVertical ? m_initialY - event.getY() : m_initialX - event.getX());
        return Math.abs(touchDifference) > m_viewDragHelper.getTouchSlop();
    }

    private int getPositionForState(SlideState state, View child)
    {
        switch (state)
        {
            case Hidden:
                return m_slideIsReversed ? -getViewSize(child) : m_parentSize;
            case Collapsed:
                return m_slideIsReversed ? m_minOffset : m_maxOffset;
            case Expanded:
                return m_slideIsReversed ? m_maxOffset : m_minOffset;
            case Dragging:
            case Settling:
                return getChildPosition(child);
            default:
                throw new RuntimeException("Unexpected state in SlidingSheetBehavior.getPositionForState(): " + state.toString());
        }
    }

    private boolean canScroll(View view, int direction, boolean inSlidingSheetDirection)
    {
        if (view == null)
            return false;

        if (inSlidingSheetDirection)
            return m_slideIsVertical ? view.canScrollVertically(direction) : view.canScrollHorizontally(direction);

        return m_slideIsVertical ? view.canScrollHorizontally(direction) : view.canScrollVertically(direction);
    }

    private boolean canScrollInEventDirection(View view, MotionEvent event)
    {
        if (view == null)
            return false;

        int verticalDifference = m_initialY - (int)event.getY();
        int horizontalDifference = m_initialX - (int)event.getX();

        if (Math.abs(verticalDifference) > Math.abs(horizontalDifference))
            return view.canScrollVertically(verticalDifference) || view.canScrollVertically(-verticalDifference);
        else
            return view.canScrollHorizontally(horizontalDifference) || view.canScrollHorizontally(-horizontalDifference);
    }

    /**
     * The new offset of this sliding sheet within [-1,1] range. Offset
     * increases as this sliding sheet is moving towards expanded state. From 0 to 1 the sheet
     * is between collapsed and expanded states and from -1 to 0 it is
     * between hidden and collapsed states.
     */
    private void dispatchOnSlide(int position)
    {
        View child = getChild();
        if (child == null || m_callback == null)
            return;

        int hiddenPosition = getPositionForState(SlideState.Hidden, child);
        int collapsedPosition = getPositionForState(SlideState.Collapsed, child);
        int expandedPosition = getPositionForState(SlideState.Expanded, child);

        boolean betweenHiddenAndCollapsed = m_slideIsReversed ?
                (position >= hiddenPosition && position < collapsedPosition) :
                (position > collapsedPosition && position <= hiddenPosition);

        boolean betweenCollapsedAndExpanded = m_slideIsReversed ?
                (position > collapsedPosition && position <= expandedPosition) :
                (position >= expandedPosition && position < collapsedPosition);

        float slideOffset = 0.0f; //Assume collapsed
        if (betweenHiddenAndCollapsed)
        {
            int distance = Math.abs(hiddenPosition - collapsedPosition);
            float relativePosition = Math.abs(position - collapsedPosition);
            slideOffset = -1 * (relativePosition / distance);
        }
        else if (betweenCollapsedAndExpanded)
        {
            int distance = Math.abs(collapsedPosition - expandedPosition);
            float relativePosition = Math.abs(position - expandedPosition);
            slideOffset = 1 - (relativePosition / distance);
        }

        m_callback.onSlide(child, slideOffset);
    }

    @VisibleForTesting
    int getPeekSizeMin()
    {
        return m_peekSizeMin;
    }

    private class SettleRunnable implements Runnable
    {
        private final View m_view;
        private final SlideState m_targetState;

        SettleRunnable(View view, SlideState targetState)
        {
            m_view = view;
            m_targetState = targetState;
        }

        @Override
        public void run()
        {
            if (m_viewDragHelper != null && m_viewDragHelper.continueSettling(true))
                ViewCompat.postOnAnimation(m_view, this);
            else
                setStateInternal(m_targetState);
        }
    }

    private class ViewDragHelperCallback extends ViewDragHelper.Callback
    {
        @Override
        public boolean tryCaptureView(View capture, int pointerId)
        {
            if (m_state == SlideState.Dragging || m_touchingScrollingChild ||
                (m_state == SlideState.Expanded && m_activePointerId == pointerId && viewCanScroll(getNestedScrollingChild())))
            {
                return false;
            }

            View childView = getChild();
            return childView != null && childView == capture;
        }

        private boolean viewCanScroll(View view)
        {
            if (view == null)
                return false;

            if (m_slideIsVertical)
                return ViewCompat.canScrollVertically(view, -1);

            return ViewCompat.canScrollHorizontally(view, -1);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId)
        {
            V child = getChild();
            if (child != null)
                m_viewDragHelper.captureChildView(child, pointerId);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        {
            dispatchOnSlide(m_slideIsVertical ? top : left);
        }

        @Override
        public void onViewDragStateChanged(int state)
        {
            if (state == ViewDragHelper.STATE_DRAGGING)
                setStateInternal(SlideState.Dragging);
        }

        @Override
        public void onViewReleased(View releasedChild, float xVelocity, float yVelocity)
        {
            float velocity = m_slideIsVertical ? yVelocity : xVelocity;

            SlideState targetState = SlideState.Collapsed;
            if (shouldHide(releasedChild, getVelocity()))
                targetState = SlideState.Hidden;
            else if (velocity < 0 && !m_slideIsReversed) // Moving up or left
                targetState = SlideState.Expanded;
            else if (velocity > 0 && m_slideIsReversed) // Moving right or down
                targetState = SlideState.Expanded;
            else if (velocity == 0 && childCloserToExpandedState(releasedChild)) // Not moving
                targetState = SlideState.Expanded;

            int position = getPositionForState(targetState, releasedChild);
            int finalLeft = m_slideIsVertical ? releasedChild.getLeft() : position;
            int finalTop = m_slideIsVertical ? position : releasedChild.getTop();

            if (m_viewDragHelper.settleCapturedViewAt(finalLeft, finalTop))
            {
                setStateInternal(SlideState.Settling);
                ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
                return;
            }

            setStateInternal(targetState);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy)
        {
            return m_slideIsVertical ? clampViewPosition(child, top) : child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx)
        {
            return m_slideIsVertical ? child.getLeft() : clampViewPosition(child, left);
        }

        @Override
        public int getViewVerticalDragRange(View child)
        {
            return m_slideIsVertical ? getDragRange() : super.getViewVerticalDragRange(child);
        }

        @Override
        public int getViewHorizontalDragRange(View child)
        {
            return m_slideIsVertical ? super.getViewHorizontalDragRange(child) : getDragRange();
        }

        private int getDragRange()
        {
            return m_hideable ? m_parentSize - m_minOffset : m_maxOffset - m_minOffset;
        }

        private int clampViewPosition(View child, int position)
        {
            int low;
            int high;

            if (m_slideIsReversed)
            {
                low = m_hideable ? -getViewSize(child) : m_minOffset;
                high = m_maxOffset;
            }
            else
            {
                low = m_minOffset;
                high = m_hideable ? m_parentSize : m_maxOffset;
            }

            return constrain(position, low, high);
        }

        private int constrain(int amount, int low, int high)
        {
            return amount < low ? low : (amount > high ? high : amount);
        }
    }
}