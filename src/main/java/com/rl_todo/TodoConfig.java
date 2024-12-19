package com.rl_todo;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;
import java.util.List;

@ConfigGroup("Todo")
public interface TodoConfig extends Config
{
	String GithubWikiDataUrl = "https://raw.githubusercontent.com/fiskmans/rl_todo_data/main/methods.json";

	@ConfigSection(
			name = "Colors",
			description = "Colors and accessibility",
			position = 0
	)
	String colorSection = "colorSection";


	@ConfigItem(
		keyName = "debug_logs",
		name = "Debug level",
		description = "What deb"
	)
	default int debug()
	{
		return 0;
	}


	@ConfigItem(
			keyName = "color_completed",
			name = "Completed color",
			description = "The progress color when completed",
			section = colorSection
	)
	default Color completedColor()
	{
		return new Color(8,83,8);
	}

	@ConfigItem(
			keyName = "color_banked",
			name = "Banked color",
			description = "The progress color when banked",
			section = colorSection
	)
	default Color bankedColor()
	{
		return new Color(140, 140, 22);
	}

	@ConfigItem(
			keyName = "color_text_outline",
			name = "Text outline color",
			description = "The outline color of text",
			section = colorSection
	)
	default Color textOutlineColor() { return new Color(5,5,5); }

	@ConfigItem(
			keyName = "color_text",
			name = "Text color",
			description = "The color of text",
			section = colorSection
	)
	default Color textColor() { return new Color(230,230,230); }

	@ConfigItem(
			keyName = "progress_source_synced",
			name = "Progress source synced",
			description = "The color of the blip on progress sources when they are syncronized and working",
			section = colorSection
	)
	default Color blipColorSynced() { return Color.green; }

	@ConfigItem(
			keyName = "progress_source_not_synced",
			name = "Progress source not synced",
			description = "The color of the blip on progress sources when they are not syncronized and working",
			section = colorSection
	)
	default Color blipColorNotSynced() { return Color.red; }

	@ConfigItem(
			keyName = "tree_color",
			name = "Tree color",
			description = "The color of the task tree, icon outlines and branches",
			section = colorSection
	)
	default Color treeColor() { return new Color(80,80,80); }

	@ConfigItem(
			keyName = "messageOnCompletion",
			name = "Message on Completion",
			description = "Whether to send a message in the game chat whenever you fully complete a goal/subgoal"
	)
	default boolean messageOnCompletion() { return true; }

	@ConfigItem(
			keyName = "method_sources",
			name = "Method sources",
			description = "Which folders to look in for methods"
	)
	default String methodSources() { return "${runelite}/todo_plugin/methods/custom;" + GithubWikiDataUrl; }

}
