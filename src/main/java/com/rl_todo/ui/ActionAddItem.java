package com.rl_todo.ui;

import com.rl_todo.ProgressManager;
import com.rl_todo.TodoPlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionAddItem  implements ActionListener
{
    private TodoPlugin myPlugin;
    private int myCount = 0;
    private ProgressManager myProgressManager;
    private String myId;

    public ActionAddItem(TodoPlugin aPlugin, ProgressManager aProgressManager, String aId, int aCount)
    {
        myPlugin = aPlugin;
        myProgressManager = aProgressManager;
        myId = aId;
        myCount = aCount;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myPlugin.mySources.Debug.SetProgress(myId, myPlugin.mySources.Debug.GetProgress(myId) + myCount);
    }
}
