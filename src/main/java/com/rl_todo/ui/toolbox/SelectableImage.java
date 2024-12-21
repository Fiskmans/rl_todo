package com.rl_todo.ui.toolbox;

import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SelectableImage extends JPanel implements Selectable
{
    BufferedImage myImage;

    public SelectableImage(BufferedImage aImage, Dimension aSize)
    {
        myImage = aImage;
        setBorder(new EmptyBorder(1,1,1,1));

        setMinimumSize(aSize);
        setPreferredSize(aSize);
        setMaximumSize(aSize);

        if (aImage instanceof AsyncBufferedImage)
            ((AsyncBufferedImage)aImage).onLoaded(this::repaint);
    }

    @Override
    public void SetSelected(boolean aSelected)
    {
        if (aSelected)
            setBorder(new LineBorder(Color.GRAY, 1));
        else
            setBorder(new EmptyBorder(1,1,1,1));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(myImage, 0, 0, getWidth(), getHeight(), null);
    }
}
