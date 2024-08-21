package com.rl_todo.methods;

import com.rl_todo.IdBuilder;
import com.rl_todo.Resource;
import com.rl_todo.TodoPlugin;
import net.runelite.api.Skill;

import java.util.ArrayList;
import java.util.List;

public class LevelMethod extends Method
{
    static final int[] XP_LOOKUP = new int[]
            {
            //      0           1           2           3           4           5           6           7           8           9
            /* 0*/  0,          0,          83,         174,        276,        388,        512,        650,        801,        969,
            /*10*/  1154,       1358,       1584,       1833,       2107,       2411,       2746,       3115,       3523,       3873,
            /*20*/  4470,       5018,       5624,       6291,       7028,       7842,       8740,       9730,       10824,      12031,
            /*30*/  13363,      14833,      16456,      18247,      20224,      22406,      24815,      27473,      30408,      33648,
            /*40*/  37224,      41171,      45529,      50339,      55649,      61512,      67983,      75127,      83014,      91721,
            /*50*/  101333,     111945,     123660,     136594,     150872,     166636,     184040,     203254,     224466,     247886,
            /*60*/  273742,     302288,     333804,     368599,     407015,     449428,     496254,     547953,     605032,     668051,
            /*70*/  737627,     814445,     899257,     992895,     1096278,    1210421,    1336443,    1475581,    1629200,    1798808,
            /*80*/  1986068,    2192818,    2421087,    2673114,    2951373,    3258594,    3597792,    3972294,    4385776,    4842295,
            /*90*/  5346332,    5902831,    6517253,    7195629,    7944614,    8771558,    9684577,    10692629,   11805606,   13034431};

    Skill mySkill;
    String myXpTag;
    String myLevelTag;

    public LevelMethod(Skill aSkill)
    {
        super(("Level up " + aSkill.getName()), "level");

        myMakes.add(new Resource(IdBuilder.levelId(aSkill), 1));
        myTakes.add(new Resource(IdBuilder.xpId(aSkill), 1));

        mySkill = aSkill;
        myLevelTag = IdBuilder.levelId(aSkill);
        myXpTag = IdBuilder.xpId(aSkill);
    }

    @Override
    public List<Resource> Calculate(TodoPlugin aPlugin, String aId, int aTarget)
    {
        assert aId.equals(myLevelTag);
        assert aTarget > 0;
        assert aTarget <= 99;

        List<Resource> out = new ArrayList<>();
        out.add(new Resource(myXpTag, XP_LOOKUP[aTarget]));

        return out;
    }

    @Override
    public int CalculateAvailable(TodoPlugin aPlugin, List<Resource> aAvailableIngredients, String aId, int aMaximum)
    {
        int baseXp = aPlugin.myProgressManager.GetProgress(IdBuilder.xpId(mySkill));
        int baseLevel = 0;
        for(; baseLevel < 99; baseLevel++)
        {
            if (baseXp < XP_LOOKUP[baseLevel])
                break;

        }

        int bankedXp = baseXp;
        for (Resource r : aAvailableIngredients)
        {
            if (r.myId.equals(IdBuilder.xpId(mySkill)))
                bankedXp += r.myAmount;
        }

        int bankedLevel = baseLevel;
        for(; bankedLevel < 99; bankedLevel++)
        {
            if (bankedXp < XP_LOOKUP[bankedLevel])
                break;

        }
        return Math.min(bankedLevel - baseLevel, aMaximum);
    }
}
