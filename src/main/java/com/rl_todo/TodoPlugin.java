package com.rl_todo;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import com.rl_todo.methods.MethodManager;
import com.rl_todo.ui.TodoPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxTextInput;
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

	@Inject
	public ItemManager myItemManager;

	@Inject
	public SpriteManager mySpriteManager;

	@Inject
	public ChatboxItemSearch myChatboxItemSearch;
	@Inject
	public ChatboxTextInput myChatboxTextInput;

	public MethodManager myMethodManager;
	public TodoPanel myPanel;
	public Quests myQuest;

	public static final BufferedImage ICON = ImageUtil.loadImageResource(TodoPlugin.class, "/Icon_16x16.png");
	public static final String CONFIG_GROUP = "Todo";
	private static final String LOG_TAG = "[todo] ";
	private NavigationButton myNavButton;
	static TodoPlugin myGlobalInstance = null;
	private boolean myIsLoaded = false;

	public static void debug(Object aMessage, int aLevel)
	{
		if (myGlobalInstance != null && aLevel < myGlobalInstance.myConfig.debug())
		{
			myGlobalInstance.myClientThread.invokeLater(() ->
			{
				myGlobalInstance.myClient.addChatMessage(ChatMessageType.CONSOLE, LOG_TAG, aMessage.toString(), "Todo");
			});
			log.info(LOG_TAG + aMessage.toString());
		}
	}


	public static void IgnorableError(Object aMessage)
	{
		myGlobalInstance.myClientThread.invokeLater(() ->
		{
			myGlobalInstance.myClient.addChatMessage(ChatMessageType.CONSOLE, LOG_TAG, aMessage.toString(), "Todo");
		});
		log.info(LOG_TAG + aMessage.toString());
	}

	public static void FixableError(Object aMessage, Object aFixLabel, Runnable aFix)
	{
		log.info(LOG_TAG + aMessage.toString());

		// TODO add 'fix the error for me' button
	}

	@Override
	protected void startUp() throws Exception
	{
		debug("startUp", 1);

		myGlobalInstance = this;



		myPanel = new TodoPanel(this);
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
			myMethodManager = new MethodManager(this);
			myQuest.Load(); // TODO: this should be done on first game tick after logging in

			SwingUtilities.invokeLater(()->
			{
				myClientToolbar.addNavigation(myNavButton);
			});
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		debug("shutDown", 1);

		myProgressManager.RemoveAllSources();

		SwingUtilities.invokeLater(()->
		{
			myClientToolbar.removeNavigation(myNavButton);
		});

		myQuest = null;
		myNavButton = null;
		myPanel = null;
		myMethodManager = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		try
		{
			switch(event.getGameState())
			{
				case HOPPING:
				case LOADING:
				case CONNECTION_LOST:
					break;
				case LOGGED_IN:
					OnLogin();
					break;
				default:
					myPanel.OnLoggedOut();
					myIsLoaded = false;
					break;
			}
		}
		catch(Exception e)
		{
			IgnorableError(LOG_TAG + " Crashed when changing gamestate");
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged ev)
	{
		if (ev.getContainerId() == InventoryID.INVENTORY.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.Inventory);
		}
		else if(ev.getContainerId() == InventoryID.BANK.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.Bank);
		}
		else if(ev.getContainerId() == InventoryID.SEED_VAULT.getId())
		{
			UpdateContainerBasedSource(ev.getItemContainer(), mySources.SeedVault);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged ev)
	{
		switch (ev.getVarpId())
		{
			case 1059:
			case 1060:
				mySources.RefreshNMZ(this);
				break;
		}
		debug("varbit changed: ", 5);
		debug("  varpid: " + ev.getVarpId(), 5);
		debug("  varbitid: " + ev.getVarbitId(), 5);
		debug("  value: " + ev.getValue(), 5);
	}

	private void UpdateContainerBasedSource(ItemContainer aContainer, ProgressSource aSource)
	{
		Map<Integer, Integer> items = new HashMap<>();

		Set<String> previousKeys = aSource.GetAllKeys();
		for(String key : previousKeys)
		{
			assert key.startsWith("item.");

			Integer i = Integer.parseInt(key.substring(5));
			items.put(i, 0);
		}

		for(Item item : aContainer.getItems())
		{
			if (myItemManager.getItemComposition(item.getId()).getPlaceholderTemplateId() != -1)
				continue;

			items.put(myItemManager.canonicalize(item.getId()), items.getOrDefault(item.getId(), 0) + item.getQuantity());
		}

		for (Map.Entry<Integer,Integer> entry : items.entrySet())
			aSource.SetProgress(IdBuilder.itemId(entry.getKey()), entry.getValue());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
			return;

		SwingUtilities.invokeLater(()->
		{
			myPanel.GetGoals().OnConfigChanged();
		});
	}

	@Subscribe
	protected void onStatChanged(StatChanged event)
	{
		mySources.Levels.SetProgress(IdBuilder.levelId(event.getSkill()), event.getLevel());
		mySources.Xp.SetProgress(IdBuilder.xpId(event.getSkill()), event.getXp());
	}

	@Subscribe
	public void onAccountHashChanged(AccountHashChanged event)
	{
		myPanel.Disable("Does not support switching accounts without restarting the plugin");
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		myProgressManager.Tick();
	}

	@Provides
	TodoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TodoConfig.class);
	}

	private void OnLogin()
	{
		if (myIsLoaded)
			return;

		myIsLoaded = true;

		myQuest.Load();
		myPanel.OnLoggedIn();
		mySources.RefreshNMZ(this);
		mySources.RefreshLevels(this);


		SwingUtilities.invokeLater(()->
		{
			myPanel.GetGoals().Load();
		});
	}
}
