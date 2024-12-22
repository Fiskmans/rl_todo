package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class GoalPopup extends JPopupMenu
{
    private TodoPlugin myPlugin;
    private Goal myGoal;
    private List<Method> myAvailableMethods;

    public GoalPopup(TodoPlugin aPlugin, Goal aGoal)
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

        JLabel prettyName = new JLabel(myGoal.GetId());

        add(prettyName);

        myPlugin.myClientThread.invokeLater(() -> myPlugin.myUtilities.PrettifyID(myGoal.GetId()).ifPresent(prettyName::setText));

        if (myPlugin.myConfig.debug() > 1)
            add(new JLabel("Raw id: " + myGoal.GetId()));

       myPlugin.myUtilities.ProgressText(myGoal.GetProgress(), myGoal.GetTarget())
               .ifPresent((string) -> add(new JLabel(string)));

        add(new JLabel("Method: " + myGoal.GetMethodName().orElse("<unset>")));

        add(new JSeparator());

        if (myGoal.HasMethod())
            add(unsetMethod);

        add(setupMethod);

        if (myGoal.CanSetTarget())
            add(setAmount);

        add(deleteGoal);
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

                        myGoal.SetTarget(val);
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
        myPlugin.myPanel.SetContent(
                new JScrollPane(
                        new MethodSelectorPanel(myPlugin, myGoal.GetId())
                                .OnSelect((method) ->  {
                                    myGoal.SetMethod(method);
                                    myPlugin.myPanel.ResetContent();
                                }),
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    void Delete()
    {
        myPlugin.myClientThread.invokeLater(() -> myGoal.Remove());
    }

    void Unset()
    {
        myGoal.UnsetMethod();
    }
}
