package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.SelectableImage;
import com.rl_todo.ui.toolbox.SelectionBox;
import net.runelite.api.Skill;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class LevelIdBuilder extends JPanel
{
    TodoPlugin myPlugin;
    Consumer<String> myConsumer;

    SelectionBox myInner;

    public LevelIdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {
        myPlugin = aPlugin;
        myConsumer = aConsumer;
        myInner = new SelectionBox((selected) -> aConsumer.accept(selected.getName()));
        myInner.setLayout(new GridLayout(0, 8));

        setLayout(new FlowLayout());
        add(myInner);

        Dimension iconSize = new Dimension(25,28);

        for (Skill skill : Skill.values())
        {
            SelectableImage image = new SelectableImage(
                ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + skill.getName().toLowerCase() + ".png"),
                iconSize);

            image.setName(IdBuilder.levelId(skill));

            myInner.AddSelectable(image);
        }
    }
}
