package com.rl_todo.ui.toolbox;

public interface TreeNodeItem
{
    int GetAnchorDepth();
    default boolean IsBefore(TreeNodeItem aOther) { return false; }
}
