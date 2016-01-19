package com.neptuniumstudios.MazePVP;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GameSession{
	
	ArrayList<Player> playersInGame = new ArrayList<Player>();
	
	public enum gameState{
		
		Rebooting,
		Queueing,
		InGame
		
	}
	
	public enum sessionMap {
		
		None,
		Hedge01,
		Ruins
		
		
	}
	
	public gameState currentState;
	public int playersInServer;
	public Boolean canJoin;
	
	Player gameWinner;
	
	sessionMap currentMap;
	
	int countdownCounter;
	
	
	public GameSession(sessionMap gameMap){
		
		currentState = gameState.Queueing;
		
		if(gameMap == sessionMap.Hedge01){
		
			currentMap = sessionMap.Hedge01;
			Hedge01Setup(Main.getWorldByMap(sessionMap.Hedge01), Material.LEAVES, (byte)5);
		
		}
		
		if(gameMap == sessionMap.Ruins){
		
			currentMap = sessionMap.Ruins;
			RuinsSetup(Main.getWorldByMap(sessionMap.Ruins), Material.IRON_FENCE);
		
		}
		
		countdownCounter = 11;
		
		canJoin = true;
				
	}

	void onPlayerJoin(Player p){
		
		playersInServer++;
		
		if(playersInServer > 4){
			
			playersInServer--;
			p.teleport(Main.thisServer.getWorld("world").getSpawnLocation());
			p.sendMessage(ChatColor.RED + "We're sorry, but there seems to be an error joining!");
			p.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Please notify an admin of error code \"sfull_join\" for 100 free coins!");
			
		} else if(currentState == gameState.InGame) {
			
			playersInServer--;
			p.teleport(Main.thisServer.getWorld("world").getSpawnLocation());
			p.sendMessage(ChatColor.RED + "We're sorry, but there seems to be an error joining!");
			p.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Please notify an admin of error code \"sactive_join\" for 100 free coins!");
			
		} else {
			
			playersInGame.add(p);
			
			givePlayerKits(p);
			
			doPlayerTeleportation(currentMap, p, playersInServer);
			
			for(Player ptn : playersInGame){
				ptn.sendMessage(p.getDisplayName() + " has joined the match! (" + playersInServer + "\\4)");
			}
			
		}
		
		if(playersInServer == 4){
			
			canJoin = false;
			new BukkitRunnable() {
		       	 
				@Override
				public void run() {
					
					startGame();
        		
				}
 
			}.runTaskLater(Bukkit.getPluginManager().getPlugin("MazePVP"), 20*3);
			
		}
		
	}
	
	void onPlayerLeave(Player p){
		
		if(currentState == gameState.Queueing){
			
			playersInServer--;
			playersInGame.remove(p);
			removePlayerKits(p);
			for(Player pl : playersInGame){
				pl.sendMessage("Player " + p.getDisplayName() + " has left the game (" + playersInServer + "\\4)");
			}
			
		} else if (currentState == gameState.InGame && p.getGameMode() != GameMode.SPECTATOR) {
			
			playersInServer--;
			playersInGame.remove(p);
			removePlayerKits(p);
			if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){					
				Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) - 15);
				Main.thisMain.updateCoins(p);					
			}
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You left a currently active game, you have been fined 15 coins!");
			for(Player pl : playersInGame){
				pl.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC +"Player " + p.getDisplayName() + "ragequit, everyone gets a bonus 5 coins!");
				if(Main.thisMain.playerCoinBank.get(pl.getUniqueId()) != null){					
					Main.thisMain.playerCoinBank.put(pl.getUniqueId(), Main.thisMain.playerCoinBank.get(pl.getUniqueId()) + 5);
					Main.thisMain.updateCoins(pl);					
				}
			}
			
		} else {
			
			playersInServer--;
			playersInGame.remove(p);
			removePlayerKits(p);
			
		}
		
	}

	private void startGame() {
		
		System.out.println("A game is starting!");
		
		doGameCountdown();
		
	}
	
	public void onGameEnd(){
		
		final World temp = gameWinner.getWorld();
		
		for(Player p : temp.getPlayers()){
			if(p != gameWinner){
				p.sendMessage(ChatColor.BLUE + "" + gameWinner.getDisplayName() + " has won the game!");
			} else {
				p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You have won the game, you win 25 coins!");
				if(Main.thisMain.playerCoinBank.get(p.getUniqueId()) != null){					
					Main.thisMain.playerCoinBank.put(p.getUniqueId(), Main.thisMain.playerCoinBank.get(p.getUniqueId()) + 25);
					Main.thisMain.updateCoins(p);					
				}
			}
			
			p.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "The map will reboot in 10 seconds.");

		}
		
		new BukkitRunnable() {
	       	 
			@Override
			public void run() {
				
				rebootGameSession(temp);
    		
			}

		}.runTaskLater(Bukkit.getPluginManager().getPlugin("MazePVP"), 20*10);
		
	}
	
	public void rebootGameSession(World w){
		
		removePlayerKits(gameWinner);
		
		for(Player p : w.getPlayers()){
			p.teleport(new Location(Main.mpvp_Hub, 0, 4, 0));
			p.setGameMode(GameMode.SURVIVAL);
			Main.thisMain.playersInGame.remove(p);
		}
		
		switch(currentMap){
		case Hedge01:
			Main.thisMain.destroyGameSession("HM_01");
			break;
		case Ruins:
			Main.thisMain.destroyGameSession("RUINS");
			break;
		case None:
			break;
		default:
			break;
		
		}
		
	}
	
	public void onPlayerDeath(Player killer, Player victim){
		
		playersInGame.remove(victim);
		removePlayerKits(victim);
		
		if(Main.thisMain.playerCoinBank.get(killer.getUniqueId()) != null && killer != null){
				
				Main.thisMain.playerCoinBank.put(killer.getUniqueId(), Main.thisMain.playerCoinBank.get(killer.getUniqueId()) + 5);
				killer.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "You got 5 coins for killing " + victim.getDisplayName() + "!");
				Main.thisMain.updateCoins(killer);
				
		}
		
		if(playersInGame.size() == 1){
			
			if(killer != null){
			
				gameWinner = killer;
				onGameEnd();
			
			} else {
				
				gameWinner = playersInGame.get(0);
				onGameEnd();
				
			}				
			
		}
		
	}
	
	public void testForWinner(){
		
		if(playersInGame.size() == 1){
			
			gameWinner = playersInGame.get(0);
			onGameEnd();			
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void Hedge01Setup(World w, Material m, byte md){
		
		w.getBlockAt(new Location(w, 8.5f, 1.0f, 0.5f)).setType(m);
		w.getBlockAt(new Location(w, 8.5f, 2.0f, 0.5f)).setType(m);
		w.getBlockAt(new Location(w, 8.5f, 3.0f, 0.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 47.5f, 1.0f, 0.5f)).setType(m);
		w.getBlockAt(new Location(w, 47.5f, 2.0f, 0.5f)).setType(m);
		w.getBlockAt(new Location(w, 47.5f, 3.0f, 0.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 8.5f, 1.0f, 37.5f)).setType(m);
		w.getBlockAt(new Location(w, 8.5f, 2.0f, 37.5f)).setType(m);
		w.getBlockAt(new Location(w, 8.5f, 3.0f, 37.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 47.5f, 1.0f, 37.5f)).setType(m);
		w.getBlockAt(new Location(w, 47.5f, 2.0f, 37.5f)).setType(m);
		w.getBlockAt(new Location(w, 47.5f, 3.0f, 37.5f)).setType(m);
		
		if(md != -1){
			
			w.getBlockAt(new Location(w, 8.5f, 1.0f, 0.5f)).setData(md);
			w.getBlockAt(new Location(w, 8.5f, 2.0f, 0.5f)).setData(md);
			w.getBlockAt(new Location(w, 8.5f, 3.0f, 0.5f)).setData(md);
		
			w.getBlockAt(new Location(w, 47.5f, 1.0f, 0.5f)).setData(md);
			w.getBlockAt(new Location(w, 47.5f, 2.0f, 0.5f)).setData(md);
			w.getBlockAt(new Location(w, 47.5f, 3.0f, 0.5f)).setData(md);
			
			w.getBlockAt(new Location(w, 8.5f, 1.0f, 37.5f)).setData(md);
			w.getBlockAt(new Location(w, 8.5f, 2.0f, 37.5f)).setData(md);
			w.getBlockAt(new Location(w, 8.5f, 3.0f, 37.5f)).setData(md);
			
			w.getBlockAt(new Location(w, 47.5f, 1.0f, 37.5f)).setData(md);
			w.getBlockAt(new Location(w, 47.5f, 2.0f, 37.5f)).setData(md);
			w.getBlockAt(new Location(w, 47.5f, 3.0f, 37.5f)).setData(md);
		
		}
	
	}
	
	private void RuinsSetup(World w, Material m) {

		w.getBlockAt(new Location(w, 2.5f, 1.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 2.5f, 2.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 2.5f, 3.0f, 2.5f)).setType(m);		
		w.getBlockAt(new Location(w, 3.5f, 1.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 3.5f, 2.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 3.5f, 3.0f, 2.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 3.5f, 1.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 3.5f, 2.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 3.5f, 3.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 2.5f, 1.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 2.5f, 2.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 2.5f, 3.0f, -42.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 39.5f, 1.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 39.5f, 2.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 39.5f, 3.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 1.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 2.0f, 2.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 3.0f, 2.5f)).setType(m);
		
		w.getBlockAt(new Location(w, 39.5f, 1.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 39.5f, 2.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 39.5f, 3.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 1.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 2.0f, -42.5f)).setType(m);
		w.getBlockAt(new Location(w, 38.5f, 3.0f, -42.5f)).setType(m);
		
	}

	
	public void doPlayerTeleportation(sessionMap sm, Player p, int index){
		
		if(sm == sessionMap.Hedge01){
			
			Hedge01SpawnMan(p, index);
			
		}
		
		if(sm == sessionMap.Ruins){
			
			RuinsSpawnMan(p, index);
			
		}
		
	}
	
	void Hedge01SpawnMan(Player p, int index){
		
		if(index == 1){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Hedge01), 0.5f, 1.0f, 0.5f));		
		}
		if(index == 2){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Hedge01), 55.5f, 1.0f, 0.5f));		
		}
		if(index == 3){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Hedge01), 0.5f, 1.0f, 37.5f));		
		}
		if(index == 4){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Hedge01), 55.5f, 1.0f, 37.5f));		
		}
		
	}
	
	void RuinsSpawnMan(Player p, int index){
		
		if(index == 1){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Ruins), 3.0f, 1.0f, 10.0f));		
		}
		if(index == 2){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Ruins), 3.0f, 1.0f, -50.0f));		
		}
		if(index == 3){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Ruins), 39.0f, 1.0f, 10.0f));		
		}
		if(index == 4){
			p.teleport(new Location(Main.getWorldByMap(sessionMap.Ruins), 39.0f, 1.0f, -50.0f));		
		}
		
	}
	
	void doGameCountdown(){
		
		countdownCounter--;
		
		for(Player p : playersInGame){
			
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "Game begins in " + countdownCounter + " seconds!");
			
		}
		
		if(countdownCounter < 4 && countdownCounter > 0){
			
			for(Player p : playersInGame){

				sendTitle(p, 0, 10, 10, ChatColor.YELLOW + "" + countdownCounter, "");
				p.getWorld().playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f);
				
			}
			
		}
		
		if(countdownCounter == 0){
			
			for(Player p : playersInGame){

				sendTitle(p, 0, 10, 10, ChatColor.GREEN + "GO!", "");
				p.getWorld().playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
				
			}
			
		}
		
		if(countdownCounter > 0){
		
			new BukkitRunnable() {
       	 
				@Override
				public void run() {
					
					doGameCountdown();
        		
				}
 
			}.runTaskLater(Bukkit.getPluginManager().getPlugin("MazePVP"), 20);
		
		} else {
			
			currentState = gameState.InGame;
			beginGame();
			
		}
		
	}
	
	void beginGame(){
		
		if(currentMap == sessionMap.Hedge01){

			Hedge01Setup(Main.getWorldByMap(sessionMap.Hedge01), Material.AIR, (byte)-1);
			
			for(Player pl : playersInGame){
				pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "The Game Has Begun!");
			}
		
		}
		
		if(currentMap == sessionMap.Ruins){

			RuinsSetup(Main.getWorldByMap(sessionMap.Ruins), Material.AIR);
			
			for(Player pl : playersInGame){
				pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "The Game Has Begun!");
			}
		
		}
		
	}
	
    public static void sendPacket(Player player, Object packet) {
    	
    	try {
    		
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
            
        } catch (Exception e) {
        	
            e.printStackTrace();
            
        }
    	
    }
    
    public static Class<?> getNMSClass(String name) {
    	
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        
        try {
        	
            return Class.forName("net.minecraft.server." + version + "." + name);
            
        } catch (ClassNotFoundException e) {
        	
            e.printStackTrace();            
            return null;
        }
        
    }
    
    @SuppressWarnings("rawtypes")
	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
    	
        try {
            
        	Object e;
            Object chatTitle;
            Object chatSubtitle;
            Constructor subtitleConstructor;
            Object titlePacket;
            Object subtitlePacket;

            if (title != null) {

                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
                chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle, fadeIn, stay, fadeOut});
                sendPacket(player, titlePacket);

                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get((Object) null);
                chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")});
                titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle});
                sendPacket(player, titlePacket);
                
            }

            if (subtitle != null) {

                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
                chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + title + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
                sendPacket(player, subtitlePacket);

                e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get((Object) null);
                chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke((Object) null, new Object[]{"{\"text\":\"" + subtitle + "\"}"});
                subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
                subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
                sendPacket(player, subtitlePacket);
                
            }
            
        } catch (Exception var11) {
        	
            var11.printStackTrace();
            
        }
    	
    }
    
    public void givePlayerKits(Player p){
    	
    	if(Main.playerKitMap.containsKey(p)){
    		
    		Main.playerKits currentKit = Main.playerKitMap.get(p);
    		
    		 switch (currentKit) {
    		 
    		 case Swordsman:
                p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                p.getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
                p.getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
                p.getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
                p.getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));
                break;
    		case Axeman:            	
            	p.getInventory().addItem(new ItemStack(Material.STONE_AXE));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));            	
				break;
    		case Brawler:
            	p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_HELMET));
            	p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            	p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            	p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS));
				break;
			case Mage:
            	ItemStack lightRod = new ItemStack(Material.STICK);
            	lightRod.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
            	p.getInventory().addItem(lightRod);
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));
				break;
			case Shadow:
            	ItemStack knockRod = new ItemStack(Material.STICK);
            	knockRod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
            	knockRod.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            	p.getPlayer().getInventory().addItem(knockRod);
            	p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
            	p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));
				break;
			default:
				break;
    		 		
    		 
    		 }
    		
    	} else {
    		
    		p.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "[WARNING!] " + ChatColor.RESET + "" + ChatColor.RED + " You have not yet selected a kit! Go to the hub and select a kit before starting!");
    		
    	}
    	
    }
    
    public void removePlayerKits(Player p){
    	
    	if(Main.playerKitMap.containsKey(p)){

    		Main.playerKits currentKit = Main.playerKitMap.get(p);
    		
    		switch (currentKit) {
			case Axeman:
            	p.getInventory().remove(new ItemStack(Material.STONE_AXE));
            	p.getInventory().remove(new ItemStack(Material.LEATHER_HELMET));
            	p.getInventory().remove(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getInventory().remove(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getInventory().remove(new ItemStack(Material.LEATHER_BOOTS)); 
            	removeArmorKits(Main.playerKits.Axeman, p);
				break;
			case Brawler:
            	p.getInventory().remove(new ItemStack(Material.CHAINMAIL_HELMET));
            	p.getInventory().remove(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            	p.getInventory().remove(new ItemStack(Material.CHAINMAIL_LEGGINGS));
            	p.getInventory().remove(new ItemStack(Material.CHAINMAIL_BOOTS));
            	removeArmorKits(Main.playerKits.Brawler, p);
				break;
			case Mage:
            	ItemStack lightRod = new ItemStack(Material.STICK);
            	lightRod.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
            	p.getPlayer().getInventory().remove(lightRod);
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_HELMET));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_BOOTS));
            	removeArmorKits(Main.playerKits.Mage, p);
				break;
			case Shadow:
            	ItemStack knockRod = new ItemStack(Material.STICK);
            	knockRod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
            	knockRod.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            	p.getPlayer().getInventory().remove(knockRod);
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_HELMET));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_CHESTPLATE));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_LEGGINGS));
            	p.getPlayer().getInventory().remove(new ItemStack(Material.LEATHER_BOOTS));
            	removeArmorKits(Main.playerKits.Shadow, p);
				break;
			case Swordsman:
                p.getInventory().remove(new ItemStack(Material.STONE_SWORD));
                p.getInventory().remove(new ItemStack(Material.LEATHER_HELMET));
                p.getInventory().remove(new ItemStack(Material.LEATHER_CHESTPLATE));
                p.getInventory().remove(new ItemStack(Material.LEATHER_LEGGINGS));
                p.getInventory().remove(new ItemStack(Material.LEATHER_BOOTS));
                removeArmorKits(Main.playerKits.Swordsman, p);
				break;
			default:
				break;    		
    		}
    		
    	}
    	
    }
    
    public void removeArmorKits(Main.playerKits pk, Player p){
    	
    	switch (pk) {
		case Axeman:
			testArmor(new ItemStack(Material.LEATHER_HELMET), p);
			testArmor(new ItemStack(Material.LEATHER_CHESTPLATE), p);
			testArmor(new ItemStack(Material.LEATHER_LEGGINGS), p);
			testArmor(new ItemStack(Material.LEATHER_BOOTS), p);
			break;
		case Brawler:
			testArmor(new ItemStack(Material.CHAINMAIL_HELMET), p);
			testArmor(new ItemStack(Material.CHAINMAIL_CHESTPLATE), p);
			testArmor(new ItemStack(Material.CHAINMAIL_LEGGINGS), p);
			testArmor(new ItemStack(Material.CHAINMAIL_BOOTS), p);
			break;
		case Mage:
			testArmor(new ItemStack(Material.LEATHER_HELMET), p);
			testArmor(new ItemStack(Material.LEATHER_CHESTPLATE), p);
			testArmor(new ItemStack(Material.LEATHER_LEGGINGS), p);
			testArmor(new ItemStack(Material.LEATHER_BOOTS), p);
			break;
		case Shadow:
			testArmor(new ItemStack(Material.LEATHER_HELMET), p);
			testArmor(new ItemStack(Material.LEATHER_CHESTPLATE), p);
			testArmor(new ItemStack(Material.LEATHER_LEGGINGS), p);
			testArmor(new ItemStack(Material.LEATHER_BOOTS), p);
			break;
		case Swordsman:
			testArmor(new ItemStack(Material.LEATHER_HELMET), p);
			testArmor(new ItemStack(Material.LEATHER_CHESTPLATE), p);
			testArmor(new ItemStack(Material.LEATHER_LEGGINGS), p);
			testArmor(new ItemStack(Material.LEATHER_BOOTS), p);
			break;
		default:
			break;    	
    	}
    	
    }
    
    void testArmor(ItemStack itemToTest, Player player){
    	
    	if(player.getInventory().getHelmet() != null){
    	
    		if(player.getInventory().getHelmet().getType() == itemToTest.getType()){

    			player.getInventory().setHelmet(null);
    			
    		}
    	
    	}
    	
    	if(player.getInventory().getChestplate() != null){
    		
    		if(player.getInventory().getChestplate().getType() == itemToTest.getType()) {

    			player.getInventory().setChestplate(null);
    			
    		}
    		
    	}
    	
    	if(player.getInventory().getLeggings() != null){
    	
    		if(player.getInventory().getLeggings().getType() == itemToTest.getType()) {

    			player.getInventory().setLeggings(null);
    			
    		}
    	
    	}
    	
    	if(player.getInventory().getBoots() != null){
    	
    		if(player.getInventory().getBoots().getType() == itemToTest.getType()) {
    			
    			player.getInventory().setBoots(null);
    			
    		}
    	
    	}
    	
    }
    
    public boolean startGameEarly(){
    	
		canJoin = false;
		new BukkitRunnable() {
		       	 
			@Override
			public void run() {
					
				startGame();
        		
			}
 
		}.runTaskLater(Bukkit.getPluginManager().getPlugin("MazePVP"), 20*3);
		
		return true;
			
    }
	 
}
