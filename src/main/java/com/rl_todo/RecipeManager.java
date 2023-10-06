package com.rl_todo;

import net.runelite.api.*;
import net.runelite.api.events.ItemDespawned;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.plugins.skillcalculator.skills.MiningAction;
import net.runelite.client.plugins.skillcalculator.skills.SmithingAction;

import java.util.*;

class Costs
{
    int myCost;
    int myResult;

    public Costs(int aCosts, int aResult)
    {
        myCost = aCosts;
        myResult = aResult;
    }
}

class BarCraft
{
    public int myBarId;
    public int myOreId;
    public int myCoalAmount;
    public int myXP;
    public int myLevel;

    BarCraft(int aBarId, int aOreId, int aCoalAmount, int aXP, int aLevel)
    {
        myBarId = aBarId;
        myOreId = aOreId;
        myCoalAmount = aCoalAmount;
        myXP = aXP;
        myLevel = aLevel;
    }
}

class WildcardRecipe
{
    public String myPath;
    public Recipe myRecipe;

    public WildcardRecipe(String aPath, Recipe aRecipe)
    {
        myPath = aPath;
        myRecipe = aRecipe;
    }
}

public class RecipeManager
{
    private TodoPlugin myPlugin;

    private Map<String, Recipe> myRecipes = new HashMap<>();
    private Map<String, Recipe> myConfigs = new HashMap<>();
    private List<WildcardRecipe> myWildcardConfigs = new ArrayList<>();
    private List<WildcardRecipe> myDefaultRecipes = new ArrayList<>();

    private Map<String, List<Recipe>> myLookup = new HashMap<>();
    final private List<Recipe> myEmptyList = new ArrayList<>();

    public RecipeManager(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;

        LoadAllLevels();
        LoadAllSkills();
        LoadCapesOfAccomplishments();
        LoadNMZInfusions();

        BuildTables();

        LoadConfig();
    }

    public Recipe GetRecipeByName(String aName) { return myRecipes.get(aName.toLowerCase()); }

    public Recipe GetRecipe(String aPath)
    {
        Recipe r = myConfigs.get(aPath);
        if (r == null)
        {
            for(WildcardRecipe wc : myWildcardConfigs)
            {
                if (aPath.endsWith(wc.myPath))
                {
                    r = wc.myRecipe;
                }
            }
        }

        if (r == null && myPlugin.myConfig.includeDefaultRecipes())
        {
            for(WildcardRecipe wc : myDefaultRecipes)
            {
                if (aPath.endsWith(wc.myPath))
                {
                    r = wc.myRecipe;
                }
            }
        }

        return r;
    }

    public List<Recipe> GetAvailableRecipes(String aId)
    {
        return myLookup.getOrDefault(aId, myEmptyList);
    }

    private void AddDefaultRecipe(String aId, Recipe aRecipe)
    {
        myDefaultRecipes.add(new WildcardRecipe(aId, aRecipe));
        AddRecipe(aRecipe);
    }

    private void AddRecipe(Recipe aRecipe)
    {
        Object prev = myRecipes.putIfAbsent(aRecipe.myName.toLowerCase(), aRecipe);
        assert prev == null : "Recipe with duplicate key " + aRecipe.myName.toLowerCase();
    }

    public void LoadConfig()
    {
        myConfigs.clear();
        String raw = myPlugin.myConfig.getRecipes();
        String[] rows = raw.split("\n");
        for(String row : rows)
        {
            int col = row.indexOf(':');
            if (col == -1)
                continue;

            String key = row.substring(0,col).trim();
            String value = row.substring(col + 1).trim();

            Recipe r = GetRecipeByName(value);
            if (r == null)
                continue;

            if (key.startsWith("*>"))
            {
                myWildcardConfigs.add(new WildcardRecipe(key.substring(1), r));
            }
            else
            {
                myConfigs.put(key, r);
            }
        }
    }

    private void BuildTables()
    {
        for (Recipe recipe : myRecipes.values())
        {
            for(Resource resource : recipe.myProducts)
            {
                myLookup.putIfAbsent(resource.myId, new ArrayList<>());
                myLookup.get(resource.myId).add(recipe);
            }
        }

        for (List<Recipe> list : myLookup.values())
            Collections.sort(list, Comparator.comparing((Recipe a) -> a.myName));

        TodoPlugin.debug(myRecipes);
    }

    private void LoadAllLevels()
    {
        for(Skill skill : Skill.values())
        {
            AddDefaultRecipe(
                    IdBuilder.levelId(skill),
                    new LevelRecipe(skill));

            AddRecipe(new StewBoostingRecipe(skill));
        }
    }

