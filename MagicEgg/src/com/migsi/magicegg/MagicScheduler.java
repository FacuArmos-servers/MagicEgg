package com.migsi.magicegg;

import org.bukkit.scheduler.BukkitRunnable;

import com.migsi.magicegg.config.MagicConfig;

public class MagicScheduler extends BukkitRunnable {

	private static MagicScheduler scheduler = null;

	public MagicScheduler() {
		if (scheduler == null) {
			scheduler = this;
			if (MagicConfig.isIsEventRunning()) {
				MagicEgg.instance.getLogger().info("Since the event was enabled on server shutdown, I'll continue it now.");
				MagicConfig.setIsEventRunning(false);
				start();
			}
		}
	}

	@Override
	public void run() {
		spawnRandomEgg();
	}

	public boolean start() {
		if (scheduler == null) {
			scheduler = this;
		}
		if (!MagicConfig.isIsEventRunning()) {
			MagicConfig.setIsEventRunning(true);
			runTaskTimer(MagicEgg.getPlugin(MagicEgg.class), 0, MagicConfig.getRepetitionDelay());
			return true;
		}
		return false;
	}

	public boolean spawnRandomEgg() {
		boolean ret = true;
		if (!MagicDropArea.getDropAreas().isEmpty()) {
			MagicDropArea area = null;
			do {
				area = MagicDropArea.get(MagicEgg.RANDOM.nextInt(MagicDropArea.getDropAreas().size()));
			} while (area == null);
			MagicEgg.instance.spawnEgg(area.getLocation());
		} else {
			ret = false;
		}
		return ret;
	}

	public void cancel() throws IllegalStateException {
		super.cancel();
		MagicConfig.setIsEventRunning(false);
		scheduler = null;
	}

}
