package com.rl_todo;

import com.rl_todo.methods.Method;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GoalCollection extends JPanel
{
    private List<Goal> myGoals = new ArrayList<>();
    private TodoPlugin myPlugin;
    private Component myBottomStrut = Box.createVerticalStrut(1);
    private GridBagLayout myLayout = new GridBagLayout();


    public GoalCollection(TodoPlugin aPlugin)
    {
        super();
        myPlugin = aPlugin;
        super.setBackground(new Color(30,30,30));
        super.setLayout(myLayout);
        super.setBorder(new EmptyBorder(2,3,3,3));

        add(myBottomStrut, new GridBagConstraints());
    }

    public void Load()
    {
        for(Goal g : myGoals)
            remove(g);

        myGoals.clear();

        String[] lines = myPlugin.myConfig.getGoals().split("\n");

        int at = 0;
        while(at < lines.length)
        {
            at = new Goal(null, myPlugin, Arrays.asList(lines), at).mySkipTo;
        }
    }

    public void AddGoal(Goal aGoal)
    {
        int index = myGoals.size();
        myGoals.add(aGoal);
        aGoal.SetOwner(this);
        myPlugin.myProgressManager.AddTracker(aGoal, aGoal.GetId());

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridy = index;
        constraint.weightx = 1;
        constraint.anchor = GridBagConstraints.NORTH;

        add(aGoal, constraint);
        //myPlugin.debug("Added goal [" + aGoal.toString() + "] in slot " + index);

        ReAnchor();
    }

    public void AddSubGoal(Goal aParent, Goal aGoal)
    {
        int index = myGoals.indexOf(aParent) + 1;
        myGoals.add(index, aGoal);
        aGoal.SetOwner(this);
        myPlugin.myProgressManager.AddTracker(aGoal, aGoal.GetId());

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridy = index;
        constraint.weightx = 1;
        constraint.anchor = GridBagConstraints.NORTH;

        add(aGoal, constraint);

        ReLayout(index + 1);
    }

    public void RemoveGoal(Goal aGoal)
    {
        remove(aGoal);
        myPlugin.myProgressManager.RemoveTracker(aGoal, aGoal.GetId());
        aGoal.OnRemoved();

        int index = myGoals.indexOf(aGoal);
        if (index == -1)
        {
            TodoPlugin.debug("Removing a goal that does not exist", 0);
            return;
        }

        myGoals.remove(index);
        ReLayout(index);
    }

    public void RepaintRoots()
    {
        for (Goal goal : myGoals)
        {
            if (goal.IsRoot())
                goal.repaint();
        }
    }

    private void ReLayout(int aFrom)
    {
        for(int i = aFrom; i < myGoals.size(); i++)
        {
            final GridBagConstraints constraint = new GridBagConstraints();
            constraint.fill = GridBagConstraints.HORIZONTAL;
            constraint.gridy = i;
            constraint.weightx = 1;
            constraint.anchor = GridBagConstraints.NORTH;

            myLayout.setConstraints(myGoals.get(i), constraint);
        }
        ReAnchor();
    }

    private void ReAnchor()
    {
        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.VERTICAL;
        constraint.gridy = myGoals.size();
        constraint.weighty = 1;
        constraint.anchor = GridBagConstraints.NORTH;

        myLayout.setConstraints(myBottomStrut, constraint);

        revalidate();
        repaint();
    }

    public void OnConfigChanged()
    {
        // TODO make this work

        // current issue:
        // Creating a goal -> user actions -> saves to config -> causes config reload -> reloads goal while it's being added

        //SwingUtilities.invokeLater(()->
        //{
        //    for (Goal goal : myGoals) {
        //        remove(goal);
        //        myPlugin.myProgressManager.RemoveTracker(goal, goal.GetId());
        //        goal.OnRemoved();
        //    }
        //
        //    myGoals.clear();;
        //    Load();
        //});
    }

    public void SaveConfig()
    {
        List<String> lines = new ArrayList<>();

        for (Goal goal : myGoals)
        {
            if (!goal.IsRoot())
            {
                continue;
            }

            goal.Serialize(lines, 0);
        }

        TodoPlugin.myGlobalInstance.myConfig.setGoals(String.join("\n", lines));
    }
}
