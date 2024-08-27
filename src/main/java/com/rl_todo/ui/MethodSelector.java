package com.rl_todo.ui;

import com.rl_todo.*;
import com.rl_todo.methods.Method;
import joptsimple.util.KeyValuePair;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodSelector extends JPanel
{
    TodoPlugin myPlugin;
    Goal myTarget;
    List<Method> myOptions;
    JPanel myMethodPanel = new JPanel();

    MethodSelector(TodoPlugin aPlugin, Goal aTarget, List<Method> aOptions)
    {
        myPlugin = aPlugin;
        myTarget = aTarget;
        myOptions = aOptions;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(220,500));
        setBorder(new CompoundBorder(new LineBorder(Color.gray, 1), new EmptyBorder(2,2,2,2)));

        Setup();
        Refresh();
    }

    private void Setup()
    {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        add(new JLabel("Select a method") , c);

        myMethodPanel.setLayout(new GridBagLayout());
        myMethodPanel.setBorder(new LineBorder(Color.darkGray));
        myMethodPanel.setBackground(getBackground());

        final JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(myMethodPanel, BorderLayout.NORTH);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        add(new JScrollPane(northPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), c);

    }

    private void Refresh()
    {
        myMethodPanel.removeAll();

        GridBagConstraints methodConstraints = new GridBagConstraints();
        methodConstraints.weightx = 1;
        methodConstraints.gridy = 0;
        methodConstraints.fill = GridBagConstraints.HORIZONTAL;
        methodConstraints.anchor = GridBagConstraints.WEST;

        Add(new Tree(myOptions), methodConstraints, 0);


        revalidate();
        repaint();
    }

    private void Add(Tree aTree, GridBagConstraints aConstraints, int aDepth)
    {
        for(Method method : aTree.myMethods)
        {
            Selectable selectable = new Selectable(method.myName, () -> {
                myTarget.SetMethod(method);
                myPlugin.myPanel.ResetContent();
            });

            myMethodPanel.add(selectable, aConstraints);
            aConstraints.gridy++;
        }

        for (Map.Entry<String, Tree> tree : aTree.myChildNodes.entrySet())
        {
            myMethodPanel.add(new JLabel(tree.getKey()), aConstraints);
            aConstraints.gridy++;

            Add(tree.getValue(), aConstraints, aDepth + 1);
        }
    }

    // TODO: migrate this to custom tree implementation
    static class Tree
    {
        Map<String, Tree> myChildNodes = new HashMap<>();
        List<Method> myMethods = new ArrayList<>();

        Tree()
        {
        }

        Tree(List<Method> aMethods)
        {
            for(Method method : aMethods)
            {
                String[] parts = method.myCategory.split("/");
                Tree at = this;

                for (String p : parts)
                {
                    at.myChildNodes.putIfAbsent(p, new Tree());
                    at = at.myChildNodes.get(p);
                }

                at.myMethods.add(method);
            }
        }

    }
}
