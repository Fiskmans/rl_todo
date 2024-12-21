package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.*;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuestIdBuilder extends JPanel
{

    TodoPlugin myPlugin;
    Consumer<String> myConsumer;

    public QuestIdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {
        myPlugin = aPlugin;
        myConsumer = aConsumer;

        setMinimumSize(new Dimension(220, 500));
        setPreferredSize(new Dimension(220, 500));
        setMaximumSize(new Dimension(220, 500));

        setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));

        Category notStarted = new Category(myPlugin, "Not started");
        Category inProgress = new Category(myPlugin, "In progress");
        Category completed = new Category(myPlugin, "Completed");

        innerPanel.add(notStarted);
        innerPanel.add(inProgress);
        innerPanel.add(completed);

        add(new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) , BorderLayout.CENTER);

        myPlugin.myClientThread.invokeLater(() ->
        {
            List<Quest> notStartedQuests = new ArrayList<>();
            List<Quest> inProgressQuests = new ArrayList<>();
            List<Quest> completedQuests = new ArrayList<>();

            for (Quest quest : Quest.values())
            {
                switch(quest.getState(myPlugin.myClient))
                {
                    case NOT_STARTED:
                        notStartedQuests.add(quest);
                        break;
                    case IN_PROGRESS:
                        inProgressQuests.add(quest);
                        break;
                    case FINISHED:
                        completedQuests.add(quest);
                        break;

                }
            }

            SwingUtilities.invokeLater(() ->
            {
                for (Quest quest : notStartedQuests)
                    notStarted.AddNode(new ClickableText(myPlugin, quest.getName(), (text) -> myConsumer.accept(IdBuilder.questId(quest)), false), false);
                for (Quest quest : inProgressQuests)
                    inProgress.AddNode(new ClickableText(myPlugin, quest.getName(), (text) -> myConsumer.accept(IdBuilder.questId(quest)), false), false);
                for (Quest quest : completedQuests)
                    completed.AddNode(new ClickableText(myPlugin, quest.getName(), (text) -> myConsumer.accept(IdBuilder.questId(quest)), false), false);

                revalidate();
                repaint();
            });
        });
    }
}
