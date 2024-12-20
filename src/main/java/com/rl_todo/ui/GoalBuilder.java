package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;

import javax.annotation.Nullable;
import javax.swing.*;

public class GoalBuilder extends JPanel
{
    TodoPlugin myPlugin;

    @Nullable
    String myId;

    public GoalBuilder(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new IdBuilder(aPlugin, (id) -> myId = id));

    }
}
