package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.com.uol.pagseguro.api.PagSeguro;
import br.com.uol.pagseguro.api.PagSeguroEnv;
import br.com.uol.pagseguro.api.common.domain.PaymentItem;
import br.com.uol.pagseguro.api.common.domain.TransactionStatus;
import br.com.uol.pagseguro.api.credential.Credential;
import br.com.uol.pagseguro.api.transaction.search.TransactionDetail;
import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class PagSeguroHook implements PaymentModel {
    private PixelVip plugin;
    private boolean sandbox;
    private PagSeguro pagSeguro;

    public PagSeguroHook(PixelVip plugin) {
        this.plugin = plugin;
        sandbox = plugin.getPVConfig().getApiRoot().getBoolean("apis.pagseguro.sandbox");
        try {
            Credential accCred = Credential.sellerCredential(plugin.getPVConfig().getApiRoot().getString("apis.pagseguro.email"), plugin.getPVConfig().getApiRoot().getString("apis.pagseguro.token"));
            PagSeguroEnv environment;
            environment = sandbox ? PagSeguroEnv.SANDBOX : PagSeguroEnv.PRODUCTION;
            pagSeguro = PagSeguro.instance(accCred, environment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPayname() {
        return "PagSeguro";
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
            TransactionDetail trans = pagSeguro.transactions().search().byCode(transCode);

            if (trans == null) {
                return false;
            }

            //check if approved
            if (!trans.getStatus().getStatus().equals(TransactionStatus.Status.APPROVED)) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting").replace("{payment}", getPayname())));
                return true;
            }

            //check if expired
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date oldCf = sdf.parse(plugin.getPVConfig().getApiRoot().getString("apis.pagseguro.ignoreOldest"));
            if (trans.getDate().compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired").replace("{payment}", getPayname())));
                return true;
            }


            // Debug
            if (test) {
                plugin.getPVLogger().severe("Items: " + trans);
                for (PaymentItem item : trans.getItems()) {
                    plugin.getPVLogger().severe("ID: " + item.getId());
                    plugin.getPVLogger().severe("Quantity: " + item.getQuantity());
                    plugin.getPVLogger().severe("Title: " + item.getDescription());
                }
            }
            // Debug

            HashMap<String, Integer> items = new HashMap<>();
            for (PaymentItem item : trans.getItems()) {
                String id;
                if (plugin.getPVConfig().getApiRoot().getString("apis.pagseguro.product-id-location").equalsIgnoreCase("ID")) {
                    id = item.getId();
                } else {
                    try {
                        String descId = item.getDescription().split(" ")[0];
                        Integer.parseInt(descId);
                        id = descId;
                    } catch (Exception ignored){
                        continue;
                    }
                }
                items.put(id, item.getQuantity());

                // Debug
                if (test) {
                    plugin.getPVLogger().severe("Added Item: " + item.getDescription() + " | ID: " + item.getId());
                }
            }

            success = plugin.getUtil().paymentItems(items, player, this.getPayname(), transCode);
        } catch (Exception e) {
            e.printStackTrace();
            plugin.processTrans.remove(transCode);
            return false;
        }

        //if success
        if (!sandbox || success) plugin.getPVConfig().addTrans(this.getPayname(), transCode, player.getName());
        plugin.processTrans.remove(transCode);
        return true;
    }
}
