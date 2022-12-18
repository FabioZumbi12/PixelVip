package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentItem;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;

import net.md_5.bungee.api.ChatColor;

public class MercadoPagoHook implements PaymentModel {
    
    private static final Gson GSON = new Gson();
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private PixelVip plugin;
    private PaymentClient client;
    
    public MercadoPagoHook(PixelVip plugin) {
        this.plugin = plugin;
        
        MercadoPagoConfig.setAccessToken(
                plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.access-token"));
        
        client = new PaymentClient();
    }
    
    @Override
    public String getPayname() {
        return "MercadoPago";
    }
    
    @Override
    public boolean checkTransaction(Player player, String transCode) {
        // check if used
        if (plugin.getPVConfig().transExist(getPayname(), transCode)) {
            player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig()
                    .getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayname())));
            return true;
        }
        boolean test = plugin.getPVConfig().getApiRoot().getBoolean("apis.in-test");
        
        try {
            Payment payment = client.get(Long.parseLong(transCode));
            
            if (payment.getStatus().equals("404")) {
                return false;
            }
            
            // check if approved
            if (!payment.getStatus().equalsIgnoreCase("approved")) {
                player.sendMessage(plugin.getUtil()
                        .toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting")
                                .replace("{payment}", getPayname())));
                return true;
            }
            
            // check if expired
            OffsetDateTime oldCf = OffsetDateTime.of(
                    LocalDateTime.parse(plugin.getPVConfig().getApiRoot()
                            .getString("apis.mercadopago.ignoreOldest"), FORMATTER),
                    ZoneOffset.systemDefault().getRules()
                            .getOffset(LocalDateTime.now(ZoneOffset.systemDefault())));
            
            if (payment.getDateLastUpdated().compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil()
                        .toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired")
                                .replace("{payment}", getPayname())));
                return true;
            }
            List<PaymentItem> jItems = payment.getAdditionalInfo().getItems();
            
            if (jItems == null || jItems.isEmpty()) {
                String[] description = payment.getDescription().split(" ");
                String desc = payment.getDescription();
                
                // Id handling
                String id;
                Optional<String> optId = Arrays.stream(description).filter(i -> i.startsWith("#"))
                        .findFirst();
                
                if (plugin.getPackageManager().getPackage(description[0]) != null) {
                    id = description[0];
                }
                else if (optId.isPresent()) {
                    id = optId.get().substring(optId.get().indexOf("#"));
                }
                else {
                    plugin.getPVLogger().warning("ID of Item not found, setting to 0: " + desc);
                    id = String.valueOf(0);
                }
                String quantity = description[description.length - 1];
                
                jItems = new ArrayList<>();
                
                jItems.add(GSON.fromJson(
                        "{\"id\":\"" + id + "\",\"quantity\":\"" + quantity + "\",\"title\":\""
                                + desc + "\",\"unit_price\":\""
                                + payment.getTransactionDetails().getNetReceivedAmount() + "\"}",
                        PaymentItem.class));
            }
            
            // Debug
            if (test) {
                plugin.getPVLogger().severe("Items: " + jItems);
                plugin.getPVLogger().severe("---------");
                
                for (PaymentItem item : jItems) {
                    plugin.getPVLogger().severe("ID: " + item.getId());
                    plugin.getPVLogger().severe("Quantity: " + item.getQuantity());
                    plugin.getPVLogger().severe("Title: " + item.getTitle());
                    plugin.getPVLogger().severe("Price: " + item.getUnitPrice());
                    plugin.getPVLogger().severe("---------");
                }
            }
            // Debug
            
            HashMap<String, Integer> items = new HashMap<>();
            
            for (PaymentItem item : jItems) {
                String id;
                if (plugin.getPVConfig().getApiRoot()
                        .getString("apis.mercadopago.product-id-location").equalsIgnoreCase("ID")) {
                    id = item.getId();
                }
                else {
                    String[] idStr = item.getTitle().split(" ");
                    Optional<String> optId = Arrays.stream(idStr).filter(i -> i.startsWith("#"))
                            .findFirst();
                    if (plugin.getPackageManager().getPackage(idStr[0]) != null) {
                        id = idStr[0];
                    }
                    else
                        id = optId.map(s -> s.substring(s.indexOf("#")))
                                .orElseGet(() -> String.valueOf(0));
                }
                if (plugin.getPackageManager().getPackage(id) == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPVConfig().getLang("_pluginTag")
                                    + plugin.getPVConfig().getLang("payment.noitems")
                                            .replace("{payment}", getPayname())
                                    + "\n" + " - Error: " + item.getTitle()));
                    continue;
                }
                items.put(id, item.getQuantity());
                
                // Debug
                if (test) {
                    plugin.getPVLogger()
                            .severe("Added Item: " + item.getTitle() + " | ID: " + item.getId());
                }
                // Debug
            }
            
            if (plugin.getUtil().paymentItems(items, player, getPayname(), transCode)) {
                plugin.getPVConfig().addTrans(getPayname(), transCode, player.getName());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            plugin.processTrans.remove(transCode);
            return false;
        }
        plugin.processTrans.remove(transCode);
        return true;
    }
}
