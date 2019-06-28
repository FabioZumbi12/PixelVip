package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.google.gson.JsonElement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import paypalnvp.core.PayPal;
import paypalnvp.profile.BaseProfile;
import paypalnvp.profile.Profile;
import paypalnvp.request.GetTransactionDetails;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PayPalHook implements PaymentModel {
    private PixelVip plugin;
    private boolean sandbox;
    private PayPal paypal;

    public PayPalHook(PixelVip plugin) {
        this.plugin = plugin;
        this.sandbox = plugin.getPVConfig().getApiRoot().getBoolean("apis.paypal.sandbox");

        try {
            Profile user = new BaseProfile.Builder(
                    plugin.getPVConfig().getApiRoot().getString("apis.paypal.username"),
                    plugin.getPVConfig().getApiRoot().getString("apis.paypal.password"))
                    .signature(plugin.getPVConfig().getApiRoot().getString("apis.paypal.signature")).build();
            paypal = new paypalnvp.core.PayPal(user,paypalnvp.core.PayPal.Environment.LIVE);
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

        //check if used
        if (plugin.getPVConfig().transExist(this.getPayname(), transCode)) {
            player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayname())));
            return true;
        }

        boolean test = plugin.getPVConfig().getApiRoot().getBoolean("apis.in-test");
        boolean success;
        try {

            GetTransactionDetails transactionDetails = new GetTransactionDetails(transCode);
            paypal.setResponse(transactionDetails);
            Map<String, String> response = transactionDetails.getNVPResponse();

            //check if exists
            if (!response.containsKey("PAYMENTSTATUS")) {
                return false;
            }

            //check if approved
            if (!response.get("PAYMENTSTATUS").equals("Completed")) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.waiting").replace("{payment}", getPayname())));
                plugin.processTrans.remove(transCode);
                return true;
            }

            /*for (Map.Entry<String, String> resp : response.entrySet()) {
                plugin.getPVLogger().severe(resp.getKey() + ": " + resp.getValue());
            }*/

            //check if expired
            Date oldCf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(plugin.getPVConfig().getApiRoot().getString("apis.paypal.ignoreOldest"));
            Date payCf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(response.get("ORDERTIME"));
            if (payCf.compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag", "payment.expired").replace("{payment}", getPayname())));
                return true;
            }

            List<String[]> itemList = new ArrayList<>();
            String index = "0";
            String[] itemArr = new String[]{"","","",""};
            for (Map.Entry<String, String> item : response.entrySet().stream().filter(map->map.getKey().startsWith("L_")).collect(Collectors.toList())) {
                String newIndex = item.getKey().substring(item.getKey().length()-1);
                if (newIndex.equals(index)){
                    String newKey = item.getKey().substring(0, item.getKey().length()-1);
                    if (newKey.equals("L_NUMBER"))
                        itemArr[0] = item.getValue();
                    if (newKey.equals("L_QTY"))
                        itemArr[1] = item.getValue();
                    if (newKey.equals("L_NAME"))
                        itemArr[2] = item.getValue();
                    if (newKey.equals("L_AMT"))
                        itemArr[3] = item.getValue();
                } else {
                    index = newIndex;
                    itemList.add(itemArr);
                    itemArr = new String[]{"","","",""};
                }
            }

            if (!itemList.contains(itemArr)) itemList.add(itemArr);

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
                // Debug
            }

            HashMap<String, Integer> items = new HashMap<>();
            for (String[] item : itemList) {
                String id;
                if (plugin.getPVConfig().getApiRoot().getString("apis.mercadopago.product-id-location").equalsIgnoreCase("ID")) {
                    id = item[0];
                } else {
                    String[] idStr = item[2].split(" ");
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
                                    " - Error: " + item[2]));
                    continue;
                }

                items.put(id, Integer.parseInt(item[1]));

                // Debug
                if (test) {
                    plugin.getPVLogger().severe("Added Item: " + item[2] + " | ID: " + item[0]);
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
