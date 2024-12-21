package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;
import net.runelite.client.ui.FontManager;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

public class GoalBuilder extends JPanel
{
    TodoPlugin myPlugin;

    @Nullable
    String myId;
    Consumer<Goal> myConsumer;
    JPanel myPreviewPanel;
    JButton myAcceptButton;
    int myTarget;

    public GoalBuilder(TodoPlugin aPlugin, Consumer<Goal> aConsumer, Runnable aOnCancel)
    {
        myPlugin = aPlugin;
        myConsumer = aConsumer;
        myTarget = 1;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new LineBorder(Color.gray, 2));

        JLabel title = new JLabel("New Goal");
        title.setFont(FontManager.getRunescapeFont().deriveFont(20.f));
        title.setBackground(Color.red);
        add(title);

        add(new IdBuilder(aPlugin, (id) -> {
            myId = id;
            RefreshPreview();
        }));

        add(new JSeparator());


        JPanel targetPanel = new JPanel();
        targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.X_AXIS));
        {
            targetPanel.add(new JLabel("Target:"));
            JTextField targetInput = new JTextField("1");

            targetInput.setBorder(new EmptyBorder(0,4,0,1));

            targetInput.addActionListener(e -> ParseTarget(targetInput.getText()));
            targetPanel.add(targetInput);
        }
        add(targetPanel);


        myPreviewPanel = new JPanel();
        myPreviewPanel.setLayout(new BorderLayout());
        myPreviewPanel.setMinimumSize(new Dimension(230, 18));
        myPreviewPanel.setPreferredSize(new Dimension(230, 18));
        myPreviewPanel.setMaximumSize(new Dimension(230, 18));

        add(myPreviewPanel);

        JPanel terminationButtons = new JPanel();
        terminationButtons.setLayout(new BoxLayout(terminationButtons, BoxLayout.X_AXIS));

        {
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener((e) -> aOnCancel.run());
            terminationButtons.add(cancel);

            myAcceptButton = new JButton("Accept");
            myAcceptButton.setEnabled(false);
            myAcceptButton.addActionListener(e -> aConsumer.accept(new Goal(myPlugin, myId, myTarget, true, null)));
            terminationButtons.add(myAcceptButton);

        }

        add(terminationButtons);
    }

    private void RefreshPreview()
    {
        if (myId == null || myTarget == -1)
        {
            myPreviewPanel.removeAll();
            myAcceptButton.setEnabled(false);
            return;
        }

        ResourceView view = new ResourceView(myPlugin, myId, (float)myTarget);

        view.Await().WhenDone((sender) ->
            SwingUtilities.invokeLater(() ->
            {
                myPreviewPanel.removeAll();
                myPreviewPanel.add(view);
                myAcceptButton.setEnabled(true);
                revalidate();
                repaint();
            }));
    }

    private void ParseTarget(String aTarget)
    {
        try
        {
            myTarget = Integer.parseInt(aTarget);
        }
        catch (NumberFormatException e)
        {
            myTarget = -1;
        }

        RefreshPreview();
    }
}
