package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class MercadoPagoHook implements PaymentModel {
    private PixelVip plugin;
    private boolean sandbox;

    public MercadoPagoHook(PixelVip plugin) {
        this.plugin = plugin;
        this.sandbox = plugin.getPVConfig().getApiRoot().getBoolean("apis.mercadopago.sandbox");
        String accToken = plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.access-token");

        try {
            MercadoPago.SDK.setAccessToken(accToken);
        } catch (MPConfException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPayname() {
        return "MercadoPago";
    }

    @Override
    public boolean checkTransaction(Player player, String transCode) {

        //check if used
        if (plugin.getPVConfig().transExist(this.getPayname(), transCode)) {
            player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayname())));
            return true;
        }

        boolean test = plugin.getPVConfig().getApiRoot().getBoolean("apis.in-test");
        boolean success;
        try {
            JsonElement payment_info = MercadoPago.SDK.Get(this.sandbox ? "/sandbox" : "" + "/v1/payments/" + transCode).getJsonElementResponse();

            if (payment_info.getAsJsonObject().getAsJsonPrimitive("status").getAsString().equals("404")) {
                return false;
            }

            //check if approved
            if (!payment_info.getAsJsonObject().getAsJsonPrimitive("status").getAsString().equalsIgnoreCase("approved")) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting").replace("{payment}", getPayname())));
                return true;
            }

            //check if expired
            try {
                Date oldCf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.ignoreOldest"));
                Date transCf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(payment_info.getAsJsonObject().getAsJsonPrimitive("date_last_updated").getAsString());
                if (transCf.compareTo(oldCf) < 0) {
                    player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired").replace("{payment}", getPayname())));
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JsonArray jItems;
            if (payment_info.getAsJsonObject().get("additional_info").getAsJsonObject().has("items")){
                jItems = payment_info.getAsJsonObject().get("additional_info").getAsJsonObject().get("items").getAsJsonArray();
            } else {
                String[] description = payment_info.getAsJsonObject().get("description").getAsString().split(" ");
                String desc = payment_info.getAsJsonObject().get("description").getAsString();

                // Id handling
                String id;
                Optional<String> optId = Arrays.stream(description).filter(i -> i.startsWith("#")).findFirst();
                if (plugin.getPackageManager().getPackage(description[0]) != null){
                    id = description[0];
                } else if (optId.isPresent()){
                    id = optId.get().substring(optId.get().indexOf("#"));
                } else {
                    plugin.getPVLogger().warning("ID of Item not found, setting to 0: " + desc);
                    id = String.valueOf(0);
                }

                String quantity = description[description.length-1];

                jItems = new JsonArray();
                jItems.add(new JsonParser().parse(
                        "{\"id\":\""+id+"\",\"quantity\":\""+quantity+"\",\"title\":\""+desc+"\",\"unit_price\":\""+payment_info.getAsJsonObject().get("transaction_details").getAsJsonObject().get("net_received_amount").getAsString()+"\"}"));
            }

            // Debug
            if (test) {
                plugin.getPVLogger().severe("Items: " + jItems);
                plugin.getPVLogger().severe("---------");
                for (JsonElement item : jItems) {
                    plugin.getPVLogger().severe("ID: " + item.getAsJsonObject().get("id").getAsString());
                    plugin.getPVLogger().severe("Quantity: " + item.getAsJsonObject().get("quantity").getAsString());
                    plugin.getPVLogger().severe("Title: " + item.getAsJsonObject().get("title").getAsString());
                    plugin.getPVLogger().severe("Price: " + item.getAsJsonObject().get("unit_price").getAsString());
                    plugin.getPVLogger().severe("---------");
                }
                // Debug
            }

            HashMap<String, Integer> items = new HashMap<>();
            for (JsonElement item : jItems) {
                String id;
                if (plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.product-id-location").equalsIgnoreCase("ID")) {
                    id = item.getAsJsonObject().get("id").getAsString();
                } else {
                    String[] idStr = item.getAsJsonObject().get("title").getAsString().split(" ");
                    Optional<String> optId = Arrays.stream(idStr).filter(i -> i.startsWith("#")).findFirst();
                    if (plugin.getPackageManager().getPackage(idStr[0]) != null){
                        id = idStr[0];
                    } else id = optId.map(s -> s.substring(s.indexOf("#"))).orElseGet(() -> String.valueOf(0));
                }

                if (plugin.getPackageManager().getPackage(id) == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getPVConfig().getLang("_pluginTag") +
                                    plugin.getPVConfig().getLang("payment.noitems")
                                            .replace("{payment}",getPayname()) + "\n" +
                                    " - Error: " + item.getAsJsonObject().get("title").getAsString()));
                    continue;
                }

                items.put(id, item.getAsJsonObject().get("quantity").getAsInt());

                // Debug
                if (test) {
                    plugin.getPVLogger().severe("Added Item: " + item.getAsJsonObject().get("title").getAsString() + " | ID: " + item.getAsJsonObject().get("id").getAsString());
                }
            }

            success = plugin.getUtil().paymentItems(items, player, this.getPayname(), transCode);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.processTrans.remove(transCode);
            return false;
        }

        //if success
        if (success && !sandbox) plugin.getPVConfig().addTrans(this.getPayname(), transCode, player.getName());
        plugin.processTrans.remove(transCode);
        return true;
    }
}
