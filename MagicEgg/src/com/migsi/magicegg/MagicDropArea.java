package com.migsi.magicegg;

import java.util.Vector;

import org.bukkit.Location;

public class MagicDropArea {

	private static Vector<MagicDropArea> DropAreas = null;

	private String world = null;
	private int x, z, radius;
	private boolean temporary = false;

	public MagicDropArea() {
		if (DropAreas == null) {
			DropAreas = new Vector<MagicDropArea>();
		}
	}

	public MagicDropArea(Location loc, int rad, boolean temp) {
		world = loc.getWorld().getName();
		x = loc.getBlockX();
		z = loc.getBlockZ();
		radius = rad;
		temporary = temp;
		if (!contains(this)) {
			DropAreas.add(this);
		}
	}

	public MagicDropArea(MagicDropArea area) {
		world = area.getWorld();
		x = area.getX();
		z = area.getZ();
		radius = area.getRadius();
		temporary = area.isTemporary();
		if (!contains(this)) {
			DropAreas.add(this);
		}
	}

	public MagicDropArea(String[] data) {
		world = data[0];
		x = Integer.parseInt(data[1]);
		z = Integer.parseInt(data[2]);
		radius = Integer.parseInt(data[3]);
		temporary = Boolean.parseBoolean(data[4]);
		if (!contains(this)) {
			DropAreas.add(this);
		}
	}

	public static Vector<MagicDropArea> getDropAreas() {
		return DropAreas;
	}

	public static void setDropAreas(Vector<MagicDropArea> dropAreas) {
		DropAreas = dropAreas;
	}

	public boolean contains(MagicDropArea area) {
		for (MagicDropArea temp : DropAreas) {
			if (temp.equals(area)) {
				return true;
			}
		}
		return false;
	}

	public boolean equals(MagicDropArea area) {
		if (world.equals(area.getWorld())) {
			if (x == area.getX()) {
				if (z == area.getZ()) {
					if (temporary == area.isTemporary()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean equals(Location loc) {
		if (world.equals(loc.getWorld().getName())) {
			if (x == loc.getBlockX()) {
				if (z == loc.getBlockZ()) {
					return true;
				}
			}
		}
		return false;
	}

	public MagicDropArea get(Location loc) {
		for (MagicDropArea area : DropAreas) {
			if (area.equals(loc)) {
				return area;
			}
		}
		return null;
	}

	public static MagicDropArea get(int index) {
		return DropAreas.get(index);
	}

	public boolean remove(Location loc) {
		return DropAreas.remove(get(loc));
	}

	public static boolean remove(MagicDropArea area) {
		if (DropAreas != null) {
			return DropAreas.remove(area);
		}
		return false;
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public int getRadius() {
		return radius;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public MagicDropArea getByLocation(Location loc) {
		return null;
	}

	public Location getLocation() {
		return new Location(MagicEgg.instance.getServer().getWorld(world), x,
				MagicEgg.instance.getServer().getWorld(world).getHighestBlockYAt(x, z), z);
	}

	// TODO for random spawn locations of the egg use this method to get a
	// random location in the defined area, then create a new temporary DropArea
	// with same radius like defined one, then spawn egg
	public Location getRandLocation() {
		int rx = x - radius + MagicEgg.RANDOM.nextInt(radius * 2 + 1);
		int rz = z - radius + MagicEgg.RANDOM.nextInt(radius * 2 + 1);
		return new Location(MagicEgg.instance.getServer().getWorld(world), rx,
				MagicEgg.instance.getServer().getWorld(world).getHighestBlockYAt(rx, rz), rz);
	}

	public String toString() {
		return world + ";" + x + ";" + z + ";" + radius + ";" + temporary;
	}

}
