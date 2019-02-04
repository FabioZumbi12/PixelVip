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
import java.util.Date;
import java.util.HashMap;

public class PagSeguroHook implements PaymentModel {
    private PixelVip plugin;
    private boolean sandbox;
    private PagSeguro pagSeguro;

    public PagSeguroHook(PixelVip plugin) {
        this.plugin = plugin;
        sandbox = plugin.getPVConfig().getBoolean(true, "apis.pagseguro.sandbox");
        try {
            Credential accCred = Credential.sellerCredential(plugin.getConfig().getString("apis.pagseguro.email"), plugin.getConfig().getString("apis.pagseguro.token"));
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date oldCf = sdf.parse(plugin.getConfig().getString("apis.pagseguro.ignoreOldest"));
            if (trans.getDate().compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired").replace("{payment}", getPayname())));
                return true;
            }

            HashMap<Integer, String> items = new HashMap<>();
            for (PaymentItem item : trans.getItems()) {
                String[] ids = item.getId().split(" ");
                for (String id : ids)
                    if (id.startsWith("#"))
                        items.put(item.getQuantity(), id.substring(1));
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
