package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;

import javax.swing.*;
import java.awt.*;

public class MethodViewer extends JPanel {

    public MethodViewer(TodoPlugin aPlugin, Method aMethod)
    {
        setLayout(new GridBagLayout());

        GridBagConstraints requirementsConstraints = new GridBagConstraints();

        requirementsConstraints.gridx = 0;
        requirementsConstraints.gridy = 0;
        requirementsConstraints.gridwidth = 2;

        add(new ResourcePoolView(aPlugin, aMethod.myRequires, "Requires"), requirementsConstraints);

        GridBagConstraints takesConstraints = new GridBagConstraints();

        takesConstraints.gridx = 0;
        takesConstraints.gridy = 1;

        add(new ResourcePoolView(aPlugin, aMethod.myTakes, "Takes"), takesConstraints);

        GridBagConstraints makesConstraints = new GridBagConstraints();

        makesConstraints.gridx = 1;
        makesConstraints.gridy = 1;

        add(new ResourcePoolView(aPlugin, aMethod.myMakes, "Makes"), makesConstraints);

    }
}
