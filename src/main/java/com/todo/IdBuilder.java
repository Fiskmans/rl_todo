package com.todo;

import net.runelite.api.Item;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class IdBuilder
{
    public static boolean isValid(String aId)
    {
        if (aId.isEmpty())
            return false;

        int dotIndex = aId.indexOf('.');
        if (dotIndex == -1)
        {
            try
            {
                Integer.parseInt(aId);
                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }

        return true;
    }

    public static Resource xpResource(Skill aSkill, int aAmount)
    {
        return new Resource(
          xpId(aSkill),
          aAmount);
    }

    public static Resource levelResource(Skill aSkill, int aAmount)
    {
        return new Resource(
                levelId(aSkill),
                aAmount);
    }

    public static Resource itemResource(Item aItem, int aAmount)
    {
        return new Resource(
                itemId(aItem),
                aAmount);
    }
    public static Resource itemResource(int aItem, int aAmount)
    {
        return new Resource(
                itemId(aItem),
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
        return Integer.toString(aItem.getId());
    }

    public static String itemId(int aItem)
    {
        return Integer.toString(aItem);
    }
    public static String questId(Quest aQuest)
    {
        return "quest." + aQuest.getId();
    }
}
