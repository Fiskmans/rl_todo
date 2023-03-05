package com.todo.ui;

import com.todo.GoalCollection;

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
