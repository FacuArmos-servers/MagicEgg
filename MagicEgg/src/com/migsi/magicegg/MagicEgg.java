package com.migsi.magicegg;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.migsi.magicegg.config.MagicConfig;
import com.migsi.magicegg.handler.MagicHandler;

public class MagicEgg extends JavaPlugin {

	public static MagicEgg instance = null;

	public static final Random RANDOM = new Random();

	private MagicConfig config = null;
	private MagicHandler handler = null;
	private MagicDropArea droparea = null;
	private MagicScheduler scheduler = null;

	private Map<Block, ArmorStand> map = new HashMap<Block, ArmorStand>();

	@Override
	public void onEnable() {
		instance = this;

		config = new MagicConfig();
		handler = new MagicHandler();
		droparea = new MagicDropArea();
		scheduler = new MagicScheduler();

		this.getServer().getPluginManager().registerEvents(handler, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean ret = true;

		if (sender instanceof Player) {
			switch (command.getName().toLowerCase()) {
			case "meggaddloc":
				if (checkPermission(sender, "magicegg.location.add")) {
					if (args.length == 1) {
						try {
							int radius = Integer.parseInt(args[0]);
							new MagicDropArea(((Player) sender).getLocation(), radius, false);
							broadcast("Added new " + MagicConfig.getName() + " spawn!");
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.DARK_RED + "You need to tell me a radius in number format!"
									+ ChatColor.RESET);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (args.length == 3) {
						try {
							int x = Integer.parseInt(args[0]);
							int z = Integer.parseInt(args[1]);
							int radius = Integer.parseInt(args[2]);
							new MagicDropArea(
									new Location(((Player) sender).getWorld(), x,
											((Player) sender).getWorld().getHighestBlockYAt(x, z) + 1, z),
									radius, false);
							broadcast("Added new " + MagicConfig.getName() + " spawn!");
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.DARK_RED
									+ "You need to tell me coordinates and radius in number format!" + ChatColor.RESET);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						ret = false;
					}
				}
				break;
			case "meggremoveloc":
				if (checkPermission(sender, "magicegg.location.remove")) {
					if (args.length == 0) {
						if (droparea.remove(((Player) sender).getLocation())) {
							deleteEgg(((Player) sender).getLocation().getBlock());
							sender.sendMessage(ChatColor.DARK_PURPLE + "Removed egg spawn." + ChatColor.RESET);
						} else {
							sender.sendMessage(
									ChatColor.DARK_PURPLE + "No egg spawn on this location found!" + ChatColor.RESET);
						}
					} else if (args.length == 2) {
						try {
							int x = Integer.parseInt(args[0]);
							int z = Integer.parseInt(args[1]);
							if (droparea.remove(new Location(((Player) sender).getWorld(), x,
									((Player) sender).getWorld().getHighestBlockYAt(x, z) + 1, z))) {
								deleteEgg(new Location(((Player) sender).getWorld(), x,
										((Player) sender).getWorld().getHighestBlockYAt(x, z) + 1, z).getBlock());
								sender.sendMessage(ChatColor.DARK_PURPLE + "Removed egg spawn." + ChatColor.RESET);
							} else {
								sender.sendMessage(ChatColor.DARK_PURPLE + "No egg spawn found on this location!"
										+ ChatColor.RESET);
							}
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.DARK_RED + "You need to tell me coordinates in number format!"
									+ ChatColor.RESET);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						ret = false;
					}
				}
				break;
			case "meggspawn":
				if (checkPermission(sender, "magicegg.spawn")) {
					if (args.length == 1) {
						try {
							int radius = Integer.parseInt(args[0]);
							new MagicDropArea(((Player) sender).getLocation(), radius, true);

							spawnEgg(((Player) sender).getLocation());

						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.DARK_RED + "You need to tell me a radius in number format!"
									+ ChatColor.RESET);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (args.length == 3) {
						try {
							int x = Integer.parseInt(args[0]);
							int z = Integer.parseInt(args[1]);
							int radius = Integer.parseInt(args[2]);
							new MagicDropArea(
									new Location(((Player) sender).getWorld(), x,
											((Player) sender).getWorld().getHighestBlockYAt(x, z) + 1, z),
									radius, true);

							spawnEgg(new Location(((Player) sender).getWorld(), x,
									((Player) sender).getWorld().getHighestBlockYAt(x, z), z));
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.DARK_RED
									+ "You need to tell me coordinates and radius in number format!" + ChatColor.RESET);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						ret = false;
					}
				}
				break;
			case "meggstart":
				if (checkPermission(sender, "magicegg.event.start")) {
					if (scheduler.start()) {
						sender.sendMessage(ChatColor.DARK_PURPLE + "The event started now and will repeat itself every "
								+ ChatColor.RESET + MagicConfig.ticksToMin(MagicConfig.getRepetitionDelay())
								+ ChatColor.DARK_PURPLE + " minutes." + ChatColor.RESET);
						broadcast(ChatColor.translateAlternateColorCodes('§', MagicConfig.getOnStartMessage()));
					} else {
						sender.sendMessage(ChatColor.DARK_PURPLE + "The event is already running!" + ChatColor.RESET);
					}
				}
				break;
			case "meggstop":
				if (checkPermission(sender, "magicegg.event.stop")) {
					try {
						scheduler.cancel();
						sender.sendMessage(ChatColor.DARK_PURPLE + "The event was stopped." + ChatColor.RESET);
						broadcast(ChatColor.translateAlternateColorCodes('§', MagicConfig.getOnStopMessage()));
					} catch (IllegalStateException e) {
						sender.sendMessage(ChatColor.DARK_RED + "The event was not started yet!" + ChatColor.RESET);
					}
				}
				break;
			case "meggforce":
				if (checkPermission(sender, "magicegg.event.force")) {
					if (scheduler.spawnRandomEgg()) {
						sender.sendMessage(ChatColor.DARK_PURPLE + "Forced the " + MagicConfig.getName()
								+ ChatColor.DARK_PURPLE + " to spawn." + ChatColor.RESET);
					} else {
						sender.sendMessage(
								ChatColor.DARK_PURPLE + "No spawn locations were defined!" + ChatColor.RESET);
					}
				}
				break;
			default:
				ret = false;
			}
		}

		return ret;
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(handler);
		deleteRemainingEggs();
		config.save();
	}

	public void spawnEgg(Location loc) {
		Location blockLoc = new Location(loc.getWorld(), Double.valueOf(loc.getBlockX()),
				Double.valueOf(loc.getBlockY()), Double.valueOf(loc.getBlockZ()));
		Block egg = loc.getWorld().getBlockAt(loc);
		if (!map.containsKey(egg)) {
			ArmorStand as = (ArmorStand) blockLoc.getWorld().spawnEntity(blockLoc.add(.5, 1, .5),
					EntityType.ARMOR_STAND);
			blockLoc.add(0, -1, 0).getBlock().setType(Material.DRAGON_EGG);
			as.setGravity(false);
			as.setVisible(false);
			as.setArms(false);
			as.setBasePlate(false);
			as.setRemoveWhenFarAway(false);
			as.setSmall(true);
			as.setCustomNameVisible(true);
			as.setCustomName(MagicConfig.getName());
			map.put(egg, as);

			broadcast(ChatColor.translateAlternateColorCodes('§', MagicConfig.getOnSpawnMessage()));
		}
	}

	public void deleteRemainingEggs() {
		getLogger().info("Deleting remaining eggs...");
		for (int i = 0; i < MagicDropArea.getDropAreas().size(); i++) {
			deleteEgg(getServer().getWorld(MagicDropArea.get(i).getWorld())
					.getBlockAt(MagicDropArea.get(i).getLocation()));
			if (MagicDropArea.get(i).isTemporary()) {
				MagicDropArea.remove(MagicDropArea.get(i));
			}
		}
	}

	public void deleteEgg(Block egg) {
		if (map.get(egg) != null) {
			map.get(egg).remove();
			egg.setType(Material.AIR);
			map.remove(egg);
		}
	}

	public void fireworks(final Location loc) {

		BukkitRunnable run = new BukkitRunnable() {

			int runcount = 0;

			@Override
			public void run() {
				if (runcount == 5) {
					this.cancel();
				} else {
					runcount++;
					Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();
					int rt = RANDOM.nextInt(5) + 1;
					Type type = Type.BALL;
					if (rt == 1)
						type = Type.BALL;
					if (rt == 2)
						type = Type.BALL_LARGE;
					if (rt == 3)
						type = Type.BURST;
					if (rt == 4)
						type = Type.CREEPER;
					if (rt == 5)
						type = Type.STAR;
					int r1i = RANDOM.nextInt(17) + 1;
					int r2i = RANDOM.nextInt(17) + 1;
					Color c1 = getColor(r1i);
					Color c2 = getColor(r2i);
					FireworkEffect effect = FireworkEffect.builder().flicker(RANDOM.nextBoolean()).withColor(c1)
							.withFade(c2).with(type).trail(RANDOM.nextBoolean()).build();
					fwm.addEffect(effect);
					int rp = RANDOM.nextInt(2) + 1;
					fwm.setPower(rp);
					fw.setFireworkMeta(fwm);
				}
			}
		};

		run.runTaskTimer(this, MagicConfig.getFireworkDelay(), MagicConfig.getFireworkPeriod());
	}

	private Color getColor(int i) {
		Color c = null;
		if (i == 1) {
			c = Color.AQUA;
		}
		if (i == 2) {
			c = Color.BLACK;
		}
		if (i == 3) {
			c = Color.BLUE;
		}
		if (i == 4) {
			c = Color.FUCHSIA;
		}
		if (i == 5) {
			c = Color.GRAY;
		}
		if (i == 6) {
			c = Color.GREEN;
		}
		if (i == 7) {
			c = Color.LIME;
		}
		if (i == 8) {
			c = Color.MAROON;
		}
		if (i == 9) {
			c = Color.NAVY;
		}
		if (i == 10) {
			c = Color.OLIVE;
		}
		if (i == 11) {
			c = Color.ORANGE;
		}
		if (i == 12) {
			c = Color.PURPLE;
		}
		if (i == 13) {
			c = Color.RED;
		}
		if (i == 14) {
			c = Color.SILVER;
		}
		if (i == 15) {
			c = Color.TEAL;
		}
		if (i == 16) {
			c = Color.WHITE;
		}
		if (i == 17) {
			c = Color.YELLOW;
		}
		return c;
	}

	/**
	 * Drops items in the given drop area
	 * 
	 * @param loc
	 */
	public void dropItems(Location loc) {

		new BukkitRunnable() {

			@Override
			public void run() {
				for (MagicItemStack item : config.getDrops()) {
					int rand = RANDOM.nextInt(101);
					if (item.getProbability() != 0 && rand <= item.getProbability()) {
						loc.getWorld().dropItemNaturally(droparea.get(loc).getRandLocation(), item);
					}
				}
				broadcast(ChatColor.translateAlternateColorCodes('§', MagicConfig.getOnDropMessage()));
				if (droparea.get(loc).isTemporary()) {
					droparea.remove(loc);
				}
			}
		}.runTaskLater(this, MagicConfig.getDropDelay());

	}

	/**
	 * Checks if sender has the given permission
	 * 
	 * @param sender
	 * @return true if player has permission
	 */
	public boolean checkPermission(CommandSender sender, String permission) {
		if (!sender.hasPermission(permission) && MagicConfig.isPermissions() || !sender.isOp() && MagicConfig.isOp()) {
			sender.sendMessage(
					ChatColor.DARK_RED + "Sorry, you don't have the permission to use this command!" + ChatColor.RESET);
			return false;
		}
		return true;
	}

	public Map<Block, ArmorStand> getMap() {
		return map;
	}

	public void Log(Level level, String msg) {
		this.getLogger().log(level, msg);
	}

	public static void broadcast(String msg) {
		Bukkit.broadcastMessage(msg);
	}

}