    private void LoadCapesOfAccomplishments()
    {
        {
            List<Resource> skills = new ArrayList<>();

            for(Skill skill : Skill.values())
                skills.add(IdBuilder.levelResource(skill, 99));

            AddDefaultRecipe(
                            IdBuilder.itemId(ItemID.MAX_CAPE),
                            new Recipe(
                                "Buy Max Cape",
                                new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.MAX_CAPE, 1))),
                                new ArrayList<Resource>(),
                                skills));
        }

        {
            List<Resource> quests = new ArrayList<>();

            List<Quest> miniQuests = new ArrayList(Arrays.asList(new Quest[]
                    {
                            Quest.ENTER_THE_ABYSS,
                            Quest.ARCHITECTURAL_ALLIANCE,
                            Quest.BEAR_YOUR_SOUL,
                            Quest.ALFRED_GRIMHANDS_BARCRAWL,
                            Quest.CURSE_OF_THE_EMPTY_LORD,
                            Quest.THE_ENCHANTED_KEY,
                            Quest.THE_GENERALS_SHADOW,
                            Quest.SKIPPY_AND_THE_MOGRES,
                            Quest.MAGE_ARENA_I,
                            Quest.LAIR_OF_TARN_RAZORLOR,
                            Quest.FAMILY_PEST,
                            Quest.MAGE_ARENA_II,
                            Quest.IN_SEARCH_OF_KNOWLEDGE,
                            Quest.DADDYS_HOME,
                            Quest.HOPESPEARS_WILL,
                            Quest.THE_FROZEN_DOOR,
                            Quest.INTO_THE_TOMBS
                    }));

            for(Quest quest : Quest.values())
                if (!miniQuests.contains(quest))
                    quests.add(IdBuilder.questResource(quest));

            AddDefaultRecipe(
                            IdBuilder.itemId(ItemID.QUEST_POINT_CAPE),
                            new Recipe(
                                "Buy Quest Cape",
                                new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.QUEST_POINT_CAPE, 1))),
                                new ArrayList<Resource>(),
                                quests));
        }

        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.ATTACK_CAPE),
                    new Recipe(
                            "Buy Attack Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.ATTACK_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.ATTACK, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.STRENGTH_CAPE),
                    new Recipe(
                            "Buy Strength Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.STRENGTH_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.STRENGTH, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.DEFENCE_CAPE),
                    new Recipe(
                            "Buy Defence Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.DEFENCE_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.DEFENCE, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.RANGING_CAPE),
                    new Recipe(
                            "Buy Ranging Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.RANGING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.RANGED, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.PRAYER_CAPE),
                    new Recipe(
                            "Buy Prayer Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.PRAYER_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.PRAYER, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.MAGIC_CAPE),
                    new Recipe(
                            "Buy Magic Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.MAGIC_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.RUNECRAFT_CAPE),
                    new Recipe(
                            "Buy Runecrafting Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.RUNECRAFT_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.RUNECRAFT, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.CONSTRUCT_CAPE),
                    new Recipe(
                            "Buy Construction Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.CONSTRUCT_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.CONSTRUCTION, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.HITPOINTS_CAPE),
                    new Recipe(
                            "Buy Hitpoints Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.HITPOINTS_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.HITPOINTS, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.AGILITY_CAPE),
                    new Recipe(
                            "Buy Agility Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.AGILITY_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.AGILITY, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.HERBLORE_CAPE),
                    new Recipe(
                            "Buy Herblore Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.HERBLORE_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.HERBLORE, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.THIEVING_CAPE),
                    new Recipe(
                            "Buy Thieving Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.THIEVING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.THIEVING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.CRAFTING_CAPE),
                    new Recipe(
                            "Buy Crafting Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.CRAFTING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.CRAFTING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.FLETCHING_CAPE),
                    new Recipe(
                            "Buy Fletching Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.FLETCHING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.FLETCHING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.SLAYER_CAPE),
                    new Recipe(
                            "Buy Slayer Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.SLAYER_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.SLAYER, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.HUNTER_CAPE),
                    new Recipe(
                            "Buy Hunter Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.HUNTER_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.HUNTER, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.MINING_CAPE),
                    new Recipe(
                            "Buy Mining Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.MINING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.MINING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.SMITHING_CAPE),
                    new Recipe(
                            "Buy Smithing Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.SMITHING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.SMITHING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.FISHING_CAPE),
                    new Recipe(
                            "Buy Fishing Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.FISHING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.FISHING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.COOKING_CAPE),
                    new Recipe(
                            "Buy Cooking Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.COOKING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.COOKING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.FIREMAKING_CAPE),
                    new Recipe(
                            "Buy Firemaking Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.FIREMAKING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.FIREMAKING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.WOODCUTTING_CAPE),
                    new Recipe(
                            "Buy Woodcutting Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.WOODCUTTING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.WOODCUTTING, 99)))));
        }
        {
            AddDefaultRecipe(
                    IdBuilder.itemId(ItemID.FARMING_CAPE),
                    new Recipe(
                            "Buy Farming Cape",
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.itemResource(ItemID.FARMING_CAPE, 1))),
                            new ArrayList<Resource>(),
                            new ArrayList<Resource>(Arrays.asList(IdBuilder.levelResource(Skill.FARMING, 99)))));
        }
    }

    private void LoadAllSkills()
    {
        for(MiningAction action : MiningAction.values())
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.MINING, (int)action.getXp() * 100));
            products.add(IdBuilder.itemResource(action.getItemId(), 1));


            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.itemResource(ItemID.RUNE_PICKAXE, 1));
            requirements.add(IdBuilder.levelResource(Skill.MINING, action.getLevel()));

            AddRecipe(new Recipe("mine " + action.getName(myPlugin.myItemManager), products, new ArrayList<>(), requirements));
        }

        LoadSmithing();
        LoadMagic();
        LoadCooking();
        LoadCrafting();
    }

    private void LoadCooking()
    {
        class Cookable
        {
            int myItem;
            int myResult;
            int myXp;
            int myLevelReq;

            public Cookable(int aItem, int aResult, int aXp, int aLevelReq)
            {
                myItem = aItem;
                myResult = aResult;
                myXp = aXp;
                myLevelReq = aLevelReq;
            }
        }

        for(Cookable cook : new Cookable[]{
                new Cookable(ItemID.RAW_BEEF, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_BEAR_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_BOAR_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_RAT_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_YAK_MEAT, ItemID.COOKED_MEAT, 3000, 1),
                new Cookable(ItemID.RAW_SHRIMPS, ItemID.SHRIMPS, 3000, 1),
                new Cookable(ItemID.RAW_CHICKEN, ItemID.COOKED_CHICKEN, 3000, 1),
                new Cookable(ItemID.RAW_RABBIT, ItemID.COOKED_RABBIT, 3000, 1),
                new Cookable(ItemID.RAW_ANCHOVIES, ItemID.ANCHOVIES, 3000, 1),
                new Cookable(ItemID.RAW_SARDINE, ItemID.SARDINE, 4000, 1),
                new Cookable(ItemID.RAW_UGTHANKI_MEAT, ItemID.UGTHANKI_MEAT, 4000, 1),
                new Cookable(ItemID.RAW_HERRING, ItemID.HERRING, 5000, 5),
                new Cookable(ItemID.RAW_MACKEREL, ItemID.MACKEREL, 6000, 10),
                new Cookable(ItemID.RAW_BIRD_MEAT, ItemID.ROAST_BIRD_MEAT, 6000, 11),
                new Cookable(ItemID.THIN_SNAIL_MEAT, ItemID.THIN_SNAIL, 7000, 12),
                new Cookable(ItemID.RAW_TROUT, ItemID.TROUT, 7000, 15),
                new Cookable(ItemID.SPIDER_ON_STICK_6297, ItemID.SPIDER_ON_STICK, 8000, 16),
                new Cookable(ItemID.SPIDER_ON_SHAFT_6299, ItemID.SPIDER_ON_SHAFT, 8000, 16),
                new Cookable(ItemID.SKEWERED_RABBIT, ItemID.ROAST_RABBIT, 7000, 16),
                new Cookable(ItemID.LEAN_SNAIL_MEAT, ItemID.LEAN_SNAIL, 8000, 17),
                new Cookable(ItemID.RAW_COD, ItemID.COD, 7500, 18),
                new Cookable(ItemID.RAW_PIKE, ItemID.PIKE, 8000, 20),
                new Cookable(ItemID.SKEWERED_BEAST, ItemID.ROAST_BEAST_MEAT, 8250, 21),
                new Cookable(ItemID.CRAB_MEAT, ItemID.COOKED_CRAB_MEAT, 10000, 21),
                new Cookable(ItemID.FAT_SNAIL_MEAT, ItemID.FAT_SNAIL, 9500, 22),
                new Cookable(ItemID.RAW_SALMON, ItemID.SALMON, 9000, 25),
                new Cookable(ItemID.RAW_SLIMY_EEL, ItemID.COOKED_SLIMY_EEL, 9500, 28),
                new Cookable(ItemID.RAW_TUNA, ItemID.TUNA, 10000, 30),
                new Cookable(ItemID.RAW_KARAMBWAN, ItemID.COOKED_KARAMBWAN, 19000, 30),
                new Cookable(ItemID.RAW_CHOMPY, ItemID.COOKED_CHOMPY, 10000, 30),
                new Cookable(ItemID.RAW_FISHCAKE, ItemID.COOKED_FISHCAKE, 10000, 31),
                new Cookable(ItemID.RAW_RAINBOW_FISH, ItemID.RAINBOW_FISH, 11000, 35),
                new Cookable(ItemID.RAW_CAVE_EEL, ItemID.CAVE_EEL, 11500, 38),
                new Cookable(ItemID.RAW_LOBSTER, ItemID.LOBSTER, 12000, 40),
                new Cookable(ItemID.RAW_JUBBLY, ItemID.COOKED_JUBBLY, 16000, 41),
                new Cookable(ItemID.RAW_BASS, ItemID.BASS, 13000, 43),
                new Cookable(ItemID.RAW_SWORDFISH, ItemID.SWORDFISH, 14000, 45),
                new Cookable(ItemID.RAW_LAVA_EEL, ItemID.LAVA_EEL, 3000, 53),
                new Cookable(ItemID.RAW_MONKFISH, ItemID.MONKFISH, 15000, 62),
                new Cookable(ItemID.RAW_SHARK, ItemID.SHARK, 21000, 80),
                new Cookable(ItemID.RAW_SEA_TURTLE, ItemID.SEA_TURTLE, 21130, 82),
                new Cookable(ItemID.RAW_ANGLERFISH, ItemID.ANGLERFISH, 23000, 84),
                new Cookable(ItemID.RAW_DARK_CRAB, ItemID.DARK_CRAB, 21500, 90),
                new Cookable(ItemID.RAW_MANTA_RAY, ItemID.MANTA_RAY, 21620, 91),

                //pies
                new Cookable(ItemID.UNCOOKED_BERRY_PIE, ItemID.REDBERRY_PIE, 7800, 10),
                new Cookable(ItemID.UNCOOKED_MEAT_PIE, ItemID.MEAT_PIE, 11000, 20),
                new Cookable(ItemID.RAW_MUD_PIE, ItemID.MUD_PIE, 12800, 29),
                new Cookable(ItemID.UNCOOKED_APPLE_PIE, ItemID.APPLE_PIE, 13000, 30),
                new Cookable(ItemID.RAW_GARDEN_PIE, ItemID.GARDEN_PIE, 13800, 34),
                new Cookable(ItemID.RAW_FISH_PIE, ItemID.FISH_PIE, 16400, 47),
                new Cookable(ItemID.UNCOOKED_BOTANICAL_PIE, ItemID.BOTANICAL_PIE, 18000, 52),
                new Cookable(ItemID.UNCOOKED_MUSHROOM_PIE, ItemID.MUSHROOM_PIE, 200000, 60),
                new Cookable(ItemID.RAW_ADMIRAL_PIE, ItemID.ADMIRAL_PIE, 210000, 70),
                new Cookable(ItemID.UNCOOKED_DRAGONFRUIT_PIE, ItemID.DRAGONFRUIT_PIE, 220000, 73),
                new Cookable(ItemID.RAW_WILD_PIE, ItemID.WILD_PIE, 240000, 85),
                new Cookable(ItemID.RAW_SUMMER_PIE, ItemID.SUMMER_PIE, 260000, 95),
        })
        {
            AddRecipe(new Recipe(
                    "Cook "+ myPlugin.myItemManager.getItemComposition(cook.myItem).getName(),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(cook.myResult, 1),
                            IdBuilder.xpResource(Skill.COOKING, cook.myXp))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(cook.myItem, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, cook.myLevelReq)))));
        }

        for(Cookable cook : new Cookable[]
                {
                        new Cookable(ItemID.RAW_BEEF, ItemID.SINEW, 300, 1),
                        new Cookable(ItemID.RAW_BEAR_MEAT, ItemID.SINEW, 300, 1),
                        new Cookable(ItemID.RAW_BOAR_MEAT, ItemID.SINEW, 300, 1)
                })
        {
            AddRecipe(new Recipe(
                    "Cook "+ myPlugin.myItemManager.getItemComposition(cook.myItem).getName() + " into Sinew",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(cook.myResult, 1),
                            IdBuilder.xpResource(Skill.COOKING, cook.myXp))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(cook.myItem, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, cook.myLevelReq)))));
        }

        // TODO Bread
        // TODO Pitta
        // TODO new Cookable(ItemID.SACRED_EEL, ItemID.ZULRAHS_SCALES, 15000, 62),
        // TODO new Cookable(ItemID.RAW_KARAMBWAN, ItemID.POISON_KARAMBWAN, 8000, 1),
        // TODO gubby
        // TODO cavefish
        // TODO tetra
        // TODO catfish

        // Pies
        {
            AddRecipe(new Recipe(
                    "Make Uncooked berry pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.UNCOOKED_BERRY_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.REDBERRIES, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 10)))));

            AddRecipe(new Recipe(
                    "Make Uncooked meat pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.UNCOOKED_MEAT_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COOKED_MEAT, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 20)))));

            AddRecipe(new Recipe(
                    "Make Raw mud pie with Bucket of Water",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_MUD_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COMPOST, 1),
                            IdBuilder.itemResource(ItemID.BUCKET_OF_WATER, 1),
                            IdBuilder.itemResource(ItemID.CLAY, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 29)))));

            AddRecipe(new Recipe(
                    "Make Raw mud pie with Bowl of Water",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_MUD_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COMPOST, 1),
                            IdBuilder.itemResource(ItemID.BOWL_OF_WATER, 1),
                            IdBuilder.itemResource(ItemID.CLAY, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 29)))));

            AddRecipe(new Recipe(
                    "Make Uncooked apple pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.UNCOOKED_APPLE_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COOKING_APPLE, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 30)))));

            AddRecipe(new Recipe(
                    "Make Raw garden pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_GARDEN_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.TOMATO, 1),
                            IdBuilder.itemResource(ItemID.ONION, 1),
                            IdBuilder.itemResource(ItemID.CABBAGE, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 34)))));

            AddRecipe(new Recipe(
                    "Make Raw fish pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_GARDEN_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.TROUT, 1),
                            IdBuilder.itemResource(ItemID.COD, 1),
                            IdBuilder.itemResource(ItemID.POTATO, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 47)))));

            AddRecipe(new Recipe(
                    "Make Uncooked mushroom pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.UNCOOKED_MUSHROOM_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.SULLIUSCEP_CAP, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 60)))));

            AddRecipe(new Recipe(
                    "Make Raw admiral pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_ADMIRAL_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.SALMON, 1),
                            IdBuilder.itemResource(ItemID.TUNA, 1),
                            IdBuilder.itemResource(ItemID.POTATO, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 70)))));

            AddRecipe(new Recipe(
                    "Make Uncooked dragonfruit pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.UNCOOKED_DRAGONFRUIT_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.DRAGONFRUIT, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 73)))));

            AddRecipe(new Recipe(
                    "Make Raw wild pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_WILD_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_BEAR_MEAT, 1),
                            IdBuilder.itemResource(ItemID.RAW_CHOMPY, 1),
                            IdBuilder.itemResource(ItemID.RAW_RABBIT, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 85)))));

            AddRecipe(new Recipe(
                    "Make Raw summer pie",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.RAW_SUMMER_PIE, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.STRAWBERRY, 1),
                            IdBuilder.itemResource(ItemID.WATERMELON, 1),
                            IdBuilder.itemResource(ItemID.COOKING_APPLE, 1),
                            IdBuilder.itemResource(ItemID.PIE_SHELL, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.COOKING, 95)))));
        }
    }

    private void LoadMagic()
    {
        LoadStandardSpellbook();

        //TODO all the other spellbooks :(
    }

    private void LoadStandardSpellbook()
    {
        // Combat
        {
            // Offensive
            {
                //Strikes
                {
                    AddRecipe(new Recipe("Cast Wind Strike",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 550))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 1))),
                            new ArrayList<>()));

                    AddRecipe(new Recipe("Cast Water Strike",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 750))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 5)))));

                    AddRecipe(new Recipe("Cast Earth Strike",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 950))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 9)))));

                    AddRecipe(new Recipe("Cast Fire Strike",
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.xpResource(Skill.MAGIC, 1150))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 3))),
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 13)))));
                }

                // Bolts
                {
                    AddRecipe(new Recipe("Cast Wind Bolt",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1350))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 17)))));

                    AddRecipe(new Recipe("Cast Water Bolt",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1650))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 23)))));

                    AddRecipe(new Recipe("Cast Earth Bolt",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1950))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 29)))));

                    AddRecipe(new Recipe("Cast Fire Bolt",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2250))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 4))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 35)))));
                }

                // Blasts
                {
                    AddRecipe(new Recipe("Cast Wind Blast",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2550))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 41)))));

                    AddRecipe(new Recipe("Cast Water Blast",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2850))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 47)))));

                    AddRecipe(new Recipe("Cast Earth Blast",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3150))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 4))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 53)))));

                    AddRecipe(new Recipe("Cast Fire Blast",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3450))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 4),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 5))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 59)))));
                }

                // Waves
                {
                    AddRecipe(new Recipe("Cast Wind Wave",
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.xpResource(Skill.MAGIC, 3600))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 5))),
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 62)))));

                    AddRecipe(new Recipe("Cast Water Wave",
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.xpResource(Skill.MAGIC, 3750))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 5),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 7))),
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 65)))));

                    AddRecipe(new Recipe("Cast Earth Wave",
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.xpResource(Skill.MAGIC, 4000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 5),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 7))),
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 70)))));

                    AddRecipe(new Recipe("Cast Fire Wave",
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.xpResource(Skill.MAGIC, 4250))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 5),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 7))),
                            new ArrayList<>(
                                    Arrays.asList(IdBuilder.levelResource(Skill.MAGIC, 75)))));
                }

                // Surges
                {
                    AddRecipe(new Recipe("Cast Wind Surge",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3600))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.WRATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 7))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 81)))));

                    AddRecipe(new Recipe("Cast Water Surge",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3750))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.WRATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 7),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 10))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 85)))));

                    AddRecipe(new Recipe("Cast Earth Surge",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 4000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.WRATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 7),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 10))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 90)))));

                    AddRecipe(new Recipe("Cast Fire Surge",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 4250))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.WRATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 7),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 10))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 95)))));
                }

                // God spells
                {
                    AddRecipe(new Recipe("Cast Saradomin Strike",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3500))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 4),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 60),
                                    IdBuilder.itemResource(ItemID.SARADOMIN_STAFF, 1)))));

                    AddRecipe(new Recipe("Cast Flames of Zamorak",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3500))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 4))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 60),
                                    IdBuilder.itemResource(ItemID.ZAMORAK_STAFF, 1)))));

                    AddRecipe(new Recipe("Cast Claws of Guthix",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3500))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 4),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 60),
                                    IdBuilder.itemResource(ItemID.GUTHIX_STAFF, 1)))));
                }

                // Misc
                {
                    AddRecipe(new Recipe("Cast Crumble Undead",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2450))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 39)))));

                    AddRecipe(new Recipe("Cast Ibans Blast",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 5))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 50),
                                    IdBuilder.itemResource(ItemID.IBANS_STAFF, 1)))));

                    AddRecipe(new Recipe("Cast Ibans Blast with upgraded staff",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 5))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 50),
                                    IdBuilder.itemResource(ItemID.IBANS_STAFF_1410, 1)))));

                    AddRecipe(new Recipe("Cast Magic Dart",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.MIND_RUNE, 4))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 50),
                                    IdBuilder.itemResource(ItemID.IBANS_STAFF_1410, 1)))));

                    AddRecipe(new Recipe("Cast Charge",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 18000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BLOOD_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 50),
                                    IdBuilder.itemResource(ItemID.IBANS_STAFF_1410, 1)))));
                }
            }

            // Curses
            {
                // Low weakens
                {
                    AddRecipe(new Recipe("Cast Confuse",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1300))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BODY_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 3)))));

                    AddRecipe(new Recipe("Cast Weaken",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2100))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BODY_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 11)))));

                    AddRecipe(new Recipe("Cast Curse",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 2900))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BODY_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 19)))));
                }

                // High weakens
                {
                    AddRecipe(new Recipe("Cast Vulnerability",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 7600))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.SOUL_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 5),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 5))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 66)))));

                    AddRecipe(new Recipe("Cast Enfeeble",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 8300))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.SOUL_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 8),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 8))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 73)))));

                    AddRecipe(new Recipe("Cast Stun",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 9000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.SOUL_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 12),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 12))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 80)))));
                }

                // Snares
                {
                    AddRecipe(new Recipe("Cast Bind",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 3000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 2),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 3))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 20)))));

                    AddRecipe(new Recipe("Cast Snare",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 6000))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 4),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 4))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 50)))));

                    AddRecipe(new Recipe("Cast Entangle",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 8900))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 4),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 5),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 6))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 79)))));
                }

                AddRecipe(new Recipe("Cast Tele Block",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 8000))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 85)))));
            }

            // Teleports
            {
                AddRecipe(new Recipe("Cast Varrock Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3500))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 25)))));

                AddRecipe(new Recipe("Cast Lumbridge Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 4100))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 31)))));

                AddRecipe(new Recipe("Cast Falador Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 4700))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 37)))));

                AddRecipe(new Recipe("Cast Teleport to House",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3000))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 40)))));

                AddRecipe(new Recipe("Cast Camelot Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 5550))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 5))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 40)))));

                AddRecipe(new Recipe("Cast Ardougne Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 6100))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 2))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 51),
                                IdBuilder.questResource(Quest.PLAGUE_CITY)))));

                AddRecipe(new Recipe("Cast Watchtower Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 6800))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 2))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 58),
                                IdBuilder.questResource(Quest.WATCHTOWER)))));

                AddRecipe(new Recipe("Cast Trollheim Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 6800))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 2))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 61),
                                IdBuilder.questResource(Quest.EADGARS_RUSE)))));

                AddRecipe(new Recipe("Cast Ape Atoll Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 7400))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 2),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 2),
                                IdBuilder.itemResource(ItemID.BANANA, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 64),
                                IdBuilder.questResource(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI)))));

                AddRecipe(new Recipe("Cast Kourend Castle Teleport",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 8200))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 4),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 5),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 2))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 69)))));
            }

            // Odd Teleports
            {
                AddRecipe(new Recipe("Cast Teleother Lumbridge",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 8400))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 1),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 64)))));

                AddRecipe(new Recipe("Cast Teleother Falador",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 9200))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 1),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 82)))));

                AddRecipe(new Recipe("Cast Teleother Camelot",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 10000))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 2))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 90)))));

                AddRecipe(new Recipe("Cast Teleport to Target",
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 4500))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                                IdBuilder.itemResource(ItemID.CHAOS_RUNE, 1),
                                IdBuilder.itemResource(ItemID.DEATH_RUNE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 85)))));
            }
        }

        // Utility
        {

            AddRecipe(new Recipe("Cast Bones to Banans",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 2500))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                            IdBuilder.itemResource(ItemID.WATER_RUNE, 2),
                            IdBuilder.itemResource(ItemID.EARTH_RUNE, 2))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 15)))));

            AddRecipe(new Recipe("Cast Low Level Alchemy",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 3100))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                            IdBuilder.itemResource(ItemID.FIRE_RUNE, 3))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 21)))));

            // superheat
            {
                class SuperHeatable
                {
                    public int myResult;
                    public int myItem;
                    public int myCoalCount;
                    public int mySmithingXp;
                    public int mySmithingLevel;

                    SuperHeatable(int aResult, int aItem, int aCoalCount, int aSmithingXp, int aSmithingLevel)
                    {
                        myResult = aResult;
                        myItem = aItem;
                        myCoalCount = aCoalCount;
                        mySmithingXp = aSmithingXp;
                        mySmithingLevel = aSmithingLevel;
                    }
                }

                for(SuperHeatable heatable : new SuperHeatable[]
                        {
                                new SuperHeatable(ItemID.IRON_BAR, ItemID.IRON_ORE, 0, 1250, 15),
                                new SuperHeatable(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 1370, 20),
                                new SuperHeatable(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 2250, 40),
                                new SuperHeatable(ItemID.LOVAKITE_BAR, ItemID.LOVAKITE_ORE, 2, 2000, 45),
                                new SuperHeatable(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 3000, 50),
                                new SuperHeatable(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 3750, 70),
                                new SuperHeatable(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 5000, 85)
                        })
                {
                    List<Resource> resources = new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                            IdBuilder.itemResource(ItemID.FIRE_RUNE, 3),
                            IdBuilder.itemResource(heatable.myItem, 1)));

                    if (heatable.myCoalCount != 0)
                        resources.add(IdBuilder.itemResource(ItemID.COAL, heatable.myCoalCount));

                    AddRecipe(new Recipe("Cast Superheat Item on " + myPlugin.myItemManager.getItemComposition(heatable.myItem).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(heatable.myResult, 1),
                                    IdBuilder.xpResource(Skill.MAGIC, 5300),
                                    IdBuilder.xpResource(Skill.SMITHING, heatable.mySmithingXp))),
                            resources,
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 43),
                                    IdBuilder.levelResource(Skill.SMITHING, heatable.mySmithingLevel)))));
                }

                {
                    AddRecipe(new Recipe("Cast Superheat Item on Copper and Tin Ore",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.BRONZE_BAR, 1),
                                    IdBuilder.xpResource(Skill.MAGIC, 5300),
                                    IdBuilder.xpResource(Skill.SMITHING, 620))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.TIN_ORE, 1),
                                    IdBuilder.itemResource(ItemID.COPPER_ORE, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 43)))));
                }

                {
                    AddRecipe(new Recipe("Cast Superheat Item on Iron Ore with Coal available",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.STEEL_BAR, 1),
                                    IdBuilder.xpResource(Skill.MAGIC, 5300),
                                    IdBuilder.xpResource(Skill.SMITHING, 620))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.IRON_ORE, 1),
                                    IdBuilder.itemResource(ItemID.COAL, 2))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 43),
                                    IdBuilder.levelResource(Skill.SMITHING, 30)))));
                }

                {
                    AddRecipe(new Recipe("Cast Superheat Item on Gold Ore with Goldsmith Guantlets",
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.GOLD_BAR, 1),
                                    IdBuilder.xpResource(Skill.MAGIC, 5300),
                                    IdBuilder.xpResource(Skill.SMITHING, 5620))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 3),
                                    IdBuilder.itemResource(ItemID.GOLD_ORE, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 43),
                                    IdBuilder.levelResource(Skill.SMITHING, 40),
                                    IdBuilder.itemResource(ItemID.GOLDSMITH_GAUNTLETS, 1)))));
                }
            }

            AddRecipe(new Recipe("Cast High Level Alchemy",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 6500))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                            IdBuilder.itemResource(ItemID.FIRE_RUNE, 5))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 55)))));

            AddRecipe(new Recipe("Cast Bones to Peaches",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 6500))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                            IdBuilder.itemResource(ItemID.EARTH_RUNE, 2),
                            IdBuilder.itemResource(ItemID.WATER_RUNE, 4))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 60)))));

            AddRecipe(new Recipe("Cast Telekinetic Grab",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 4300))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                            IdBuilder.itemResource(ItemID.AIR_RUNE, 2))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 33)))));

            AddRecipe(new Recipe("Cast Telekinetic Grab on Wine of Zamorak",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 4300),
                            IdBuilder.itemResource(ItemID.WINE_OF_ZAMORAK, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.LAW_RUNE, 1),
                            IdBuilder.itemResource(ItemID.AIR_RUNE, 2))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 33)))));
        }

        // Orbs
        {
            AddRecipe(new Recipe("Cast Charge Water Orb",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 5600),
                            IdBuilder.itemResource(ItemID.WATER_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COSMIC_RUNE, 3),
                            IdBuilder.itemResource(ItemID.WATER_RUNE, 30),
                            IdBuilder.itemResource(ItemID.UNPOWERED_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 56)))));

            AddRecipe(new Recipe("Cast Charge Earth Orb",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 7000),
                            IdBuilder.itemResource(ItemID.EARTH_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COSMIC_RUNE, 3),
                            IdBuilder.itemResource(ItemID.EARTH_RUNE, 30),
                            IdBuilder.itemResource(ItemID.UNPOWERED_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 60)))));

            AddRecipe(new Recipe("Cast Charge Fire Orb",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 7300),
                            IdBuilder.itemResource(ItemID.FIRE_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COSMIC_RUNE, 3),
                            IdBuilder.itemResource(ItemID.FIRE_RUNE, 30),
                            IdBuilder.itemResource(ItemID.UNPOWERED_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 63)))));

            AddRecipe(new Recipe("Cast Charge Air Orb",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.xpResource(Skill.MAGIC, 7600),
                            IdBuilder.itemResource(ItemID.AIR_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(ItemID.COSMIC_RUNE, 3),
                            IdBuilder.itemResource(ItemID.AIR_RUNE, 30),
                            IdBuilder.itemResource(ItemID.UNPOWERED_ORB, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.levelResource(Skill.MAGIC, 66)))));
        }

        // Enchants
        {
            class Enchantable
            {
                int myTarget;
                int myResult;

                public Enchantable(int aTarget, int aResult)
                {
                    myTarget = aTarget;
                    myResult = aResult;
                }
            }

            // level 1
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.SAPPHIRE_RING, ItemID.RING_OF_RECOIL),
                                new Enchantable(ItemID.SAPPHIRE_BRACELET, ItemID.BRACELET_OF_CLAY),
                                new Enchantable(ItemID.SAPPHIRE_NECKLACE, ItemID.GAMES_NECKLACE8),
                                new Enchantable(ItemID.SAPPHIRE_AMULET, ItemID.AMULET_OF_MAGIC),
                                new Enchantable(ItemID.SILVTHRILL_ROD, ItemID.SILVTHRILL_ROD_7638),
                                new Enchantable(ItemID.OPAL_RING, ItemID.RING_OF_PURSUIT),
                                new Enchantable(ItemID.OPAL_BRACELET, ItemID.EXPEDITIOUS_BRACELET),
                                new Enchantable(ItemID.OPAL_NECKLACE, ItemID.DODGY_NECKLACE),
                                new Enchantable(ItemID.OPAL_AMULET, ItemID.AMULET_OF_BOUNTY),

                        })
                {
                    AddRecipe(new Recipe("Cast Lv1 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1750),
                                    IdBuilder.itemResource(ench.myResult, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 1),
                                    IdBuilder.itemResource(ench.myTarget, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 7)))));
                }
            }

            // Level 2
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.EMERALD_RING, ItemID.RING_OF_DUELING8),
                                new Enchantable(ItemID.EMERALD_NECKLACE, ItemID.BINDING_NECKLACE),
                                new Enchantable(ItemID.EMERALD_BRACELET, ItemID.CASTLE_WARS_BRACELET3),
                                new Enchantable(ItemID.EMERALD_AMULET, ItemID.AMULET_OF_DEFENCE),
                                new Enchantable(ItemID.PRENATURE_AMULET, ItemID.AMULET_OF_NATURE),
                                new Enchantable(ItemID.EMERALD_SICKLE_B, ItemID.ENCHANTED_EMERALD_SICKLE_B),
                                new Enchantable(ItemID.JADE_RING, ItemID.RING_OF_RETURNING5),
                                new Enchantable(ItemID.JADE_BRACELET, ItemID.FLAMTAER_BRACELET),
                                new Enchantable(ItemID.JADE_NECKLACE, ItemID.NECKLACE_OF_PASSAGE5),
                                new Enchantable(ItemID.JADE_AMULET, ItemID.AMULET_OF_CHEMISTRY),

                        })
                {
                    AddRecipe(new Recipe("Cast Lv2 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 1750),
                                    IdBuilder.itemResource(ench.myResult, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                    IdBuilder.itemResource(ench.myTarget, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 27)))));
                }
            }

            // Level 3
            {

                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.RUBY_RING, ItemID.RING_OF_FORGING),
                                new Enchantable(ItemID.RUBY_BRACELET, ItemID.INOCULATION_BRACELET),
                                new Enchantable(ItemID.RUBY_AMULET, ItemID.AMULET_OF_STRENGTH),
                                new Enchantable(ItemID.RUBY_SICKLE_B, ItemID.ENCHANTED_RUBY_SICKLE_B),
                                new Enchantable(ItemID.TOPAZ_RING, ItemID.EFARITAYS_AID),
                                new Enchantable(ItemID.TOPAZ_BRACELET, ItemID.BRACELET_OF_SLAUGHTER),
                                new Enchantable(ItemID.TOPAZ_NECKLACE, ItemID.NECKLACE_OF_FAITH),
                                new Enchantable(ItemID.TOPAZ_AMULET, ItemID.BURNING_AMULET5),

                        })
                {
                    AddRecipe(new Recipe("Cast Lv3 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 5900),
                                    IdBuilder.itemResource(ench.myResult, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.FIRE_RUNE, 5),
                                    IdBuilder.itemResource(ench.myTarget, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 49)))));
                }

                AddRecipe(new Recipe("Cast Lv3 Enchant on " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_NECKLACE).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 5900),
                                IdBuilder.itemResource(ItemID.DIGSITE_PENDANT_5, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 5),
                                IdBuilder.itemResource(ItemID.RUBY_NECKLACE, 1))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 49),
                                IdBuilder.questResource(Quest.THE_DIG_SITE)))));
            }

            // Level 4
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.DIAMOND_RING, ItemID.RING_OF_LIFE),
                                new Enchantable(ItemID.DIAMOND_NECKLACE, ItemID.PHOENIX_NECKLACE),
                                new Enchantable(ItemID.DIAMOND_BRACELET, ItemID.ABYSSAL_BRACELET5),
                                new Enchantable(ItemID.DIAMOND_AMULET, ItemID.AMULET_OF_POWER)
                        })
                {
                    AddRecipe(new Recipe("Cast Lv4 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 6700),
                                    IdBuilder.itemResource(ench.myResult, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 10),
                                    IdBuilder.itemResource(ench.myTarget, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 57)))));
                }
            }

            // Level 5
            {
                for(Enchantable ench : new Enchantable[]
                        {
                                new Enchantable(ItemID.DRAGONSTONE_RING, ItemID.RING_OF_WEALTH),
                                new Enchantable(ItemID.DRAGON_NECKLACE, ItemID.SKILLS_NECKLACE),
                                new Enchantable(ItemID.DRAGONSTONE_BRACELET, ItemID.COMBAT_BRACELET),
                                new Enchantable(ItemID.DRAGONSTONE_AMULET, ItemID.AMULET_OF_GLORY)
                        })
                {
                    AddRecipe(new Recipe("Cast Lv5 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.xpResource(Skill.MAGIC, 7800),
                                    IdBuilder.itemResource(ench.myResult, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                    IdBuilder.itemResource(ItemID.EARTH_RUNE, 15),
                                    IdBuilder.itemResource(ItemID.WATER_RUNE, 15),
                                    IdBuilder.itemResource(ench.myTarget, 1))),
                            new ArrayList<>(Arrays.asList(
                                    IdBuilder.levelResource(Skill.MAGIC, 68)))));
                }

                // Level 6
                {
                    for (Enchantable ench : new Enchantable[]
                            {
                                    new Enchantable(ItemID.ONYX_RING, ItemID.RING_OF_STONE),
                                    new Enchantable(ItemID.ONYX_NECKLACE, ItemID.BERSERKER_NECKLACE),
                                    new Enchantable(ItemID.ONYX_BRACELET, ItemID.REGEN_BRACELET),
                                    new Enchantable(ItemID.ONYX_AMULET, ItemID.AMULET_OF_FURY)
                            }) {
                        AddRecipe(new Recipe("Cast Lv6 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.xpResource(Skill.MAGIC, 9700),
                                        IdBuilder.itemResource(ench.myResult, 1))),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                        IdBuilder.itemResource(ItemID.EARTH_RUNE, 20),
                                        IdBuilder.itemResource(ItemID.FIRE_RUNE, 20),
                                        IdBuilder.itemResource(ench.myTarget, 1))),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.levelResource(Skill.MAGIC, 87)))));
                    }
                }

                // Level 7
                {
                    for (Enchantable ench : new Enchantable[]
                            {
                                    new Enchantable(ItemID.ZENYTE_RING, ItemID.RING_OF_SUFFERING),
                                    new Enchantable(ItemID.ZENYTE_NECKLACE, ItemID.NECKLACE_OF_ANGUISH),
                                    new Enchantable(ItemID.ZENYTE_BRACELET, ItemID.TORMENTED_BRACELET),
                                    new Enchantable(ItemID.ZENYTE_AMULET, ItemID.AMULET_OF_TORTURE)
                            }) {
                        AddRecipe(new Recipe("Cast Lv7 Enchant on " + myPlugin.myItemManager.getItemComposition(ench.myTarget).getName(),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.xpResource(Skill.MAGIC, 11000),
                                        IdBuilder.itemResource(ench.myResult, 1))),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                        IdBuilder.itemResource(ItemID.BLOOD_RUNE, 20),
                                        IdBuilder.itemResource(ItemID.SOUL_RUNE, 20),
                                        IdBuilder.itemResource(ench.myTarget, 1))),
                                new ArrayList<>(Arrays.asList(
                                        IdBuilder.levelResource(Skill.MAGIC, 93)))));
                    }
                }
            }
        }

        // Enchant bolts
        {
            // opal
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.OPAL_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 900),
                                IdBuilder.itemResource(ItemID.OPAL_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                IdBuilder.itemResource(ItemID.OPAL_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 4)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.OPAL_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 900),
                                IdBuilder.itemResource(ItemID.OPAL_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 2),
                                IdBuilder.itemResource(ItemID.OPAL_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 4)))));
            }

            // Sapphire
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.SAPPHIRE_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 1750),
                                IdBuilder.itemResource(ItemID.SAPPHIRE_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 1),
                                IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                IdBuilder.itemResource(ItemID.SAPPHIRE_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 7)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.SAPPHIRE_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 1750),
                                IdBuilder.itemResource(ItemID.SAPPHIRE_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 1),
                                IdBuilder.itemResource(ItemID.MIND_RUNE, 1),
                                IdBuilder.itemResource(ItemID.SAPPHIRE_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 7)))));
            }

            // Jade
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.JADE_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 1900),
                                IdBuilder.itemResource(ItemID.JADE_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 2),
                                IdBuilder.itemResource(ItemID.JADE_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 14)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.JADE_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 1900),
                                IdBuilder.itemResource(ItemID.JADE_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 2),
                                IdBuilder.itemResource(ItemID.JADE_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 14)))));
            }

            // Pearl
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.PEARL_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 2900),
                                IdBuilder.itemResource(ItemID.PEARL_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 2),
                                IdBuilder.itemResource(ItemID.PEARL_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 24)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.PEARL_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 2900),
                                IdBuilder.itemResource(ItemID.PEARL_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.WATER_RUNE, 2),
                                IdBuilder.itemResource(ItemID.PEARL_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 24)))));
            }

            // Emerald
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.EMERALD_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3700),
                                IdBuilder.itemResource(ItemID.EMERALD_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EMERALD_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 27)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.EMERALD_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3700),
                                IdBuilder.itemResource(ItemID.EMERALD_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.AIR_RUNE, 3),
                                IdBuilder.itemResource(ItemID.NATURE_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EMERALD_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 27)))));
            }

            // Red Topaz
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.TOPAZ_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3300),
                                IdBuilder.itemResource(ItemID.TOPAZ_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 2),
                                IdBuilder.itemResource(ItemID.TOPAZ_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 29)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.TOPAZ_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 3300),
                                IdBuilder.itemResource(ItemID.TOPAZ_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 2),
                                IdBuilder.itemResource(ItemID.TOPAZ_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 29)))));
            }

            // Ruby
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 5900),
                                IdBuilder.itemResource(ItemID.RUBY_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 5),
                                IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                IdBuilder.itemResource(ItemID.RUBY_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 49)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.RUBY_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 5900),
                                IdBuilder.itemResource(ItemID.RUBY_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 5),
                                IdBuilder.itemResource(ItemID.BLOOD_RUNE, 1),
                                IdBuilder.itemResource(ItemID.RUBY_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 49)))));
            }

            // Diamond
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DIAMOND_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 6700),
                                IdBuilder.itemResource(ItemID.DIAMOND_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 10),
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.DIAMOND_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 57)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DIAMOND_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 6700),
                                IdBuilder.itemResource(ItemID.DIAMOND_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 10),
                                IdBuilder.itemResource(ItemID.LAW_RUNE, 2),
                                IdBuilder.itemResource(ItemID.DIAMOND_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 57)))));
            }

            // DragonStone
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DRAGONSTONE_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 7800),
                                IdBuilder.itemResource(ItemID.DRAGONSTONE_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 15),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 1),
                                IdBuilder.itemResource(ItemID.DRAGONSTONE_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 68)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.DRAGONSTONE_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 7800),
                                IdBuilder.itemResource(ItemID.DRAGONSTONE_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.EARTH_RUNE, 15),
                                IdBuilder.itemResource(ItemID.SOUL_RUNE, 1),
                                IdBuilder.itemResource(ItemID.DRAGONSTONE_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 68)))));
            }

            // Onyx
            {
                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.ONYX_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 9700),
                                IdBuilder.itemResource(ItemID.ONYX_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 20),
                                IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                IdBuilder.itemResource(ItemID.ONYX_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 87)))));

                AddRecipe(new Recipe("Cast Enchant Crossbow Bolt " + myPlugin.myItemManager.getItemComposition(ItemID.ONYX_DRAGON_BOLTS).getName(),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.xpResource(Skill.MAGIC, 9700),
                                IdBuilder.itemResource(ItemID.ONYX_DRAGON_BOLTS_E, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.itemResource(ItemID.COSMIC_RUNE, 1),
                                IdBuilder.itemResource(ItemID.FIRE_RUNE, 20),
                                IdBuilder.itemResource(ItemID.DEATH_RUNE, 1),
                                IdBuilder.itemResource(ItemID.ONYX_DRAGON_BOLTS, 10))),
                        new ArrayList<>(Arrays.asList(
                                IdBuilder.levelResource(Skill.MAGIC, 87)))));
            }
        }
    }

    private void LoadSmithing()
    {
        Map<String, Costs> barCosts = new HashMap<>();

        barCosts.put("DAGGER", new Costs(1,1));
        barCosts.put("AXE", new Costs(1,1));
        barCosts.put("MACE", new Costs(1,1));
        barCosts.put("MED_HELM", new Costs(1,1));
        barCosts.put("BOLTS_UNF", new Costs(1,10));
        barCosts.put("SWORD", new Costs(1,1));
        barCosts.put("DART_TIP", new Costs(1,10));
        barCosts.put("WIRE", new Costs(1,1));
        barCosts.put("NAILS", new Costs(1,15));
        barCosts.put("ARROWTIPS", new Costs(1,15));
        barCosts.put("SCIMITAR", new Costs(2,1));
        barCosts.put("HASTA", new Costs(1,1));
        barCosts.put("SPEAR", new Costs(1,1));
        barCosts.put("JAVELIN_HEADS", new Costs(1,5));
        barCosts.put("LIMBS", new Costs(1,1));
        barCosts.put("KNIFE", new Costs(1,5));
        barCosts.put("FULL_HELM", new Costs(2,1));
        barCosts.put("SQ_SHIELD", new Costs(2,1));
        barCosts.put("WARHAMMER", new Costs(3,1));
        barCosts.put("LONGSWORD", new Costs(2,1));
        barCosts.put("BATTLEAXE", new Costs(3,1));
        barCosts.put("CHAINBODY", new Costs(3,1));
        barCosts.put("KITESHIELD", new Costs(3,1));
        barCosts.put("CLAWS", new Costs(2,1));
        barCosts.put("2H_SWORD", new Costs(3,1));
        barCosts.put("PLATELEGS", new Costs(3,1));
        barCosts.put("PLATESKIRT", new Costs(3,1));
        barCosts.put("PLATEBODY", new Costs(5,1));
        barCosts.put("SPIT", new Costs(1,1));
        barCosts.put("STUDS", new Costs(1,1));
        barCosts.put("GRAPPLE_TIP", new Costs(1,1));
        barCosts.put("LANTERN_UNF", new Costs(1,1));
        barCosts.put("LANTERN_FRAME", new Costs(1,1));

        Map<String, Integer> metals = new HashMap<>();

        metals.put("BRONZE", ItemID.BRONZE_BAR);
        metals.put("BLURITE", ItemID.BLURITE_BAR);
        metals.put("IRON", ItemID.IRON_BAR);
        metals.put("SILVER", ItemID.SILVER_BAR);
        metals.put("STEEL", ItemID.STEEL_BAR);
        metals.put("MITHRIL", ItemID.MITHRIL_BAR);
        metals.put("MITH", ItemID.MITHRIL_BAR);
        metals.put("ADAMANT", ItemID.ADAMANTITE_BAR);
        metals.put("ADAMANTITE", ItemID.ADAMANTITE_BAR);
        metals.put("RUNE", ItemID.RUNITE_BAR);
        metals.put("RUNITE", ItemID.RUNITE_BAR);
        metals.put("BULLSEYE", ItemID.RUNITE_BAR);
        metals.put("OIL", ItemID.RUNITE_BAR);

        String[] ignored = new String[]{
                "BARRONITE_DEPOSITS",
                "CANNONBALL",
                "GOLD_BAR_GOLDSMITH_GAUNTLETS",
                "DRAGON_SQ_SHIELD",
                "DRAGONFIRE_SHIELD",
        };

        for(SmithingAction action : SmithingAction.values())
        {
            if (Arrays.asList(ignored).contains(action.name()))
            {
                continue;
            }

            int del = action.name().indexOf("_");
            if (del == -1)
            {
                TodoPlugin.debug("Weird action [" + action.name() + "]");
                continue;
            }

            String metal = action.name().substring(0, del);
            String type = action.name().substring(del + 1);

            if (type.equals("BAR"))
                continue;

            Costs costs = barCosts.get(type);
            if (costs == null)
            {
                TodoPlugin.debug("Missing bar cost for " + action.name());
                continue;
            }

            Integer barId = metals.get(metal);
            if (barId == null)
            {
                TodoPlugin.debug("Missing bar type for " + action.name());
                continue;
            }

            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, (int)action.getXp() * 100));
            products.add(IdBuilder.itemResource(action.getItemId(), costs.myResult));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(barId, costs.myCost));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.itemResource(ItemID.HAMMER, 1));
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, action.getLevel()));

            AddRecipe(new Recipe("Smith " + action.getName(myPlugin.myItemManager), products, resources, requirements));
        }

        //Bars in furnace
        for(BarCraft bar : new BarCraft[] {
                new BarCraft(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 1370, 20),
                new BarCraft(ItemID.ELEMENTAL_METAL, ItemID.ELEMENTAL_ORE, 4, 750, 20),
                new BarCraft(ItemID.STEEL_BAR, ItemID.IRON_ORE, 2, 1750, 30),
                new BarCraft(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 2250, 40),
                new BarCraft(ItemID.LOVAKITE_BAR, ItemID.LOVAKITE_ORE, 2, 2000, 45),
                new BarCraft(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 3000, 50),
                new BarCraft(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 50, 70),
                new BarCraft(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 5000, 85)
        })
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, bar.myXP));
            products.add(IdBuilder.itemResource(bar.myBarId, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(bar.myOreId, 1));
            resources.add(IdBuilder.itemResource(ItemID.COAL, bar.myCoalAmount));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, bar.myLevel));

            AddRecipe(new Recipe("Smelt " + myPlugin.myItemManager.getItemComposition(bar.myBarId).getMembersName(),
                    products, resources, requirements));
        }

        //bronze normally
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 620));
            products.add(IdBuilder.itemResource(ItemID.BRONZE_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.TIN_ORE, 1));
            resources.add(IdBuilder.itemResource(ItemID.COPPER_ORE, 1));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 1));

            AddRecipe(new Recipe("Smelt Bronze Bar", products, resources, requirements));
        }

        //bronze in blast furnace
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 620));
            products.add(IdBuilder.itemResource(ItemID.BRONZE_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.TIN_ORE, 1));
            resources.add(IdBuilder.itemResource(ItemID.COPPER_ORE, 1));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 1));
            requirements.add(IdBuilder.questResource(Quest.THE_GIANT_DWARF));

            AddRecipe(new Recipe("Smelt Bronze Bar at Blast Furnace", products, resources, requirements));
        }

        //Blurite
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 800));
            products.add(IdBuilder.itemResource(ItemID.BLURITE_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.BLURITE_ORE, 1));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 13));

            AddRecipe(new Recipe("Smelt Blurite Bar", products, resources, requirements));
        }

        //iron normally
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 1250));
            products.add(IdBuilder.itemResource(ItemID.IRON_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.IRON_ORE, 2));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 15));

            AddRecipe(new Recipe("Smelt Iron Bar", products, resources, requirements));
        }

        //Bars in blast furnace
        for(BarCraft bar : new BarCraft[] {
                new BarCraft(ItemID.SILVER_BAR, ItemID.SILVER_ORE, 0, 1370, 20),
                new BarCraft(ItemID.IRON_BAR, ItemID.IRON_ORE, 0, 1250, 15),
                new BarCraft(ItemID.STEEL_BAR, ItemID.IRON_ORE, 2, 1750, 30),
                new BarCraft(ItemID.GOLD_BAR, ItemID.GOLD_ORE, 0, 2250, 40),
                new BarCraft(ItemID.MITHRIL_BAR, ItemID.MITHRIL_ORE, 4, 3000, 50),
                new BarCraft(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE, 6, 3750, 70),
                new BarCraft(ItemID.RUNITE_BAR, ItemID.RUNITE_ORE, 8, 5000, 85)
        })
        {
            {
                List<Resource> products = new ArrayList<>();
                products.add(IdBuilder.xpResource(Skill.SMITHING, bar.myXP));
                products.add(IdBuilder.itemResource(bar.myBarId, 1));

                List<Resource> resources = new ArrayList<>();
                resources.add(IdBuilder.itemResource(bar.myOreId, 1));
                resources.add(IdBuilder.itemResource(ItemID.COAL, bar.myCoalAmount / 2));

                List<Resource> requirements = new ArrayList<>();
                requirements.add(IdBuilder.levelResource(Skill.SMITHING, bar.myLevel));
                requirements.add(IdBuilder.questResource(Quest.THE_GIANT_DWARF));

                AddRecipe(new Recipe("Smelt " + myPlugin.myItemManager.getItemComposition(bar.myBarId).getMembersName() + " at blast furnace",
                        products, resources, requirements));
            }
        }

        //goldsmith
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 5620));
            products.add(IdBuilder.itemResource(ItemID.GOLD_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.GOLD_ORE, 1));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 40));
            requirements.add(IdBuilder.itemResource(ItemID.GOLDSMITH_GAUNTLETS, 1));

            AddRecipe(new Recipe("Smelt Gold Bar with Goldsmith Gauntlets", products, resources, requirements));
        }

        //goldsmith at blast
        {
            List<Resource> products = new ArrayList<>();
            products.add(IdBuilder.xpResource(Skill.SMITHING, 5620));
            products.add(IdBuilder.itemResource(ItemID.GOLD_BAR, 1));

            List<Resource> resources = new ArrayList<>();
            resources.add(IdBuilder.itemResource(ItemID.GOLD_ORE, 1));

            List<Resource> requirements = new ArrayList<>();
            requirements.add(IdBuilder.levelResource(Skill.SMITHING, 40));
            requirements.add(IdBuilder.itemResource(ItemID.GOLDSMITH_GAUNTLETS, 1));
            requirements.add(IdBuilder.questResource(Quest.THE_GIANT_DWARF));

            AddRecipe(new Recipe("Smelt Gold Bar with Goldsmith Gauntlets at Blast Furnace", products, resources, requirements));
        }
    }

    private void LoadCrafting()
    {
        // spinning wheel
        {
            class Spinnable
            {
                int myItem;
                int myResult;
                int myLevel;
                int myXp;
                public Spinnable(int aItem, int aResult,int aLevel, int aXP)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXp = aXP;
                }
            }


            for (Spinnable spin : new Spinnable[]{
                    new Spinnable(ItemID.WOOL, ItemID.BALL_OF_WOOL, 1, 250),
                    new Spinnable(ItemID.FLAX, ItemID.BOW_STRING, 10, 1500),
                    new Spinnable(ItemID.SINEW, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.OAK_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.WILLOW_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.MAPLE_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.YEW_ROOTS, ItemID.CROSSBOW_STRING, 10, 1500),
                    new Spinnable(ItemID.MAGIC_ROOTS, ItemID.MAGIC_STRING, 19, 3000),
                    new Spinnable(ItemID.HAIR, ItemID.ROPE, 30, 2500),

            })
            {
                AddRecipe(new Recipe("Spin " + myPlugin.myItemManager.getItemComposition(spin.myItem).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, spin.myXp),
                                IdBuilder.itemResource(spin.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(spin.myItem, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, spin.myLevel)
                        )));
            }
        }

        // Loom
        {
            class Weaveable
            {
                int myItem;
                int myItemAmount;
                int myResult;
                int myLevel;
                int myXp;
                public Weaveable(int aItem, int aItemAmount, int aResult,int aLevel, int aXP)
                {
                    myItem = aItem;
                    myItemAmount = aItemAmount;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXp = aXP;
                }
            }

            for (Weaveable weave : new Weaveable[]{
                    new Weaveable(ItemID.BALL_OF_WOOL, 2, ItemID.STRIP_OF_CLOTH, 10, 1200),
                    new Weaveable(ItemID.JUTE_FIBRE, 4, ItemID.STRIP_OF_CLOTH, 21, 3800),
                    new Weaveable(ItemID.JUTE_FIBRE, 2, ItemID.DRIFT_NET, 26, 5500),
                    new Weaveable(ItemID.BASKET, 2, ItemID.STRIP_OF_CLOTH, 36, 5600)
            })
            {
                AddRecipe(new Recipe("Weave " + myPlugin.myItemManager.getItemComposition(weave.myItem).getName() + " into " + myPlugin.myItemManager.getItemComposition(weave.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, weave.myXp),
                                IdBuilder.itemResource(weave.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(weave.myItem, weave.myItemAmount)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, weave.myLevel)
                        )));
            }
        }

        // Pottery wheel
        {
            class Pottery
            {
                int myIntermediate;
                int myResult;
                int myLevel;
                int myFirstXp;
                int mySecondXp;
                public Pottery(int aIntermediate, int aResult, int aLevel, int aFirstXP, int aSecondXP)
                {
                    myIntermediate = aIntermediate;
                    myResult = aResult;
                    myLevel = aLevel;
                    myFirstXp = aFirstXP;
                    mySecondXp = aSecondXP;
                }
            }

            for (Pottery pot : new Pottery[]{
                    new Pottery(ItemID.UNFIRED_POT, ItemID.POT, 1, 630, 630),
                    new Pottery(ItemID.UNFIRED_PIE_DISH, ItemID.PIE_DISH, 7, 1500, 1000),
                    new Pottery(ItemID.UNFIRED_BOWL, ItemID.BOWL, 8, 1800, 1500),
                    new Pottery(ItemID.UNFIRED_PLANT_POT, ItemID.PLANT_POT, 19, 2000, 1750)
            })
            {
                AddRecipe(new Recipe("Shape Soft clay into " + myPlugin.myItemManager.getItemComposition(pot.myIntermediate).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, pot.myFirstXp),
                                IdBuilder.itemResource(pot.myIntermediate, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.SOFT_CLAY, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, pot.myLevel)
                        )));

                AddRecipe(new Recipe("Fire " + myPlugin.myItemManager.getItemComposition(pot.myIntermediate).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, pot.mySecondXp),
                                IdBuilder.itemResource(pot.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(pot.myIntermediate, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, pot.myLevel)
                        )));
            }

            {
                AddRecipe(new Recipe("Shape Soft clay into " + myPlugin.myItemManager.getItemComposition(ItemID.UNFIRED_POT_LID).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, 2000),
                                IdBuilder.itemResource(ItemID.UNFIRED_POT_LID, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.SOFT_CLAY, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, 25),
                                IdBuilder.questResource(Quest.ONE_SMALL_FAVOUR)
                        )));

                AddRecipe(new Recipe("Fire " + myPlugin.myItemManager.getItemComposition(ItemID.UNFIRED_POT_LID).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, 2000),
                                IdBuilder.itemResource(ItemID.POT_LID, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.UNFIRED_POT_LID, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, 25),
                                IdBuilder.questResource(Quest.ONE_SMALL_FAVOUR)
                        )));
            }
        }

        // Sewing
        {
            class Sewable
            {
                int myItem;
                int myItemsNeeded;
                int myResult;
                int myLevel;
                int myXP;
                public Sewable(int aItem, int aItemsNeeded, int aResult, int aLevel, int aXP)
                {
                    myItem = aItem;
                    myItemsNeeded = aItemsNeeded;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Sewable sew : new Sewable[]{
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_GLOVES, 1, 1380),
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_BOOTS, 7, 1620),
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_COWL, 9, 1850),
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_VAMBRACES, 14, 2200),
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_BODY, 11, 2500),
                            new Sewable(ItemID.LEATHER, 1, ItemID.LEATHER_CHAPS, 18, 2700),
                            new Sewable(ItemID.HARD_LEATHER, 1, ItemID.LEATHER_GLOVES, 28, 3500),
                            new Sewable(ItemID.LEATHER, 1, ItemID.COIF, 38, 3700),

                            new Sewable(ItemID.GREEN_DRAGON_LEATHER, 1, ItemID.GREEN_DHIDE_VAMBRACES, 57, 6200),
                            new Sewable(ItemID.GREEN_DRAGON_LEATHER, 2, ItemID.GREEN_DHIDE_CHAPS, 60, 12400),
                            new Sewable(ItemID.GREEN_DRAGON_LEATHER, 3, ItemID.GREEN_DHIDE_BODY, 62, 18600),

                            new Sewable(ItemID.BLUE_DRAGON_LEATHER, 1, ItemID.BLUE_DHIDE_VAMBRACES, 66, 7000),
                            new Sewable(ItemID.BLUE_DRAGON_LEATHER, 2, ItemID.BLUE_DHIDE_CHAPS, 68, 14000),
                            new Sewable(ItemID.BLUE_DRAGON_LEATHER, 3, ItemID.BLUE_DHIDE_BODY, 71, 21000),

                            new Sewable(ItemID.RED_DRAGON_LEATHER, 1, ItemID.RED_DHIDE_VAMBRACES, 73, 7800),
                            new Sewable(ItemID.RED_DRAGON_LEATHER, 2, ItemID.RED_DHIDE_CHAPS, 75, 15600),
                            new Sewable(ItemID.RED_DRAGON_LEATHER, 3, ItemID.RED_DHIDE_BODY, 77, 23400),

                            new Sewable(ItemID.BLACK_DRAGON_LEATHER, 1, ItemID.BLACK_DHIDE_VAMBRACES, 79, 8600),
                            new Sewable(ItemID.BLACK_DRAGON_LEATHER, 2, ItemID.BLACK_DHIDE_CHAPS, 82, 17200),
                            new Sewable(ItemID.BLACK_DRAGON_LEATHER, 3, ItemID.BLACK_DHIDE_BODY, 84, 25800),

                            new Sewable(ItemID.SNAKESKIN, 6, ItemID.SNAKESKIN_BOOTS, 45, 3000),
                            new Sewable(ItemID.SNAKESKIN, 8, ItemID.SNAKESKIN_VAMBRACES, 47, 3500),
                            new Sewable(ItemID.SNAKESKIN, 5, ItemID.SNAKESKIN_BANDANA, 48, 4500),
                            new Sewable(ItemID.SNAKESKIN, 12, ItemID.SNAKESKIN_CHAPS, 51, 5000),
                            new Sewable(ItemID.SNAKESKIN, 15, ItemID.SNAKESKIN_BODY, 53, 5500),

                            new Sewable(ItemID.XERICIAN_FABRIC, 3, ItemID.XERICIAN_HAT, 14, 6600),
                            new Sewable(ItemID.XERICIAN_FABRIC, 4, ItemID.XERICIAN_ROBE, 17, 8800),
                            new Sewable(ItemID.XERICIAN_FABRIC, 5, ItemID.XERICIAN_TOP, 22, 11000),
                    })
            {
                AddRecipe(new Recipe("Sew " + myPlugin.myItemManager.getItemComposition(sew.myItem).getName() + " into " + myPlugin.myItemManager.getItemComposition(sew.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, sew.myXP),
                                IdBuilder.itemResource(sew.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(sew.myItem, sew.myItemsNeeded)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, sew.myLevel),
                                IdBuilder.itemResource(ItemID.NEEDLE, 1)
                        )));
            }
            {
                AddRecipe(new Recipe("Sew " + myPlugin.myItemManager.getItemComposition(ItemID.CURED_YAKHIDE).getName() + " into " + myPlugin.myItemManager.getItemComposition(ItemID.YAKHIDE_ARMOUR).getName() + " (top)",
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, 3200),
                                IdBuilder.itemResource(ItemID.YAKHIDE_ARMOUR, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.CURED_YAKHIDE, 2)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, 46),
                                IdBuilder.itemResource(ItemID.NEEDLE, 1)
                        )));
            }
            {
                AddRecipe(new Recipe("Sew " + myPlugin.myItemManager.getItemComposition(ItemID.CURED_YAKHIDE).getName() + " into " + myPlugin.myItemManager.getItemComposition(ItemID.YAKHIDE_ARMOUR).getName() + " (legs)",
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, 3200),
                                IdBuilder.itemResource(ItemID.YAKHIDE_ARMOUR_10824, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.CURED_YAKHIDE, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, 43),
                                IdBuilder.itemResource(ItemID.NEEDLE, 1)
                        )));
            }


        }

        // Shields
        {
            class Shield
            {
                int myItem;
                int myNailType;
                int myHideType;
                int myResult;
                int myLevel;
                int myXP;
                public Shield(int aItem, int aNailType, int aHideType, int aResult, int aLevel, int aXP)
                {
                    myItem = aItem;
                    myNailType = aNailType;
                    myHideType = aHideType;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Shield shield : new Shield[]{
                new Shield(ItemID.OAK_SHIELD, ItemID.BRONZE_NAILS, ItemID.HARD_LEATHER, ItemID.HARD_LEATHER_SHIELD, 41, 7000),
                new Shield(ItemID.MAPLE_SHIELD, ItemID.STEEL_NAILS, ItemID.GREEN_DRAGON_LEATHER, ItemID.GREEN_DHIDE_SHIELD, 62, 12400),
                new Shield(ItemID.YEW_SHIELD, ItemID.MITHRIL_NAILS, ItemID.BLUE_DRAGON_LEATHER, ItemID.BLUE_DHIDE_SHIELD, 69, 14000),
                new Shield(ItemID.MAGIC_SHIELD, ItemID.ADAMANTITE_NAILS, ItemID.RED_DRAGON_LEATHER, ItemID.RED_DHIDE_SHIELD, 76, 15600),
                new Shield(ItemID.REDWOOD_SHIELD, ItemID.RUNE_NAILS, ItemID.BLACK_DRAGON_LEATHER, ItemID.BLACK_DHIDE_SHIELD, 83, 17200),
                new Shield(ItemID.WILLOW_SHIELD, ItemID.IRON_NAILS, ItemID.SNAKESKIN, ItemID.SNAKESKIN_SHIELD, 56, 10000)
            })
            {
                AddRecipe(new Recipe("Make " + myPlugin.myItemManager.getItemComposition(shield.myItem).getName() + " into " + myPlugin.myItemManager.getItemComposition(shield.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, shield.myXP),
                                IdBuilder.itemResource(shield.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(shield.myItem, 1),
                                IdBuilder.itemResource(shield.myNailType, 15),
                                IdBuilder.itemResource(shield.myHideType, 2)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, shield.myLevel),
                                IdBuilder.itemResource(ItemID.HAMMER, 1)
                        )));
            }

            class BroodooShield
            {
                int myItem;
                int myResult;
                int myLevel;
                int myXP;
                String mySuffix;
                public BroodooShield(int aItem, int aResult, int aLevel, int aXP, String aSuffix)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                    mySuffix = aSuffix;
                }
            }

            for(BroodooShield shield : new BroodooShield[]{
                    new BroodooShield(ItemID.TRIBAL_MASK,ItemID.BROODOO_SHIELD_10, 35, 100000, " (combat)"),
                    new BroodooShield(ItemID.TRIBAL_MASK_6337, ItemID.BROODOO_SHIELD_10_6237, 35, 100000, " (disease)"),
                    new BroodooShield(ItemID.TRIBAL_MASK_6339, ItemID.BROODOO_SHIELD_10_6259, 35, 100000, " (poison)"),
            })
            {
                for( int nails : new int[]{
                        ItemID.BRONZE_NAILS,
                        ItemID.IRON_NAILS,
                        ItemID.STEEL_NAILS,
                        ItemID.BLACK_NAILS,
                        ItemID.MITHRIL_NAILS,
                        ItemID.ADAMANTITE_NAILS,
                        ItemID.RUNE_NAILS
                })
                {
                    AddRecipe(new Recipe("Make " + myPlugin.myItemManager.getItemComposition(shield.myItem).getName() + shield.mySuffix + " into " + myPlugin.myItemManager.getItemComposition(shield.myResult).getName() + shield.mySuffix + " using " + myPlugin.myItemManager.getItemComposition(nails).getName(),
                            Arrays.asList(
                                    IdBuilder.xpResource(Skill.CRAFTING, shield.myXP),
                                    IdBuilder.itemResource(shield.myResult, 1)),
                            Arrays.asList(
                                    IdBuilder.itemResource(shield.myItem, 1),
                                    IdBuilder.itemResource(nails, 8),
                                    IdBuilder.itemResource(ItemID.SNAKESKIN, 2)),
                            Arrays.asList(
                                    IdBuilder.levelResource(Skill.CRAFTING, shield.myLevel),
                                    IdBuilder.itemResource(ItemID.HAMMER, 1)
                            )));
                }

            }

        }

        // Combining
        {
            class Combinable
            {
                int myFirstItem;
                int mySecondItem;
                int myResult;
                int myLevel;
                int myXP;
                public Combinable(int aFirstItem, int aSecondItem, int aResult, int aLevel, int aXP)
                {
                    myFirstItem = aFirstItem;
                    mySecondItem = aSecondItem;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Combinable combine : new Combinable[]{
                    new Combinable(ItemID.STEEL_STUDS, ItemID.LEATHER_BODY, ItemID.STUDDED_BODY, 41, 4000),
                    new Combinable(ItemID.STEEL_STUDS, ItemID.LEATHER_VAMBRACES, ItemID.STUDDED_CHAPS, 44, 4200),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.LEATHER_VAMBRACES, ItemID.SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.RED_DHIDE_VAMBRACES, ItemID.RED_SPIKY_VAMBRACES, 32, 580),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.GREEN_DHIDE_VAMBRACES, ItemID.GREEN_SPIKY_VAMBRACES, 32, 500),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.BLUE_DHIDE_VAMBRACES, ItemID.BLUE_SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.KEBBIT_CLAWS, ItemID.BLACK_DHIDE_VAMBRACES, ItemID.BLACK_SPIKY_VAMBRACES, 32, 600),
                    new Combinable(ItemID.EMPTY_OIL_LAMP, ItemID.OIL_LANTERN_FRAME, ItemID.OIL_LANTERN, 26, 5000),
                    new Combinable(ItemID.EMPTY_LIGHT_ORB, ItemID.CAVE_GOBLIN_WIRE, ItemID.LIGHT_ORB, 87, 10400),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.WATER_ORB, ItemID.WATER_BATTLESTAFF, 54, 10000),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.EARTH_ORB, ItemID.EARTH_BATTLESTAFF, 58, 11250),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.FIRE_ORB, ItemID.FIRE_BATTLESTAFF, 62, 12500),
                    new Combinable(ItemID.BATTLESTAFF, ItemID.AIR_ORB, ItemID.AIR_BATTLESTAFF, 66, 13750),
                })
            {
                AddRecipe(new Recipe("Combine " + myPlugin.myItemManager.getItemComposition(combine.myFirstItem).getName() + " with " + myPlugin.myItemManager.getItemComposition(combine.mySecondItem).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, combine.myXP),
                                IdBuilder.itemResource(combine.myResult, 1)
                        ),
                        Arrays.asList(
                                IdBuilder.itemResource(combine.myFirstItem, 1),
                                IdBuilder.itemResource(combine.mySecondItem, 1)
                        ),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, combine.myLevel)
                        )));
            }
        }

        // Chisel
        {
            class Chiselable
            {
                int myItem;
                int myResult;
                int myAmount;
                int myLevel;
                int myXP;
                String mySuffix;
                public Chiselable(int aItem, int aResult, int aAmount, int aLevel, int aXP, String aSuffix)
                {
                    myItem = aItem;
                    myResult = aResult;
                    myAmount = aAmount;
                    myLevel = aLevel;
                    myXP = aXP;
                    mySuffix = aSuffix;
                }
            }

            for(Chiselable chisel : new Chiselable[]{
                        new Chiselable(ItemID.BLOODNTAR_SNELM, ItemID.BLAMISH_RED_SHELL, 1, 15, 3250, " (round)"),
                        new Chiselable(ItemID.BLOODNTAR_SNELM_3339, ItemID.BLAMISH_RED_SHELL_3357, 1, 15, 3250, " (pointed)"),
                        new Chiselable(ItemID.BROKEN_BARK_SNELM, ItemID.BLAMISH_BARK_SHELL, 1, 15, 3250, ""),
                        new Chiselable(ItemID.BRUISE_BLUE_SNELM, ItemID.BLAMISH_BLUE_SHELL, 1, 15, 3250, " (round)"),
                        new Chiselable(ItemID.BRUISE_BLUE_SNELM_3343, ItemID.BLAMISH_BLUE_SHELL_3361, 1, 15, 3250, " (pointed)"),
                        new Chiselable(ItemID.MYRE_SNELM, ItemID.BLAMISH_MYRE_SHELL, 1, 15, 3250, " (round)"),
                        new Chiselable(ItemID.MYRE_SNELM_3337, ItemID.BLAMISH_MYRE_SHELL_3355, 1, 15, 3250, " (pointed)"),
                        new Chiselable(ItemID.OCHRE_SNELM, ItemID.BLAMISH_OCHRE_SHELL, 1, 15, 3250, " (round)"),
                        new Chiselable(ItemID.OCHRE_SNELM_3341, ItemID.BLAMISH_OCHRE_SHELL_3359, 1, 15, 3250, " (pointed)"),


                        new Chiselable(ItemID.FRESH_CRAB_SHELL, ItemID.CRAB_HELMET, 1, 15, 3250, ""),
                        new Chiselable(ItemID.FRESH_CRAB_CLAW, ItemID.CRAB_CLAW, 1, 15, 3250, ""),

                        new Chiselable(ItemID.UNCUT_OPAL, ItemID.OPAL, 1, 1, 1500, ""),
                        new Chiselable(ItemID.UNCUT_JADE, ItemID.JADE, 1, 13, 2000, ""),
                        new Chiselable(ItemID.UNCUT_RED_TOPAZ, ItemID.RED_TOPAZ, 1, 16, 2500, ""),
                        new Chiselable(ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE, 1, 20, 5000, ""),
                        new Chiselable(ItemID.UNCUT_EMERALD, ItemID.EMERALD, 1, 27, 6750, ""),
                        new Chiselable(ItemID.UNCUT_RUBY, ItemID.RUBY, 1, 34, 8500, ""),
                        new Chiselable(ItemID.UNCUT_DIAMOND, ItemID.DIAMOND, 1, 43, 10750, ""),
                        new Chiselable(ItemID.UNCUT_DRAGONSTONE, ItemID.DRAGONSTONE, 1, 55, 13750, ""),
                        new Chiselable(ItemID.UNCUT_ONYX, ItemID.ONYX, 1, 67, 16750, ""),
                        new Chiselable(ItemID.UNCUT_ZENYTE, ItemID.ZENYTE, 1, 89, 20000, "")
                    })
            {
                AddRecipe(new Recipe("Chisel " + myPlugin.myItemManager.getItemComposition(chisel.myItem).getName() + chisel.mySuffix,
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, chisel.myXP),
                                IdBuilder.itemResource(chisel.myResult, chisel.myAmount)),
                        Arrays.asList(
                                IdBuilder.itemResource(chisel.myItem, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, chisel.myLevel),
                                IdBuilder.itemResource(ItemID.CHISEL, 1)
                        )));
            }

            //new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_BOLT_TIPS, 15, 83, 6000, ""),
            //new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_ARROWTIPS, 15, 85, 6000, ""),
            //new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_JAVELIN_HEADS, 5, 87, 6000, ""),
            //new Chiselable(ItemID.AMETHYST, ItemID.AMETHYST_DART_TIP, 8, 89, 6000, "")
        }

        // Splitbark
        {
            class Splitbark
            {
                int myAmount;
                int myResult;
                int myLevel;
                int myXP;
                public Splitbark(int aAmount, int aResult, int aLevel, int aXP)
                {
                    myAmount = aAmount;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for (Splitbark bark : new Splitbark[]{
                    new Splitbark(1, ItemID.SPLITBARK_GAUNTLETS, 60, 6200),
                    new Splitbark(1, ItemID.SPLITBARK_BOOTS, 60, 6200),
                    new Splitbark(2, ItemID.SPLITBARK_HELM, 61, 12400),
                    new Splitbark(3, ItemID.SPLITBARK_LEGS, 62, 18600),
                    new Splitbark(4, ItemID.SPLITBARK_BODY, 62, 24800)
            })
            {
                AddRecipe(new Recipe("Sew " + myPlugin.myItemManager.getItemComposition(ItemID.FINE_CLOTH).getName() + " into " + myPlugin.myItemManager.getItemComposition(bark.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, bark.myXP),
                                IdBuilder.itemResource(bark.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.FINE_CLOTH, bark.myAmount),
                                IdBuilder.itemResource(ItemID.BARK, bark.myAmount)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, bark.myLevel),
                                IdBuilder.itemResource(ItemID.NEEDLE, 1)
                        )));
            }
        }

        // Glassblowing
        {
            class Blowable
            {
                int myResult;
                int myLevel;
                int myXP;
                public Blowable(int aResult, int aLevel, int aXP)
                {
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Blowable blow : new Blowable[]{
                    new Blowable(ItemID.BEER_GLASS, 1, 1750),
                    new Blowable(ItemID.EMPTY_CANDLE_LANTERN, 4, 1900),
                    new Blowable(ItemID.EMPTY_OIL_LAMP, 12, 2500),
                    new Blowable(ItemID.VIAL, 33, 3500),
                    new Blowable(ItemID.FISHBOWL, 42, 4250),
                    new Blowable(ItemID.UNPOWERED_ORB, 46, 5250),
                    new Blowable(ItemID.LANTERN_LENS, 49, 5500),
                    new Blowable(ItemID.EMPTY_LIGHT_ORB, 87, 7000)
            })
            {
                AddRecipe(new Recipe("Blow " + myPlugin.myItemManager.getItemComposition(ItemID.MOLTEN_GLASS).getName() + " into " + myPlugin.myItemManager.getItemComposition(blow.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, blow.myXP),
                                IdBuilder.itemResource(blow.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(ItemID.MOLTEN_GLASS, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, blow.myLevel),
                                IdBuilder.itemResource(ItemID.GLASSBLOWING_PIPE, 1)
                        )));
            }
        }

        // Jewelery
        {
            class Jewelery
            {
                int myMetal;
                int myGem;
                int myMould;
                int myResult;
                int myLevel;
                int myXP;

                public Jewelery(int aMetal, int aGem, int aMould, int aResult, int aLevel, int aXP)
                {
                    myMetal = aMetal;
                    myGem = aGem;
                    myMould = aMould;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Jewelery jewel : new Jewelery[]{
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.RING_MOULD, ItemID.OPAL_RING, 1, 1000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.NECKLACE_MOULD, ItemID.OPAL_NECKLACE, 16, 3500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.BRACELET_MOULD, ItemID.OPAL_BRACELET, 22, 4500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.OPAL, ItemID.AMULET_MOULD, ItemID.OPAL_AMULET_U, 27, 5500),

                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.RING_MOULD, ItemID.JADE_RING, 13, 3200),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.NECKLACE_MOULD, ItemID.JADE_NECKLACE, 25, 5400),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.BRACELET_MOULD, ItemID.JADE_BRACELET, 29, 6000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.JADE, ItemID.AMULET_MOULD, ItemID.JADE_AMULET_U, 34, 7000),

                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.RING_MOULD, ItemID.TOPAZ_RING, 16, 3500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.NECKLACE_MOULD, ItemID.TOPAZ_NECKLACE, 32, 7000),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.BRACELET_MOULD, ItemID.TOPAZ_BRACELET, 38, 7500),
                    new Jewelery(ItemID.SILVER_BAR, ItemID.RED_TOPAZ, ItemID.AMULET_MOULD, ItemID.TOPAZ_AMULET_U, 45, 8000),


                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.RING_MOULD, ItemID.SAPPHIRE_RING, 20, 4000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.NECKLACE_MOULD, ItemID.SAPPHIRE_NECKLACE, 22, 5500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.BRACELET_MOULD, ItemID.SAPPHIRE_BRACELET, 23, 6000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.SAPPHIRE, ItemID.AMULET_MOULD, ItemID.SAPPHIRE_AMULET_U, 24, 6500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.RING_MOULD, ItemID.EMERALD_RING, 27, 5500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.NECKLACE_MOULD, ItemID.EMERALD_NECKLACE, 29, 6000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.BRACELET_MOULD, ItemID.EMERALD_BRACELET, 30, 6500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.EMERALD, ItemID.AMULET_MOULD, ItemID.EMERALD_AMULET_U, 31, 7000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.RING_MOULD, ItemID.RUBY_RING, 34, 7000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.NECKLACE_MOULD, ItemID.RUBY_NECKLACE, 40, 7500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.BRACELET_MOULD, ItemID.RUBY_BRACELET, 42, 8000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.RUBY, ItemID.AMULET_MOULD, ItemID.RUBY_AMULET_U, 50, 8500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.RING_MOULD, ItemID.DIAMOND_RING, 43, 8500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.NECKLACE_MOULD, ItemID.DIAMOND_NECKLACE, 56, 9000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.BRACELET_MOULD, ItemID.DIAMOND_BRACELET, 58, 9500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DIAMOND, ItemID.AMULET_MOULD, ItemID.DIAMOND_AMULET_U, 70, 10000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.RING_MOULD, ItemID.DRAGONSTONE_RING, 55, 10000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.NECKLACE_MOULD, ItemID.DRAGON_NECKLACE, 72, 10500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.BRACELET_MOULD, ItemID.DRAGONSTONE_BRACELET, 74, 11000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.DRAGONSTONE, ItemID.AMULET_MOULD, ItemID.DRAGONSTONE_AMULET_U, 80, 15000),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ENCHANTED_GEM, ItemID.RING_MOULD, ItemID.SLAYER_RING_8, 75, 1500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ETERNAL_GEM, ItemID.RING_MOULD, ItemID.SLAYER_RING_ETERNAL, 75, 1500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.RING_MOULD, ItemID.ONYX_RING, 67, 11500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.NECKLACE_MOULD, ItemID.ONYX_NECKLACE, 82, 12000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.BRACELET_MOULD, ItemID.ONYX_BRACELET, 84, 12500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ONYX, ItemID.AMULET_MOULD, ItemID.ONYX_AMULET_U, 90, 16500),

                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.RING_MOULD, ItemID.ZENYTE_RING, 89, 15000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.NECKLACE_MOULD, ItemID.ZENYTE_NECKLACE, 92, 16500),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.BRACELET_MOULD, ItemID.ZENYTE_BRACELET, 95, 18000),
                    new Jewelery(ItemID.GOLD_BAR, ItemID.ZENYTE, ItemID.AMULET_MOULD, ItemID.ZENYTE_AMULET_U, 98, 20000)
            })
            {
                AddRecipe(new Recipe("Craft " + myPlugin.myItemManager.getItemComposition(jewel.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, jewel.myXP),
                                IdBuilder.itemResource(jewel.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(jewel.myGem, 1),
                                IdBuilder.itemResource(jewel.myMetal, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, jewel.myLevel),
                                IdBuilder.itemResource(jewel.myMould, 1)
                        )));
            }
        }

        // Moulds
        {
            class Smeltable
            {
                int myMetal;
                int myMould;
                int myResult;
                int myAmount;
                int myLevel;
                int myXP;

                public Smeltable(int aMetal, int aMould, int aResult, int aAmount, int aLevel, int aXP)
                {
                    myMetal = aMetal;
                    myMould = aMould;
                    myResult = aResult;
                    myAmount = aAmount;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }
            for(Smeltable smelt : new Smeltable[]{
                        new Smeltable(ItemID.GOLD_BAR, ItemID.RING_MOULD, ItemID.GOLD_RING, 1, 5, 1500),
                        new Smeltable(ItemID.GOLD_BAR, ItemID.NECKLACE_MOULD, ItemID.GOLD_NECKLACE, 1, 6, 2000),
                        new Smeltable(ItemID.GOLD_BAR, ItemID.BRACELET_MOULD, ItemID.GOLD_BRACELET, 1, 5, 2500),
                        new Smeltable(ItemID.GOLD_BAR, ItemID.AMULET_MOULD, ItemID.GOLD_AMULET, 1, 7,3000),
                        new Smeltable(ItemID.GOLD_BAR, ItemID.TIARA_MOULD, ItemID.GOLD_TIARA, 1, 42, 3500),

                        new Smeltable(ItemID.SILVER_BAR, ItemID.SICKLE_MOULD, ItemID.SILVER_SICKLE, 1, 18, 5000),
                        new Smeltable(ItemID.SILVER_BAR, ItemID.HOLY_MOULD, ItemID.UNSTRUNG_SYMBOL, 1, 16, 5000),
                        new Smeltable(ItemID.SILVER_BAR, ItemID.UNHOLY_MOULD, ItemID.UNSTRUNG_EMBLEM, 1, 17, 5000),
                        new Smeltable(ItemID.SILVER_BAR, ItemID.TIARA_MOULD, ItemID.TIARA, 1, 23, 5250),

                        new Smeltable(ItemID.SILVER_BAR, ItemID.BOLT_MOULD, ItemID.SILVER_BOLTS, 10, 21, 5000),
                    })
            {
                AddRecipe(new Recipe("Craft " + myPlugin.myItemManager.getItemComposition(smelt.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, smelt.myXP),
                                IdBuilder.itemResource(smelt.myResult, smelt.myAmount)),
                        Arrays.asList(
                                IdBuilder.itemResource(smelt.myMetal, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, smelt.myLevel),
                                IdBuilder.itemResource(smelt.myMould, 1)
                        )));
            }
        }

        // Birdhouses
        {
            class Birdhouse
            {
                int myLog;
                int myResult;
                int myLevel;
                int myXP;

                public Birdhouse(int aLog, int aResult, int aLevel, int aXP)
                {
                    myLog = aLog;
                    myResult = aResult;
                    myLevel = aLevel;
                    myXP = aXP;
                }
            }

            for(Birdhouse birdhouse : new Birdhouse[]{
                new Birdhouse(ItemID.LOGS, ItemID.BIRD_HOUSE, 5, 1500),
                new Birdhouse(ItemID.OAK_LOGS, ItemID.OAK_BIRD_HOUSE, 15, 2000),
                new Birdhouse(ItemID.WILLOW_LOGS, ItemID.WILLOW_BIRD_HOUSE, 25, 2500),
                new Birdhouse(ItemID.TEAK_LOGS, ItemID.TEAK_BIRD_HOUSE, 35, 3000),
                new Birdhouse(ItemID.MAPLE_LOGS, ItemID.MAPLE_BIRD_HOUSE, 45, 3500),
                new Birdhouse(ItemID.MAHOGANY_LOGS, ItemID.MAHOGANY_BIRD_HOUSE, 50, 4000),
                new Birdhouse(ItemID.YEW_LOGS, ItemID.YEW_BIRD_HOUSE, 60, 4500),
                new Birdhouse(ItemID.MAGIC_LOGS, ItemID.MAGIC_BIRD_HOUSE, 75, 5000),
                new Birdhouse(ItemID.REDWOOD_LOGS, ItemID.REDWOOD_BIRD_HOUSE, 90, 5500)
            })
            {
                AddRecipe(new Recipe("Craft " + myPlugin.myItemManager.getItemComposition(birdhouse.myResult).getName(),
                        Arrays.asList(
                                IdBuilder.xpResource(Skill.CRAFTING, birdhouse.myXP),
                                IdBuilder.itemResource(birdhouse.myResult, 1)),
                        Arrays.asList(
                                IdBuilder.itemResource(birdhouse.myLog, 1),
                                IdBuilder.itemResource(ItemID.CLOCKWORK, 1)),
                        Arrays.asList(
                                IdBuilder.levelResource(Skill.CRAFTING, birdhouse.myLevel),
                                IdBuilder.itemResource(ItemID.HAMMER, 1),
                                IdBuilder.itemResource(ItemID.CHISEL, 1)
                        )));
            }
        }

        // crystal singing
        {

        }
    }

    private void LoadNMZInfusions()
    {
        class Infusable
        {
            public int myItem;
            public int myResult;
            public int myPoints;

            public Infusable(int aItem, int aResult, int aPoints)
            {
                myItem = aItem;
                myResult = aResult;
                myPoints = aPoints;
            }
        }

        for (Infusable inf : new Infusable[]
                {
                        new Infusable(ItemID.BLACK_MASK_10, ItemID.BLACK_MASK_10_I, 1250000),
                        new Infusable(ItemID.SLAYER_HELMET, ItemID.SLAYER_HELMET_I, 1250000),
                        new Infusable(ItemID.SALVE_AMULET, ItemID.SALVE_AMULETI, 800000),
                        new Infusable(ItemID.SALVE_AMULET_E, ItemID.SALVE_AMULETEI, 800000),
                        new Infusable(ItemID.RING_OF_SUFFERING, ItemID.RING_OF_SUFFERING_I, 725000),
                        new Infusable(ItemID.RING_OF_THE_GODS, ItemID.RING_OF_THE_GODS_I, 650000),
                        new Infusable(ItemID.BERSERKER_RING, ItemID.BERSERKER_RING_I, 650000),
                        new Infusable(ItemID.WARRIOR_RING, ItemID.WARRIOR_RING_I, 650000),
                        new Infusable(ItemID.ARCHERS_RING, ItemID.ARCHERS_RING_I, 650000),
                        new Infusable(ItemID.SEERS_RING, ItemID.SEERS_RING_I, 650000),
                        new Infusable(ItemID.TYRANNICAL_RING, ItemID.TYRANNICAL_RING_I, 650000),
                        new Infusable(ItemID.TREASONOUS_RING, ItemID.TREASONOUS_RING_I, 650000),
                        new Infusable(ItemID.GRANITE_RING, ItemID.GRANITE_RING_I, 500000),
                })
        {
            AddRecipe(new Recipe("Infuse " + myPlugin.myItemManager.getItemComposition(inf.myItem).getName() + " at NMZ",
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(inf.myResult, 1))),
                    new ArrayList<>(Arrays.asList(
                            IdBuilder.itemResource(inf.myItem, 1),
                            IdBuilder.NMZPoints( inf.myPoints))),
                    new ArrayList<>()));
        }
    }
}
