package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.IdBuilder;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddGoalPanel extends JPanel
{
    private TodoPlugin myPlugin;
    GoalPanel myOwner;

    public AddGoalPanel(TodoPlugin aPlugin, GoalPanel aOwner)
    {
        myPlugin = aPlugin;
        myOwner = aOwner;

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1;
        constraints.gridy  = 0;
        constraints.gridx  = 0;

        add(new Selectable("Item", () -> { AddItemGoal(); }), constraints);

        constraints.gridy ++;
        add(new Selectable("Level", () -> { AddLevelGoal(); }), constraints);

        constraints.gridy ++;
        add(new Selectable("Quest", () -> { AddQuestGoal(); }), constraints);

    }

    void AddItemGoal()
    {
        myPlugin.myPanel.ResetContent();

        myPlugin.myChatboxItemSearch
                .tooltipText("Select Goal")
                .onItemSelected((aItemId)->
                {
                    String id = IdBuilder.itemId(aItemId);

                    SwingUtilities.invokeLater(() ->
                    {
                        myOwner.myGoals.AddGoal(new Goal(myPlugin, id, 1, true,null));
                    });
                })
                .build();
    }

    void AddLevelGoal()
    {
        myPlugin.IgnorableError("Interface Not yet implemented");
    }

    void AddQuestGoal()
    {
        QuestSetupPopup popup = new QuestSetupPopup(myPlugin);
        popup.show(myPlugin.myPanel, 10, 50);
    }
}