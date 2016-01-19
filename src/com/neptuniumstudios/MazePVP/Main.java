package com.neptuniumstudios.MazePVP;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {

	static World mpvp_Hedge01;
	static World mpvp_Hub;
	static World mpvp_Ruins;
	
	public ArrayList<Player> playersInGame = new ArrayList<Player>();
	Map<Player,Scoreboard> playerSBs = new HashMap<Player,Scoreboard>();
	Map<Player, GameSession> gameSessionMap = new HashMap<Player, GameSession>();
	Map<UUID,Integer> playerCoinBank;
	ScoreboardManager sbManager;
	Boolean doCoinSave;
	public static Map<Player,playerKits> playerKitMap = new HashMap<Player,playerKits>();
	ShopHandler thisShopHandler;	
	
	GameSession HM_01;
	GameSession RUINS;
	
	public static Server thisServer;
	public static Main thisMain;
	
	public enum playerKits {
		
		Swordsman,
		Axeman,
		Shadow,
		Mage,
		Brawler
		
	}
	
	public static World getWorldByMap(GameSession.sessionMap worldToGet){
		
		if(worldToGet == GameSession.sessionMap.Hedge01){		
			return mpvp_Hedge01;
		}
		
		if(worldToGet == GameSession.sessionMap.Ruins){		
			return mpvp_Ruins;
		}
		
		System.out.println("We returned null, not anything else...");
		
		return null;
		
	}
	

	
    @Override
    public void onEnable() {
    	
    	System.out.println("-=MazePVP for Minecraft 1.8.8 by Toxoid_49b=-");
    	System.out.println("[MazePVP] Registering Events...");
    	getServer().getPluginManager().registerEvents(this, this);
    	System.out.println("[MazePVP] Getting world \"world\"...");
    	mpvp_Hub = getServer().getWorld("world");
    	System.out.println("[MazePVP] Getting world \"Hedge01\"...");
    	mpvp_Hedge01 = getServer().getWorld("MPVP_Hedge01");
    	System.out.println("[MazePVP] Getting world \"Ruins\"...");
    	mpvp_Ruins = getServer().getWorld("MPVP_Ruins");  
    	
    	if(mpvp_Hub == null){   		

    		WorldCreator creator = new WorldCreator("MPVP_Hub");
    		mpvp_Hub = creator.createWorld();
        	mpvp_Hub.setSpawnLocation(0, 4, 0);
    		
    	}
    	
    	if(mpvp_Hedge01 == null){   		

    		WorldCreator creator = new WorldCreator("MPVP_Hedge01");
    		mpvp_Hedge01 = creator.createWorld();
        	mpvp_Hedge01.setSpawnLocation(0, 4, 0);
    		
    	}
    	
    	if(mpvp_Ruins == null){   		

    		WorldCreator creator = new WorldCreator("MPVP_Ruins");
    		mpvp_Ruins = creator.createWorld();
        	mpvp_Ruins.setSpawnLocation(0, 4, 0);
    		
    	}
    	
    	mpvp_Hub.setPVP(false);
    	
    	sbManager = Bukkit.getScoreboardManager();
    	
    	if(!getServer().getOnlineMode()){
    		
    		doCoinSave = false;
        	System.out.println("[MazePVP] [WARNING] The server is running in OFFLINE MODE! The server will make NO ATTEMPT to save player stats! All progress made by players is NOT safe! Please enable online mode to enable stat saving!");
    	
    	} else {
    		
    		doCoinSave = true;
    		
    	}
    	
    	thisServer = getServer();
    	
        try
        {
           FileInputStream fis = new FileInputStream("playercoins.ser");
           ObjectInputStream ois = new ObjectInputStream(fis);
           @SuppressWarnings("unchecked")
           HashMap<UUID,Integer> readObject = (HashMap<UUID,Integer>) ois.readObject();
           playerCoinBank = readObject;
	       ois.close();
	       fis.close();
        
        } catch (IOException e) {
        	
        	e.printStackTrace();
        	
        } catch (ClassNotFoundException e) {
			
        	e.printStackTrace();
        	
		}
        
        if(playerCoinBank == null){
        	
        	playerCoinBank = new HashMap<UUID, Integer>();
        	
        }
        
        HM_01 = new GameSession(GameSession.sessionMap.Hedge01);
        RUINS = new GameSession(GameSession.sessionMap.Ruins);
    	
        thisMain = this;
        thisShopHandler = new ShopHandler();
    	
    }
    
    @Override
    public void onDisable() {
        
    	System.out.println("-=MazePVP for Minecraft 1.8.8 by Toxoid_49b=-");
    	System.out.println("[MazePVP] Pluggin Disabled!");
    	
    	if(doCoinSave){
    		
    		try
    		{
    		   FileOutputStream fos = new FileOutputStream("playercoins.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(playerCoinBank);
               oos.close();
               fos.close();
               
    		} catch (IOException e){
        	
    			System.out.println("[MazePVP] [CRITICAL ERROR] Could not save player coins! Stacktrace below:");
    			e.printStackTrace();
        	
    		}
    		
    	}
    	
    }
    
    @EventHandler
    public void onSignChange(SignChangeEvent sign) {
  
    	Player player = sign.getPlayer();
        
        if (sign.getLine(0).equalsIgnoreCase("[bkSword]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.GREEN + "" + ChatColor.BOLD + "Swordsman");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[bkAxe]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.GOLD + "" + ChatColor.BOLD + "Axeman");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[bkShadow]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Shadow");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[bkMage]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.BLUE + "" + ChatColor.BOLD + "Mage");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[bkBrawler]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + "Brawler");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[mapHedge01]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.GREEN + "" + ChatColor.ITALIC + "Hedgemaze 1");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
        if (sign.getLine(0).equalsIgnoreCase("[mapRuins]") && player.isOp()){
        	
            sign.setLine(0, ChatColor.GREEN + "" + ChatColor.ITALIC + "Ruins");
            player.sendMessage(ChatColor.DARK_AQUA + "[MazePVP] " + ChatColor.GREEN + "Sign Created!");
            
        }
        
    }
    
    @EventHandler
    public void onSignUse(PlayerInteractEvent event)
    {
    	
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
        	
            if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
            {
            	
                Sign s = (Sign) event.getClickedBlock().getState();
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.ITALIC + "Hedgemaze 1"))
                {

                	playersInGame.add(event.getPlayer());
                	HM_01.onPlayerJoin(event.getPlayer());
                	gameSessionMap.put(event.getPlayer(), HM_01);
                	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.ITALIC + "Ruins"))
                {

                	playersInGame.add(event.getPlayer());
                	RUINS.onPlayerJoin(event.getPlayer());
                	gameSessionMap.put(event.getPlayer(), RUINS);
                	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "" + ChatColor.BOLD + "Swordsman"))
                {
                	
                	event.getPlayer().sendMessage(ChatColor.GREEN + "Kit Set To SWORDSMAN!");          		
                	playerKitMap.put(event.getPlayer(), playerKits.Swordsman);
                 	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.GOLD + "" + ChatColor.BOLD + "Axeman"))
                {

                	event.getPlayer().sendMessage(ChatColor.GREEN + "Kit Set To AXEMAN!");
                	playerKitMap.put(event.getPlayer(), playerKits.Axeman);
                	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Shadow"))
                {
                	
                	event.getPlayer().sendMessage(ChatColor.GREEN + "Kit Set To SHADOW!");
                	playerKitMap.put(event.getPlayer(), playerKits.Shadow);
                	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.BLUE + "" + ChatColor.BOLD + "Mage"))
                {                	

                	event.getPlayer().sendMessage(ChatColor.GREEN + "Kit Set To MAGE!"); 
                	playerKitMap.put(event.getPlayer(), playerKits.Mage);
                	
                }
                
                if(s.getLine(0).equalsIgnoreCase(ChatColor.RED + "" + ChatColor.BOLD + "Brawler"))
                { 
                	
                	event.getPlayer().sendMessage(ChatColor.GREEN + "Kit Set To BRAWLER!");
                	playerKitMap.put(event.getPlayer(), playerKits.Brawler);
                	
                } 
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    	
    	thisShopHandler.inventoryClick(event);
    	
    }
    
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
    	
    	e.getPlayer().setGameMode(GameMode.SURVIVAL);
    	if(!e.getPlayer().getInventory().contains(new ItemStack(Material.NETHER_STAR))){
    		
        	e.getPlayer().getInventory().addItem(new ItemStack(Material.NETHER_STAR));
    		
    	}
    	e.getPlayer().teleport(new Location(mpvp_Hub, 0, 4, 0));
    	
        Scoreboard scoreBoard = sbManager.getNewScoreboard();
        Objective playerCoins = scoreBoard.registerNewObjective("Stats", "dummy");
        playerCoins.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerCoins.setDisplayName("-=Stats=-");
        Score score = playerCoins.getScore(ChatColor.GREEN + "Coins");
        
        if(playerCoinBank != null){
        
        	if(playerCoinBank.get(e.getPlayer().getUniqueId()) != null){
        
        		score.setScore(playerCoinBank.get(e.getPlayer().getUniqueId()));
        
        	} else {
        	
        		playerCoinBank.put(e.getPlayer().getUniqueId(), 0);
        		score.setScore(0);
        	
        	}
        
        }
        
        playerSBs.put(e.getPlayer(), scoreBoard);
        e.getPlayer().setScoreboard(scoreBoard);
    	
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent  e){
    	
    	e.setCancelled(true);
    	
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent  e){
    	
    	if(playersInGame.contains(e.getPlayer())){    		
    		
    		e.setRespawnLocation(new Location(e.getPlayer().getWorld(), 0, 20, 0));
    		e.getPlayer().setGameMode(GameMode.SPECTATOR);
    		
    	} else {
    	
    		e.setRespawnLocation(new Location(mpvp_Hub, 0, 4, 0));
    		e.getPlayer().setGameMode(GameMode.SURVIVAL);
    	
    	}
    	
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	
    	if (command.getName().equalsIgnoreCase("showactions")) {
            Player temp = (Player)sender;
            temp.openInventory(thisShopHandler.getInventory(ShopHandler.inventoryList.pvpActionsMenu));
    		return true;
    	}
    	
    	if (command.getName().equalsIgnoreCase("givecoins")) {
    		System.out.println("Give coins called!");
            Player temp = (Player)sender;
            if(args.length == 2){
            	System.out.println("Args are 2...");
            	if(args[0] != null){
            		System.out.println("Args[0] not null...");
            		Player coinReceiver = this.getServer().getPlayer(args[0]);
            		if(coinReceiver != null){
                		System.out.println("Reciever is not null...");
            			if(args[1] != null && Integer.parseInt(args[1]) > 0){
                    		System.out.println("Args[1] not null and greater than 0...");
            				if(temp.isOp()){
            					System.out.println("Is OP...");
            					Scoreboard sb = playerSBs.get(coinReceiver);
            					if(sb != null){
            						System.out.println("Scoreboard not null...");
            						Objective obj = sb.getObjective("Stats");
            						if(obj != null){
            							System.out.println("Objective not null...");
            							Score sc = obj.getScore(ChatColor.GREEN + "Coins");
            							if(sc != null){
            								System.out.println("Score not null, transfering!");
            								sc.setScore(sc.getScore() + Integer.parseInt(args[1]));
            								playerCoinBank.put(coinReceiver.getUniqueId(), playerCoinBank.get(coinReceiver.getUniqueId()) + Integer.parseInt(args[1]));
            								temp.sendMessage(ChatColor.GREEN + "Sucessfully gave coins to " + coinReceiver.getDisplayName() + "!");
            								coinReceiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your have received " + args[1] + " coins from " + temp.getDisplayName() + "!");
            							}
            						}
            					}
            				}else{
            					System.out.println("Is NOT OP...");
            					Scoreboard sb = playerSBs.get(coinReceiver);
            					if(sb != null){
            						System.out.println("Scoreboard not null...");
            						Objective obj = sb.getObjective("Stats");
            						if(obj != null){
            							System.out.println("Objective not null...");
            							Score sc = obj.getScore(ChatColor.GREEN + "Coins");
            							if(sc != null){
            								System.out.println("Score not null...");
            								Scoreboard gsb = playerSBs.get(temp);
            								if(gsb != null){
            									System.out.println("Giver Scoreboard not null...");
            									Objective gobj = gsb.getObjective("Stats");
            									if(gobj != null){
            										System.out.println("Giver Objective not null...");
            										Score gsc = gobj.getScore(ChatColor.GREEN + "Coins");
            										if(gsc != null){
            											System.out.println("Giver Score not null...");
            											if(Integer.parseInt(args[1]) <= gsc.getScore()){
            												System.out.println("Giver has the funds, transfering!");            												
            												playerCoinBank.put(coinReceiver.getUniqueId(), playerCoinBank.get(coinReceiver.getUniqueId()) + Integer.parseInt(args[1]));
            												sc.setScore(playerCoinBank.get(coinReceiver.getUniqueId()));
            												coinReceiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your have received " + args[1] + " coins from " + temp.getDisplayName() + "!");
            												playerCoinBank.put(temp.getUniqueId(), playerCoinBank.get(temp.getUniqueId()) - Integer.parseInt(args[1]));
            												gsc.setScore(playerCoinBank.get(temp.getUniqueId()));
            												temp.sendMessage(ChatColor.GREEN + "Sucessfully gave coins to " + coinReceiver.getDisplayName() + "!");
            											} else {
            												temp.sendMessage(ChatColor.RED + "Sorry, you do not have " + args[1] + " coins to give!");          												
            											}
            										}
            									}
            								}
            							}
            						}
            					}
            				}
            			}
            		}
            	}
            }
    		return true;
    	}
    	
    	if (command.getName().equalsIgnoreCase("playersingame")) {
            Player temp = (Player)sender;
            	if(temp.isOp()){
            		for(Player p : playersInGame){            			
            			System.out.println(p.getName());
            		}
            	}else{
            		temp.sendMessage(ChatColor.RED + "Sorry, you must be OP to use this function!");
            	}
    		return true;
    	}
    	
    	if (command.getName().equalsIgnoreCase("startroundnow")) {
    		Player p = (Player)sender;
    		if(p.isOp()){
    			if(gameSessionMap.containsKey(p)){
    				if(gameSessionMap.get(p).playersInGame.size() > 1){
    					if(gameSessionMap.get(p).canJoin || gameSessionMap.get(p).currentState != GameSession.gameState.InGame){
    						if(gameSessionMap.get(p).startGameEarly()){
    							p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Command executed sucessfully!");
    						} else {
    							p.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "IDKEK how, but it broke. That's not even possible...");
    						}
    					} else {
    						p.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "A game has already started!");
    					}
    				} else {
    					p.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "There must be at least two players!");
    				}
    			} else {
    				p.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC +"You are not in a game right now!");
    			}
    		} else {
    			p.sendMessage(ChatColor.RED + "Sorry, you must be OP to use this function!");
    		}
    	}
    	
    	return false;
    	
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
    	
    	Action interactAction = e.getAction();
    	ItemStack itemStack = e.getItem();
    	
    	if(interactAction == Action.PHYSICAL || itemStack == null || itemStack.getType() == Material.AIR) return;
    	
    	if(itemStack.getType() == Material.NETHER_STAR){
    		
    		e.getPlayer().openInventory(thisShopHandler.getInventory(ShopHandler.inventoryList.pvpActionsMenu));
    		
    	}
    	
    }    

    public void updateCoins(Player playerToUpdate){    	
    	Scoreboard sb = playerSBs.get(playerToUpdate);
		if(sb != null){
			System.out.println("Scoreboard not null...");
			Objective obj = sb.getObjective("Stats");
			if(obj != null){
				System.out.println("Objective not null...");
				Score sc = obj.getScore(ChatColor.GREEN + "Coins");
				if(sc != null){
					System.out.println("Score not null, updating!");
					sc.setScore(playerCoinBank.get(playerToUpdate.getUniqueId()));
				}
			}
		}    	
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
    	
    	e.setKeepInventory(true);
    	
    	if(e.getEntity().getKiller() != null){
    	
    		e.setDeathMessage(e.getEntity().getDisplayName() + " was slain with a " + e.getEntity().getKiller().getItemInHand().getType().toString() + " by " + e.getEntity().getKiller().getDisplayName());
    		
    		if(gameSessionMap.containsKey(e.getEntity().getPlayer())){
    			
    			gameSessionMap.get(e.getEntity().getPlayer()).onPlayerDeath(e.getEntity().getKiller(), e.getEntity().getPlayer());
    			
    		}
    	
    	} else {
    		
    		e.setDeathMessage(e.getEntity().getDisplayName() + " was killed by an unseen force!");
    		
    		if(gameSessionMap.containsKey(e.getEntity().getPlayer())){
    			
    			gameSessionMap.get(e.getEntity().getPlayer()).onPlayerDeath(null, e.getEntity().getPlayer());
    			
    		}
    		
    	}
    	
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
    	
    	if(!e.getPlayer().isOp() || e.getPlayer().getGameMode() == GameMode.SURVIVAL){
    	
    		e.setCancelled(true);
    		e.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "You cannot break blocks here!");
    		
    	}
    	
    }
    
    public void destroyGameSession(String gsName){
    	
    	if(gsName == "HM_01") {
    	
    		HM_01 = null;
    		HM_01 = new GameSession(GameSession.sessionMap.Hedge01);
    	
    	}
    	
    	if(gsName == "RUINS") {
        	
    		RUINS = null;
    		RUINS = new GameSession(GameSession.sessionMap.Ruins);
    	
    	}
    	
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
    	
		if(playersInGame.contains(e.getPlayer())){
			
			playersInGame.remove(e.getPlayer());
			
			if(gameSessionMap.containsKey(e.getPlayer())){
				
				gameSessionMap.get(e.getPlayer()).onPlayerLeave(e.getPlayer());
				gameSessionMap.remove((Player) e.getPlayer());
				
			}
		
		}
    	
    }
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent e){
    	
		if(playersInGame.contains(e.getPlayer())){
			
			playersInGame.remove(e.getPlayer());
			
			if(gameSessionMap.containsKey(e.getPlayer())){
				
				gameSessionMap.get(e.getPlayer()).onPlayerLeave(e.getPlayer());
				gameSessionMap.remove((Player) e.getPlayer());
				
			}
		
		}
    	
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
    	
    	e.setCancelled(true);
    	
    }
        
}