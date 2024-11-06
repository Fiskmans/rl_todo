package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class Arrow extends JPanel {

    public Arrow()
    {
        setMinimumSize(new Dimension(26,20));
        setPreferredSize(new Dimension(26,20));
        setMaximumSize(new Dimension(26,20));

    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(Color.gray);

        final int backing = 12;
        final int thickness = 3;
        final int span = 7;
        final int center = getHeight() / 2;

        final int leftSpace = 3;
        final int rightSpace = 4;

        g.drawLine(leftSpace, center - thickness, backing, center - thickness); // top flat
        g.drawLine(leftSpace, center - thickness, leftSpace, center + thickness); // back
        g.drawLine(leftSpace, center + thickness, backing, center + thickness); // bottom flat

        g.drawLine(backing, center - thickness, backing, center - span); // arrow top backing
        g.drawLine(backing, center + thickness, backing, center + span); // arrow bottom backing

        g.drawLine(backing, center - span, getWidth() - rightSpace, center); // arrow top
        g.drawLine(backing, center + span, getWidth() - rightSpace, center); // arrow bottom
    }
}
