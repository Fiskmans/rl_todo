package com.rl_todo;

import com.rl_todo.ui.IdBuilder;
import net.runelite.api.*;

public class ProgressSources
{
    public ProgressSource Debug = new ProgressSource("Debug");
    public ProgressSource Inventory = new ProgressSource("Inventory");
    public ProgressSource Bank = new ProgressSource("Bank");
    public ProgressSource SeedVault = new ProgressSource("Seed vault");

    public ProgressSource Levels = new ProgressSource("Levels");
    public ProgressSource Xp = new ProgressSource("Xp");

    public ProgressSource Quests = new ProgressSource("Quests");

    public ProgressSource Generic = new ProgressSource("Generic");

    void RefreshNMZ(TodoPlugin aPlugin)
    {
        int totalPoints = aPlugin.myClient.getVarbitValue(Varbits.NMZ_POINTS)
                + aPlugin.myClient.getVarpValue(VarPlayer.NMZ_REWARD_POINTS);

        Generic.SetProgress("minigame.nmz_points", totalPoints);
    }

    void RefreshLevels(TodoPlugin aPlugin)
    {
        for (Skill skill : Skill.values())
        {
            Levels.SetProgress(IdBuilder.levelId(skill), aPlugin.myClient.getRealSkillLevel(skill));
            Xp.SetProgress(IdBuilder.xpId(skill), aPlugin.myClient.getSkillExperience(skill));
        }
    }

}
