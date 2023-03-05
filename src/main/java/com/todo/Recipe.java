package com.todo;

import net.runelite.api.Skill;
import net.runelite.client.plugins.skillcalculator.skills.SkillAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Recipe
{
    public String myName;
    public List<Resource> myProducts;
    public List<Resource> myResources;
    public List<Resource> myRequirements; // assumtion: no resources are also requirements

    public Recipe(String aName, List<Resource> aProducts, List<Resource> aResources)
    {
        this(aName, aProducts, aResources, new ArrayList<Resource>());
    }

    public Recipe(String aName, List<Resource> aProducts, List<Resource> aResources, List<Resource> aRequirements)
    {
        myName = aName;
        myProducts = aProducts;
        myResources = aResources;
        myRequirements = aRequirements;
    }

    public List<Resource> Calculate(TodoPlugin aPlugin, String aId, int aTarget)
    {
        List<Resource> out = new ArrayList<>();

        int has = aPlugin.myProgressManager.GetProgress(aId);
        int missing = aTarget - has;
        if (missing < 0)
            return out;

        int perCraft = 0;
        for(Resource r : myProducts)
        {
            if (r.myId.equals(aId))
            {
                perCraft = r.myAmount;
            }
        }

        assert perCraft != 0; // you Calculated a recipe that doesn't produce what you ask for

        int repeatCount = (int)Math.ceil((float)missing / perCraft);

        for(Resource r : myResources)
            out.add(new Resource(r.myId, r.myAmount * repeatCount));

        for (Resource r : myRequirements)
            out.add(new Resource(r.myId, r.myAmount));

        return out;
    }

    public int CalculateAvailable(TodoPlugin aPlugin, List<Resource> aAvailableIngredients, String aId, int aMaximum)
    {
        int crafts = -1;

        for (Resource r : myRequirements) {
            int available = aPlugin.myProgressManager.GetProgress(r.myId);

            for (Resource r2 : aAvailableIngredients)
            {
                if (r2.myId.equals(r.myId))
                    available += r2.myAmount;
            }

            if (available < r.myAmount)
                return 0;
        }

        for (Resource r : myResources)
        {
            int available = aPlugin.myProgressManager.GetProgress(r.myId);

            for (Resource r2 : aAvailableIngredients)
            {
                if (r2.myId.equals(r.myId))
                    available += r2.myAmount;
            }

            int craftsAvailable = available / r.myAmount;

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

        for (Resource r : myProducts)
        {
            if (r.myId.equals(aId))
                return Math.min(crafts * r.myAmount, aMaximum);
        }

        assert false : "This recipe does not produce requested resource";
        return 0;
    }
}
