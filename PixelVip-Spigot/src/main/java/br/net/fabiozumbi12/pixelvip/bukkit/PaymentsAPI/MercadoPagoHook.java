package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
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

            JsonArray jItems = payment_info.getAsJsonObject().get("additional_info").getAsJsonObject().get("items").getAsJsonArray();

            // Debug
            if (test) {
                plugin.getPVLogger().severe("Items: " + jItems);
                for (JsonElement item : jItems) {
                    plugin.getPVLogger().severe("ID: " + item.getAsJsonObject().get("id").getAsString());
                    plugin.getPVLogger().severe("Quantity: " + item.getAsJsonObject().get("quantity").getAsString());
                    plugin.getPVLogger().severe("Title: " + item.getAsJsonObject().get("title").getAsString());
                    plugin.getPVLogger().severe("Price: " + item.getAsJsonObject().get("unit_price").getAsString());
                }
                // Debug
            }

            HashMap<String, Integer> items = new HashMap<>();
            for (JsonElement item : jItems) {
                String id = null;
                if (plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.product-id-location").equalsIgnoreCase("ID")) {
                    id = item.getAsJsonObject().get("id").getAsString();
                } else {
                    Optional<String> optId = Arrays.stream(item.getAsJsonObject().get("title").getAsString().split(" ")).filter(i -> i.startsWith("#")).findFirst();
                    if (optId.isPresent())
                        id = optId.get().substring(1);
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
