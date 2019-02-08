package br.net.fabiozumbi12.pixelvip.sponge.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;
import com.google.gson.JsonElement;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;
import org.spongepowered.api.entity.living.player.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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
            plugin.processTrans.remove(transCode);
            return true;
        }

        boolean success;
        try {
            JsonElement payment_info = MercadoPago.SDK.Get(this.sandbox ? "/sandbox" : "" + "/v1/payments/" + transCode).getJsonElementResponse();

            //plugin.getPVLogger().severe("All: " + payment_info);
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
