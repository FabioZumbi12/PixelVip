package br.net.fabiozumbi12.pixelvip.bukkit;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class SpigotText {

    TextComponent text;
    public SpigotText(){
        this.text = new TextComponent();
    }

    public void setText(String text){
        this.text.setText(text);
    }

    public void setHover(String hover){
        this.text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
    }

    public void setClick(String click){
        this.text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
    }

    public void sendMessage(CommandSender sender){
        sender.spigot().sendMessage(this.text);
    }
}
