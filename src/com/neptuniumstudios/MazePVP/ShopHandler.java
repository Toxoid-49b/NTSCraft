package com.neptuniumstudios.MazePVP;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopHandler {
	
	public static Inventory pvpActionsMenu = Bukkit.createInventory(null, 9, "Actions");
	public static Inventory storeMenu = Bukkit.createInventory(null, 18, "Store");
	
	public enum inventoryList{
		
		pvpActionsMenu
		
	}
	
	public ShopHandler(){
		
		setupPvpActionsMenu();
		setupStoreMenu();
		
	}
	
	public void inventoryClick(InventoryClickEvent e){
		
    	Player p = (Player) e.getWhoClicked();
    	ItemStack itemClicked = e.getCurrentItem();
    	Inventory inventory = e.getInventory();
    	
    	if (inventory.getName().equals(pvpActionsMenu.getName())) {
    		
    		pvpActionsMenu(p, itemClicked, inventory, e);
    		
    	}
    	
    	if (inventory.getName().equals(storeMenu.getName())) {    		

    		storeMenu(p, itemClicked, inventory, e);
    	}
		
	}
	
	public void pvpActionsMenu(Player p, ItemStack itemClicked, Inventory i, InventoryClickEvent e){
		
		if (itemClicked.getType() == Material.BARRIER) {
			
			e.setCancelled(true);
			p.closeInventory(); 
			e.getWhoClicked().teleport(new Location(Main.mpvp_Hub, 0, 4, 0));
			e.getWhoClicked().setGameMode(GameMode.SURVIVAL);
			
			if(Main.thisMain.playersInGame.contains(e.getWhoClicked())){
			
				Main.thisMain.playersInGame.remove(e.getWhoClicked());
				
				if(Main.thisMain.gameSessionMap.containsKey(e.getWhoClicked())){
					
					Main.thisMain.gameSessionMap.get(e.getWhoClicked()).onPlayerLeave((Player) e.getWhoClicked());
					Main.thisMain.gameSessionMap.remove((Player) e.getWhoClicked());
					
				}
			
			}
			
		}
		
		if (itemClicked.getType() == Material.DIAMOND_BLOCK) {
			
			e.setCancelled(true);
			p.closeInventory();
			p.openInventory(storeMenu);
			    			
		}
		
	}	
	
	public void storeMenu(Player p, ItemStack itemClicked, Inventory i, InventoryClickEvent e){
		
		System.out.println(itemClicked.getType().toString() + " was clicked and has enchantments of type: " + itemClicked.getEnchantments().toString());
		
		if (itemClicked.getType() == Material.IRON_SWORD && itemClicked.getEnchantments().isEmpty()) {
			
			e.setCancelled(true);
			p.closeInventory();
			
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){
				
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) >= 50){
					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 50);
					p.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Thank you for your purchase!");
					Main.thisMain.updateCoins(p);
					
				}
				
			}
			
		}
		
		if (itemClicked.getType() == Material.DIAMOND_SWORD && itemClicked.getEnchantments().isEmpty()) {
			
			e.setCancelled(true);
			p.closeInventory();
			
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){
				
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) >= 100){
					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 100);
					p.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Thank you for your purchase!");
					Main.thisMain.updateCoins(p);
					
				}
				
			}
			
		}
		
		if (itemClicked.getType() == Material.GOLD_SWORD && itemClicked.getEnchantments().isEmpty()) {
			
			e.setCancelled(true);
			p.closeInventory();
			
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){
				
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) >= 25){
					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 25);
					p.getInventory().addItem(new ItemStack(Material.GOLD_SWORD));
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Thank you for your purchase!");
					Main.thisMain.updateCoins(p);
					
				}
				
			}
			
		}
		
		if (itemClicked.getType() == Material.IRON_SWORD && itemClicked.getEnchantments().containsKey(Enchantment.DAMAGE_ALL) && itemClicked.getEnchantments().containsValue(1)) {
			
			e.setCancelled(true);
			p.closeInventory();
			
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){
				
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) >= 200){
					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 200);
					ItemStack sharpSword = new ItemStack(Material.IRON_SWORD);
					sharpSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
					p.getInventory().addItem(sharpSword);
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Thank you for your purchase!");
					Main.thisMain.updateCoins(p);
					
				}
				
			}
			
		}
		
		if (itemClicked.getType() == Material.DIAMOND_SWORD && itemClicked.getEnchantments().containsKey(Enchantment.DAMAGE_ALL) && itemClicked.getEnchantments().containsValue(1)) {
			
			e.setCancelled(true);
			p.closeInventory();
			
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){
				
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) >= 500){
					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 500);
					ItemStack sharpSword = new ItemStack(Material.DIAMOND_SWORD);
					sharpSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
					p.getInventory().addItem(sharpSword);
					p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Thank you for your purchase!");
					Main.thisMain.updateCoins(p);
					
				}
				
			}
			
		}
		
	}
	
	public void setupPvpActionsMenu(){
		
    	ItemStack actionQuit = new ItemStack(Material.BARRIER);
    	ItemMeta actionQuitMeta = actionQuit.getItemMeta();
    	
    	ItemStack actionStore = new ItemStack(Material.DIAMOND_BLOCK);
    	ItemMeta actionStoreMeta = actionStore.getItemMeta();
    	
    	actionQuitMeta.setDisplayName(ChatColor.DARK_RED + "Quit Round");
    	actionQuit.setItemMeta(actionQuitMeta);
    	
    	actionStoreMeta.setDisplayName(ChatColor.AQUA + "Open Store");
    	actionStore.setItemMeta(actionStoreMeta);
    	
    	pvpActionsMenu.addItem(actionQuit);
    	pvpActionsMenu.addItem(actionStore);
		
	}
	
	public void setupStoreMenu(){
		
    	ItemStack itemIronSword = new ItemStack(Material.IRON_SWORD);
    	ItemMeta ironSwordMeta = itemIronSword.getItemMeta();
    	
    	ItemStack itemDiamondSword = new ItemStack(Material.DIAMOND_SWORD);
    	ItemMeta diamondSwordMeta = itemIronSword.getItemMeta();
    	
    	ItemStack itemGoldenSword = new ItemStack(Material.GOLD_SWORD);
    	ItemMeta goldenSwordMeta = itemIronSword.getItemMeta();
    	
    	ItemStack itemSharpIronSword = new ItemStack(Material.IRON_SWORD);
    	itemSharpIronSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
    	ItemMeta sharpIronSwordMeta = itemSharpIronSword.getItemMeta();
    	
    	ItemStack itemSharpDiamondSword = new ItemStack(Material.DIAMOND_SWORD);
    	itemSharpDiamondSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
    	ItemMeta sharpDiamondSwordMeta = itemSharpIronSword.getItemMeta();
   	
    	ironSwordMeta.setDisplayName(ChatColor.AQUA + "Iron Sword");
    	ArrayList<String> ironSwordLore = new ArrayList<String>();
    	ironSwordLore.add(ChatColor.GREEN + "50 Coins");
    	ironSwordLore.add(ChatColor.DARK_PURPLE + "A nice upgrade from the stone sword!");
    	ironSwordMeta.setLore(ironSwordLore);
    	itemIronSword.setItemMeta(ironSwordMeta);  
    	
    	diamondSwordMeta.setDisplayName(ChatColor.AQUA + "Diamond Sword");
    	ArrayList<String> diamondSwordLore = new ArrayList<String>();
    	diamondSwordLore.add(ChatColor.GREEN + "100 Coins");
    	diamondSwordLore.add(ChatColor.DARK_PURPLE + "The most sought-after weapon in Minecraft!");
    	diamondSwordMeta.setLore(diamondSwordLore);
    	itemDiamondSword.setItemMeta(diamondSwordMeta);
    	
    	goldenSwordMeta.setDisplayName(ChatColor.AQUA + "GOLDen Sword");
    	ArrayList<String> goldenSwordLore = new ArrayList<String>();
    	goldenSwordLore.add(ChatColor.GREEN + "25 Coins");
    	goldenSwordLore.add(ChatColor.DARK_PURPLE + "A GOLDen sword for those who like their swords covered in GOLD");
    	goldenSwordMeta.setLore(goldenSwordLore);
    	itemGoldenSword.setItemMeta(goldenSwordMeta);
    	
    	sharpIronSwordMeta.setDisplayName(ChatColor.AQUA + "Sharp Iron Sword");
    	ArrayList<String> sharpIronSwordLore = new ArrayList<String>();
    	sharpIronSwordLore.add(ChatColor.GREEN + "200");
    	sharpIronSwordLore.add(ChatColor.DARK_PURPLE + "Notice the attack damage!");
    	sharpIronSwordMeta.setLore(sharpIronSwordLore);
    	itemSharpIronSword.setItemMeta(sharpIronSwordMeta);
    	
    	sharpDiamondSwordMeta.setDisplayName(ChatColor.AQUA + "Sharp Diamond Sword");
    	ArrayList<String> sharpDiamondSwordLore = new ArrayList<String>();
    	sharpDiamondSwordLore.add(ChatColor.GREEN + "500");
    	sharpDiamondSwordLore.add(ChatColor.DARK_PURPLE + "A very powerful sword!");
    	sharpDiamondSwordLore.add(ChatColor.DARK_PURPLE + "Slices through just about everything!");
    	sharpDiamondSwordMeta.setLore(sharpDiamondSwordLore);
    	itemSharpDiamondSword.setItemMeta(sharpDiamondSwordMeta);
    	
    	storeMenu.addItem(itemGoldenSword);
    	storeMenu.addItem(itemIronSword);
    	storeMenu.addItem(itemDiamondSword);
    	storeMenu.addItem(itemSharpIronSword);
    	storeMenu.addItem(itemSharpDiamondSword);
		
	}
	
	public Inventory getInventory(inventoryList invToOpen){
		
		switch(invToOpen){
		case pvpActionsMenu:
			return pvpActionsMenu;
		default:
			return null;		
		}
		
	}

}
