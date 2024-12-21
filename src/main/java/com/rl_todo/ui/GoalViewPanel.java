package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.Stretchable;
import net.runelite.api.ItemID;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static net.runelite.api.SpriteID.*;

public class GoalViewPanel extends JPanel
{
    public GoalCollectionPanel myGoals;
    TodoPlugin myPlugin;
    JPanel myViewed;

    public GoalViewPanel(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
        myGoals = new GoalCollectionPanel(myPlugin);

        myViewed = new JPanel();
        myViewed.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JButton addGoalButton = new JButton("Add Goal");
        addGoalButton.addActionListener(e -> SetView(new GoalBuilder(myPlugin, (goal) -> { myGoals.AddGoal(goal); SetView(myGoals); }, () -> SetView(myGoals))));

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
        add(new JSeparator());

        {
            JPanel window = new JPanel();
            window.setLayout(new BorderLayout());
            window.add(new JScrollPane(myViewed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
            add(window);
        }

        SetView(myGoals);
    }

    private void SetView(JComponent aComponent)
    {
        myViewed.removeAll();
        myViewed.add(aComponent, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

}
