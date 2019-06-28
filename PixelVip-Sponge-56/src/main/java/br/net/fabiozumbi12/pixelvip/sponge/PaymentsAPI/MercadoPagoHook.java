package br.net.fabiozumbi12.pixelvip.sponge.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import org.spongepowered.api.entity.living.player.Player;

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
        this.sandbox = plugin.getConfig().root().apis.mercadopago.sandbox;
        String accToken = plugin.getConfig().root().apis.mercadopago.access_token;

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

        if (plugin.getConfig().transExist(getPayname(), transCode)) {
            player.sendMessage(plugin.getUtil().toText(plugin.getConfig().root().strings._pluginTag + plugin.getConfig().root().strings.pay_codeused.replace("{payment}", getPayname())));
            return true;
        }

        boolean success;
        try {
            JsonElement payment_info = MercadoPago.SDK.Get(this.sandbox ? "/sandbox" : "" + "/v1/payments/" + transCode).getJsonElementResponse();

            if (payment_info.getAsJsonObject().getAsJsonPrimitive("status").getAsString().equals("404")) {
                return false;
            }

            //check if approved
            if (!payment_info.getAsJsonObject().getAsJsonPrimitive("status").getAsString().equalsIgnoreCase("approved")) {
                player.sendMessage(plugin.getUtil().toText(plugin.getConfig().root().strings._pluginTag + plugin.getConfig().root().strings.pay_waiting.replace("{payment}", getPayname())));
                return true;
            }

            //check if expired
            try {
                Date oldCf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(plugin.getConfig().root().apis.mercadopago.ignoreOldest);
                Date transCf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(payment_info.getAsJsonObject().getAsJsonPrimitive("date_last_updated").getAsString());
                if (transCf.compareTo(oldCf) < 0) {
                    player.sendMessage(plugin.getUtil().toText(plugin.getConfig().root().strings._pluginTag + plugin.getConfig().root().strings.pay_expired.replace("{payment}", getPayname())));
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
                    plugin.getLogger().info("ID of Item not found, setting to 0: " + desc);
                    id = String.valueOf(0);
                }

                String quantity = description[description.length-1];

                jItems = new JsonArray();
                jItems.add(new JsonParser().parse(
                        "{\"id\":\""+id+"\",\"quantity\":\""+quantity+"\",\"title\":\""+desc+"\",\"unit_price\":\""+payment_info.getAsJsonObject().get("transaction_details").getAsJsonObject().get("net_received_amount").getAsString()+"\"}"));
            }

            HashMap<Integer, String> items = new HashMap<>();
            String[] itemArr = payment_info.getAsJsonObject().getAsJsonPrimitive("description").getAsString().split(",");
            for (String item : itemArr) {
                String[] qtdArr = item.split("x");
                int qtd = Integer.parseInt(qtdArr[qtdArr.length - 1].replace(" ", ""));
                String[] ids = item.split(" ");
                for (String id : ids)
                    if (id.startsWith("#"))
                        items.put(qtd, id.substring(1));
            }

            success = plugin.getUtil().paymentItems(items, player, this.getPayname(), transCode);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.processTrans.remove(transCode);
            return false;
        }

        //if success
        if (success && !sandbox) plugin.getConfig().addTrans(this.getPayname(), transCode, player.getName());
        plugin.processTrans.remove(transCode);
        return true;
    }
}
