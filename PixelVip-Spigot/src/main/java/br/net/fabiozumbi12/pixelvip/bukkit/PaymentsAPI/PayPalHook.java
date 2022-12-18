package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

import com.paypal.orders.Order;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.PurchaseUnit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PayPalHook implements PaymentModel {
    
    private PixelVip plugin;
    private PayPalHttpClient client;
    
    public PayPalHook(PixelVip plugin) {
        this.plugin = plugin;
        
        client = new PayPalHttpClient(new PayPalEnvironment.Sandbox(
                plugin.getPVConfig().getApiRoot().getString("apis.paypal.username"),
                plugin.getPVConfig().getApiRoot().getString("apis.paypal.token")));
    }
    
    @Override
    public String getPayname() {
        return "PayPal";
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
            Order order = client.execute(new OrdersCaptureRequest(transCode)).result();
            
            // check if approved
            if (!"APPROVED".equals(order.status())) {
                player.sendMessage(plugin.getUtil()
                        .toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting")
                                .replace("{payment}", getPayname())));
                plugin.processTrans.remove(transCode);
                return true;
            }
            
            /*for (Map.Entry<String, String> resp : response.entrySet()) {
                plugin.getPVLogger().severe(resp.getKey() + ": " + resp.getValue());
            }*/
            
            // check if expired
            Date oldCf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .parse(plugin.getPVConfig().getApiRoot().getString("apis.paypal.ignoreOldest"));
            Date payCf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(order.createTime());
            if (payCf.compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil()
                        .toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired")
                                .replace("{payment}", getPayname())));
                return true;
            }
            List<String[]> itemList = new ArrayList<>();
            
            order.purchaseUnits().stream().map(PurchaseUnit::items).flatMap(List::stream)
                    .forEach(i -> itemList.add(new String[] { i.sku(), i.quantity(), i.name(),
                            i.unitAmount().value() }));
            
            // Debug
            if (test) {
                plugin.getPVLogger().severe("---------");
                for (String[] item : itemList) {
                    plugin.getPVLogger().severe("ID: " + item[0]);
                    plugin.getPVLogger().severe("Quantity: " + item[1]);
                    plugin.getPVLogger().severe("Title: " + item[2]);
                    plugin.getPVLogger().severe("Price: " + item[3]);
                    plugin.getPVLogger().severe("---------");
                }
            }
            // Debug
            
            HashMap<String, Integer> items = new HashMap<>();
            for (String[] item : itemList) {
                String id;
                if (plugin.getPVConfig().getApiRoot()
                        .getString("apis.paypal.product-id-location").equalsIgnoreCase("ID")) {
                    id = item[0];
                }
                else {
                    String[] idStr = item[2].split(" ");
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
                                    + "\n" + " - Error: " + item[2]));
                    continue;
                }
                items.put(id, Integer.parseInt(item[1]));
                
                // Debug
                if (test) {
                    plugin.getPVLogger().severe("Added Item: " + item[2] + " | ID: " + item[0]);
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
