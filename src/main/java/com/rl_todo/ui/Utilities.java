package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemComposition;
import net.runelite.api.Quest;

import java.util.Optional;

public class Utilities {

    TodoPlugin myPlugin;

    public Utilities(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
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
                for(Quest q : Quest.values())
                {
                    if (Integer.toString(q.getId()).equals(part))
                    {
                        return Optional.of(q.getName());
                    }
                }
                return Optional.empty();
            case "xp":
                return Optional.of(part.substring(0,1).toUpperCase() + part.substring(1).toLowerCase() + " xp");
            case "level":
                return Optional.of(part.substring(0,1).toUpperCase() + part.substring(1).toLowerCase() + " level");
            case "nmz":
                return Optional.of("NMZ points");

            default:
                return Optional.empty();
        }
    }

    public static String PrettyNumber(int aNumber)
    {
        if (aNumber > 1000000)
            return String.format("%.2fm", (float) aNumber / 1000000);

        if (aNumber > 1000)
            return String.format("%.2fk", (float) aNumber / 1000);

        return Integer.toString(aNumber);
    }

    public Optional<String> ProgressText(int aProgress, int aTarget)
    {
        if (aProgress >= aTarget)
            return Optional.of("Done");

        if (aTarget <= 1)
            return Optional.empty();

        return Optional.of(Utilities.PrettyNumber(aProgress) + "/" + Utilities.PrettyNumber(aTarget));
    }
}
