package com.rl_todo.ui;

import com.rl_todo.methods.Method;
import com.rl_todo.ui.toolbox.TreeNodeItem;

public class SelectableMethod extends Selectable implements TreeNodeItem {

    public SelectableMethod(Method aMethod, Runnable aOnSelect) {
        super(aMethod.GetName(), aOnSelect);
    }

    @Override
    public int GetAnchorDepth() {
        return 8;
    }
}
