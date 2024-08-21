package com.rl_todo.methods;

import com.rl_todo.Alternative;
import com.rl_todo.IdBuilder;
import com.rl_todo.Resource;
import com.rl_todo.TodoPlugin;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import java.util.ArrayList;
import java.util.List;


public class Method
{
    public String myName;
    public String myCategory;
    public List<Resource> myTakes = new ArrayList<>();
    public List<Resource> myMakes = new ArrayList<>();
    public List<Resource> myRequires = new ArrayList<>();

    Method(String aName, String aCategory)
    {
        myName = aName;
        myCategory = aCategory;
    }

    public Method takes(int aItem)
    {
        myTakes.add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method takes(int aItem, double aAmount)
    {
        myTakes.add(IdBuilder.itemResource(aItem, aAmount));
        return this;
    }

    public Method takes(Resource aCustom)
    {
        myTakes.add(aCustom);
        return this;
    }

    public Method takes(Alternative aAlternative, int aAmount)
    {
        myTakes.add(IdBuilder.alternativeResource(aAlternative, aAmount));
        return this;
    }

    public Method makes(Quest aQuest)
    {
        myMakes.add(IdBuilder.questResource(aQuest));
        return this;
    }
    public Method makes(int aItem)
    {
        myMakes.add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method makes(int aItem, float aAmount)
    {
        myMakes.add(IdBuilder.itemResource(aItem, aAmount));
        return this;
    }

    public Method makes(int aItem, double aAmount)
    {
        myMakes.add(IdBuilder.itemResource(aItem, (float)aAmount));
        return this;
    }

    public Method makes(Skill aSkill, double aXpAmount)
    {
        myMakes.add(IdBuilder.xpResource(aSkill, aXpAmount));
        return this;
    }

    public Method requires(int aItem)
    {
        myRequires.add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method requires(Alternative aAlternative)
    {
        myRequires.add(IdBuilder.alternativeResource(aAlternative, 1));
        return this;
    }

    public Method requires(Quest aSkill)
    {
        myRequires.add(IdBuilder.questResource(aSkill));
        return this;
    }

    public Method requires(Skill aSkill, int aLevel)
    {
        myRequires.add(IdBuilder.levelResource(aSkill, aLevel));
        return this;
    }

    public Method build()
    {
        Validate();
        return this;
    }

    private void Validate()
    {
        for (Resource r : myMakes) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [makes] " + r.myId + " has amount zero");
        }

        for (Resource r : myTakes) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [takes] " + r.myId + " has amount zero");
        }

        for (Resource r : myRequires) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [requires] " + r.myId + " has amount zero");


            for (Resource r2 : myTakes) {
                if (r.myId.equals(r2.myId))
                    TodoPlugin.IgnorableError(myName + ": [takes & requires] " + r.myId);
            }
        }
    }

    public List<Resource> Calculate(TodoPlugin aPlugin, String aId, int aTarget)
    {
        List<Resource> out = new ArrayList<>();

        int has = aPlugin.myProgressManager.GetProgress(aId);
        int missing = aTarget - has;

        float perCraft = 0;
        for(Resource r : myMakes)
        {
            if (r.myId.equals(aId))
            {
                perCraft = r.myAmount;
            }
        }

        assert perCraft != 0; // you Calculated a recipe that doesn't produce what you ask for

        int repeatCount = (int)Math.ceil((float)missing / perCraft);

        for(Resource r : myTakes)
            out.add(new Resource(r.myId, r.myAmount * repeatCount));

        for (Resource r : myRequires)
            out.add(new Resource(r.myId, r.myAmount));

        return out;
    }

    public int CalculateAvailable(TodoPlugin aPlugin, List<Resource> aAvailableIngredients, String aId, int aMaximum)
    {
        int crafts = -1;

        for (Resource r : myRequires) {
            int available = aPlugin.myProgressManager.GetProgress(r.myId);

            for (Resource r2 : aAvailableIngredients)
            {
                if (r2.myId.equals(r.myId))
                    available += r2.myAmount;
            }

            if (available < r.myAmount)
                return 0;
        }

        for (Resource r : myTakes)
        {
            int available = aPlugin.myProgressManager.GetProgress(r.myId);

            for (Resource r2 : aAvailableIngredients)
            {
                if (r2.myId.equals(r.myId))
                    available += r2.myAmount;
            }

            int craftsAvailable = (int)Math.floor(available / r.myAmount);

            if (crafts == -1)
            {
                crafts = craftsAvailable;
            }
            else
            {
                crafts = Math.min(crafts, craftsAvailable);
            }
        }

        if (crafts == -1)
            return aMaximum;

        for (Resource r : myMakes)
        {
            if (r.myId.equals(aId))
                return (int)Math.floor(Math.min(crafts * r.myAmount, aMaximum));
        }

        assert false : "This recipe does not produce requested resource";
        return 0;
    }
}
