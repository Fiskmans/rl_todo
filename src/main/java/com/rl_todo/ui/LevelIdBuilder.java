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

public class LevelIdBuilder extends SelectionBox
{
    TodoPlugin myPlugin;
    Consumer<String> myConsumer;

    public LevelIdBuilder(TodoPlugin aPlugin, Consumer<String> aConsumer)
    {
        super((selected) -> aConsumer.accept(selected.getName()));
        myPlugin = aPlugin;
        myConsumer = aConsumer;

        setLayout(new GridLayout(0, 8));

        Dimension iconSize = new Dimension(20,28);

        setMinimumSize(iconSize);
        setPreferredSize(new Dimension(iconSize.width * 8, iconSize.height * 3));
        setMaximumSize(new Dimension(iconSize.width * 8, iconSize.height * 5));

        for (Skill skill : Skill.values())
        {
            SelectableImage image = new SelectableImage(
                ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + skill.getName().toLowerCase() + ".png"),
                iconSize);

            image.setName(IdBuilder.levelId(skill));

            AddSelectable(image);
        }
    }
}
