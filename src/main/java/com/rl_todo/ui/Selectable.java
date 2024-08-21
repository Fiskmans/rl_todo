package com.rl_todo.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Selectable extends JLabel
{
    public Selectable(String aText, Runnable aOnSelect)
    {
        super(aText);
        setBorder(new EmptyBorder(1,1,1,1));
        setToolTipText(aText);

        JLabel instance = this;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                super.mouseEntered(e);
                instance.setBorder(new LineBorder(Color.gray));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                super.mouseExited(e);
                instance.setBorder(new EmptyBorder(1,1,1,1));
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() != 1)
                    return;

                aOnSelect.run();
            }
        });
    }
}
