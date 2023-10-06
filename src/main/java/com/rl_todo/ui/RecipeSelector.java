package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.Recipe;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class RecipeSelector extends JPopupMenu
{
    Goal myTarget;
    private JTextField mySearch = new JTextField();
    private List<Recipe> myOptions;
    private List<JMenuItem> myItems = new ArrayList<>();
    private JPanel myRecipes = new JPanel();

    private JButton myPrev;
    private JButton myNext;

    private static final int PAGE_SIZE = 15;
    private int myPage = 0;

    RecipeSelector(Goal aTarget)
    {
        myTarget = aTarget;

        myPrev = new JButton(new AbstractAction("Prev") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(myPage > 0)
                {
                    myPage--;
                    Refresh();
                }
            }
        });

        myNext = new JButton(new AbstractAction("Next") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(myPage * PAGE_SIZE + 1 < myOptions.size())
                {
                    myPage++;
                    Refresh();
                }
            }
        });

        setBorder(new LineBorder(Color.gray, 1));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTH;

        add(new JLabel("Select a recipe") , c);

        c.gridy++;
        add(mySearch, c);

        mySearch.setSize(220, 18);

        c.gridy++;
        c.gridwidth = 1;
        add(myPrev, c);

        c.gridx++;
        add(myNext, c);

        myOptions = myTarget.GetRecipeCandidates();

        setSize(230, 400);

        Refresh();
    }

    private void Refresh()
    {
        for(JMenuItem item : myItems)
            remove(item);

        myItems.clear();

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;

        for(int i = PAGE_SIZE * myPage; i < myOptions.size() && i < PAGE_SIZE * (myPage + 1); i++)
        {
            Recipe recipe = myOptions.get(i);
            JMenuItem item = new JMenuItem(new AbstractAction(recipe.myName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    myTarget.SetRecipe(recipe, true);
                }
            });

            add(item, c);
            myItems.add(item);

            c.gridy++;
        }

        revalidate();
        repaint();
    }


}
