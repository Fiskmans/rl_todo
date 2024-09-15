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
import java.util.*;
import java.util.List;

public class MethodSelectorPanel extends JPanel
{
    TodoPlugin myPlugin;
    Goal myTarget;

    HashMap<String, MethodCategory> myRootCategories = new HashMap<>();

    MethodSelectorPanel(TodoPlugin aPlugin, Goal aTarget)
    {
        myPlugin = aPlugin;
        myTarget = aTarget;

        if (aTarget != null)
        {
            myPlugin.myMethodManager.GetAvailableMethods(myTarget.GetId()).forEach(this::AddOption);
        }
        else
        {
            myPlugin.myMethodManager.GetAllMethods().forEach(this::AddOption);
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new CompoundBorder(new LineBorder(Color.gray, 1), new EmptyBorder(2,2,2,2)));
        setPreferredSize(new Dimension(220,500));
    }

    void AddOption(Method aMethod)
    {
        String[] parts = aMethod.myCategory.split("[/\\\\]");

        if (parts.length == 0)
        {
            //TODO: put them in the root? 'uncategorised'? idk, figure it out
            return;
        }


        MethodCategory at = null;

        for (String key : parts)
        {
            if (at == null)
            {
                if (!myRootCategories.containsKey(parts[0]))
                {
                    at = new MethodCategory(parts[0]);
                    myRootCategories.put(parts[0], at);

                    at.setAlignmentX(LEFT_ALIGNMENT);
                    at.setAlignmentY(TOP_ALIGNMENT);
                    add(at);
                }
                else
                {
                    at = myRootCategories.get(key);
                }
                continue;
            }

            at = at.GetOrCreateChild(key);
        }

        at.AddNode(new SelectableMethod(aMethod, () ->
        {
            if (myTarget != null)
                myTarget.SetMethod(aMethod);
        }));
    }
}
