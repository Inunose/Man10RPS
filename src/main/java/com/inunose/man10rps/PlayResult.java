package com.inunose.man10rps;

public class PlayResult {
    String botTe;
    int playerResult;

    public PlayResult(String botTe, int playerResult){
        this.botTe = botTe;
        this.playerResult = playerResult;
    }

    public String getBotTeText(){
        if(botTe.equalsIgnoreCase("g")) return "グー";
        if(botTe.equalsIgnoreCase("c")) return "チョキ";
        if(botTe.equalsIgnoreCase("p")) return "パー";
        return "error";
    }


}
