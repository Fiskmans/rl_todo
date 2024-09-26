package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;
import com.rl_todo.ui.toolbox.TreeNodeItem;

public class SelectableMethod extends Selectable implements TreeNodeItem {

    public SelectableMethod(TodoPlugin aPlugin, Method aMethod, Runnable aOnSelect) {
        super(aPlugin, aMethod.GetName(), aOnSelect);
    }

    @Override
    public int GetAnchorDepth() {
        return 8;
    }
}
