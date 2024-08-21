package com.rl_todo.ui;

import com.rl_todo.ProgressSource;
import com.rl_todo.ProgressSourceStatusTracker;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ProgressSourceStatusIcon extends JPanel implements ProgressSourceStatusTracker
{
    TodoPlugin myPlugin;
    ProgressSource mySource;
    BufferedImage myIcon;
    Color myBlipColor = new Color(220,80,80);
    final Dimension SIZE = new Dimension(40,40);

    ProgressSourceStatusIcon(TodoPlugin aPlugin, ProgressSource aSource, BufferedImage aIcon)
    {
        super();
        myPlugin = aPlugin;
        mySource = aSource;

        setPreferredSize(SIZE);

        myIcon = aIcon;

        mySource.OnStatusChanged(this);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        final int inset = 3;

        g.setColor(getBackground());
        g.fillRect(0,0, SIZE.width, SIZE.height);

        g.drawImage(myIcon, inset, inset, SIZE.width - inset, SIZE.height - inset, null);

        g.setColor(myBlipColor);

        g.fillOval(SIZE.width * 6 / 10 + inset, SIZE.height * 6 / 10 + inset, SIZE.width / 5, SIZE.height / 5);

        g.setColor(Color.black);
        g.drawOval(SIZE.width * 6 / 10 + inset, SIZE.height * 6 / 10 + inset, SIZE.width / 5, SIZE.height / 5);
    }

    @Override
    public void OnStatusChanged(ProgressSource.Status aNewStatus)
    {
        switch (aNewStatus)
        {
            case SYNCED:
                myBlipColor = myPlugin.myConfig.blipColorSynced();
                setToolTipText(mySource.GetName() + ": syncronized");
                break;
            case NOT_SYNCED:
                myBlipColor = myPlugin.myConfig.blipColorNotSynced();
                setToolTipText(mySource.GetName() + ": not syncronized");
                break;
        }

        repaint();
    }
}
