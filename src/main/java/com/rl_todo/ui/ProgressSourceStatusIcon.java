package com.rl_todo.ui;

import com.rl_todo.ProgressSource;
import com.rl_todo.ProgressSourceStatusTracker;
import com.rl_todo.ResourcePool;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.Selectable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ProgressSourceStatusIcon extends JPanel implements ProgressSourceStatusTracker, Selectable
{
    TodoPlugin myPlugin;
    ProgressSource mySource;
    BufferedImage myIcon;
    public String myName;
    Color myBlipColor = new Color(220,80,80);
    final Dimension SIZE = new Dimension(40,40);

    ProgressSourceStatusIcon(TodoPlugin aPlugin, ProgressSource aSource, BufferedImage aIcon, String aName)
    {
        myPlugin = aPlugin;
        mySource = aSource;
        myName = aName;

        setMaximumSize(SIZE);
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);

        myIcon = aIcon;

        setBorder(new EmptyBorder(1,1,1,1));
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

    @Override
    public void SetSelected(boolean aSelected) {

        if (aSelected)
            setBorder(new LineBorder(Color.gray, 1));
        else
            setBorder(new EmptyBorder(1,1,1,1));
    }
}
