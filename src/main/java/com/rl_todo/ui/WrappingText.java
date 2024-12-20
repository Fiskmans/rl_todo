package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.utils.DrawingUtils;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WrappingText extends JPanel
{
    static final int Padding = 2;

    TodoPlugin myPlugin;
    List<String> myLines;


    WrappingText(TodoPlugin aPlugin, String aText, int aWidth)
    {
        myPlugin = aPlugin;
        myLines = new ArrayList<>();

        final String[] bufferWrapper = {""};

        FontMetrics metrics = getFontMetrics(FontManager.getDefaultFont());

        aText.chars()
            .forEach((c) ->
            {
                String buffer = bufferWrapper[0];

                String next = buffer + (char)c;

                if (metrics.stringWidth(next) > aWidth)
                {
                    if (c == ' ')
                    {
                        myLines.add(buffer);
                        bufferWrapper[0] = String.valueOf((char)c);
                    }
                    else
                    {
                        String trimmed = next.trim();
                        int lastSpace = trimmed.lastIndexOf(' '); // split on spaces if possible
                        if (lastSpace == -1)
                        {
                            myLines.add(buffer);
                            bufferWrapper[0] = String.valueOf((char)c);
                        }
                        else
                        {
                            myLines.add(trimmed.substring(0, lastSpace));
                            bufferWrapper[0] = trimmed.substring(lastSpace + 1);
                        }
                    }
                }
                else
                {
                    bufferWrapper[0] = next;
                }
            });

        myLines.add(bufferWrapper[0].trim());

        int height = metrics.getHeight() * myLines.size();

        setMinimumSize(new Dimension(aWidth, height));
        setPreferredSize(new Dimension(aWidth, height));
        setMaximumSize(new Dimension(aWidth, height));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(), getHeight());

        int at = Padding;

        for (String line : myLines)
        {
            Dimension size = DrawingUtils.DrawText(myPlugin, g, line, Padding, at, false, true, null);
            at += size.height;
        }
    }
}
