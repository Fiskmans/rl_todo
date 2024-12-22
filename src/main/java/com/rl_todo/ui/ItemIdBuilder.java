package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.EventListener;
import java.util.function.Consumer;

public class ItemIdBuilder extends JPanel
{
    TodoPlugin myPlugin;
    Consumer<String> myConsumer;

    public ItemIdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {
        myPlugin = aPlugin;
        myConsumer = aConsumer;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JButton inGameSearch = new JButton("Use in game search");
        inGameSearch.addActionListener(e -> myPlugin.myChatboxItemSearch
                .tooltipText("Select Item")
                .onItemSelected((aItemId) -> myConsumer.accept(IdBuilder.itemId(aItemId)))
                .build());

        inGameSearch.setAlignmentX(0.0f);

        add(inGameSearch);
    }
}
