package com.rl_todo;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GoalCollection extends JPanel
{
    private List<Goal> myGoals = new ArrayList<>();
    private TodoPlugin myPlugin;
    private Component myBottomStrut = Box.createVerticalStrut(1);
    private GridBagLayout myLayout = new GridBagLayout();


    public GoalCollection()
    {
        super();
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

        String[] goals = myPlugin.myConfig.getGoals().split("\n");

        for(String goal : goals)
        {
            String id = "";
            String rawAmount = "";

            int index = goal.indexOf(':');
            if (index != -1)
            {
                id = goal.substring(0, index).trim();
                rawAmount = goal.substring(index + 1).trim();
            }
            else
            {
                id = goal.trim();
                rawAmount = "1";
            }

            if (!IdBuilder.isValid(id))
                continue;

            int amount = 1;
            try
            {
                amount = Integer.parseInt(rawAmount);
            }
            catch (NumberFormatException e) { }

            AddGoal(new Goal(myPlugin, id, amount));
        }

    }

    public void setPlugin(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
    }

    public void AddGoal(Goal aGoal)
    {
        int index = myGoals.size();
        myGoals.add(aGoal);
        aGoal.SetOwner(this);

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridy = index;
        constraint.weightx = 1;
        constraint.anchor = GridBagConstraints.NORTH;

        add(aGoal, constraint);
        //myPlugin.debug("Added goal [" + aGoal.toString() + "] in slot " + index);

        ReAnchor();
        aGoal.onAdded();
    }

    public void AddSubGoal(Goal aParent, Goal aGoal)
    {
        int index = myGoals.indexOf(aParent) + 1;
        myGoals.add(index, aGoal);
        aGoal.SetOwner(this);

        final GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.gridy = index;
        constraint.weightx = 1;
        constraint.anchor = GridBagConstraints.NORTH;

        add(aGoal, constraint);
        //myPlugin.debug("Added subgoal [" + aGoal.toString() + "] in slot " + index);

        ReLayout(index + 1);
        aGoal.onAdded();
    }

    public void RemoveGoal(Goal aGoal)
    {
        remove(aGoal);
        int index = myGoals.indexOf(aGoal);
        myGoals.remove(index);
        ReLayout(index);
        aGoal.onRemoved();
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
}
