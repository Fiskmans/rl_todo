package com.rl_todo.ui;

import com.rl_todo.Alternative;
import com.rl_todo.Resource;
import com.rl_todo.TodoPlugin;
import net.runelite.api.Item;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import javax.swing.*;
import java.util.function.Consumer;

public class IdBuilder extends JPanel
{
    public static Resource xpResource(Skill aSkill, double aAmount) { return new Resource(xpId(aSkill), (float)aAmount); }
    public static Resource levelResource(Skill aSkill, int aAmount) { return new Resource(levelId(aSkill), aAmount); }
    public static Resource itemResource(Item aItem, double aAmount) { return new Resource(itemId(aItem), (float)aAmount); }
    public static Resource itemResource(int aItem, double aAmount) { return new Resource(itemId(aItem), (float)aAmount); }
    public static Resource alternativeResource(Alternative aAlternative, int aAmount) { return new Resource(alternativeId(aAlternative), aAmount); }
    public static Resource questResource(Quest aQuest) { return new Resource(questId(aQuest), 1); }
    public static Resource NMZPointsResource(int aAmount) { return new Resource("nmz.points", aAmount); }

    public static String alternativeId(Alternative aAlternative)
    {
        return "any." + aAlternative.getId();
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

    Consumer<String> myConsumer;



    IdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {

    }


}
