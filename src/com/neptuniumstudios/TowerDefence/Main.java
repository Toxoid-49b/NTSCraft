package com.neptuniumstudios.TowerDefence;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	public static Inventory towersMenu = Bukkit.createInventory(null, 9, "Towers");
	
    @Override
    public void onEnable() {
    	
    	getServer().getPluginManager().registerEvents(this, this);
    	
    	
    }
    
    @EventHandler
    public void onSignUse(PlayerInteractEvent event)
    {
    	
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            
        	if (event.getClickedBlock().getType() == Material.BEACON)
            {
            	
        		event.setCancelled(true);
        		event.getPlayer().openInventory(towersMenu);
        		
            }
        	
        }
        
    }
    
	public void setupPvpActionsMenu(){
		
    	ItemStack towerArrow = new ItemStack(Material.DISPENSER);
    	ItemMeta towerArrowMeta = towerArrow.getItemMeta();

    	towerArrowMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Archer Tower");
    	ArrayList<String> towerArrowLore = new ArrayList<String>();
    	towerArrowLore.add(ChatColor.GREEN + "A basic tower that shoot arrows.");
    	towerArrowMeta.setLore(towerArrowLore);
    	towerArrow.setItemMeta(towerArrowMeta);

    	towersMenu.addItem(towerArrow);
		
	}

}
