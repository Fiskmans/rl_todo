package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.SpriteID.*;

public class GoalViewPanel extends JPanel
{
    public GoalCollectionPanel myGoals;
    TodoPlugin myPlugin;

    public GoalViewPanel(TodoPlugin aPlugin)
    {
        super();
        myPlugin = aPlugin;
        myGoals = new GoalCollectionPanel(myPlugin);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JButton addGoalButton = new JButton("Add Goal");
        addGoalButton.addActionListener(e -> {
            TodoPlugin.debug("Switched to add goal panel", 3);
            myPlugin.myPanel.SetContent(new AddGoalPanel(myPlugin, this));
        });

        addGoalButton.setAlignmentX(0.0f);

        buttonPanel.add(addGoalButton);

        JButton allMethods = new JButton("View all methods");
        allMethods.addActionListener(e -> {
            TodoPlugin.debug("Switched to add view methods", 3);
            myPlugin.myPanel.SetContent(new MethodSelectorPanel(myPlugin, null));
        });

        allMethods.setAlignmentX(0.0f);

        buttonPanel.add(allMethods);

        add(buttonPanel);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));

        myPlugin.myClientThread.invokeLater(() ->
        {
            BufferedImage inventoryIcon = myPlugin.mySpriteManager.getSprite(TAB_INVENTORY, 0);
            BufferedImage bankIcon = myPlugin.mySpriteManager.getSprite(MAP_ICON_BANK, 0);
            BufferedImage levelsIcon = myPlugin.mySpriteManager.getSprite(TAB_STATS, 0);
            BufferedImage seedVaultIcon = myPlugin.myItemManager.getImage(ItemID.SEED_BOX);
            BufferedImage questIcon = myPlugin.mySpriteManager.getSprite(TAB_QUESTS, 0);

            SwingUtilities.invokeLater(() ->
            {
                statusPanel.add(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Inventory,  inventoryIcon));
                statusPanel.add(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Bank, bankIcon));
                statusPanel.add(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Levels, levelsIcon));
                statusPanel.add(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.SeedVault, seedVaultIcon));
                statusPanel.add(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Quests, questIcon));

                revalidate();
                repaint();
            });
        });

        add(statusPanel);

        add(new JScrollPane(myGoals, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

}
