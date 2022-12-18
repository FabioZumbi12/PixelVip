package br.net.fabiozumbi12.pixelvip.sponge.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.sponge.PixelVip;

import com.google.gson.Gson;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentItem;

import org.spongepowered.api.entity.living.player.Player;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MercadoPagoHook implements PaymentModel {
    
    private static final Gson GSON = new Gson();
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private PixelVip plugin;
    private PaymentClient client;
    
    public MercadoPagoHook(PixelVip plugin) {
        this.plugin = plugin;
        
        MercadoPagoConfig.setAccessToken(plugin.getConfig().root().apis.mercadopago.access_token);
        
        client = new PaymentClient();
    }
    
    @Override
    public String getPayname() {
        return "MercadoPago";
    }
    
    @Override
    public boolean checkTransaction(Player player, String transCode) {
        // check if used
        if (plugin.getConfig().transExist(getPayname(), transCode)) {
            player.sendMessage(plugin.getUtil()
                    .toText(plugin.getConfig().root().strings._pluginTag
                            + plugin.getConfig().root().strings.pay_codeused.replace("{payment}",
                                    getPayname())));
            return true;
        }
        boolean test = plugin.getConfig().root().apis.in_test;
        
        try {
            Payment payment = client.get(Long.parseLong(transCode));
            
            if (payment.getStatus().equals("404")) {
                return false;
            }
            
            // check if approved
            if (!payment.getStatus().equalsIgnoreCase("approved")) {
                player.sendMessage(plugin.getUtil()
                        .toText(plugin.getConfig().root().strings._pluginTag
                                + plugin.getConfig().root().strings.pay_waiting.replace("{payment}",
                                        getPayname())));
                return true;
            }
            
            // check if expired
            OffsetDateTime oldCf = OffsetDateTime.of(
                    LocalDateTime.parse(plugin.getConfig().root().apis.mercadopago.ignoreOldest,
                            FORMATTER),
                    ZoneOffset.systemDefault().getRules()
                            .getOffset(LocalDateTime.now(ZoneOffset.systemDefault())));
            
            if (payment.getDateLastUpdated().compareTo(oldCf) < 0) {
                player.sendMessage(plugin.getUtil()
                        .toText(plugin.getConfig().root().strings._pluginTag
                                + plugin.getConfig().root().strings.pay_expired.replace("{payment}",
                                        getPayname())));
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
                    plugin.getLogger().warn("ID of Item not found, setting to 0: " + desc);
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
                plugin.getLogger().error("Items: " + jItems);
                plugin.getLogger().error("---------");
                
                for (PaymentItem item : jItems) {
                    plugin.getLogger().error("ID: " + item.getId());
                    plugin.getLogger().error("Quantity: " + item.getQuantity());
                    plugin.getLogger().error("Title: " + item.getTitle());
                    plugin.getLogger().error("Price: " + item.getUnitPrice());
                    plugin.getLogger().error("---------");
                }
            }
            // Debug
            
            HashMap<Integer, String> items = new HashMap<>();
                        
            for (String item : payment.getDescription().split(",")) {
                String[] qtdArr = item.split("x");
                int qtd = Integer.parseInt(qtdArr[qtdArr.length - 1].replace(" ", ""));
                String[] ids = item.split(" ");

                for (String id : ids) {
                    if (id.startsWith("#")) {
                        items.put(qtd, id.substring(1));
                    }
                }
            }
            if (plugin.getUtil().paymentItems(items, player, getPayname(), transCode)) {
                plugin.getConfig().addTrans(this.getPayname(), transCode, player.getName());
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
