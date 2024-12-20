package com.rl_todo.ui;

import com.rl_todo.utils.DrawingUtils;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Selectable extends JPanel
{
    TodoPlugin myPlugin;
    String myText;
    Runnable myOnSelected;
    boolean myIsUnderlined = false;

    public Selectable(TodoPlugin aPlugin, String aText, Runnable aOnSelect)
    {
        myPlugin = aPlugin;
        myText = aText;
        myOnSelected = aOnSelect;

        setBorder(new EmptyBorder(1,1,1,1));

        setMaximumSize(new Dimension(400, 20));
        setPreferredSize(new Dimension(200, 20));
        setMinimumSize(new Dimension(200, 20));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                super.mouseEntered(e);
                setBorder(new LineBorder(Color.gray));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                super.mouseExited(e);
                setBorder(new EmptyBorder(1,1,1,1));
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() != 1)
                    return;

                myOnSelected.run();
            }
        });
    }

    public void SetOnSelection(Runnable aOnSelected)
    {
        myOnSelected = aOnSelected;
    }
    public void setUnderlined(boolean aValue)
    {
        if (aValue != myIsUnderlined)
        {
            myIsUnderlined = aValue;
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0,0, getWidth(), getHeight());
        DrawingUtils.DrawText(myPlugin, g, myText, 3, 2, false ,true, null);

        Dimension textSize = DrawingUtils.MeasureText(g, myText);

        if (myIsUnderlined)
        {
            g.setColor(new Color(130,130,158));
            g.drawLine(3, 2 + textSize.height, 3 + textSize.width, 2 + textSize.height);
        }
    }
}
