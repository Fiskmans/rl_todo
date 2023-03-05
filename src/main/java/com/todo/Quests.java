package com.todo;

import jdk.tools.jlink.plugin.Plugin;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

public class Quests
{
    public TodoPlugin myPlugin;

    public Quests(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
    }

    public void Load()
    {
        for(Quest quest : Quest.values())
        {
            myPlugin.mySources.Quests.SetProgress(IdBuilder.questId(quest), (quest.getState(myPlugin.myClient) == QuestState.FINISHED) ? 1 : 0);
        }
    }
}
