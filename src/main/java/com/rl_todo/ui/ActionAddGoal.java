package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.runelite.client.game.chatbox.ChatboxItemSearch;

import javax.swing.*;


public class ActionAddGoal implements ActionListener
{
    private TodoPlugin myPlugin;
    private int myCount = 0;

    public ActionAddGoal(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myPlugin.myChatboxItemSearch.tooltipText("Select Goal").onItemSelected(
            (aItemId)->
            {
                String id = Integer.toString(aItemId);

                myPlugin.myClientThread.invokeLater(() ->
                {
                    String config = myPlugin.myConfig.getGoals();

                    if (!config.isEmpty() && !config.endsWith("\n"))
                        config = config.concat("\n");

                    myPlugin.myConfig.setGoals(config + id);
                });

                SwingUtilities.invokeLater(() ->
                {
                    myPlugin.myPanel.GetGoals().AddGoal(new Goal(myPlugin, id, 1));
                });
            }).build();
    }
}
