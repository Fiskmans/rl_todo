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

    private JPanel myContentArea;
    private Component myStashedContent;
    private GoalViewPanel myDefaultContent;
    private TodoPlugin myPlugin;

    public GoalCollectionPanel GetGoals()
    {
        return myDefaultContent.myGoals;
    }

    public TodoPanel(TodoPlugin aPlugin)
    {
        super(false);

        myPlugin = aPlugin;
        myDefaultContent = new GoalViewPanel(myPlugin);
        myStashedContent = myDefaultContent;

        setBorder(new EmptyBorder(3, 4, 3, 3));
        setLayout(new GridBagLayout());

        final GridBagConstraints headerContraints = new GridBagConstraints();
        headerContraints.fill = GridBagConstraints.HORIZONTAL;
        headerContraints.weightx = 1;
        headerContraints.gridx = 0;
        headerContraints.gridy = 0;

        add(myTodoHeader, headerContraints);

        myBackButton.addActionListener(e -> ResetContent());
        myBackButton.setVisible(false);

        final GridBagConstraints backButtonConstraint = new GridBagConstraints();
        backButtonConstraint.weightx = 1;
        backButtonConstraint.gridx = 0;
        backButtonConstraint.gridy = 1;
        add(myBackButton, backButtonConstraint);

        final GridBagConstraints separatorConstraint = new GridBagConstraints();
        separatorConstraint.gridy = 2;
        add(new JSeparator(), separatorConstraint);

        myContentArea = new JPanel();
        myContentArea.setLayout(new BorderLayout());
        myContentArea.setBackground(Color.RED);

        final GridBagConstraints contentConstraints = new GridBagConstraints();
        contentConstraints.fill = GridBagConstraints.BOTH;
        contentConstraints.weightx = 1;
        contentConstraints.weighty = 1;
        contentConstraints.gridx = 0;
        contentConstraints.gridy = 3;

        add(myContentArea, contentConstraints);

        ResetContent();
    }

    public void SetContent(JComponent aPanel)
    {
        myBackButton.setVisible(true);
        myContentArea.removeAll();
        myContentArea.add(aPanel, BorderLayout.CENTER);
    }

    public void ResetContent()
    {
        TodoPlugin.debug("Returned to main panel", 3);
        myBackButton.setVisible(false);
        myContentArea.removeAll();
        myContentArea.add(myDefaultContent, BorderLayout.CENTER);
    }
}
