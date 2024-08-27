package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class TreeBranch extends JPanel {

    GridBagLayout myLayout = new GridBagLayout();
    JPanel myNodePanel = new JPanel();

    static int Indent = 20;

    TreeBranch()
    {
        setLayout(myLayout);

        GridBagConstraints strutContraints = new GridBagConstraints();

        strutContraints.gridx = 0;
        strutContraints.gridy = 0;
        strutContraints.weightx = 0;
        strutContraints.weighty = 0;
        strutContraints.anchor = GridBagConstraints.NORTHWEST;

        add(Box.createHorizontalStrut(Indent), strutContraints);

        GridBagConstraints panelConstraints = new GridBagConstraints();

        panelConstraints.gridx = 1;
        panelConstraints.gridy = 0;
        panelConstraints.weightx = 1;
        panelConstraints.weighty = 0;
        panelConstraints.anchor = GridBagConstraints.NORTHWEST;

        myNodePanel.setLayout(new BoxLayout(myNodePanel, BoxLayout.PAGE_AXIS));

        add(myNodePanel, panelConstraints);

        setBackground(Color.lightGray);
    }

    public <T extends JComponent & TreeNodeItem> void AddNode(T aNode)
    {
        myNodePanel.add(aNode);
    }

    public <T extends JComponent & TreeNodeItem> void RemoveNode(T aNode)
    {
        myNodePanel.remove(aNode);
    }

    Stream<TreeNodeItem> Nodes()
    {
        return Arrays.stream(myNodePanel.getComponents())
                .map((child) -> { return (TreeNodeItem)child; });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // TODO: Paint branches

    }
}
