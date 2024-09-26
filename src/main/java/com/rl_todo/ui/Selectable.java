package com.rl_todo.ui;

import com.rl_todo.DrawingUtils;
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

    public void SetOnSelection(Runnable aOnSeleced)
    {
        myOnSelected = aOnSeleced;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(40,40,48));
        g.fillRect(0,0, getWidth(), getHeight());
        DrawingUtils.DrawText(myPlugin, g, myText, 3, 2, false ,true, null);
    }
}
