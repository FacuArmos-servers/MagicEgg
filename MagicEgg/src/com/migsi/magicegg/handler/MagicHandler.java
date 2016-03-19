package com.migsi.magicegg.handler;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.migsi.magicegg.MagicEgg;

public class MagicHandler implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onRightClickOne(PlayerInteractEvent evt) {
        Block block = evt.getClickedBlock();
        evt.setCancelled(true);
        if (MagicEgg.instance.getMap().containsKey(block)) {
            if (block.getType() == Material.DRAGON_EGG) {
                if (evt.getAction() == Action.RIGHT_CLICK_BLOCK || evt.getAction() == Action.LEFT_CLICK_BLOCK) {
                    MagicEgg.instance.deleteEgg(block);
                    MagicEgg.instance.fireworks(block.getLocation().add(.5, 0, .5));
                    MagicEgg.instance.dropItems(block.getLocation());
                } else evt.setCancelled(false);
            } else evt.setCancelled(false);
        } else evt.setCancelled(false);
    }

}
