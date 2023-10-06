package com.rl_todo.ui;

import com.rl_todo.GoalCollection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionRecalculateGoals implements ActionListener
{
    private GoalCollection myCollection;

    public ActionRecalculateGoals(GoalCollection aCollection)
    {
        myCollection = aCollection;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myCollection.Load();
    }
}
