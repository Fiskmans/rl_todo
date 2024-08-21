package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MethodMultiSelector extends JPanel
{
    TodoPlugin myPlugin;
    Goal myTarget;
    JPanel myMethodPanel = new JPanel();

    MethodMultiSelector(TodoPlugin aPlugin, Goal aTarget)
    {
        myPlugin = aPlugin;
        myTarget = aTarget;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(220,500));
        setBorder(new CompoundBorder(new LineBorder(Color.gray, 1), new EmptyBorder(2,2,2,2)));

        Setup();
        Refresh();
    }

    private void Setup()
    {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        add(new JLabel("Select a method") , c);

        myMethodPanel.setLayout(new GridBagLayout());
        myMethodPanel.setBorder(new LineBorder(Color.darkGray));
        myMethodPanel.setBackground(getBackground());

        final JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(myMethodPanel, BorderLayout.NORTH);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        add(new JScrollPane(northPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), c);

    }

    private void Refresh()
    {
        myMethodPanel.removeAll();

        GridBagConstraints methodConstraints = new GridBagConstraints();
        methodConstraints.weightx = 1;
        methodConstraints.gridy = 0;
        methodConstraints.fill = GridBagConstraints.HORIZONTAL;
        methodConstraints.anchor = GridBagConstraints.WEST;

        GridBagConstraints checkboxConstraints = new GridBagConstraints();
        checkboxConstraints.weightx = 0;
        checkboxConstraints.gridy = 0;
        checkboxConstraints.fill = GridBagConstraints.HORIZONTAL;
        checkboxConstraints.anchor = GridBagConstraints.WEST;

        for(Method method : myTarget.GetMethodCandidates())
        {
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(myTarget.IsUsingMethod(method));
            checkbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    switch(e.getStateChange())
                    {
                        case 2:
                            myTarget.RemoveMethod(method, true);
                            break;
                        case 1:
                            myTarget.AddMethod(method, true);
                            break;
                        default:
                            TodoPlugin.debug("Unkown checkbox event: " + e.getStateChange(), 0);
                            break;
                    }
                }
            });

            myMethodPanel.add(checkbox, checkboxConstraints);
            myMethodPanel.add(new JLabel(method.myName), methodConstraints);

            methodConstraints.gridy++;
            checkboxConstraints.gridy++;
        }

        revalidate();
        repaint();
    }
}
