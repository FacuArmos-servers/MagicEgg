package com.migsi.magicegg.config;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.migsi.magicegg.exceptions.OutdatedConfigException;
import com.migsi.magicegg.MagicDropArea;
import com.migsi.magicegg.MagicEgg;
import com.migsi.magicegg.MagicItemStack;
import com.migsi.magicegg.enums.MagicConfigEnum;
import com.migsi.magicegg.exceptions.InvalidProbabilityException;

public class MagicConfig {

	// Name which gets shown above the egg
	private static String Name = "§5M§8a§5g§8i§5c §8E§5g§8g§f";

	// Fireworks timings
	private static long FireworkDelay = 0;
	private static long FireworkPeriod = 5;

	// Drop timings
	private static long DropDelay = 20;

	// Repetition delay
	private static long RepetitionDelay = 72000;

	// Holds the event state
	private static boolean IsEventRunning = false;
	
	// Determines which permissions system is used
	private static boolean Permissions = true;
	private static boolean Op = false;

	// Custom broadcast messages
	private static String OnStartMessage = "The " + MagicConfig.getName() + " event just started!";
	private static String OnStopMessage = "The " + MagicConfig.getName() + " event was canceled now...";
	private static String OnSpawnMessage = "A new " + MagicConfig.getName() + " just spawned!";
	private static String OnDropMessage = "§5L§8o§5o§8t§5!§8!§5!§f";

	// Holds all items which get dropped
	private static Vector<MagicItemStack> Drops = null;

	// File configurations
	private static final String CONFIGFILENAME = "config.yml";
	private static final String DROPSFILENAME = "drops.yml";
	private static final String AREASFILENAME = "areas.yml";
	private static File DropsFile = null;
	private static FileConfiguration DropsFileConfig = null;
	private static File AreasFile = null;
	private static FileConfiguration AreasFileConfig = null;

	public MagicConfig() {
		load();
	}

