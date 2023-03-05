package com.todo.ui;

import com.todo.Goal;
import com.todo.TodoPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoalPopup extends JPopupMenu
{
    private TodoPlugin myPlugin;
    private Goal myGoal;

    private JMenuItem mySetup;
    private JMenuItem myUnset;
    private JMenuItem myDelete;

    public GoalPopup(TodoPlugin aPlugin,Goal aGoal)
    {
        myPlugin = aPlugin;
        myGoal = aGoal;

        mySetup = new JMenuItem(new AbstractAction("Setup") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Setup();
            }
        });

        myUnset = new JMenuItem(new AbstractAction("Unset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Unset();
            }
        });

        myDelete  = new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Delete();
            }
        });

        if (myGoal.GetRecipeCandidates().size() != 0)
        {
            add(mySetup);
        }
        else
        {
            add(new JLabel("No known ways to acquire"));
        }

        if (myGoal.HasRecipe())
            add(myUnset);

        if (myGoal.IsRoot())
            add(myDelete);
    }

    void Setup()
    {
        RecipeSelector menu = new RecipeSelector(myGoal);
        menu.show(myGoal, 0,myGoal.getHeight());
    }

    void Delete()
    {
        myPlugin.myClientThread.invokeLater(() ->
        {
            {
                List<String> rows = new ArrayList<>(Arrays.asList(myPlugin.myConfig.getGoals().split("\n")));
                if (rows.remove(myGoal.GetId()))
                    myPlugin.myConfig.setGoals(String.join("\n", rows));
            }

            {
                List<String> rows = new ArrayList<>(Arrays.asList(myPlugin.myConfig.getRecipes().split("\n")));
                List<String> filtered = rows.stream().filter((String row) -> !row.startsWith(myGoal.GetId())).collect(Collectors.toList());

                if (rows.size() != filtered.size())
                    myPlugin.myConfig.setRecipes(String.join("\n", filtered));
            }
        });


        myPlugin.myPanel.GetGoals().RemoveGoal(myGoal);
    }

    void Unset()
    {
        myGoal.SetRecipe(null, true);
    }
}
