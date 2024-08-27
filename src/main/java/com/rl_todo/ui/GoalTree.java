package com.rl_todo.ui;

import com.rl_todo.GoalSubscriber;
import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.TreeNode;
import com.rl_todo.ui.toolbox.TreeNodeItem;

import javax.swing.*;
import java.awt.*;

public class GoalTree extends TreeNode implements GoalSubscriber, TreeNodeItem
{
    TodoPlugin myPlugin;

    public GoalTree(TodoPlugin aPlugin, Goal aGoal)
    {
        super(new GoalUI(aPlugin, aGoal));

        myPlugin = aPlugin;

        aGoal.AddSubscriber(this);
    }

    public GoalUI GetGoalUI()
    {
        return (GoalUI)myContent;
    }

    @Override
    public void OnSubGoalAdded(Goal aSubGoal) {
        AddNode(new GoalTree(myPlugin, aSubGoal));
    }

    @Override
    public void OnTargetChanged() {

    }

    @Override
    public void OnBankedChanged() {

    }

    @Override
    public void OnProgressChanged() {

    }

    @Override
    public void OnMethodChanged() {

    }

    @Override
    public void OnCompleted() {

    }

    @Override
    public float GetAnchorDepth() {
        return 10;
    }
}
