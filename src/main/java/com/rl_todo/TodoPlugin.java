package com.rl_todo;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import com.rl_todo.ui.TodoPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Todo"
)
public class TodoPlugin extends Plugin
{
	@Inject
	public Client myClient;

	@Inject
	public ClientThread myClientThread;

	@Inject
	private ClientToolbar myClientToolbar;

	@Inject
	public TodoConfig myConfig;

	@Inject
	public ProgressManager myProgressManager;

	@Inject
	public ProgressSources mySources;
	boolean myInventoryLoaded = false;
	boolean myBankLoaded = false;

	@Inject
	public ItemManager myItemManager;

	@Inject
	public ChatboxItemSearch myChatboxItemSearch;

	boolean myIsEnabled = false;

	public RecipeManager myRecipeManager;
	public TodoPanel myPanel;
	public Quests myQuest;

	public static final BufferedImage ICON = ImageUtil.loadImageResource(TodoPlugin.class, "/Icon_16x16.png");
	public static final String CONFIG_GROUP = "Todo";
	private static final String LOG_TAG = "[todo] ";
	private NavigationButton myNavButton;
	static TodoPlugin myGlobalInstance = null;

	public static void debug(Object aMessage)
	{
		if (myGlobalInstance != null && myGlobalInstance.myConfig.debug())
			log.info(LOG_TAG + aMessage.toString());
	}

	@Override
	protected void startUp() throws Exception
	{
		debug("startUp");

		myGlobalInstance = this;

		myPanel = new TodoPanel(this, myProgressManager);
		myNavButton = NavigationButton.builder()
				.tooltip("Todo")
				.priority(9)
				.icon(ICON)
				.panel(myPanel)
				.build();

		for(Field field : mySources.getClass().getDeclaredFields())
		{
			if (field.getType() == ProgressSource.class)
			{
				myProgressManager.AddSource((ProgressSource) field.get(mySources));
			}
		}

		myClientThread.invokeLater(()->
		{
			myQuest = new Quests(this);
			myRecipeManager = new RecipeManager(this);
			myQuest.Load();

			SwingUtilities.invokeLater(()->
			{
				myClientToolbar.addNavigation(myNavButton);
			});
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		debug("shutDown");

		myProgressManager.RemoveAllSources();

		myClientToolbar.removeNavigation(myNavButton);

		myQuest = null;
		myNavButton = null;
		myPanel = null;
		myRecipeManager = null;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged ev)
	{
		if (ev.getContainerId() == InventoryID.INVENTORY.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.Inventory);
			myInventoryLoaded = true;
			CheckEnableconditions();
		}
		else if(ev.getContainerId() == InventoryID.BANK.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.Bank);
			myBankLoaded = true;
			CheckEnableconditions();
		}
		else if(ev.getContainerId() == InventoryID.SEED_VAULT.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.SeedVault);
		}
	}

	private void UpdateContainerBasedSource(ItemContainer aContainer, ProgressSource aSource)
	{
		Map<Integer, Integer> items = new HashMap<>();

		Set<String> previousKeys = aSource.GetAllKeys();
		for(String key : previousKeys)
		{
			Integer i = Integer.parseInt(key);
			items.put(i,0);
		}

		for(Item item : aContainer.getItems())
		{
			if (myItemManager.getItemComposition(item.getId()).getPlaceholderTemplateId() != -1)
				continue;

			items.put(myItemManager.canonicalize(item.getId()), items.getOrDefault(item.getId(), 0) + item.getQuantity());
		}

		for (Map.Entry<Integer,Integer> entry : items.entrySet())
			aSource.SetProgress(Integer.toString(entry.getKey()), entry.getValue());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
			return;

		if (myRecipeManager != null)
			myRecipeManager.LoadConfig();
	}

	@Subscribe
	protected void onStatChanged(StatChanged event) {

		mySources.Levels.SetProgress(IdBuilder.levelId(event.getSkill()), event.getLevel());
		mySources.Xp.SetProgress(IdBuilder.xpId(event.getSkill()), event.getXp() * 100);
	}

	@Provides
	TodoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TodoConfig.class);
	}

	private void CheckEnableconditions()
	{
		debug("checked enable");

		if (myIsEnabled) return;
		if (!myInventoryLoaded) return;
		if (!myBankLoaded) return;

		debug("enabled");
		myIsEnabled = true;

		myRecipeManager.LoadConfig();
		myPanel.Enable();
		myQuest.Load();

		for (Skill skill : Skill.values())
		{
			mySources.Levels.SetProgress(IdBuilder.levelId(skill), myClient.getRealSkillLevel(skill));
			mySources.Xp.SetProgress(IdBuilder.xpId(skill), myClient.getSkillExperience(skill) * 100);
		}

		SwingUtilities.invokeLater(()->
		{
			myPanel.GetGoals().Load();
		});
	}
}
