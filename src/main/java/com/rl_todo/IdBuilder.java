package com.rl_todo;

import net.runelite.api.Item;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class IdBuilder
{
    public static boolean isValid(String aId)
    {
        if (aId.isEmpty())
            return false;

        return aId.indexOf('.') != -1;
    }

    public static Resource xpResource(Skill aSkill, double aAmount)
    {
        return new Resource(
          xpId(aSkill),
                (float)aAmount);
    }

    public static Resource levelResource(Skill aSkill, int aAmount)
    {
        return new Resource(
                levelId(aSkill),
                aAmount);
    }

    public static Resource itemResource(Item aItem, double aAmount)
    {
        return new Resource(
                itemId(aItem),
                (float)aAmount);
    }
    public static Resource itemResource(int aItem, double aAmount)
    {
        return new Resource(
                itemId(aItem),
                (float)aAmount);
    }

    public static String alternativeId(Alternative aAlternative)
    {
        return "any." + aAlternative.getId();
    }

    public static Resource alternativeResource(Alternative aAlternative, int aAmount)
    {
        return new Resource(
                alternativeId(aAlternative),
                aAmount);
    }

    public static Resource questResource(Quest aQuest)
    {
        return new Resource(
                questId(aQuest),
                1);
    }

    public static Resource NMZPoints(int aAmount)
    {
        return new Resource(
                "nmz.points",
                aAmount);
    }


    public static String xpId(Skill aSkill)
    {
        return "xp." + aSkill.getName();
    }

    public static String levelId(Skill aSkill)
    {
        return "level." + aSkill.getName();
    }

    public static String itemId(Item aItem)
    {
        return itemId(aItem.getId());
    }

    public static String itemId(int aItem)
    {
        return "item." + aItem;
    }

    public static String questId(Quest aQuest)
    {
        return "quest." + aQuest.getId();
    }
}
