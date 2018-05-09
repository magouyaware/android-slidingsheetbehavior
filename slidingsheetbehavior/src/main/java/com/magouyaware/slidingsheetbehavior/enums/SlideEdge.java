package com.magouyaware.slidingsheetbehavior.enums;


import android.support.v4.widget.ViewDragHelper;

/**
 * Enum for determining the edge from which a child will be dragged
 */
public enum SlideEdge
{
    Left(1, ViewDragHelper.EDGE_LEFT),
    Right(2, ViewDragHelper.EDGE_RIGHT),
    Top(3, ViewDragHelper.EDGE_TOP),
    Bottom(4, ViewDragHelper.EDGE_BOTTOM);

    private int m_intValue;
    private int m_dragEdge;
    SlideEdge(int value, int slideEdge)
    {
        m_intValue = value;
        m_dragEdge = slideEdge;
    }

    public int getIntValue()
    {
        return m_intValue;
    }

    public int getDragEdge()
    {
        return m_dragEdge;
    }

    public static SlideEdge fromIntValue(int value)
    {
        switch (value)
        {
            case 1:
                return Left;
            case 2:
                return Right;
            case 3:
                return Top;
            case 4:
                return Bottom;
            default:
                throw new IllegalArgumentException("Invalid location passed to fromIntValue(): " + value);
        }
    }
}