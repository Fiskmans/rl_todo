package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GoalPopup extends JPopupMenu
{
    private TodoPlugin myPlugin;
    private Goal myGoal;

    public GoalPopup(TodoPlugin aPlugin,Goal aGoal)
    {
        myPlugin = aPlugin;
        myGoal = aGoal;

        JMenuItem setAmount = new JMenuItem(new AbstractAction("Set amount") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SetAmount();
            }
        });

        JMenuItem setupMethod = new JMenuItem(new AbstractAction("Select method") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Setup();
            }
        });

        JMenuItem setupMultipleMethod = new JMenuItem(new AbstractAction("Select multiple methods") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SetupMultiple();
            }
        });

        JMenuItem unsetMethod = new JMenuItem(new AbstractAction("Unset method") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Unset();
            }
        });

        JMenuItem deleteGoal  = new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Delete();
            }
        });

        add(new JLabel(myGoal.GetPrettyId()));

        if (myPlugin.myConfig.debug() > 1)
            add(new JLabel("Raw id: " + myGoal.GetId()));

        String progress = myGoal.GetProgressText();
        if (!progress.equals(""))
            add(new JLabel(progress));

        add(new JLabel("Method: " + myGoal.GetMethodName()));

        add(new JSeparator());

        if (myGoal.HasMethod())
            add(unsetMethod);

        switch(myGoal.GetMethodCandidates().size())
        {
            default:
                add(setupMultipleMethod);
            case 1:
                add(setupMethod);
                break;
            case 0:
                add(new JLabel("Select method: No known ways to acquire"));
                break;
        }

        if (myGoal.IsRoot())
        {
            if (myGoal.MaxTarget() > 1)
            {
                add(setAmount);
            }

            add(deleteGoal);
        }
    }

    void SetAmount()
    {
        myPlugin.myChatboxTextInput
            .prompt("Set amount")
            .charValidator(null)
            .value(Integer.toString(myGoal.GetTarget()))
            .onDone((String aValue) ->
                {
                    try
                    {
                        int val = Integer.parseInt(aValue);
                        int max = myGoal.MaxTarget();

                        if (val > max)
                        {
                            TodoPlugin.IgnorableError(aValue + " is larger than the maximum allowed value of " + max);
                            return;
                        }

                        myGoal.SetTarget(val, true);
                    }
                    catch (Exception e)
                    {
                        TodoPlugin.IgnorableError("Amount needs to be a number: " + aValue);
                    }
                })
            .build();
    }

    void Setup()
    {
        TodoPlugin.debug("Switched to method selector for " + myGoal, 3);
        myPlugin.myPanel.SetContent(new MethodSelector(myPlugin, myGoal));
    }

    void SetupMultiple()
    {
        TodoPlugin.debug("Switched to multiple method selector for " + myGoal, 3);
        myPlugin.myPanel.SetContent(new MethodMultiSelector(myPlugin, myGoal));
    }

    void Delete()
    {
        myPlugin.myClientThread.invokeLater(() ->
        {
            myPlugin.myPanel.GetGoals().RemoveGoal(myGoal);
            myPlugin.myPanel.GetGoals().SaveConfig();
        });
    }

    void Unset()
    {
        myGoal.UnsetMethods(true);
    }
}
