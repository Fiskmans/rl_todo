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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodSelectorPanel extends JPanel
{
    TodoPlugin myPlugin;
    JPanel myInnerPanel = new JPanel();
    MethodViewer myViewer;
    boolean myIsPinned = false;


    Consumer<Method> myOnSelected;

    HashMap<String, MethodCategory> myRootCategories = new HashMap<>();

    MethodSelectorPanel(TodoPlugin aPlugin, String aFilterByProduct)
    {
        myPlugin = aPlugin;

        if (aFilterByProduct != null)
        {
            myPlugin.myMethodManager.GetAvailableMethods(aFilterByProduct).forEach(this::AddOption);
        }
        else
        {
            myPlugin.myMethodManager.GetAllMethods().forEach(this::AddOption);
        }

        setLayout(new BorderLayout());

        myViewer = new MethodViewer(myPlugin);

        myViewer.setMinimumSize(new Dimension(230, 20));
        myViewer.setPreferredSize(new Dimension(230, 250));
        myViewer.setMaximumSize(new Dimension(230, 10000));

        JScrollPane previewPane = new JScrollPane(myViewer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        previewPane.setMinimumSize(new Dimension(230,250));
        previewPane.setPreferredSize(new Dimension(230,250));
        previewPane.setMaximumSize(new Dimension(230,250));
        add(previewPane, BorderLayout.NORTH);

        myInnerPanel.setLayout(new BoxLayout(myInnerPanel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(myInnerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentY(TOP_ALIGNMENT);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        setBackground(Color.RED);

        setMinimumSize(new Dimension(220, 40));
        setPreferredSize(new Dimension(220, 40));
        setMaximumSize(new Dimension(220, 4000));

        add(scrollPane, BorderLayout.CENTER);
    }

    public MethodSelectorPanel OnSelect(Consumer<Method> aOnSelect)
    {
        myOnSelected = aOnSelect;
        return this;
    }

    void AddOption(Method aMethod)
    {
        List<String> parts = Arrays.stream(aMethod.myCategory.split("[/\\\\]")).filter((s) -> !s.equals("")).collect(Collectors.toList());

        if (parts.isEmpty())
        {
            //TODO: put them in the root? 'uncategorised'? idk, figure it out
            return;
        }

        SwingUtilities.invokeLater(() ->
        {
            MethodCategory at = null;

            for (String key : parts)
            {
                if (at == null)
                {
                    if (!myRootCategories.containsKey(key))
                    {
                        at = new MethodCategory(myPlugin, key);
                        myRootCategories.put(key, at);

                        at.setAlignmentX(LEFT_ALIGNMENT);
                        at.setAlignmentY(TOP_ALIGNMENT);
                        myInnerPanel.add(at);
                    }
                    else
                    {
                        at = myRootCategories.get(key);
                    }
                    continue;
                }

                at = at.GetOrCreateChild(key);
            }

            SelectableMethod selector = new SelectableMethod(myPlugin, aMethod, () ->
            {
                if (myOnSelected != null)
                {
                    myOnSelected.accept(aMethod);
                }

                myViewer.SetMethod(aMethod);
                myIsPinned = true;
            });


            selector.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {

                    if (!myIsPinned)
                        myViewer.SetMethod(aMethod);
                }

                @Override
                public void mouseExited(MouseEvent e) {}
            });

            at.AddNode(selector, false);

            repaint();
        });
    }
}
