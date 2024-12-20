package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.IdType;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.SelectableText;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AddGoalPopup extends JPopupMenu {

    TodoPlugin myPlugin;
    String myType = "";
    Consumer<Goal> myConsumer;

    AddGoalPopup(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        BuildTypeSelector();
    }

    AddGoalPopup OnDone(Consumer<Goal> aConsumer)
    {
        myConsumer = aConsumer;
        return this;
    }

    void BuildTypeSelector()
    {
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.PAGE_AXIS));

        for(IdType type : IdType.values())
        {
            SelectableText selectableText = new SelectableText(myPlugin, type.GetName(), null);

            selectableText.SetOnSelection(() ->
            {
                myType = type.GetName();
                for(Component comp : typePanel.getComponents())
                {
                    comp.setBackground(new Color(20,20,20));
                }

                selectableText.setBackground(new Color(255,0,0));
            });

            typePanel.add(selectableText);
        }

        GridBagConstraints typeSelectorconstraints = new GridBagConstraints();
        typeSelectorconstraints.gridx = 0;
        typeSelectorconstraints.gridy = 0;

        add(typePanel, typeSelectorconstraints);
    }
}
