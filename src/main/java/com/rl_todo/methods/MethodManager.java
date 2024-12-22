package com.rl_todo.methods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.rl_todo.*;
import com.rl_todo.serialization.SerializableMethod;
import lombok.NonNull;
import net.runelite.api.*;
import net.runelite.client.plugins.skillcalculator.skills.MiningAction;
import net.runelite.client.plugins.skillcalculator.skills.SmithingAction;
import okhttp3.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class MethodManager
{
    private static JsonParser myJsonParser;

    private TodoPlugin myPlugin;

    private List<Method> myMethods = new ArrayList<>();

    private Map<String, List<Method>> myLookup = new HashMap<>();

    public MethodManager(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        myJsonParser = new JsonParser();

        LoadFromConfig();
        for (Skill skill : Skill.values())
            AddMethod(new LevelMethod(myPlugin, skill));

        TodoPlugin.debug("Loaded " + myMethods.size() + " recipes", 1);
    }

    private void LoadFromConfig()
    {
        Arrays.stream(myPlugin.myConfig.methodSources().split("[\\n\\r,;]"))
            .map((string) -> string.replace("${runelite}", RUNELITE_DIR.toString()))
            .forEach(this::LoadFromPath);
    }

    private void LoadFromPath(String aPath)
    {
        if (aPath.startsWith("http"))
        {
            Request req = new Request.Builder().url(aPath).build();

            Call call = myPlugin.myHttpClient.newCall(req);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    TodoPlugin.IgnorableError("Failed to load methods from: " + aPath);
                    TodoPlugin.IgnorableError(e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    LoadFromString(aPath, response.body().charStream());

                    TodoPlugin.debug("Loaded " + myMethods.size() + " recipes", 1);
                }
            });
            return;
        }

        try
        {
            FileReader reader = new FileReader(aPath);
            LoadFromString(aPath, reader);
        }
        catch (FileNotFoundException e)
        {
            TodoPlugin.IgnorableError("File not found: " + aPath);
        }
    }

    private void LoadFromString(String aContext, Reader aContent)
    {
        JsonElement element = myJsonParser.parse(aContent);

        if (element.isJsonObject())
        {
            LoadJsonObject(aContext, element.getAsJsonObject());
        }
        else
        {
            element.getAsJsonArray()
                .forEach((obj) ->
                {
                    if (obj.isJsonObject())
                    {
                        LoadJsonObject(aContext, obj.getAsJsonObject());
                    }
                    else
                    {
                        TodoPlugin.IgnorableError("Malformed data, expected only: method object, or array of method objects");
                    }
                });
        }
    }

    private void LoadJsonObject(String aContext, JsonObject aMethod)
    {
        SerializableMethod serializedMethod = myPlugin.myGson.fromJson(aMethod, SerializableMethod.class);

        if (serializedMethod == null)
        {
            TodoPlugin.IgnorableError("Failed to load method in " + aContext);
            return;
        }

        Method.FromSerialized(myPlugin, serializedMethod)
            .ifPresentOrElse(
                this::AddMethod,
                () -> TodoPlugin.IgnorableError("Failed to load method in " + aContext));
    }


    public Stream<Method> GetAllMethods()
    {
        return myMethods.stream();
    }

    public Stream<Method> GetAvailableMethods(String aId)
    {
        return GetAllMethods().filter((method) -> method.myMakes.GetSpecific(aId) > 0.f);
    }

    protected void AddMethod(Method aMethod)
    {
        myMethods.add(aMethod);

        for(String resourceId : aMethod.myMakes.Ids())
        {
            myLookup.putIfAbsent(resourceId, new ArrayList<>());
            List<Method> list = myLookup.get(resourceId);

            int index = Collections.binarySearch(list, aMethod, Comparator.comparing(Method::GetName));
            if (index < 0)
            {
                index = -index - 1;
            }
            list.add(index, aMethod);
        }
    }
}
