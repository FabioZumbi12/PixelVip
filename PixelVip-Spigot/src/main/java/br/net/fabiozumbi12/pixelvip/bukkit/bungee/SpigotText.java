package br.net.fabiozumbi12.pixelvip.bukkit.bungee;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class SpigotText {
    
    TextComponent text;
    
    public SpigotText() {
        this.text = new TextComponent();
    }
    
    @SuppressWarnings("deprecation")
    public SpigotText(String text, String click, String run, String hover) {
        this.text = new TextComponent(text);
        if (click != null) this.text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
        if (run != null) this.text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, run));
        if (hover != null) {
            this.text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        }
    }
    
    public TextComponent getText() {
        return this.text;
    }
    
    public TextComponent setText(String text) {
        this.text.setText(text);
        return this.text;
    }
    
    @SuppressWarnings("deprecation")
    public TextComponent setHover(String hover) {
        this.text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return this.text;
    }
    
    public TextComponent setClick(String click) {
        this.text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
        return this.text;
    }
    
    public TextComponent setRunCommand(String click) {
        this.text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click));
        return this.text;
    }
    
    public void sendMessage(CommandSender sender) {
        sender.spigot().sendMessage(this.text);
    }
}
