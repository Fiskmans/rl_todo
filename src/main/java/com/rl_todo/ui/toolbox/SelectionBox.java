package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.util.function.Consumer;

public class SelectionBox extends JPanel
{
    Consumer<JComponent> myConsumer;
    SelectionBox(Consumer<JComponent> aConsumer)
    {
        myConsumer = aConsumer;
    }


}
