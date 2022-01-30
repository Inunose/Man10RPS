package com.inunose.man10rps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class NoteBook {

    /*public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!label.equalsIgnoreCase("mrps")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage("§4§lこのコマンドはプレイヤーでなければ実行できません");
            return false;
        }

        Player p = (Player) sender;
        if(!p.hasPermission("mrps.play")){
            p.sendMessage("§4§lあなたは権限を持っていません");
            return false;
        }

        if(args.length != 1){
            p.sendMessage("/mrps <g/c/p> を入力してください");
            return false;
        }

        String te = args[0];
        if(!te.equalsIgnoreCase( "g") && !te.equalsIgnoreCase("c") && !te.equalsIgnoreCase( "p")){
            p.sendMessage("/mrps <g/c/p> を入力してください");
            return false;
        }

        PlayResult result2 = play(te);
        if(result2 == null){
            p.sendMessage("内部エラーが発生しました");
            return false;
        }

        if(result2.playerResult == 1){
            p.sendMessage("Botは" +result2.getBotTeText()+ "を出しました。あなたの勝ちです");
            return false;
        }
        if(result2.playerResult == -1){
            p.sendMessage("Botは" +result2.getBotTeText()+ "を出しました。あなたの負けです");
            return false;
        }
        if(result2.playerResult == 0){
            p.sendMessage("Botは" +result2.getBotTeText()+ "を出しました。あいこです");
            return false;
        }

        return true;
    }

    public  PlayResult play(String playerHand){
        int teNumber = 0;
        if(playerHand.equalsIgnoreCase( "g")) teNumber = 1;
        if(playerHand.equalsIgnoreCase( "c")) teNumber = 2;
        if(playerHand.equalsIgnoreCase( "p")) teNumber = 3;

        int botTeNumber = new Random().nextInt(3) + 1;

        int result = teNumber - botTeNumber;

        if(result == -1||result == 2) return new PlayResult(botTeNumber, 1);
        if(result == -2||result == 1) return new PlayResult(botTeNumber, -1);
        if(result == 0) return  new PlayResult(botTeNumber, 0);
        return null;
    }
*/
}
