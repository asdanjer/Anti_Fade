package asdanjer.anti_Fade;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class Anti_Fade extends JavaPlugin implements Listener, CommandExecutor {
public List<OfflinePlayer> fadeList = new LinkedList<OfflinePlayer>();
public int cooldown = 5;
public HashMap<UUID, Long> history = new HashMap<UUID, Long>();
public ArrayList<UUID> optedIn = new ArrayList<UUID>();
public boolean debug = false;
public boolean indicate = false;
NamespacedKey toggleKey = new NamespacedKey(this, "anti-fade");
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.saveDefaultConfig();
        // Load config values
        debug = getConfig().getBoolean("debug");
        indicate = getConfig().getBoolean("indicate");
        cooldown = getConfig().getInt("cooldown");

        getServer().getPluginManager().registerEvents(this, this);
        try {
            this.getCommand("repeatconnections").setExecutor(this);
        } catch (NullPointerException e) {
            getLogger().warning("Could not register command.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID pid=e.getPlayer().getUniqueId();
        PersistentDataContainer data = e.getPlayer().getPersistentDataContainer();
        if(data.getOrDefault(toggleKey, PersistentDataType.BYTE, (byte) 0) == 1) optedIn.add(pid);
        if((history.containsKey(pid) && System.currentTimeMillis()-history.get(pid)<cooldown*1000L) || debug ){
            String message=e.getJoinMessage();
            if(message==null) return;
            e.setJoinMessage(null);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!optedIn.contains(p.getUniqueId())) {
                    p.sendMessage(message+ (indicate?" (replaced)":""));
                }
            }
        }
        history.put(pid, System.currentTimeMillis());

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent e) {
        UUID pid=e.getPlayer().getUniqueId();

        if((history.containsKey(pid) && System.currentTimeMillis()-history.get(pid)<cooldown*1000L) || debug ){
            String message=e.getQuitMessage();
            if(message==null) return;
            e.setQuitMessage(null);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!optedIn.contains(p.getUniqueId())) {

                    p.sendMessage(message+ (indicate?" (replaced)":""));
                }
            }
        }
        history.put(pid, System.currentTimeMillis());


        optedIn.remove(pid);


    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            UUID pid=player.getUniqueId();
            PersistentDataContainer data = player.getPersistentDataContainer();
            boolean currentValue = data.getOrDefault(toggleKey, PersistentDataType.BYTE, (byte) 0) == 1;
            data.set(toggleKey, PersistentDataType.BYTE, (byte) (currentValue ? 0 : 1));
            if(currentValue){
                player.sendMessage("You have opted out of the anti-fade system.");
                optedIn.remove(pid);
            }
            else{
                player.sendMessage("You have opted in to the anti-fade system.");
                optedIn.add(pid);
            }

        }
        return false;
    }

}
