package com.todo;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@ConfigGroup("Todo")
public interface TodoConfig extends Config
{
	@ConfigItem(
		keyName = "debug_logs",
		name = "Debug",
		description = "Whether to enable debugging to console"
	)
	default boolean debug()
	{
		return true;
	}

	@ConfigItem(
			keyName = "color_completed",
			name = "Completed color",
			description = "The progressbar color when completed"
	)
	default Color completedColor()
	{
		return new Color(8,83,8);
	}

	@ConfigItem(
			keyName = "color_banked",
			name = "Banked color",
			description = "The progressbar color when banked"
	)
	default Color bankedColor()
	{
		return new Color(140, 140, 22);
	}

	@ConfigItem(
			keyName = "color_text_outline",
			name = "Text outline color",
			description = "The color of the outline/background of text"
	)
	default Color textOutlineColor() { return new Color(5,5,5); }

	@ConfigItem(
			keyName = "color_text",
			name = "Text color",
			description = "The color of text"
	)
	default Color textColor() { return new Color(230,230,230); }

	@ConfigItem(
			keyName = "row_size",
			name = "Row height",
			description = "How high every goal should be displayed, also affects icon size"
	)
	default int rowHeight() { return 18; }

	@ConfigItem(
			keyName = "indent",
			name = "Indent",
			description = "How indented subtasks should be"
	)
	default int indent() { return 12; }

	@ConfigItem(
			keyName = "defaultRecipes",
			name = "Use default recipes",
			description = "Whether to include the default recipes.\nThis, among other things, makes quests and diaries automatically expand"
	)
	default boolean includeDefaultRecipes()
	{
		return true;
	}

	@ConfigItem(
			keyName = "goals",
			name = "Goals",
			description = "Current goals"
	)
	default String getGoals() { return ""; }

	@ConfigItem(
			keyName = "goals",
			name = "",
			description = ""
	)
	void setGoals(String aString);

	@ConfigItem(
			keyName = "recipes",
			name = "Recipes",
			description = "What recipes to use"
	)
	default String getRecipes() { return ""; }

	@ConfigItem(
			keyName = "recipes",
			name = "",
			description = ""
	)
	void setRecipes(String aString);


	static void SmartInsert(List<String> aItems, String aItem)
	{
		int best = 0;
		int bestScore = 0;

		for(int i = 0; i < aItems.size(); i++)
		{
			int same = 0;
			for(;same < aItems.get(i).length() && same < aItem.length(); same++)
			{
				if (aItem.charAt(same) != aItems.get(i).charAt(same))
					break;
			}

			if (same <= bestScore)
				continue;

			best = i;
			bestScore = same;
		}

		if (best < aItems.size())
			best++;

		aItems.add(best, aItem);
	}
}
