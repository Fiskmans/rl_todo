package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;
import com.rl_todo.ui.toolbox.Arrow;

import javax.swing.*;
import java.awt.*;

public class MethodViewer extends JPanel {

    public MethodViewer(TodoPlugin aPlugin, Method aMethod)
    {
        setLayout(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.gridwidth = 3;
        add(new JLabel(aMethod.myName), labelConstraints);

        if (!aMethod.myRequires.IsEmpty())
        {
            GridBagConstraints requirementsConstraints = new GridBagConstraints();

            requirementsConstraints.gridx = 0;
            requirementsConstraints.gridy = 1;
            requirementsConstraints.gridwidth = 3;
            requirementsConstraints.fill = GridBagConstraints.HORIZONTAL;

            add(new ResourcePoolView(aPlugin, aMethod.myRequires, "Requires"), requirementsConstraints);
        }

        if (!aMethod.myTakes.IsEmpty())
        {
            GridBagConstraints takesConstraints = new GridBagConstraints();

            takesConstraints.gridx = 0;
            takesConstraints.gridy = 2;
            takesConstraints.fill = GridBagConstraints.VERTICAL;

            add(new ResourcePoolView(aPlugin, aMethod.myTakes, "Takes"), takesConstraints);
        }

        if (!aMethod.myTakes.IsEmpty() && !aMethod.myMakes.IsEmpty())
        {
            GridBagConstraints arrowConstraints = new GridBagConstraints();

            arrowConstraints.gridx = 1;
            arrowConstraints.gridy = 2;

            add(new Arrow(), arrowConstraints);
        }

        if (!aMethod.myMakes.IsEmpty())
        {
            GridBagConstraints makesConstraints = new GridBagConstraints();

            makesConstraints.gridx = 2;
            makesConstraints.gridy = 2;
            makesConstraints.fill = GridBagConstraints.VERTICAL;

            add(new ResourcePoolView(aPlugin, aMethod.myMakes, "Makes"), makesConstraints);
        }
    }
}
