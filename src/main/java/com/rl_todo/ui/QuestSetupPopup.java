package com.rl_todo.ui;

import com.rl_todo.*;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.FlatTextField;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class QuestSetupPopup extends JPopupMenu
{
    TodoPlugin myPlugin;

    private JCheckBox myCompleted = new JCheckBox();
    private JCheckBox myStarted = new JCheckBox();
    private JCheckBox myMiniquests = new JCheckBox();

    JPanel myQuestsPanel = new JPanel();

    QuestSetupPopup(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(220,500));
        setBorder(new CompoundBorder(new LineBorder(Color.gray, 1), new EmptyBorder(2,2,2,2)));
        setFocusable(true);
        setRequestFocusEnabled(true);

        myCompleted.setSelected(true);
        myCompleted.addItemListener(e -> Refresh());

        myStarted.setSelected(true);
        myStarted.addItemListener(e -> Refresh());

        myMiniquests.setSelected(true);
        myMiniquests.addItemListener(e -> Refresh());

        Setup();
        Refresh();
    }

    private void Setup()
    {

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        add(new JLabel("Select a quest") , c);

        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        add(new JLabel("Done: "), c);

        c.gridx++;
        c.weightx = 1;
        add(myCompleted, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        add(new JLabel("Started: "), c);

        c.gridx++;
        c.weightx = 1;
        add(myStarted, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        add(new JLabel("Miniquests: "), c);

        c.gridx++;
        c.weightx = 1;
        add(myMiniquests, c);

        myQuestsPanel.setLayout(new GridBagLayout());
        myQuestsPanel.setBorder(new LineBorder(Color.darkGray));
        myQuestsPanel.setBackground(getBackground());

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        JScrollPane scrollPane = new JScrollPane(myQuestsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
        add(scrollPane, c);
    }

    private void Refresh()
    {
        class ListedQuest
        {
            public Quest myGameQuest;
            public QuestState myState;

            public ListedQuest(Quest aQuest, QuestState aState)
            {
                myGameQuest = aQuest;
                myState = aState;
            }
        }

        List<ListedQuest> filteredQuests = new ArrayList<>();

        boolean completed = myCompleted.isSelected();
        boolean started = myStarted.isSelected();
        boolean miniQuests = myMiniquests.isSelected();

        myPlugin.myClientThread.invokeLater(() ->
        {
            for(Quest quest : Quest.values()) {
                QuestState state = quest.getState(myPlugin.myClient);

                if (state == QuestState.FINISHED && !completed)
                    continue;

                if (state == QuestState.IN_PROGRESS && !started)
                    continue;

                if (Quests.MiniQuests.contains(quest) && !miniQuests)
                    continue;

                filteredQuests.add(new ListedQuest(quest, state));
            }

            SwingUtilities.invokeLater(() ->
            {
                myQuestsPanel.removeAll();

                GridBagConstraints questPanelConstraints = new GridBagConstraints();
                questPanelConstraints.weightx = 1;
                questPanelConstraints.gridy = 0;
                questPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
                questPanelConstraints.anchor = GridBagConstraints.WEST;

                for(ListedQuest quest : filteredQuests)
                {
                    String name = "";
                    if (Quests.MiniQuests.contains(quest.myGameQuest))
                        name += "MQ: ";

                    name += quest.myGameQuest.getName();


                    Selectable selectable = new Selectable(myPlugin, name, () -> {
                        new Goal(myPlugin, IdBuilder.questId(quest.myGameQuest), 1, true, null);
                        myPlugin.myPanel.ResetContent();
                    });

                    switch (quest.myState)
                    {
                        case NOT_STARTED:
                            selectable.setForeground(Color.red);
                            break;
                        case IN_PROGRESS:
                            selectable.setForeground(Color.yellow);
                            break;
                        case FINISHED:
                            selectable.setForeground(Color.green);
                            break;
                    }

                    myQuestsPanel.add(selectable, questPanelConstraints);
                    questPanelConstraints.gridy++;
                }

                revalidate();
                repaint();
            });
        });
    }


}
