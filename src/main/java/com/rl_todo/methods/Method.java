package com.rl_todo.methods;

import com.rl_todo.*;
import com.rl_todo.serialization.SerializableMethod;
import com.rl_todo.serialization.SerializableRecursiveMethod;
import com.rl_todo.ui.IdBuilder;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Method
{
    public String myName;
    public String myCategory;
    public ResourcePool myTakes = new ResourcePool();
    public ResourcePool myMakes = new ResourcePool();
    public ResourcePool myRequires = new ResourcePool();

    public String GetName()
    {
        return  myName;
    }

    Method(String aName, String aCategory)
    {
        myName = aName;
        myCategory = aCategory;

        if (myCategory == null)
            myCategory = "/";
    }

    public static Optional<Method> FromSerialized(TodoPlugin aPlugin, SerializableRecursiveMethod aSerialized, String aMainProduct)
    {
        if (aSerialized.special != null)
        {
            switch (aSerialized.special)
            {
                case "level":
                    return LevelMethod.LevelFromSerialized(aPlugin, aMainProduct);
            }

            return Optional.empty();
        }

        Method method = new Method(aSerialized.name, "from_saved");

        if (aSerialized.requires != null)
            for (Map.Entry<String, SerializableRecursiveMethod> kvPair : aSerialized.requires.entrySet())
                method.myRequires.Add(kvPair.getKey(), kvPair.getValue().per_craft);

        if (aSerialized.takes != null)
            for (Map.Entry<String, SerializableRecursiveMethod> kvPair : aSerialized.takes.entrySet())
                method.myTakes.Add(kvPair.getKey(), kvPair.getValue().per_craft);

        if (aSerialized.byproducts != null)
            aSerialized.byproducts.forEach((key, value) -> method.myMakes.Add(key, value));

        method.myMakes.Add(aMainProduct, aSerialized.per_craft);

        return Optional.of(method);
    }

    public static Optional<Method> FromSerialized(TodoPlugin aPlugin, SerializableMethod aSerialized)
    {
        if (aSerialized.name == null)
            return Optional.empty();

        if (aSerialized.makes == null)
            return Optional.empty();


        if (aSerialized.special != null)
        {
            switch (aSerialized.special)
            {
                case "level":
                    return LevelMethod.LevelFromSerialized(aPlugin, aSerialized);
            }

            return Optional.empty();
        }

        Method out = new Method(aSerialized.name, aSerialized.category);

        aSerialized.makes.forEach((key, value) -> out.myMakes.Add(new Resource(key, value)));

        if (aSerialized.takes != null)
            aSerialized.takes.forEach((key, value) -> out.myTakes.Add(new Resource(key, value)));

        if (aSerialized.requires != null)
            aSerialized.requires.forEach((key, value) -> out.myRequires.Add(new Resource(key, value)));

        return Optional.of(out);
    }

    public SerializableRecursiveMethod SerializeSparse(String aMainProduct)
    {
        assert myMakes.GetSpecific(aMainProduct) > 0.f;

        SerializableRecursiveMethod out = new SerializableRecursiveMethod();

        out.per_craft = myMakes.GetSpecific(aMainProduct);

        return out;
    }

    public void SerializeInto(SerializableRecursiveMethod aSparseMethod, String aMainProduct)
    {
        aSparseMethod.name = myName;

        aSparseMethod.byproducts = new HashMap<>();

        myMakes.All().forEach((kvPair) -> {
            if (!kvPair.getKey().equals(aMainProduct))
                aSparseMethod.byproducts.put(kvPair.getKey(), kvPair.getValue());
        });

        aSparseMethod.requires = new HashMap<>();
        myRequires.All().forEach((kvPair) -> aSparseMethod.requires.put(kvPair.getKey(), new SerializableRecursiveMethod(kvPair.getValue())));

        aSparseMethod.takes = new HashMap<>();
        myTakes.All().forEach((kvPair) -> aSparseMethod.takes.put(kvPair.getKey(), new SerializableRecursiveMethod(kvPair.getValue())));

        if (aSparseMethod.takes.isEmpty())
            aSparseMethod.takes = null;

        if (aSparseMethod.requires.isEmpty())
            aSparseMethod.requires = null;

        if (aSparseMethod.byproducts.isEmpty())
            aSparseMethod.byproducts = null;
    }

    public Method takes(int aItem)
    {
        myTakes.Add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method takes(int aItem, double aAmount)
    {
        myTakes.Add(IdBuilder.itemResource(aItem, aAmount));
        return this;
    }

    public Method takes(Resource aCustom)
    {
        myTakes.Add(aCustom);
        return this;
    }

    public Method takes(Alternative aAlternative, int aAmount)
    {
        myTakes.Add(IdBuilder.alternativeResource(aAlternative, aAmount));
        return this;
    }

    public Method makes(Quest aQuest)
    {
        myMakes.Add(IdBuilder.questResource(aQuest));
        return this;
    }
    public Method makes(int aItem)
    {
        myMakes.Add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method makes(int aItem, float aAmount)
    {
        myMakes.Add(IdBuilder.itemResource(aItem, aAmount));
        return this;
    }

    public Method makes(int aItem, double aAmount)
    {
        myMakes.Add(IdBuilder.itemResource(aItem, (float)aAmount));
        return this;
    }

    public Method makes(Skill aSkill, double aXpAmount)
    {
        myMakes.Add(IdBuilder.xpResource(aSkill, aXpAmount));
        return this;
    }

    public Method requires(int aItem)
    {
        myRequires.Add(IdBuilder.itemResource(aItem, 1));
        return this;
    }

    public Method requires(Alternative aAlternative)
    {
        myRequires.Add(IdBuilder.alternativeResource(aAlternative, 1));
        return this;
    }

    public Method requires(Quest aSkill)
    {
        myRequires.Add(IdBuilder.questResource(aSkill));
        return this;
    }

    public Method requires(Skill aSkill, int aLevel)
    {
        myRequires.Add(IdBuilder.levelResource(aSkill, aLevel));
        return this;
    }

    public Method build()
    {
        Validate();
        return this;
    }

    private void Validate()
    {
        for (Resource r : myMakes.GetAsList()) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [makes] " + r.myId + " has amount zero");
        }

        for (Resource r : myTakes.GetAsList()) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [takes] " + r.myId + " has amount zero");
        }

        for (Resource r : myRequires.GetAsList()) {
            if (r.myAmount == 0)
                TodoPlugin.IgnorableError(myName + ": [requires] " + r.myId + " has amount zero");
        }

        for(String key : myTakes.SharedKeys(myRequires))
            TodoPlugin.IgnorableError(myName + ": [takes & requires same item] " + key);
    }

    public ResourcePool CalculateNeeded(TodoPlugin aPlugin, String aId, int aTarget)
    {
        int has = aPlugin.myProgressManager.GetProgress(aId);
        int missing = aTarget - has;

        float perCraft = myMakes.GetSpecific(aId);

        assert perCraft != 0; // you Calculated a recipe that doesn't produce what you ask for

        int repeatCount = (int)Math.ceil((float)missing / perCraft);

        ResourcePool out = myTakes.Scaled(repeatCount);
        out.AddAll(myRequires);

        return out;
    }

    public ResourcePool CalculateAvailable(ProgressManager aProgressManager, String aId, float aWanted)
    {
        ResourcePool available = new ResourcePool();

        myTakes.Ids().forEach((id) -> available.Add(id, aProgressManager.GetProgress(id)));
        myRequires.Ids().forEach((id) -> available.Add(id, aProgressManager.GetProgress(id)));

        return CalculateAvailable(available, aId, aWanted);
    }

    public ResourcePool CalculateAvailable(ResourcePool aAvailableResources, String aId, float aWanted)
    {
        ResourcePool out = new ResourcePool();

        float perCraft = myMakes.GetSpecific(aId);

        assert perCraft > 0;

        float missing = aWanted - aAvailableResources.GetSpecific(aId);

        int wantedCrafts = Math.min(Integer.MAX_VALUE, (int)Math.ceil(missing / perCraft));

        // check the requirements
        if (myRequires.AvailableRepeats(aAvailableResources) <= 0)
            return out;

        int maxCrafts = myTakes.AvailableRepeats(aAvailableResources);
        int crafts = Math.min(wantedCrafts, maxCrafts);

        out.AddAll(myTakes.Scaled(-crafts));
        out.AddAll(myMakes.Scaled(crafts));
        return out;
    }
}
