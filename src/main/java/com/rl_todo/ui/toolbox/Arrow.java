package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class Arrow extends JPanel {

    public Arrow()
    {
        setMinimumSize(new Dimension(20,26));
        setPreferredSize(new Dimension(20,26));
        setMaximumSize(new Dimension(300,26));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(Color.gray);

        final int backing = 12;
        final int thickness = 3;
        final int span = 7;
        final int center = getWidth() / 2;

        final int topSpace = 3;
        final int bottomSpace = 4;

        g.drawLine( center - thickness,topSpace,center - thickness,  backing); // left base
        g.drawLine(center - thickness, topSpace, center + thickness,  topSpace); // top base
        g.drawLine(center + thickness, topSpace, center + thickness,  backing); // right base

        g.drawLine(center - thickness,backing, center - span,  backing); // arrow left backing
        g.drawLine(center + thickness,backing, center + span,  backing); // arrow right backing

        g.drawLine(center - span,backing, center,  getHeight() - bottomSpace); // arrow left
        g.drawLine( center + span,backing, center, getHeight() - bottomSpace); // arrow right
    }
}
