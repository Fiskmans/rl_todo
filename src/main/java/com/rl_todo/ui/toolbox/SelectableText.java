package com.rl_todo.ui.toolbox;

import com.rl_todo.TodoPlugin;

import java.awt.*;

public class SelectableText extends ClickableText implements Selectable {

    boolean myIsSelected = false;
    Runnable myOnSelected;

    public SelectableText(TodoPlugin aPlugin, String aText, Runnable aOnSelect) {
        super(aPlugin, aText, null, false);
        myOnClick = (text) -> this.SetSelected(!myIsSelected);
    }

    @Override
    public void SetSelected(boolean aSelected) {
        if (myIsSelected)
        {
            setBackground(new Color(20,20,20));
            return;
        }

        setBackground(new Color(255,0,0));

        myOnSelected.run();
    }
}
