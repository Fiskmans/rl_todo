package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.IdType;
import com.rl_todo.TodoPlugin;
import org.apache.commons.lang3.text.translate.CodePointTranslator;

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
            Selectable selectable = new Selectable(myPlugin, type.GetName(), null);

            selectable.SetOnSelection(() ->
            {
                myType = type.GetName();
                for(Component comp : typePanel.getComponents())
                {
                    comp.setBackground(new Color(20,20,20));
                }

                selectable.setBackground(new Color(40,40,40));
            });

            typePanel.add(selectable);
        }

        GridBagConstraints typeSelectorconstraints = new GridBagConstraints();
        typeSelectorconstraints.gridx = 0;
        typeSelectorconstraints.gridy = 0;

        add(typePanel, typeSelectorconstraints);
    }
}
