package com.rl_todo.ui;

import com.rl_todo.GoalSubscriber;
import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.ClickDecorator;
import com.rl_todo.ui.toolbox.TreeNode;
import com.rl_todo.ui.toolbox.TreeNodeItem;

import javax.swing.*;
import java.awt.*;

public class GoalTree extends TreeNode implements GoalSubscriber, TreeNodeItem
{
    TodoPlugin myPlugin;
    Runnable myOnModified;

    public GoalTree(TodoPlugin aPlugin, Goal aGoal, Runnable aOnModified)
    {
        super(null);

        SetContent(new ClickDecorator<>(new GoalUI(aPlugin, aGoal), (sender) -> Toggle()));

        myPlugin = aPlugin;
        myOnModified = aOnModified;

        for (Goal subGoal : aGoal.GetChildren())
            OnSubGoalAdded(subGoal);

        aGoal.AddSubscriber(this);
    }

    public GoalUI GetGoalUI()
    {
        return (GoalUI)myContent;
    }

    @Override
    public void OnSubGoalAdded(Goal aSubGoal) {
        AddNode(new GoalTree(myPlugin, aSubGoal, myOnModified), true);
        myOnModified.run();
    }

    @Override
    public void OnSubGoalsCleared() {
        RemoveAllNodes();
        myOnModified.run();
    }

    @Override
    public void OnTargetChanged() {
        myOnModified.run();
    }

    @Override
    public void OnBankedChanged() {

    }

    @Override
    public void OnProgressChanged() {

    }

    @Override
    public void OnMethodChanged() {
        myOnModified.run();
    }

    @Override
    public void OnCompleted() {

    }

    @Override
    public int GetAnchorDepth() {
        return 10;
    }
}
