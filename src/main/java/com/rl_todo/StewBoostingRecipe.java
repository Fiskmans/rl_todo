package com.rl_todo;

import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StewBoostingRecipe extends Recipe
{
    Skill mySkill;
    public StewBoostingRecipe(Skill aSkill)
    {
        super("Stew boost " + aSkill.getName(), Arrays.asList(IdBuilder.levelResource(aSkill, 1)), new ArrayList<Resource>(), new ArrayList<Resource>());

        mySkill = aSkill;
    }

    @Override
    public List<Resource> Calculate(TodoPlugin aPlugin, String aId, int aTarget)
    {
        assert aId.equals(IdBuilder.levelId(mySkill));

        return Arrays.asList(
                IdBuilder.levelResource(mySkill, Math.max(aTarget - 5, 1)),
                IdBuilder.itemResource(ItemID.SPICY_STEW, 1),
                IdBuilder.questResource(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE));
    }

    @Override
    public int CalculateAvailable(TodoPlugin aPlugin, List<Resource> aAvailableIngredients, String aId, int aMaximum)
    {
        assert aId.equals(IdBuilder.levelId(mySkill));

        if (aPlugin.myProgressManager.GetProgress(IdBuilder.questId(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE)) == 0)
            return 0;

        return 5;
    }
}
