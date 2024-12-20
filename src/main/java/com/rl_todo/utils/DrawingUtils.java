package com.rl_todo.utils;

import com.rl_todo.TodoPlugin;
import net.runelite.client.ui.FontManager;

import java.awt.*;

public class DrawingUtils
{
    public static Dimension DrawText(TodoPlugin aPlugin, Graphics g, String aText, int aX, int aY, boolean aRightAlign, boolean aTopAlign, Color aTextColor)
    {
        char[] chars = new char[aText.length()];
        aText.getChars(0,aText.length(), chars, 0);

        int x = aX;
        int y = aY;
        FontMetrics m = g.getFontMetrics();

        Dimension dims = new Dimension(m.charsWidth(chars,0, chars.length), m.getHeight());

        if (aRightAlign)
        {
            x -= dims.width;
        }
        if (aTopAlign)
        {
            y += dims.height;
        }

        g.setColor(aPlugin.myConfig.textOutlineColor());
        g.drawChars(chars, 0, chars.length, x + 1, y );
        g.drawChars(chars, 0, chars.length, x , y + 1);
        g.drawChars(chars, 0, chars.length, x - 1, y);
        g.drawChars(chars, 0, chars.length, x , y - 1);

        if (aTextColor != null)
            g.setColor(aTextColor);
        else
            g.setColor(aPlugin.myConfig.textColor());
        g.drawChars(chars, 0, chars.length, x, y);

        return dims;
    }

    public static Dimension MeasureText(Graphics g, String aText)
    {
        FontMetrics metrics = g.getFontMetrics();

        return new Dimension(metrics.stringWidth(aText), metrics.getHeight());
    }
}
