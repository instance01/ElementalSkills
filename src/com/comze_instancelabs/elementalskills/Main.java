package com.comze_instancelabs.elementalskills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Main extends JavaPlugin implements Listener {

	public Economy econ;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		if (!setupEconomy()) {
			getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
		}

		Skills s = new Skills(this, getConfig());

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Skills.loadPlayerData(p);
				}
			}
		}, 20L);
	}

	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			Skills.saveAllPlayerData(p);
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("skill")) {
			if (args.length > 0) {
				if(args[0].equalsIgnoreCase("reload")){
					this.reloadConfig();
					return true;
				}
				Skills.displayPlayerData(sender, args[0]);
			} else {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					Skills.saveAllPlayerData(p);
					Skills.displayPlayerData(sender, p.getName());
					Skills.updateScoreboard(p);
				}
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("element")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length > 0) {
					Skills.setPlayerElement(p, args[0]);
					sender.sendMessage(ChatColor.AQUA + "Your main element is " + args[0] + " now.");
					return true;
				}
				sender.sendMessage(ChatColor.AQUA + "You can change your element by typing /element [type], where [type] can be water, air, earth and fire.");
				sender.sendMessage(ChatColor.GRAY + Skills.getPlayerElement(p));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("highscore")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;

				Map<String, Integer> pscores = new HashMap<String, Integer>();
				Set<String> players = getConfig().getKeys(false);
				for (String t : players) {
					pscores.put(t, Skills.getFullScore(t));
				}

				Map<String, Integer> pscores_s = sortByValue(pscores);

				ScoreboardManager manager = Bukkit.getScoreboardManager();
				Scoreboard board = manager.getNewScoreboard();
				Objective objective = board.registerNewObjective("test", "dummy");
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);

				objective.setDisplayName(ChatColor.AQUA + "Top (" + Integer.toString(Skills.getFullScore(p.getName())) + ")");

				int c = 0;
				for (Map.Entry<String, Integer> entry : pscores_s.entrySet()) {
					c++;
					if(c > 10){
						break;
					}
					if (entry.getKey().length() < 15) {
						objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + entry.getKey())).setScore(entry.getValue());
					} else {
						objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + entry.getKey().substring(0, entry.getKey().length() - 3))).setScore(entry.getValue());
					}
					p.sendMessage(ChatColor.GRAY + Integer.toString(entry.getValue()) + ChatColor.AQUA + " - " + ChatColor.GRAY + entry.getKey());
				}

				p.setScoreboard(board);

			}
			return true;
		}
		return false;
	}

	public static Map sortByValue(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Skills.loadPlayerData(event.getPlayer());
	}

	// Mining
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Material m = event.getBlock().getType();
		if (m == Material.STONE) {
			Skills.addXP(p, -8, "mining");
		} else if (m == Material.SANDSTONE) {
			Skills.addXP(p, -4, "mining");
		} else if (m == Material.COAL_ORE) {
			Skills.addXP(p, -1, "mining");
		} else if (m == Material.IRON_ORE) {
			Skills.addXP(p, 1, "mining");
		} else if (m == Material.GOLD_ORE) {
			Skills.addXP(p, 1, "mining");
		} else if (m == Material.REDSTONE_ORE) {
			Skills.addXP(p, 0, "mining");
		} else if (m == Material.LAPIS_ORE) {
			Skills.addXP(p, 3, "mining");
		} else if (m == Material.DIAMOND_ORE) {
			Skills.addXP(p, 5, "mining");
		} else if (m == Material.EMERALD_ORE) {
			Skills.addXP(p, 6, "mining");
		} else if (m == Material.QUARTZ_ORE) {
			Skills.addXP(p, 0, "mining");
		} else if (m == Material.GLOWSTONE) {
			Skills.addXP(p, -1, "mining");
		}
	}

	// TODO MetaData
	// Alchemy
	@EventHandler
	public void onBrew(BrewEvent event) {
		Location l = event.getBlock().getLocation();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getLocation().distance(l) < 5) {
				Skills.addXP(p, -2, "alchemy");
			}
		}
	}

	// Alchemy
	@EventHandler
	public void onFurnaceExtract(FurnaceExtractEvent event) {
		Material m = event.getItemType();
		Player p = event.getPlayer();
		if (m == Material.IRON_INGOT || m == Material.GOLD_INGOT) {
			Skills.addXP(p, event.getItemAmount() / 10, "alchemy");
		} else if (m == Material.DIAMOND || m == Material.EMERALD) {
			Skills.addXP(p, event.getItemAmount() / 8, "alchemy");
		} else if (m == Material.QUARTZ) {
			Skills.addXP(p, event.getItemAmount() / 14, "alchemy");
		} else if (m == Material.STONE) {
			if (event.getItemAmount() < 33) {
				Skills.addXP(p, -4, "alchemy");
			} else {
				Skills.addXP(p, -1, "alchemy");
			}
		} else if (m == Material.COOKED_BEEF || m == Material.COOKED_CHICKEN || m == Material.COOKED_FISH || m == Material.BAKED_POTATO || m == Material.GRILLED_PORK) {
			Skills.addXP(p, event.getItemAmount() / 18, "alchemy");
		} else if (m == Material.INK_SACK) {
			Skills.addXP(p, event.getItemAmount() / 20, "alchemy");
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// TODO travel
	}

	// Archery
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
				if (e.getDamager() instanceof Player || e.getDamager() instanceof Arrow) {
					if (e.getDamager() instanceof Arrow) {
						if (((Arrow) e.getDamager()).getShooter() instanceof Player) {
							Player damager = (Player) (((Arrow) e.getDamager()).getShooter());
							Entity damaged = e.getEntity();
							damager.sendMessage("test");
							Skills.addXP(damager, (int) (damaged.getLocation().distance(damager.getLocation()) / 15), "archery");
						}
					} else {
						Player damager = (Player) e.getDamager();
						Skills.addXP(damager, 2, "melee");
					}
				}
			}
		}
	}

	// Wildlife
	@EventHandler
	public void onTame(EntityTameEvent event) {
		if (event.getOwner() instanceof Player) {
			Player p = (Player) event.getOwner();
			Skills.addXP(p, 1, "wildlife");
		}
	}

	// Wildlife
	@EventHandler
	public void onShear(PlayerShearEntityEvent event) {
		Player p = event.getPlayer();
		Skills.addXP(p, -2, "wildlife");
	}
	
	// Wildlife
	@EventHandler
	public void onEggHatch(PlayerEggThrowEvent event){
		Player p = event.getPlayer();
		if(event.isHatching()){
			Skills.addXP(p, -2, "wildlife");
		}
	}

}
