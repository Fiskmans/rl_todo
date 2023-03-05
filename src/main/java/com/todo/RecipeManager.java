package com.todo;

import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
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
            AddDefaultRecipe(
                    IdBuilder.levelId(skill),
                    new LevelRecipe(skill));
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
