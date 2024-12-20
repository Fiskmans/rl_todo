package com.rl_todo.ui;

import com.google.gson.JsonObject;
import com.rl_todo.Goal;
import com.rl_todo.GoalSubscriber;
import com.rl_todo.TodoPlugin;
import com.rl_todo.serialization.SerializableGoal;
import com.rl_todo.serialization.SerializableGoalCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GoalCollectionPanel extends JPanel
{
    private List<Goal> myGoals = new ArrayList<>();
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

    public void AddGoal(Goal aGoal)
    {
        myGoals.add(aGoal);
        add(new GoalTree(myPlugin, aGoal, () -> myPlugin.RequestSave()));

        revalidate();
        repaint();

        myPlugin.RequestSave();
    }

    public void RemoveGoal(Goal aGoal)
    {
        myGoals.remove(aGoal);
        for(Component child : getComponents())
        {
            if (((GoalTree)child).GetGoalUI().myGoal == aGoal)
            {
                remove(child);
                revalidate();
                repaint();
                return;
            }
        }

        revalidate();
        repaint();

        myPlugin.RequestSave();
    }

    public String Serialize()
    {
        SerializableGoalCollection serialized = new SerializableGoalCollection();

        serialized.goals = new ArrayList<>();
        myGoals.forEach((goal) -> serialized.goals.add(goal.Serialize()));

        return myPlugin.myGson
                .newBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(serialized);
    }

    public void Deserialize(JsonObject aBlob)
    {
        removeAll();
        myGoals.clear();

        SerializableGoalCollection serializedGoals = myPlugin.myGson.fromJson(aBlob, SerializableGoalCollection.class);

        if (serializedGoals != null)
        {
            if (serializedGoals.goals != null)
            {
                serializedGoals.goals.forEach((goal) -> AddGoal(Goal.FromSerialized(myPlugin, goal)));
            }
        }
    }
}
