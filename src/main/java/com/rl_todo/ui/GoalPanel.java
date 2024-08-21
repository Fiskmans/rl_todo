package com.rl_todo.ui;

import com.rl_todo.GoalCollection;
import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.SpriteID.*;

public class GoalPanel extends JPanel
{
    public GoalCollection myGoals;
    TodoPlugin myPlugin;

    public GoalPanel(TodoPlugin aPlugin)
    {
        super();
        myPlugin = aPlugin;
        myGoals = new GoalCollection(myPlugin);

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;


        JButton addGoal = new JButton("Add Goal");
        addGoal.addActionListener(e -> {
            TodoPlugin.debug("Switched to add goal panel", 3);
            myPlugin.myPanel.SetContent(new AddGoalPanel(myPlugin));
        });

        add(addGoal, constraints);

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


        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 2;
        add(statusPanel, constraints);


        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        constraints.gridy = 3;
        add(new JScrollPane(myGoals, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), constraints);
    }

}
