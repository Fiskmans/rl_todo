package com.rl_todo;

import com.rl_todo.ui.IdBuilder;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.VarPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quests
{
    public static List<Quest> MiniQuests = new ArrayList<>(Arrays.asList(
            Quest.ENTER_THE_ABYSS,
            Quest.BEAR_YOUR_SOUL,
            Quest.ALFRED_GRIMHANDS_BARCRAWL,
            Quest.CURSE_OF_THE_EMPTY_LORD,
            Quest.THE_ENCHANTED_KEY,
            Quest.THE_GENERALS_SHADOW,
            Quest.SKIPPY_AND_THE_MOGRES,
            Quest.MAGE_ARENA_I,
            Quest.LAIR_OF_TARN_RAZORLOR,
            Quest.FAMILY_PEST,
            Quest.MAGE_ARENA_II,
            Quest.IN_SEARCH_OF_KNOWLEDGE,
            Quest.DADDYS_HOME,
            Quest.HOPESPEARS_WILL,
            Quest.THE_FROZEN_DOOR,
            Quest.INTO_THE_TOMBS));

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

        myPlugin.mySources.Quests.SetProgress(IdBuilder.QUEST_POINTS, myPlugin.myClient.getVarpValue(VarPlayer.QUEST_POINTS));

    }
}
