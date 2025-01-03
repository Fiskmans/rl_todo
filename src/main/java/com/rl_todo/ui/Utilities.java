package com.rl_todo.ui;

import com.rl_todo.Alternative;
import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemComposition;
import net.runelite.api.Quest;
import net.runelite.api.SpriteID;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class Utilities {

    TodoPlugin myPlugin;
    BufferedImage myErrorImage;

    public Utilities(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
        myErrorImage = ImageUtil.loadImageResource(TodoPlugin.class, "/Icon_16x16.png");
    }

    public Optional<String> PrettifyID(String aRawId)
    {
        int index =  aRawId.indexOf('.');
        if (index == -1)
            return Optional.empty();

        String type = aRawId.substring(0, index);
        String part = aRawId.substring(index + 1);
        switch (type)
        {
            case "item":
                try
                {
                    int itemId = Integer.parseInt(part);

                    ItemComposition itemComposition = myPlugin.myItemManager.getItemComposition(itemId);
                    return Optional.of(itemComposition.getMembersName());
                }
                catch (final NumberFormatException ignored)
                {
                    return Optional.empty();
                }
            case "quest":
                return Optional.of("Completion of " + TranslateQuestIdToName(aRawId));
            case "xp":
                return Optional.of(part.substring(0,1).toUpperCase() + part.substring(1).toLowerCase() + " xp");
            case "level":
                return Optional.of(part.substring(0,1).toUpperCase() + part.substring(1).toLowerCase() + " level");
            case "minigame":
            {
                switch (part)
                {
                    case "nmz_points":
                        return Optional.of("NMZ points");
                }
            }

            case "any":
                return Alternative
                        .FromID(part)
                        .map(Alternative::getName);
            case "progression":
                switch (part)
                {
                    case "quest_points":
                        return  Optional.of("Quest Point");
                }

            default:
                return Optional.empty();
        }
    }

    public Optional<String> PrettifyIDCompact(String aRawId)
    {
        int index =  aRawId.indexOf('.');
        if (index == -1)
            return Optional.empty();

        String type = aRawId.substring(0, index);
        String part = aRawId.substring(index + 1);
        switch (type)
        {
            case "item":
                try
                {
                    int itemId = Integer.parseInt(part);

                    ItemComposition itemComposition = myPlugin.myItemManager.getItemComposition(itemId);
                    return Optional.of(itemComposition.getMembersName());
                }
                catch (final NumberFormatException ignored)
                {
                    return Optional.empty();
                }
            case "quest":
                return Optional.of(TranslateQuestIdToName(aRawId));
            case "xp":
                return Optional.of("xp");
            case "level":
                return Optional.of("");
            case "minigame":
            {
                switch (part)
                {
                    case "nmz_points":
                        return Optional.of("NMZ points");
                }
            }

            case "any":
                return Alternative
                        .FromID(part)
                        .map(Alternative::getName);

            case "progression":
                switch (part)
                {
                    case "quest_points":
                        return  Optional.of("Quest Point");
                }
            default:
                return Optional.empty();
        }
    }

    private String TranslateQuestIdToName(String aId)
    {
        for (Quest quest : Quest.values())
        {
            if (IdBuilder.questId(quest).equals(aId))
                return quest.getName();
        }

        return aId.substring(6);
    }

    public static String PrettyNumber(int aNumber)
    {
        if (aNumber > 1000000)
            return String.format("%.2fm", (float) aNumber / 1000000);

        if (aNumber > 1000)
            return String.format("%.2fk", (float) aNumber / 1000);

        return Integer.toString(aNumber);
    }

    public static String PrettyNumber(float aNumber)
    {
        if (aNumber > 1000000)
            return String.format("%.2fm", aNumber / 1000000);

        if (aNumber > 1000)
            return String.format("%.2fk", aNumber / 1000);

        if (aNumber == (long)aNumber)
            return String.format("%d", (long) aNumber);

        return Float.toString(aNumber);
    }

    public Optional<String> ProgressText(int aProgress, int aTarget)
    {
        if (aProgress >= aTarget)
            return Optional.of("Done");

        if (aTarget <= 1)
            return Optional.empty();

        return Optional.of(Utilities.PrettyNumber(aProgress) + "/" + Utilities.PrettyNumber(aTarget));
    }

    public BufferedImage IconFromID(String aRawId, int aStackSize)
    {
        int index =  aRawId.indexOf('.');
        if (index == -1)
            return myErrorImage;

        String type = aRawId.substring(0, index);
        String part = aRawId.substring(index + 1);
        switch (type)
        {
            case "item":
                try
                {
                    int itemId = Integer.parseInt(part);

                    ItemComposition itemComposition = myPlugin.myItemManager.getItemComposition(itemId);
                    return myPlugin.myItemManager.getImage(itemId, aStackSize, itemComposition.isStackable());
                }
                catch (final NumberFormatException ignored)
                {
                    return myErrorImage;
                }
            case "quest":
                return myPlugin.mySpriteManager.getSprite(SpriteID.TAB_QUESTS, 0);
            case "xp":
            case "level":
                return ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + part.toLowerCase() + ".png");
            case "minigame":
                return  myPlugin.mySpriteManager.getSprite(SpriteID.TAB_QUESTS_RED_MINIGAMES, 0);

            case "any":
                return Alternative
                        .FromID(part)
                        .map((a) -> IconFromID("item." + a.getAlternatives()[0], aStackSize))
                        .orElse(myErrorImage);
            case "progression":
                switch (part)
                {
                    case "quest_points":
                        return myPlugin.mySpriteManager.getSprite(SpriteID.TAB_QUESTS, 0);
                }
            default:
                return myErrorImage;
        }
    }

    static public Color BlendColors(Color aLeft, Color aRight)
    {
        return BlendColors(aLeft, aRight, 0.5f);
    }

    static public Color BlendColors(Color aLeft, Color aRight, Float aFraction)
    {
        return new Color(
                (aLeft.getRed() * (1.f - aFraction) + aRight.getRed() * aFraction) / 255.f,
                (aLeft.getGreen() * (1.f - aFraction) + aRight.getGreen() * aFraction) / 255.f,
                (aLeft.getBlue() * (1.f - aFraction) + aRight.getBlue() * aFraction) / 255.f);
    }
}
