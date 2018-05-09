package com.magouyaware.slidingsheetbehavior.enums;

/**
 * Enum for representing different states of a SlidingSheet instance
 */
public enum SlideState
{
    Dragging(1, true),
    Settling(2, true),
    Expanded(3, false),
    Collapsed(4, false),
    Hidden(5, false);

    private int m_intValue;
    private boolean m_moving;
    SlideState(int value, boolean moving)
    {
        m_intValue = value;
        m_moving = moving;
    }

    /**
     * Used for storing/restoring state
     * @param value The integer value of a @SlideState enum
     * @return The SlideState value that corresponds to the given int value
     */
    public static SlideState fromIntValue(int value)
    {
        switch (value)
        {
            case 1:
                return Dragging;
            case 2:
                return Settling;
            case 3:
                return Expanded;
            case 4:
                return Collapsed;
            case 5:
                return Hidden;
            default:
                throw new IllegalArgumentException("Expected value from 1-5 in SlideState.fromIntValue(). Actual value: " + value);
        }
    }

    public boolean isMoving()
    {
        return m_moving;
    }

    /**
     * Used for storing/restoring state
     * @return The int value of this enum value
     */
    public int getIntValue()
    {
        return m_intValue;
    }
}