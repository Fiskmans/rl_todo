package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MethodViewerPopup extends JPopupMenu
{
    public MethodViewerPopup(TodoPlugin aPlugin, Method aMethod, JComponent aRelativeTo, int x, int y)
    {
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(new LineBorder(Color.gray, 1), new EmptyBorder(2,2,2,2)));

        add(new MethodViewer(aPlugin, aMethod));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

                setLocation(SwingUtilities.convertPoint(aRelativeTo, x - getWidth(), y - getHeight(), getParent()));
                super.componentResized(e);
                setLocation(SwingUtilities.convertPoint(aRelativeTo, x - getWidth(), y - getHeight(), getParent()));
            }
        });

        show(aRelativeTo, x - 150, y - 100);

        //TODO: make this less flickery
    }
}
