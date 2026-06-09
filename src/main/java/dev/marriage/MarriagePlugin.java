package dev.marriage;

import dev.marriage.command.DivorceCommand;
import dev.marriage.command.MarriageCommand;
import dev.marriage.command.MarryCommand;
import dev.marriage.config.ConfigManager;
import dev.marriage.config.MessageConfig;
import dev.marriage.config.RingConfig;
import dev.marriage.database.DatabaseManager;
import dev.marriage.item.RingChecker;
import dev.marriage.listener.PlayerJoinListener;
import dev.marriage.listener.PlayerQuitListener;
import dev.marriage.model.CoupleData;
import dev.marriage.reward.RewardManager;
import dev.marriage.scheduler.AttendanceScheduler;
import dev.marriage.service.AttendanceService;
import dev.marriage.service.EconomyService;
import dev.marriage.service.MarriageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarriagePlugin extends JavaPlugin {

private static MarriagePlugin instance;

    private ConfigManager     configManager;
    private MessageConfig     messageConfig;
    private DatabaseManager   databaseManager;
    private MarriageService   marriageService;
    private AttendanceService attendanceService;
    private EconomyService    economyService;
    private RewardManager     rewardManager;
    private AttendanceScheduler attendanceScheduler;
    private RingConfig        ringConfig;
    private RingChecker       ringChecker;

    private Economy economy;

@Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        messageConfig = new MessageConfig(this);
        rewardManager = new RewardManager(this);
        ringConfig    = new RingConfig(this);

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Database initialisation failed — disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            getLogger().severe("No Vault economy provider found — disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        marriageService   = new MarriageService(this, economy);
        attendanceService = new AttendanceService(this);
        economyService    = new EconomyService(this, economy);
        ringChecker       = new RingChecker(this);

        for (CoupleData couple : marriageService.getAllCouples()) {
            attendanceService.initializeCouple(couple);
            economyService.initializeCouple(couple);
        }

        registerCommand("marry",    new MarryCommand(this));
        registerCommand("divorce",  new DivorceCommand(this));
        registerCommand("marriage", new MarriageCommand(this));



var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);

attendanceScheduler = new AttendanceScheduler(this);
        attendanceScheduler.start();
        economyService.startPolling();

        getLogger().info("CookieMarriage v" + getPluginMeta().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {

        if (attendanceScheduler != null) {
            attendanceScheduler.cancel();
        }
        if (economyService != null) {
            economyService.stopPolling();
        }

if (attendanceService != null) {
            attendanceService.saveAll();
        }

if (databaseManager != null) {
            databaseManager.close();
        }

instance = null;

        getLogger().info("CookieMarriage disabled.");
    }

private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugin not found in the plugins folder!");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy plugin is registered with Vault.");
            return false;
        }
        economy = rsp.getProvider();
        if (economy == null) {
            getLogger().severe("Economy provider returned null.");
            return false;
        }
        getLogger().info("Hooked into economy: " + economy.getName());
        return true;
    }

private void registerCommand(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command '/" + name + "' is not defined in plugin.yml!");
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof org.bukkit.command.TabCompleter tc)    cmd.setTabCompleter(tc);
    }

public String getMsg(String key, Object... replacements) {
        if (messageConfig == null) return key; 
        return messageConfig.get(key, replacements);
    }

public Component msgToComponent(String legacyColored) {
        return LegacyComponentSerializer.legacySection().deserialize(legacyColored);
    }

public static MarriagePlugin getInstance() {
        return instance;
    }

    public ConfigManager     getConfigManager()     { return configManager; }
    public MessageConfig     getMessageConfig()      { return messageConfig; }
    public DatabaseManager   getDatabaseManager()    { return databaseManager; }
    public MarriageService   getMarriageService()    { return marriageService; }
    public AttendanceService getAttendanceService()  { return attendanceService; }
    public EconomyService    getEconomyService()     { return economyService; }
    public RewardManager     getRewardManager()      { return rewardManager; }
    public Economy           getEconomy()            { return economy; }
    public RingConfig        getRingConfig()         { return ringConfig; }
    public RingChecker       getRingChecker()        { return ringChecker; }
}
