package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GoalCollectionPanel extends JPanel
{
    private TodoPlugin myPlugin;
    private BoxLayout myLayout;

    public GoalCollectionPanel(TodoPlugin aPlugin)
    {
        super();
        myPlugin = aPlugin;
        myLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);

        super.setBackground(new Color(30,30,30));
        super.setLayout(myLayout);
        super.setBorder(new EmptyBorder(2,3,3,3));
    }

    public void Load()
    {
        /*
        TODO: reimplement serialize/deserialize
        removeAll();

        String[] lines = myPlugin.myConfig.getGoals().split("\n");

        int at = 0;
        while(at < lines.length)
        {
            at = new Goal(myPlugin, Arrays.asList(lines), at).mySkipTo;
        }
        */
    }

    public void AddGoal(Goal aGoal)
    {
        add(new GoalTree(myPlugin, aGoal));
        revalidate();
        repaint();
    }

    public void RemoveGoal(Goal aGoal)
    {
        for(Component child : getComponents())
        {
            if (((GoalTree)child).GetGoalUI().myGoal == aGoal)
            {
                remove(child);
                return;
            }
        }
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
        // TODO
        //List<String> lines = new ArrayList<>();

        //for (Goal goal : Arrays.stream(getComponents()).map((comp) -> { return (Goal)comp; }).toArray())
        //{
        //    if (!goal.IsRoot())
        //    {
        //        continue;
        //    }

        //    goal.Serialize(lines, 0);
        //}

        //TodoPlugin.myGlobalInstance.myConfig.setGoals(String.join("\n", lines));
    }
}
