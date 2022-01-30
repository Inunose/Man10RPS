package com.inunose.man10rps;

import com.inunose.man10rps.MySQL.MySQLAPI;
import com.inunose.man10rps.MySQL.ThreadedMySQLAPI;
import net.milkbowl.vault.Vault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Man10RPS extends JavaPlugin implements @NotNull Listener {

    //ArrayList<UUID> players = new ArrayList<>();         メニュー開いてる人
    //ArrayList<UUID> rollingPlayers = new ArrayList<>();  回している人

    HashMap<UUID, String> playerData = new HashMap<>();
    HashMap<UUID, Integer> playerPrice = new HashMap<>();

    VaultAPI vault = null;
    ThreadedMySQLAPI mysql = null;

    int maxPrice;
    int minPrice;

    int winMultiplier;
    double winRate;


    public void loadConfig(){
        maxPrice = getConfig().getInt("price.max");
        if(maxPrice < 1) maxPrice = 1;
        minPrice = getConfig().getInt("price.min");
        if(minPrice > maxPrice) minPrice = maxPrice;
        winMultiplier = getConfig().getInt("price.winMultiplier");
        if( winMultiplier < 1)  winMultiplier = 1;
        winRate = getConfig().getDouble("bot.winRate");
        if(winRate < 0) winRate = 0;
        if(winRate > 1) winRate = 1;

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getCommand("mrps").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        vault = new VaultAPI();
        mysql = new ThreadedMySQLAPI(this);
        loadConfig();

        String createTable =
                "CREATE TABLE `man10_rps_log` (\n" +
                "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "\t`name` VARCHAR(64) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',\n" +
                "\t`uuid` VARCHAR(64) NULL DEFAULT NULL COLLATE 'latin1_swedish_ci',\n" +
                "\t`bet_price` BIGINT(19) NULL DEFAULT NULL,\n" +
                "\t`win_price` BIGINT(19) NULL DEFAULT NULL,\n" +
                "\t`date_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "\tPRIMARY KEY (`id`) USING BTREE\n" +
                ")\n" +
                "COLLATE='latin1_swedish_ci'\n" +
                "ENGINE=InnoDB\n" +
                ";";
        mysql.execute(createTable);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!label.equalsIgnoreCase("mrps")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage("§4§lこのコマンドはプレイヤーでなければ実行できません");
            return false;
        }

        Player p = (Player) sender;
        if (!p.hasPermission("mrps.play")) {
            p.sendMessage("§4§lあなたは権限を持っていません");
            return false;
        }

        if (args.length != 1) {
            p.sendMessage("/mrps <掛け金> を入力してください");
            return false;
        }
        if(args[0].equalsIgnoreCase("reload")){
            reloadConfig();
            loadConfig();
            p.sendMessage("リロードしました");
            return false;
        }


        int betPrice;
        try {
            betPrice = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            p.sendMessage("§4§l数字で入力してください");
            return false;
        }

        if (!(minPrice <= betPrice && betPrice <= maxPrice)) {
            p.sendMessage("§4§l掛け金は" + minPrice +"以上"+maxPrice+"で入力してください");
            return false;
        }

        if (betPrice <= 0) {
            p.sendMessage("§4§l掛け金は1以上で入力してください");
            return false;
        }

        if(vault.getBalance(p.getUniqueId()) < betPrice){
            p.sendMessage("§4§l掛け金が不足しています");
            return false;
        }


        Inventory inv = Bukkit.getServer().createInventory(null, 6 * 9);

        for (int i = 0; i < 54; i++) {

            ItemStack backGround = createItem(" ", Material.BLUE_STAINED_GLASS_PANE);
            inv.setItem(i, backGround);
        }

        ItemStack g = createItem("グー ", Material.STONE);
        inv.setItem(38, g);

        ItemStack c = createItem("チョキ", Material.SHEARS);
        inv.setItem(42, c);

        ItemStack pa = createItem("パー", Material.PAPER);
        inv.setItem(40, pa);

        ItemStack playerHead = createItem("相手", Material.PLAYER_HEAD);
        inv.setItem(13, playerHead);

        playerPrice.put(p.getUniqueId(), betPrice);
        playerData.put(p.getUniqueId(), "open");

        p.openInventory(inv);
        return true;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if(playerPrice.get(p.getUniqueId())== null) return;  //playerPriceの保険
        if(!playerData.containsKey(p.getUniqueId())) return; //playerDataの保険

        if(e.getClickedInventory() == null) return;
        if(e.getClickedInventory().getType() != InventoryType.CHEST) return;
        e.setCancelled(true);
        if(playerData.get(p.getUniqueId()).equalsIgnoreCase("rolling")) return;

        //ここでグーチョキパークリック
        String te = null;
        if(e.getRawSlot() == 38) te = "g";
        if(e.getRawSlot() == 42) te = "c";
        if(e.getRawSlot() == 40) te = "p";
        if(te == null) return;

        int betPrice = playerPrice.get(p.getUniqueId());

        if(vault.getBalance(p.getUniqueId()) < betPrice){  //超重要!!　更新する直前で再度お金の確認
            p.sendMessage("§4§l掛け金が不足しています");
            return;
        }

        if(!vault.withdraw(p.getUniqueId(), betPrice)){
            p.sendMessage("§4§l内部エラーが発生しました");
            return;
        }

        PlayResult result2 = play2(te);

        Inventory inv = e.getClickedInventory();

        ItemStack g = createItem("グー ",Material.STONE);
        ItemStack c = createItem("チョキ",Material.SHEARS);
        ItemStack pa = createItem("パー",Material.PAPER);


        playerData.put(p.getUniqueId(), "rolling");
        p.sendMessage("じゃんけん");

        Bukkit.getScheduler().runTaskLater(this, ()-> {

            if(result2.botTe.equalsIgnoreCase("g")) inv.setItem(22, g);
            if(result2.botTe.equalsIgnoreCase("c")) inv.setItem(22, c);
            if(result2.botTe.equalsIgnoreCase("p")) inv.setItem(22, pa);

            double winPrice = 0;

            if(result2.playerResult == 1){
                p.sendMessage("ぽん あなたの勝ちです");
                vault.deposit(p.getUniqueId(),((double) betPrice*winMultiplier));
                winPrice = (double) betPrice*winMultiplier;
            }
            if(result2.playerResult == -1){
                p.sendMessage("ぽん あなたの負けです");
                winPrice = -betPrice;
            }

            if(result2.playerResult == 0){
                p.sendMessage("ぽん あいこです");
                vault.deposit(p.getUniqueId(),betPrice);
                winPrice = betPrice;
            }

            mysql.futureExecute("INSERT INTO man10_rps_log (`name`,`uuid`,`bet_price`,`win_price`) VALUES" +
                    "(\""+ p.getName() +"\", \""+ p.getUniqueId() + "\"," + betPrice + ","+ winPrice +");");

            Bukkit.getScheduler().runTaskLater(this, ()-> {

                ItemStack backGround = createItem(" ",Material.BLUE_STAINED_GLASS_PANE);
                inv.setItem(22, backGround);
                playerData.put(p.getUniqueId(), "open");
            }, 20);
        }, 20);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        playerData.remove(e.getPlayer().getUniqueId());
    }

