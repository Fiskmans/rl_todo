package com.rl_todo.ui.toolbox;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class SelectionBox extends JPanel
{
    Consumer<JComponent> myOnSelectionChanged;

    @Nullable
    //Implements Selectable
    JComponent mySelected;

    SelectionBox(Consumer<JComponent> aOnSelectionChanged)
    {
        myOnSelectionChanged = aOnSelectionChanged;
        mySelected = null;
    }

    public <T extends JComponent & Selectable> void Add(T aComponent, Object aConstraints)
    {
        add(new Clickable<>(aComponent, this::Select), aConstraints);
    }

    public Optional<JComponent> GetSelection()
    {
        if (mySelected == null)
            return Optional.empty();

        return Optional.of(mySelected);
    }

    public <T extends JComponent & Selectable> void Select(T aComponent)
    {
        //assert Arrays.stream(getComponents()).anyMatch((c) -> c.myInner == aComponent);

        if (mySelected != null)
            ((Selectable)mySelected).SetSelected(false);

        aComponent.SetSelected(true);
        mySelected = aComponent;
    }

}
