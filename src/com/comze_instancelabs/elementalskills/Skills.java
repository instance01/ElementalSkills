package com.comze_instancelabs.elementalskills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Skills {
	public static Main m;
	public static FileConfiguration config;
	public static HashMap<String, HashMap<String, Integer>> pxp = new HashMap<String, HashMap<String, Integer>>();
	public static HashMap<String, HashMap<String, Integer>> plvl = new HashMap<String, HashMap<String, Integer>>();
	public static HashMap<String, String> pelem = new HashMap<String, String>();
	
	public Skills(Main m, FileConfiguration config){
		this.m = m;
		this.config = config;
	}
	
	public static int base_money = 10;
	public static int base_xp = 10;
	public static int base_xp_for_next_level = 150;
	
	public static FileConfiguration getConfig(){
		return config;
	}
	
	/***
	 * 
	 * @param p
	 * @param amplifier
	 */
	public static void addXP(Player p, int amplifier, String type){
		int lv = getPlayerLevel(p, type);
		int oldxp = getPlayerXP(p, type);
		int newxp = (int) (Math.round((lv + 1) * (1.2 + (0.02 * amplifier))) + 2 * amplifier + base_xp);
		newxp += (newxp + oldxp) * (0.01 + (0.001 * amplifier));
		if(newxp < 0){
			newxp = 0;
		}
		int needed = (int) (base_xp_for_next_level + (base_xp_for_next_level * Math.pow(1.5, lv)) * lv);
		
		HashMap<String, Integer> f = pxp.get(p.getName());
		f.put(type, oldxp + newxp);
		pxp.put(p.getName(), f);
		
		if(amplifier > 0){
			if(Math.random() > 0.5){
				if(!((0.01F * amplifier) < 0)){
					p.setExp(p.getExp() + 0.01F * amplifier);
					if(p.getExp() > 1){
						p.setExp(0.0F);
						p.setLevel(p.getLevel() + 1);
					}
				}
			}
		}
		
		p.sendMessage(Integer.toString(newxp) + " added to " + Integer.toString(oldxp) + ". Needed for levelup: " + Integer.toString(needed));
		
		if(oldxp + newxp > needed){
			addPlayerLevel(p, type);
			//TODO add money
		}
	}
	
	public static void addPlayerLevel(Player p, String type){
		HashMap<String, Integer> f = plvl.get(p.getName());
		f.put(type, getPlayerLevel(p, type) + 1);
		plvl.put(p.getName(), f);
		saveAllPlayerData(p);
		p.sendMessage("new level in " + type + ": " + Integer.toString(getPlayerLevel(p, type)));
	}
	
	public static int getPlayerLevel(Player p, String type){
		return plvl.get(p.getName()).get(type);
	}
	
	public static int getPlayerXP(Player p, String type){
		return pxp.get(p.getName()).get(type);
	}
	
	public static String getPlayerElement(Player p){
		return pelem.get(p.getName());
	}
	
	public static void setPlayerElement(Player p, String element){
		pelem.put(p.getName(), element);
		saveAllPlayerData(p);
	}
	
	public static void saveAllPlayerData(Player p){
		for(String t : alltypes){
			getConfig().set(p.getName() + "." + t + ".lv", plvl.get(p.getName()).get(t));
			getConfig().set(p.getName() + "." + t + ".xp", pxp.get(p.getName()).get(t));
		}
		getConfig().set(p.getName() + ".elem", pelem.get(p.getName()));
		m.saveConfig();
	}
	
	static ArrayList<String> alltypes = new ArrayList<String>(Arrays.asList("mining", "alchemy", "melee", "archery", "travel", "wildlife", "eating", "herbalism", "enchanting", "karma"));
	
	public static void loadPlayerData(Player p){
		if(getConfig().isSet(p.getName())){
			Set<String> types = getConfig().getConfigurationSection(p.getName() + ".").getKeys(false);
			HashMap<String, Integer> lv = new HashMap<String, Integer>();
			HashMap<String, Integer> xp = new HashMap<String, Integer>();
			for(String t : types){
				lv.put(t, getConfig().getInt(p.getName() + "." + t + ".lv"));
				xp.put(t, getConfig().getInt(p.getName() + "." + t + ".xp"));
			}
			plvl.put(p.getName(), lv);
			pxp.put(p.getName(), xp);
			pelem.put(p.getName(), getConfig().getString(p.getName() + ".elem"));
		}else{
			for(String t : alltypes){
				getConfig().set(p.getName() + "." + t + ".lv", 0);
				getConfig().set(p.getName() + "." + t + ".xp", 0);
			}
			getConfig().set(p.getName() + ".elem", "default");
			m.saveConfig();
			loadPlayerData(p);
		}	
	}
	
	public static boolean displayPlayerData(CommandSender sender, String p){
		if(getConfig().isSet(p)){
			Set<String> types = getConfig().getConfigurationSection(p + ".").getKeys(false);
			HashMap<String, Integer> lv = new HashMap<String, Integer>();
			HashMap<String, Integer> xp = new HashMap<String, Integer>();
			
			sender.sendMessage(ChatColor.AQUA + "-- Elements --");
			//TODO Elements
			sender.sendMessage(ChatColor.AQUA + "-- Skills --");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".mining.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".mining.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".mining.lv"))) + ChatColor.DARK_GREEN + " ◆ " + ChatColor.AQUA + "Mining");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".alchemy.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".alchemy.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".alchemy.lv"))) + ChatColor.GOLD + " ◆ " + ChatColor.AQUA + "Alchemy");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".melee.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".melee.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".melee.lv"))) + ChatColor.DARK_RED + " ◆ " + ChatColor.AQUA + "Melee");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".archery.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".archery.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".archery.lv"))) + ChatColor.YELLOW + " ◆ " + ChatColor.AQUA + "Archery");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".travel.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".travel.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".travel.lv"))) + ChatColor.BLUE + " ◆ " + ChatColor.AQUA + "Travel");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".wildlife.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".wildlife.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".wildlife.lv"))) + ChatColor.DARK_AQUA + " ◆ " + ChatColor.AQUA + "Wildlife");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".eating.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".eating.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".eating.lv"))) + ChatColor.DARK_BLUE + " ◆ " + ChatColor.AQUA + "Eating");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".herbalism.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".herbalism.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".herbalism.lv"))) + ChatColor.GREEN + " ◆ " + ChatColor.AQUA + "Herbalism");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".enchanting.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".enchanting.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".enchanting.lv"))) + ChatColor.DARK_PURPLE + " ◆ " + ChatColor.AQUA + "Enchanting");
			sender.sendMessage(ChatColor.GRAY + String.format("&8%03d", getConfig().getInt(p + ".karma.lv")).replaceAll("&", "§").replace(Integer.toString(getConfig().getInt(p + ".karma.lv")), "§7" + Integer.toString(getConfig().getInt(p + ".karma.lv"))) + ChatColor.RED + " ◆ " + ChatColor.AQUA + "Karma");

			int c = 0;
			for(String t : types){
				c += getConfig().getInt(p + "." + t + ".lv");
			}
			
			sender.sendMessage(ChatColor.AQUA + "-- Total --");
			sender.sendMessage(ChatColor.GRAY + Integer.toString(c));
			return true;
		}
		return false;
	}
	
	public static int getFullScore(String p){
		if(getConfig().isSet(p)){
			Set<String> types = getConfig().getConfigurationSection(p + ".").getKeys(false);
			HashMap<String, Integer> lv = new HashMap<String, Integer>();
			HashMap<String, Integer> xp = new HashMap<String, Integer>();
			int full = 0;
			for(String t : types){
				full += getConfig().getInt(p + "." + t + ".lv");
			}
			return full;
		}
		return 0;
	}

	
	public static void updateScoreboard(Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();

			Scoreboard board = manager.getNewScoreboard();

			Objective objective = board.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			Set<String> types = getConfig().getConfigurationSection(p.getName() + ".").getKeys(false);

			int c = 0;
			for(String t : types){
				c += getConfig().getInt(p.getName() + "." + t + ".lv");
			}
			
			objective.setDisplayName(ChatColor.AQUA + "Skills (" + Integer.toString(c) + ")");

			
			for(String t : types){
				objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + t)).setScore(getConfig().getInt(p.getName() + "." + t + ".lv"));
			}
			
			p.setScoreboard(board);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
