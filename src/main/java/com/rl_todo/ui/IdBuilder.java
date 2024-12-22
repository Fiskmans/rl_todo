package com.rl_todo.ui;

import com.rl_todo.Alternative;
import com.rl_todo.Resource;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.SelectableImage;
import com.rl_todo.ui.toolbox.SelectionBox;
import com.rl_todo.ui.toolbox.Stretchable;
import com.rl_todo.ui.toolbox.WrappingText;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import static net.runelite.api.SpriteID.*;
import static net.runelite.api.SpriteID.TAB_QUESTS;

public class IdBuilder extends JPanel
{
    public static final String QUEST_POINTS = "progression.quest_points";

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
        return "quest." + SafeName(aQuest.getName());
    }

    private static String SafeName(String aRawName)
    {
        return aRawName
                .replace(" ", "_")
                .replace("(", "")
                .replace(")", "")
                .replace("'", "")
                .replace("/", "-")
                .replace("_-_", "-")
                .replace(".","")
                .replace("&", "and")
                .replace("#", "__")
                .replace("?", "_")
                .toLowerCase();
    }


    TodoPlugin myPlugin;
    Consumer<String> myConsumer;

    JPanel myTypeBuilderPanel;

    SelectableImage myItemTab;
    SelectableImage myLevelTab;
    SelectableImage myQuestTab;

    IdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {
        myPlugin = aPlugin;
        myConsumer = aConsumer;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        {
            JPanel builderPanel = new JPanel();
            builderPanel.setLayout(new BoxLayout(builderPanel, BoxLayout.PAGE_AXIS));

            SelectionBox typeSelector = new SelectionBox(this::TabChanged);
            typeSelector.setLayout(new BoxLayout(typeSelector, BoxLayout.X_AXIS));
            builderPanel.add(typeSelector);

            JLabel typeLabel = new JLabel("Type:");
            typeLabel.setFont(FontManager.getDefaultFont().deriveFont(24.f));

            typeSelector.add(typeLabel);

            myPlugin.myClientThread.invokeLater(() ->
            {
                BufferedImage inventoryIcon = myPlugin.mySpriteManager.getSprite(TAB_INVENTORY, 0);
                BufferedImage questIcon = myPlugin.mySpriteManager.getSprite(TAB_QUESTS, 0);
                BufferedImage levelsIcon = myPlugin.mySpriteManager.getSprite(TAB_STATS, 0);

                myItemTab = new SelectableImage(inventoryIcon, new Dimension(30, 30));
                myLevelTab = new SelectableImage(levelsIcon, new Dimension(30, 30));
                myQuestTab = new SelectableImage(questIcon, new Dimension(30, 30));

                myItemTab.setToolTipText("Item");
                myLevelTab.setToolTipText("Level");
                myQuestTab.setToolTipText("Quest");

                SwingUtilities.invokeLater(() ->
                {
                    typeSelector.AddSelectable(myItemTab);
                    typeSelector.AddSelectable(myLevelTab);
                    typeSelector.AddSelectable(myQuestTab);
                    repaint();
                    revalidate();
                });
            });

            myTypeBuilderPanel = new JPanel();
            myTypeBuilderPanel.setLayout(new BorderLayout());
            builderPanel.add(myTypeBuilderPanel);

            add(builderPanel);
        }
    }

    public void TabChanged(JComponent aComponent)
    {
        if (aComponent == myItemTab)
        {
            SetType(new ItemIdBuilder(myPlugin, myConsumer));
            return;
        }
        if (aComponent == myLevelTab)
        {
            SetType(new LevelIdBuilder(myPlugin, myConsumer));
            return;
        }
        if (aComponent == myQuestTab)
        {
            SetType(new QuestIdBuilder(myPlugin, myConsumer));
            return;
        }
    }

    private void SetType(JComponent aTypeBuilder)
    {
        myTypeBuilderPanel.removeAll();
        myTypeBuilderPanel.add(aTypeBuilder, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
