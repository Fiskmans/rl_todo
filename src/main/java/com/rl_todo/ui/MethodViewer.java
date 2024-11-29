package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;
import com.rl_todo.ui.toolbox.Arrow;

import javax.swing.*;
import java.awt.*;

public class MethodViewer extends JPanel {

    TodoPlugin myPlugin;

    public MethodViewer(TodoPlugin aPlugin)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        myPlugin = aPlugin;
    }

    public void SetMethod(Method aMethod)
    {
        removeAll();
        JLabel name = new JLabel("<html><p style=\\\"width:160px\\\">"+aMethod.myName+"</p></html>");
        name.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        add(name);

        if (!aMethod.myRequires.IsEmpty())
            add(new ResourcePoolView(myPlugin, aMethod.myRequires, "Requires"));

        if (!aMethod.myTakes.IsEmpty())
            add(new ResourcePoolView(myPlugin, aMethod.myTakes, "Takes"));

        if (!aMethod.myTakes.IsEmpty() && !aMethod.myMakes.IsEmpty())
            add(new Arrow());

        if (!aMethod.myMakes.IsEmpty())
            add(new ResourcePoolView(myPlugin, aMethod.myMakes, "Makes"));

        repaint();
        revalidate();
    }
}
