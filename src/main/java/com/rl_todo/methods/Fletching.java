package com.rl_todo.methods;

import com.rl_todo.TodoPlugin;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;

public class Fletching
{
    static void AddAll(MethodManager aManager, TodoPlugin aPlugin)
    {
        class Bow
        {
            int myLogId;
            int myUnstringBowId;
            int myBowId;
            int myLevel;
            double myXP;
            Bow(int aLogId, int aUnstrungBow, int aBowId, int aLevel, double aXP)
            {
                myLogId = aLogId;
                myUnstringBowId = aUnstrungBow;
                myBowId = aBowId;
                myLevel = aLevel;
                myXP = aXP;
            }
        }

        for (Bow bow : new Bow[]
                {
                        new Bow(ItemID.LOGS, ItemID.SHORTBOW_U, ItemID.SHORTBOW, 5, 5),
                        new Bow(ItemID.LOGS, ItemID.LONGBOW_U, ItemID.LONGBOW, 10, 10),
                        new Bow(ItemID.OAK_LOGS, ItemID.OAK_SHORTBOW_U, ItemID.OAK_SHORTBOW, 20, 16.5),
                        new Bow(ItemID.OAK_LOGS, ItemID.OAK_LONGBOW_U, ItemID.OAK_LONGBOW, 25, 25),
                        new Bow(ItemID.WILLOW_LOGS, ItemID.WILLOW_SHORTBOW_U, ItemID.WILLOW_SHORTBOW, 35, 33.3),
                        new Bow(ItemID.WILLOW_LOGS, ItemID.WILLOW_LONGBOW_U, ItemID.WILLOW_LONGBOW, 40, 41.5),
                        new Bow(ItemID.MAPLE_LOGS, ItemID.MAPLE_SHORTBOW_U, ItemID.MAPLE_SHORTBOW, 50, 50),
                        new Bow(ItemID.MAPLE_LOGS, ItemID.MAPLE_LONGBOW_U, ItemID.MAPLE_LONGBOW, 55, 58.3),
                        new Bow(ItemID.YEW_LOGS, ItemID.YEW_SHORTBOW_U, ItemID.YEW_SHORTBOW, 65, 67.5),
                        new Bow(ItemID.YEW_LOGS, ItemID.YEW_LONGBOW_U, ItemID.YEW_LONGBOW, 70, 75),
                        new Bow(ItemID.MAGIC_LOGS, ItemID.MAGIC_SHORTBOW_U, ItemID.MAGIC_SHORTBOW, 80, 83.3),
                        new Bow(ItemID.MAGIC_LOGS, ItemID.MAGIC_LONGBOW_U, ItemID.MAGIC_LONGBOW, 85, 91.5),
                })
        {
            aManager.AddMethod(new Method("Fletch " + aPlugin.myItemManager.getItemComposition(bow.myUnstringBowId), "fletching/bows")
                    .takes(bow.myLogId)
                    .makes(bow.myUnstringBowId)
                    .makes(Skill.FLETCHING, bow.myXP)
                    .requires(ItemID.KNIFE)
                    .requires(Skill.FLETCHING, bow.myLevel)
                    .build());

            aManager.AddMethod(new Method("String " + aPlugin.myItemManager.getItemComposition(bow.myUnstringBowId), "fletching/stringing")
                    .takes(bow.myUnstringBowId)
                    .takes(ItemID.BOW_STRING)
                    .makes(bow.myBowId)
                    .makes(Skill.FLETCHING, bow.myXP)
                    .requires(Skill.FLETCHING, bow.myLevel)
                    .build());
        }

        class Crossbow
        {
            int myLogId;
            int myStockId;
            int myLimbsId;
            int myUnstrungId;
            int myCrossbowId;
            int myLevel;
            double myXP;

            Crossbow(int aLogId, int aStockId, int aLimbsId, int aUnstrungCrossbow, int aCrossbowId, int aLevel, double aXp)
            {
                myLogId = aLogId;
                myStockId = aStockId;
                myLimbsId = aLimbsId;
                myUnstrungId = aUnstrungCrossbow;
                myCrossbowId = aCrossbowId;
                myLevel = aLevel;
                myXP = aXp;
            }
        }

        for (Crossbow crossbow : new Crossbow[]{
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
            new Crossbow(ItemID.LOGS, ItemID.WOODEN_STOCK, ItemID.BRONZE_LIMBS, ItemID.BRONZE_CROSSBOW_U, ItemID.BRONZE_CROSSBOW, 9, 6),
        })
        {

        }
    }
}
