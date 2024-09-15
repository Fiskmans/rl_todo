package com.rl_todo.methods;

import com.rl_todo.*;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import java.util.Arrays;
import java.util.List;

public class StewBoostingMethod extends Method
{
    Skill mySkill;
    public StewBoostingMethod(Skill aSkill)
    {
        super("Stew boost " + aSkill.getName(), "boost/stew");

        myMakes.Add(IdBuilder.levelResource(aSkill, 1));

        myTakes.Add(IdBuilder.itemResource(ItemID.SPICY_STEW, 1));

        myRequires.Add(IdBuilder.levelResource(aSkill, 1));
        myRequires.Add(IdBuilder.questResource(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE));

        mySkill = aSkill;
    }

    @Override
    public ResourcePool CalculateNeeded(TodoPlugin aPlugin, String aId, int aTarget)
    {
        assert aId.equals(IdBuilder.levelId(mySkill));

        ResourcePool out = new ResourcePool();

        out.Add(IdBuilder.levelId(mySkill), Math.max(aTarget - 5, 1));
        out.Add(IdBuilder.itemId(ItemID.SPICY_STEW), 1);
        out.Add(IdBuilder.questId(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE), 1);

        return out;
    }

    @Override
    public ResourcePool CalculateAvailable(ProgressManager aProgressManager, String aId, float aWanted)
    {
        assert aId.equals(IdBuilder.levelId(mySkill));

        ResourcePool out = new ResourcePool();

        if (aProgressManager.GetProgress(IdBuilder.questId(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE)) > 0)
            out.Add(IdBuilder.levelId(mySkill), Math.min(aWanted, 5));

        return out;
    }
}
