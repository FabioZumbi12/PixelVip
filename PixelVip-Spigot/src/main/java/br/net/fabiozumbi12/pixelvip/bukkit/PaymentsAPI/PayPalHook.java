package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import org.bukkit.entity.Player;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsReq;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetTransactionDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.apis.eBLBaseComponents.PaymentItemType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PayPalHook implements PaymentModel {
    private PixelVip plugin;
    private boolean sandbox;
    private PayPalAPIInterfaceServiceService payPalAPIInterfaceServiceService;

    public PayPalHook(PixelVip plugin) {
        this.plugin = plugin;
        this.sandbox = plugin.getConfig().getBoolean("apis.paypal.sandbox");
        try {
            Map<String, String> customProperties = new HashMap<String, String>() {{
                put("mode", sandbox ? "sandbox" : "live");
                put("acct1.UserName", plugin.getConfig().getString("apis.paypal.username"));
                put("acct1.Password", plugin.getConfig().getString("apis.paypal.password"));
                put("acct1.Signature", plugin.getConfig().getString("apis.paypal.signature"));
            }};

            payPalAPIInterfaceServiceService = new PayPalAPIInterfaceServiceService(customProperties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getPayname() {
        return "PayPal";
    }

    @Override
    public boolean checkTransaction(Player player, String transCode) {
        boolean success;
        try {
            GetTransactionDetailsReq getTransactionDetailsReq = new GetTransactionDetailsReq();
            GetTransactionDetailsRequestType requestType = new GetTransactionDetailsRequestType();
            requestType.setTransactionID(transCode);
            requestType.setDetailLevel(null);
            getTransactionDetailsReq.setGetTransactionDetailsRequest(requestType);
            GetTransactionDetailsResponseType trans = payPalAPIInterfaceServiceService.getTransactionDetails(getTransactionDetailsReq);

            if (!trans.getErrors().isEmpty()) {
                return false;
            }

            if (!trans.getPaymentTransactionDetails().getPaymentInfo().getPaymentStatus().getValue().equalsIgnoreCase("Completed")) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting").replace("{payment}", getPayname())));
                plugin.processTrans.remove(transCode);
                return true;
            }

            if (plugin.getPVConfig().transExist(getPayname(), transCode)) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayname())));
                plugin.processTrans.remove(transCode);
                return true;
            }

            Date oldCf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(plugin.getConfig().getString("apis.paypal.ignoreOldest"));
            Date payCf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(trans.getPaymentTransactionDetails().getPaymentInfo().getPaymentDate());
            if (payCf.compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired").replace("{payment}", getPayname())));
                return true;
            }

            HashMap<Integer, String> items = new HashMap<>();
            for (PaymentItemType pay : trans.getPaymentTransactionDetails().getPaymentItemInfo().getPaymentItem()) {
                String[] ids = pay.getName().split(" ");
                for (String id : ids)
                    if (id.startsWith("#"))
                        items.put(Integer.parseInt(pay.getQuantity()), id.substring(1));
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
