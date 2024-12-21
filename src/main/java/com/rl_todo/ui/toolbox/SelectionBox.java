package com.rl_todo.ui.toolbox;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Optional;
import java.util.function.Consumer;

public class SelectionBox extends JPanel
{
    Consumer<JComponent> myOnSelectionChanged;

    @Nullable
    //Implements Selectable
    JComponent mySelected;

    public SelectionBox(Consumer<JComponent> aOnSelectionChanged)
    {
        myOnSelectionChanged = aOnSelectionChanged;
        mySelected = null;
    }

    public <T extends JComponent & Selectable> void AddSelectable(T aComponent, Object aConstraints)
    {
        add(new ClickDecorator<>(aComponent, this::Select), aConstraints);
    }

    public <T extends JComponent & Selectable> void AddSelectable(T aComponent)
    {
        add(new ClickDecorator<>(aComponent, this::Select));
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

        myOnSelectionChanged.accept(mySelected);
    }

}
