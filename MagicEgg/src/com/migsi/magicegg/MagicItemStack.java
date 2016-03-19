package com.migsi.magicegg;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.migsi.magicegg.exceptions.InvalidProbabilityException;

public class MagicItemStack extends ItemStack {

	// Drop probability (0-100)
	private int probability = 0;

	public MagicItemStack(String[] data) throws InvalidProbabilityException {
		super(Material.getMaterial(data[0]), Integer.parseInt(data[1]));
		probability = Integer.parseInt(data[2]);
		if (probability > 100 || probability < 0) {
			throw new InvalidProbabilityException("Drop probability was set higher than 100 or lower than 0.");
		}
	}

	public MagicItemStack(Material type, int amount, int probability) throws InvalidProbabilityException {
		super(type, amount);
		this.probability = probability;
		if (probability > 100 || probability < 0) {
			throw new InvalidProbabilityException("Drop probability was set higher than 100 or lower than 0.");
		}
	}

	public int getProbability() {
		return probability;
	}

	@Override
	public String toString() {
		return getType().name() + ";" + getAmount() + ";" + probability;
	}

}