//    public  PlayResult play(String playerHand){
//        String[] hands = new String[]{"g","c","p"};
//        String botTe = hands[new Random().nextInt(hands.length)];
//
//        if(playerHand.equalsIgnoreCase(botTe)) return new PlayResult(botTe,0);
//        if(playerHand.equalsIgnoreCase("g") && botTe.equalsIgnoreCase("c")) return new PlayResult(botTe, 1);
//        if(playerHand.equalsIgnoreCase("c") && botTe.equalsIgnoreCase("p")) return new PlayResult(botTe, 1);
//        if(playerHand.equalsIgnoreCase("p") && botTe.equalsIgnoreCase("g")) return new PlayResult(botTe, 1);
//        return new PlayResult(botTe, -1);
//
//    }

    public ItemStack createItem (String name, Material type){
        ItemStack x = new ItemStack(type);
        ItemMeta xMeta = x.getItemMeta();
        xMeta.setDisplayName(name);
        x.setItemMeta(xMeta);
        return x;
    }

    public  PlayResult play2(String playerHand){

        int aiko = new Random().nextInt(3);
        if(aiko == 0) return new PlayResult(playerHand,0); //Pあいこ

        String[] loseHands = new String[]{"p","g","c"};
        String[] hands = new String[]{"g","c","p"};
        String[] winHands = new String[]{"c","p","g"};

        int make = new Random().nextInt(100);
        if(make < winRate*100) {  //負け
            return new PlayResult(loseHands[Arrays.asList(hands).indexOf(playerHand)],-1); //P負け
        }else {//負け以外だったら勝ち
            return new PlayResult(winHands[Arrays.asList(hands).indexOf(playerHand)],1); //P勝ち
        }

    }

}