	public void load() {
		try {
			if (!MagicEgg.instance.getDataFolder().exists()) {
				MagicEgg.instance.getDataFolder().mkdirs();
			}

			File config = new File(MagicEgg.instance.getDataFolder(), CONFIGFILENAME);

			if (!config.exists()) {
				MagicEgg.instance.getLogger().info("Config.yml not found, creating a new one...");

				writeConfig();

				MagicEgg.instance.getLogger().info("Config created!");

			} else {

				MagicEgg.instance.getLogger().info("Config.yml found, loading...");

				Name = MagicEgg.instance.getConfig().getString(MagicConfigEnum.NAME.name());

				OnStartMessage = MagicEgg.instance.getConfig().getString(MagicConfigEnum.ON_START_MESSAGE.name());
				OnStopMessage = MagicEgg.instance.getConfig().getString(MagicConfigEnum.ON_STOP_MESSAGE.name());
				OnSpawnMessage = MagicEgg.instance.getConfig().getString(MagicConfigEnum.ON_SPAWN_MESSAGE.name());
				OnDropMessage = MagicEgg.instance.getConfig().getString(MagicConfigEnum.ON_DROP_MESSAGE.name());

				IsEventRunning = MagicEgg.instance.getConfig().getBoolean(MagicConfigEnum.IS_EVENT_RUNNING.name());
				
				RepetitionDelay = minToTicks(
						MagicEgg.instance.getConfig().getDouble(MagicConfigEnum.REPETITION_DELAY_MIN.name()));
				FireworkDelay = MagicEgg.instance.getConfig().getLong(MagicConfigEnum.FIREWORK_DELAY.name());
				FireworkPeriod = MagicEgg.instance.getConfig().getLong(MagicConfigEnum.FIREWORK_PERIOD.name());
				DropDelay = MagicEgg.instance.getConfig().getLong(MagicConfigEnum.DROP_DELAY.name());

				Permissions = MagicEgg.instance.getConfig().getBoolean(MagicConfigEnum.PERMISSIONS.name());
				Op = MagicEgg.instance.getConfig().getBoolean(MagicConfigEnum.OP.name());

				MagicEgg.instance.getLogger().info("Config loaded!");

				if (!MagicEgg.getPlugin(MagicEgg.class).getDescription().getVersion()
						.equals(MagicEgg.instance.getConfig().getString(MagicConfigEnum.VERSION.name()))) {
					throw new OutdatedConfigException("Older version of config.yml detected");
				}
			}

			DropsFile = new File(MagicEgg.instance.getDataFolder(), DROPSFILENAME);
			DropsFileConfig = YamlConfiguration.loadConfiguration(DropsFile);

			Drops = new Vector<MagicItemStack>();

			if (!DropsFile.exists()) {
				MagicEgg.instance.getLogger().info("Drops.yml not found, creating a new one...");

				DropsFileConfig.set("0", "APPLE;1;100");
				DropsFileConfig.save(DropsFile);

				Drops.add(new MagicItemStack(DropsFileConfig.getString("0").split(";")));

				MagicEgg.instance.getLogger().info("Default drops created!");

			} else {

				MagicEgg.instance.getLogger().info("Drops.yml found, loading...");

				for (String key : DropsFileConfig.getKeys(false)) {
					try {
						Drops.add(new MagicItemStack(DropsFileConfig.getString(key).split(";")));
					} catch (InvalidProbabilityException e) {
						MagicEgg.instance.getLogger().log(Level.SEVERE, "Set invalid probability on key: " + key);
					}
				}

				MagicEgg.instance.getLogger().info("Drops loaded!");
			}

			AreasFile = new File(MagicEgg.instance.getDataFolder(), AREASFILENAME);
			AreasFileConfig = YamlConfiguration.loadConfiguration(AreasFile);

			if (!AreasFile.exists()) {
				MagicEgg.instance.getLogger().info("Areas.yml not found, creating a new one...");

				AreasFileConfig.save(AreasFile);

				MagicEgg.instance.getLogger().info("Areas file created!");

			} else {

				MagicEgg.instance.getLogger().info("Areas.yml found, loading...");

				new MagicDropArea();

				for (String key : AreasFileConfig.getKeys(false)) {
					try {
						new MagicDropArea(AreasFileConfig.getString(key).split(";"));
					} catch (Exception e) {
						MagicEgg.instance.getLogger().log(Level.SEVERE, "Set invalid key: " + key);
					}
				}

				MagicEgg.instance.getLogger().info("Areas loaded!");
			}

		} catch (OutdatedConfigException e) {
			MagicEgg.instance.getLogger().info("But you were using an older version. I'll update it for you!");
			new File(MagicEgg.instance.getDataFolder(), "config.yml").delete();
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		MagicEgg.instance.getLogger().info("Saving config...");
		writeConfig();
		MagicEgg.instance.getLogger().info("Saving drops...");
		writeDrops();
		MagicEgg.instance.getLogger().info("Saving areas...");
		writeAreas();
		MagicEgg.instance.getLogger().info("Saved!");
	}

	public void writeConfig() {
		MagicEgg.instance.getConfig().set(MagicConfigEnum.VERSION.name(),
				MagicEgg.getPlugin(MagicEgg.class).getDescription().getVersion());
		MagicEgg.instance.getConfig().set(MagicConfigEnum.NAME.name(), Name);

		MagicEgg.instance.getConfig().set(MagicConfigEnum.ON_START_MESSAGE.name(), OnStartMessage);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.ON_STOP_MESSAGE.name(), OnStopMessage);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.ON_SPAWN_MESSAGE.name(), OnSpawnMessage);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.ON_DROP_MESSAGE.name(), OnDropMessage);
		
		MagicEgg.instance.getConfig().set(MagicConfigEnum.IS_EVENT_RUNNING.name(), IsEventRunning);

		MagicEgg.instance.getConfig().set(MagicConfigEnum.REPETITION_DELAY_MIN.name(), ticksToMin(RepetitionDelay));
		MagicEgg.instance.getConfig().set(MagicConfigEnum.FIREWORK_DELAY.name(), FireworkDelay);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.FIREWORK_PERIOD.name(), FireworkPeriod);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.DROP_DELAY.name(), DropDelay);

		MagicEgg.instance.getConfig().set(MagicConfigEnum.PERMISSIONS.name(), Permissions);
		MagicEgg.instance.getConfig().set(MagicConfigEnum.OP.name(), Op);
		MagicEgg.instance.saveConfig();
	}

	public void writeDrops() {
		for (int index = 0; index < Drops.size(); index++) {
			DropsFileConfig.set(Integer.toString(index), Drops.get(index).toString());
		}

		try {
			DropsFileConfig.save(DropsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeAreas() {
		for (int index = 0; index < MagicDropArea.getDropAreas().size(); index++) {
			AreasFileConfig.set(Integer.toString(index), MagicDropArea.getDropAreas().get(index).toString());
		}

		try {
			AreasFileConfig.save(AreasFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static long secToTicks(double sec) {
		return Math.round(sec * 20);
	}

	public static double ticksToSec(long ticks) {
		return ticks / 20;
	}

	public static long minToTicks(double min) {
		return secToTicks(min * 60);
	}

	public static double ticksToMin(long ticks) {
		return ticksToSec(ticks) / 60;
	}

	public static String getName() {
		return Name;
	}

	public static void setName(String name) {
		MagicConfig.Name = name;
	}

	public static String getOnStartMessage() {
		return OnStartMessage;
	}

	public static String getOnStopMessage() {
		return OnStopMessage;
	}

	public static String getOnSpawnMessage() {
		return OnSpawnMessage;
	}

	public static String getOnDropMessage() {
		return OnDropMessage;
	}

	public static boolean isIsEventRunning() {
		return IsEventRunning;
	}

	public static void setIsEventRunning(boolean isEventRunning) {
		IsEventRunning = isEventRunning;
	}

	public static long getRepetitionDelay() {
		return RepetitionDelay;
	}

	public static long getFireworkDelay() {
		return FireworkDelay;
	}

	public static long getFireworkPeriod() {
		return FireworkPeriod;
	}

	public static long getDropDelay() {
		return DropDelay;
	}

	public static boolean isPermissions() {
		return Permissions;
	}

	public static boolean isOp() {
		return Op;
	}

	public Vector<MagicItemStack> getDrops() {
		return Drops;
	}

}
