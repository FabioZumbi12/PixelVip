package br.net.fabiozumbi12.pixelvip.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class PVLogger {
    public void sucess(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[PixelVip] &a&l" + s));
    }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[PixelVip] " + s));
    }

    public void warning(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[PixelVip] &6" + s + "&r"));
    }

    public void severe(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[PixelVip] &c&l" + s + "&r"));
    }

    public void log(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[PixelVip] " + s));
    }
}
