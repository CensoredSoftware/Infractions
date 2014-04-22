package com.censoredsoftware.infractions.bukkit.legacy.data.file;

import com.demigodsrpg.demigods.engine.DemigodsPlugin;
import com.demigodsrpg.demigods.engine.data.DataAccess;
import com.demigodsrpg.demigods.engine.data.DataManager;
import com.demigodsrpg.demigods.engine.data.DataType;
import com.demigodsrpg.demigods.engine.data.TempDataManager;
import com.demigodsrpg.demigods.engine.language.English;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * This is the data management file for Demigods.
 */
@SuppressWarnings("unchecked")
public class FileDataManager extends DataManager
{
	// -- VARIABLES -- //

	// Data Folder
	public static final String SAVE_PATH = DemigodsPlugin.getInst().getDataFolder() + "/data/"; // Don't change this.

	// -- YAML FILES -- //

	ConcurrentMap<Class, InfractionsFile> yamlFiles;

	// -- UTIL METHODS -- //

	// Prevent accidental double init.
	private static boolean didInit = false;

	@Override
	protected boolean preInit()
	{
		return true;
	}

	@Override
	public void init()
	{
		// Check if init has happened already...
		if(didInit) throw new RuntimeException("Data tried to initialize more than once.");

		// Create/Load YAML files.
		yamlFiles = Maps.newConcurrentMap();
		for(Class clazz : DataType.classes())
		{
			InfractionsFile file = InfractionsFileFactory.create(DataType.typeFromClass(clazz), SAVE_PATH);
			if(file == null) continue;
			file.loadDataFromFile();
			yamlFiles.put(clazz, file);
		}

		// Let the plugin know that this has finished.
		didInit = true;
	}

	@Override
	public void save()
	{
		for(InfractionsFile data : yamlFiles.values())
			data.saveDataToFile();
	}

	@Override
	public void flushData()
	{
		// Kick everyone
		for(Player player : Bukkit.getOnlinePlayers())
			player.kickPlayer(ChatColor.GREEN + English.DATA_RESET_KICK.getLine());

		// Clear the data
		for(InfractionsFile data : yamlFiles.values())
			data.clear();
		TempDataManager.clear();

		save();

		// Reload the PLUGIN
		Bukkit.getServer().getPluginManager().disablePlugin(DemigodsPlugin.getInst());
		Bukkit.getServer().getPluginManager().enablePlugin(DemigodsPlugin.getInst());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K extends Comparable, V extends DataAccess<K, V>> V getFor(final Class<V> clazz, final K key)
	{
		if(getFile(clazz).containsKey(key)) return getFile(clazz).get(key);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K extends Comparable, V extends DataAccess<K, V>> Collection<V> getAllOf(final Class<V> clazz)
	{
		return getFile(clazz).values();
	}

	@Override
	public <K extends Comparable, V extends DataAccess<K, V>> ConcurrentMap<K, V> getMapFor(final Class<V> clazz)
	{
		return getFile(clazz).getLoadedData();
	}

	@Override
	public <K extends Comparable, V extends DataAccess<K, V>> void putFor(final Class<V> clazz, final K key, final V value)
	{
		getFile(clazz).put(key, value);
	}

	@Override
	public <K extends Comparable, V extends DataAccess<K, V>> void removeFor(final Class<V> clazz, final K key)
	{
		getFile(clazz).remove(key);
	}

	@SuppressWarnings("unchecked")
	private <K extends Comparable, V extends DataAccess<K, V>> InfractionsFile<K, V> getFile(Class<V> clazz)
	{
		if(yamlFiles.containsKey(clazz)) return (InfractionsFile<K, V>) yamlFiles.get(clazz);
		throw new UnsupportedOperationException("Demigods wants a data type that does not exist.");
	}
}
