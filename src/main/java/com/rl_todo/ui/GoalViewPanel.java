package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.SelectionBox;
import com.rl_todo.ui.toolbox.Stretchable;
import net.runelite.api.ItemID;
import net.runelite.client.util.SwingUtil;

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

        SelectionBox statusPanel = new SelectionBox(this::OnStatusSelectionChanged);
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
                statusPanel.AddSelectable(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Inventory,  inventoryIcon, "Inventory"));
                statusPanel.AddSelectable(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Bank, bankIcon, "Bank"));
                statusPanel.AddSelectable(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Levels, levelsIcon, "Levels"));
                statusPanel.AddSelectable(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.SeedVault, seedVaultIcon, "Seed vault"));
                statusPanel.AddSelectable(new ProgressSourceStatusIcon(myPlugin, myPlugin.mySources.Quests, questIcon, "Quests"));

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

    private void OnStatusSelectionChanged(JComponent aStatusSelection)
    {
        assert aStatusSelection instanceof ProgressSourceStatusIcon;

        ProgressSourceStatusIcon prog = (ProgressSourceStatusIcon)aStatusSelection;

        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.PAGE_AXIS));

        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            SetView(myGoals);
            prog.SetSelected(false);
        });

        ResourcePoolView resourceView = new ResourcePoolView(myPlugin, prog.mySource.All(), prog.myName);

        JPanel alignementFix = new JPanel();
        alignementFix.add(back, BorderLayout.WEST);

        view.add(alignementFix);
        view.add(new JSeparator());

        JPanel alignmentFix2 = new JPanel();
        alignmentFix2.add(resourceView);

        view.add(alignmentFix2);

        resourceView.Await().WhenDone((sender) -> SwingUtilities.invokeLater(() -> SetView(view)));
    }

    private void SetView(JComponent aComponent)
    {
        myViewed.remove(myGoals); // preserve this component from the devastation that is fastRemoveAll
        SwingUtil.fastRemoveAll(myViewed);
        myViewed.add(aComponent, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

}
