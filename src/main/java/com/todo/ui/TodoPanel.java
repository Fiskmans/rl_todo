package com.todo.ui;

import com.todo.GoalCollection;
import com.todo.ProgressManager;
import com.todo.TodoPlugin;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;

public class TodoPanel extends PluginPanel
{
    private final DefaultMutableTreeNode myGoalsRoot = new DefaultMutableTreeNode("Root");
    private final JLabel myTodoHeader = new JLabel("Todo");
    private final JButton myAddGoal = new JButton("Add Goal");
    private final JButton myRecalculate = new JButton("Recalculate");
    private final GoalCollection myGoals = new GoalCollection();

    private final JScrollPane myScrollPane = new JScrollPane(myGoals, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private final JLabel myVisitBankLabel = new JLabel("Please visit the bank");
    private final Component myStrut = Box.createVerticalStrut(1);
    private TodoPlugin myPlugin;

    public GoalCollection GetGoals()
    {
        return myGoals;
    }

    public TodoPanel(TodoPlugin aPlugin, ProgressManager aProgressManager)
    {
        super();

        myPlugin = aPlugin;
        myGoals.setPlugin(aPlugin);

        setBorder(new EmptyBorder(3, 4, 3, 3));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(240, 1000));

        myAddGoal.addActionListener(new ActionAddGoal(aPlugin));
        myRecalculate.addActionListener(new ActionRecalculateGoals(myGoals));

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 1;
        constraint.gridwidth = 2;
        constraint.gridx = 0;
        constraint.gridy = 0;

        add(myTodoHeader, constraint);

        constraint.gridy++;
        add(Box.createVerticalStrut(1), constraint);

        constraint.gridy++;
        constraint.gridwidth = 1;
        add(myAddGoal, constraint);

        constraint.gridx++;
        add(myRecalculate, constraint);

        constraint.gridwidth = 2;
        constraint.gridx = 0;
        constraint.gridy++;
        add(Box.createVerticalStrut(2), constraint);

        constraint.gridy++;
        add(myVisitBankLabel, constraint);

        constraint.gridy++;
        constraint.weighty = 1;
        add(myStrut, constraint);
    }

    public void Enable()
    {
        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.weightx = 1;
        constraint.weighty = 1;
        constraint.gridwidth = 2;
        constraint.gridy = 4;

        constraint.fill = GridBagConstraints.BOTH;
        add(myScrollPane, constraint);

        remove(myStrut);
        remove(myVisitBankLabel);

        revalidate();
        repaint();
    }
}
