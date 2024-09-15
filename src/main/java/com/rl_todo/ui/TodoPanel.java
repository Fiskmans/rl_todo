package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class TodoPanel extends PluginPanel
{
    private final JLabel myTodoHeader = new JLabel("Todo");
    private  final JButton myBackButton = new JButton("Back");

    private Component myContent;
    private Component myStashedContent;
    private GoalPanel myDefaultContent;
    private TodoPlugin myPlugin;

    public GoalCollectionPanel GetGoals()
    {
        return myDefaultContent.myGoals;
    }

    public TodoPanel(TodoPlugin aPlugin)
    {
        super(false);

        myPlugin = aPlugin;
        myDefaultContent = new GoalPanel(myPlugin);
        myStashedContent = myDefaultContent;

        setBorder(new EmptyBorder(3, 4, 3, 3));
        setLayout(new GridBagLayout());

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;

        add(myTodoHeader, constraint);

        myBackButton.addActionListener(e -> ResetContent());
        myBackButton.setVisible(false);

        constraint.gridy = 1;
        add(myBackButton, constraint);

        constraint.gridy = 1;
        add(new JSeparator(), constraint);

        ResetContent();
    }

    private void Priv_SetContent(Component aComponent)
    {
        if (!Objects.isNull(myContent))
            remove(myContent);

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.BOTH;
        constraint.weightx = 1;
        constraint.weighty = 1;
        constraint.gridx = 0;
        constraint.gridy = 3;

        myContent = aComponent;

        add(myContent, constraint);

        repaint();
    }

    public void SetContent(JPanel aPanel)
    {
        myBackButton.setVisible(true);
        Priv_SetContent(aPanel);
    }

    public void ResetContent()
    {
        TodoPlugin.debug("Returned to main panel", 3);
        myBackButton.setVisible(false);
        Priv_SetContent(myDefaultContent);
    }
}
