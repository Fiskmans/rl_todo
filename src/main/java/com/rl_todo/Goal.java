package com.rl_todo;

import com.rl_todo.ui.GoalPopup;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class GoalMouseListener extends MouseAdapter
{
    private TodoPlugin myPlugin;
    private Goal myOwner;
    public GoalMouseListener(TodoPlugin aPlugin, Goal aOwner)
    {
        myPlugin = aPlugin;
        myOwner = aOwner;
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.isPopupTrigger())
            doPop(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
            doPop(e);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        myOwner.SetHovered(true);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        myOwner.SetHovered(false);
    }

    private void doPop(MouseEvent e)
    {
        GoalPopup menu = new GoalPopup(myPlugin, myOwner);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}

public class Goal extends JComponent implements ProgressTracker
{
    private Goal myParent = null;
    private Recipe myRecipe = null;
    public boolean IsRoot() { return myParent == null; }
    public boolean HasRecipe() { return myRecipe != null; }
    private boolean myIsDone = false;

    private List<Goal> myChildren = new ArrayList<>();

    protected BufferedImage myIcon;
    private boolean myIsHovered = false;
    public void SetHovered(boolean aState)
    {
        if (myIsHovered != aState)
            repaint();

        myIsHovered = aState;
    }

    protected int myTarget = 1;
    protected int myProgress = 0;
    protected int myBanked = 0;
    public int GetTarget() { return  myTarget; }
    public int GetProgress() { return myProgress; }
    public int GetBanked() { return myBanked; }

    protected int myDepth = 0;

    protected TodoPlugin myPlugin;

    private String myId;
    private String myPrettyId;
    public String GetId() { return myId; };

    private GoalCollection myOwner;

    public void SetOwner(GoalCollection aCollection)
    {
        myOwner = aCollection;
    }

    @Override
    public String toString() {
        return myPrettyId + ": " + myProgress + "/" + myTarget;
    }

    private Goal(Goal aParent, TodoPlugin aPlugin, String aId, int aCount)
    {
        myPlugin = aPlugin;
        myId = aId;
        myPrettyId = aId;
        myTarget = aCount;
        myParent = aParent;

        myDepth = myParent.myDepth + 1;

        Setup();
    }

    public Goal(TodoPlugin aPlugin, String aId, int aCount)
    {
        myPlugin = aPlugin;
        myId = aId;
        myPrettyId = aId;
        myTarget = aCount;

        Setup();
    }

    private void Setup()
    {

        Dimension dim = new Dimension(0, myPlugin.myConfig.rowHeight());
        setMinimumSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);

        addMouseListener(new GoalMouseListener(myPlugin, this));

        {
            int index =  myId.indexOf('.');
            if (index == -1)
            {
                try
                {
                    int itemId = Integer.parseInt(myId);
                    myPlugin.myClientThread.invokeLater(()->
                    {
                        AsyncBufferedImage icon = myPlugin.myItemManager.getImage(itemId);
                        myIcon = icon;
                        icon.onLoaded(() -> { repaint(); });
                        myPrettyId = myPlugin.myItemManager.getItemComposition(itemId).getMembersName();
                        repaint();
                    });
                }
                catch (final NumberFormatException e)
                {
                    myIcon = ImageUtil.loadImageResource(TodoPlugin.class, "/Icon_16x16.png");
                }
            }
            else
            {
                String type = myId.substring(0, index);
                String part = myId.substring(index + 1);
                switch (type)
                {
                    case "quest":
                        for(Quest q : Quest.values())
                        {
                            if (Integer.toString(q.getId()).equals(part))
                            {
                                myPrettyId = q.getName();
                            }
                        }
                        //TODO set icon to quest symbol
                        break;
                    case "xp":
                        myPrettyId = part.substring(0,1).toUpperCase() + part.substring(1) + " xp";

                        myIcon = ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + part.toLowerCase() + ".png");
                        break;
                    case "level":
                        myPrettyId = "level " + myTarget + " " + part.substring(0,1).toUpperCase() + part.substring(1);
                        myIcon = ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + part.toLowerCase() + ".png");
                        break;
                }
            }
        }

        AutoSelectRecipe();
        RecalculateBanked();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (myIsHovered)
        {
            g.setColor(new Color(50, 50, 51));
        }
        else
        {
            g.setColor(new Color(43, 43, 44));
        }
        g.fillRect(0,0,getWidth(),getHeight());


        int iconStart = myDepth * myPlugin.myConfig.indent();

        if (myIcon != null)
            g.drawImage(myIcon, iconStart,0,myPlugin.myConfig.rowHeight(),myPlugin.myConfig.rowHeight(), null);

        float progress = (float)GetProgress() / GetTarget();
        float banked = (float)Math.min(GetProgress() + GetBanked(), GetTarget()) / GetTarget();

        int barStart = iconStart + myPlugin.myConfig.rowHeight();

        int barWidth = getWidth() - barStart;

        int progressWidth = (int)(barWidth * progress);
        int bankedWidth = (int)(barWidth * banked);

        g.setColor(myPlugin.myConfig.completedColor());
        g.fillRect(barStart,0,progressWidth,getHeight());

        g.setColor(myPlugin.myConfig.bankedColor());
        g.fillRect(barStart + progressWidth,0,bankedWidth - progressWidth,getHeight());

        DrawText(g, myPrettyId, barStart + 3, getHeight() - 3, false);


        if (myTarget != 1)
        {
            String s = DisplayNum(myProgress) + "/" + DisplayNum(myTarget);

            if (myIsDone)
                s = "Done";

            DrawText(g, s, getWidth(), getHeight() - 3, true);
        }

        g.setColor(Color.DARK_GRAY);
        g.drawLine(iconStart, getHeight() - 1, getWidth(), getHeight() - 1);
    }

    private String DisplayNum(int aNumber)
    {
        int num = aNumber;
        if (myId.startsWith("xp."))
            num /= 100;

        if (num > 1000000)
            return String.format("%.2fm", (float)num / 1000000);

        if (num > 1000)
            return String.format("%.2fk", (float)num / 1000);

        return Integer.toString(num);
    }

    private void DrawText(Graphics g, String aText, int aX, int aY, boolean aRightAlign)
    {

        char[] chars = new char[aText.length()];
        aText.getChars(0,aText.length(), chars, 0);

        int x = aX;
        if (aRightAlign)
        {
            FontMetrics m = g.getFontMetrics();
            x -= m.charsWidth(chars,0, chars.length);
        }

        g.setColor(myPlugin.myConfig.textOutlineColor());
        g.drawChars(chars, 0, chars.length, x + 1, aY );
        g.drawChars(chars, 0, chars.length, x , aY + 1);
        g.drawChars(chars, 0, chars.length, x - 1, aY);
        g.drawChars(chars, 0, chars.length, x , aY - 1);
        g.setColor(myPlugin.myConfig.textColor());
        g.drawChars(chars, 0, chars.length, x, aY);

    }

    public List<Recipe> GetRecipeCandidates()
    {
        List<Recipe> blacklist = new ArrayList<>();

        String path = myId;

        Goal at = myParent;
        while(at != null)
        {
            blacklist.add(at.myRecipe);
            path = at.myId + ">" + path;
            at = at.myParent;
        }

        List<Recipe> candidates = myPlugin.myRecipeManager.GetAvailableRecipes(myId);

        return candidates.stream().filter((Recipe aRecipe) -> !blacklist.contains(aRecipe)).collect(Collectors.toList());
    }

    void onAdded()
    {
        for (Goal child : myChildren)
        {
            myOwner.AddSubGoal(this, child);
        }
        myProgress = myPlugin.myProgressManager.AddTracker(this, myId);
        repaint();
    }

    void onRemoved()
    {
        for (Goal child : myChildren)
        {
            myOwner.RemoveGoal(child);
        }
        myPlugin.myProgressManager.RemoveTracker(this, myId);
    }

    void AddChild(Goal aGoal)
    {
        myChildren.add(aGoal);
        if (myOwner != null)
        {
            myPlugin.myPanel.GetGoals().AddSubGoal(this, aGoal);
        }
    }

    @Override
    public void CountUpdated(String aId, int aCount)
    {
        if (aId.equals(myId))
        {
            int clamped = Math.min(aCount, myTarget);
            if (clamped != myProgress)
            {
                if(!myIsDone && clamped == myTarget)
                {
                    myIsDone = true;
                    if (myPlugin.myConfig.messageOnCompletion())
                    {
                        myPlugin.myClient.addChatMessage(ChatMessageType.GAMEMESSAGE, "Todo", "You completed a goal!", "Todo");
                    }
                }


                myProgress = clamped;
                repaint();
                TodoPlugin.debug("Progress on " + toString());

                RecalculateParents();
            }
        }
    }

    private String GetPath()
    {
        String path = myId;

        Goal at = myParent;
        while(at != null)
        {
            path = at.myId + ">" + path;
            at = at.myParent;
        }
        return path;
    }

    private void AutoSelectRecipe()
    {
        Recipe recipe = myPlugin.myRecipeManager.GetRecipe(GetPath());

        if (recipe != null)
            SetRecipe(recipe, false);
    }

    public void SetRecipe(Recipe aRecipe, boolean aShouldAddToConfig)
    {
        for (Goal child : myChildren)
            myOwner.RemoveGoal(child);

        myChildren.clear();
        if (aRecipe != null)
        {
            boolean produces = false;
            for(Resource r : aRecipe.myProducts)
                if (r.myId.equals(myId))
                    produces = true;

            if (!produces)
            {
                TodoPlugin.debug("The selected recipe does not produce this");
                return;
            }
        }

        if (aShouldAddToConfig)
        {
            List<String> rows = new ArrayList<>(Arrays.asList(myPlugin.myConfig.getRecipes().split("\n")));

            if (myRecipe != null)
            {
                String oldValue = GetPath() + ":" + myRecipe.myName;
                int index = rows.indexOf(oldValue);
                if (index != -1)
                    rows.remove(index);
            }

            if (aRecipe != null)
            {
                String value = GetPath() + ":" + aRecipe.myName;
                TodoConfig.SmartInsert(rows,value);
            }
            myPlugin.myConfig.setRecipes(String.join("\n", rows));
        }

        myRecipe = aRecipe;

        if (aRecipe == null)
            return;

        List<Resource> resources = aRecipe.Calculate(myPlugin, myId, myTarget);
        Collections.reverse(resources);
        for(Resource resource : resources)
        {
            if (myPlugin.myProgressManager.GetProgress(resource.myId) >= resource.myAmount)
                continue;

            AddChild(new Goal(this, myPlugin, resource.myId, resource.myAmount));
        }

        RecalculateBanked();
    }

    public void RecalculateBanked()
    {
        int banked = 0;
        if (myRecipe == null)
        {
            banked = 0;
        }
        else
        {
            int possible = myTarget - myProgress;
            List<Resource> resources = new ArrayList<>();
            for(Goal child : myChildren)
                resources.add(new Resource(child.myId, child.myBanked));

            banked = myRecipe.CalculateAvailable(myPlugin, resources, myId, possible);
        }

        if (myBanked != banked)
        {
            myBanked = banked;
            repaint();
        }
    }

    public void RecalculateParents()
    {
        if (myParent != null)
        {
            myParent.RecalculateBanked();
            myParent.RecalculateParents();
        }
    }
}
