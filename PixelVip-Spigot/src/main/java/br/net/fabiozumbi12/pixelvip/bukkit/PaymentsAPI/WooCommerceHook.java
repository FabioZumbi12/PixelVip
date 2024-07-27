package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.WooCommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WooCommerceHook  implements PaymentModel {
    private final WooCommerce wooCommerce;
    private final PixelVip _plugin;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss");

    public WooCommerceHook(PixelVip plugin){
        _plugin = plugin;
        OAuthConfig config = new OAuthConfig(
                plugin.getPVConfig().getApiRoot().getString("apis.woocommerce.shopurl"),
                plugin.getPVConfig().getApiRoot().getString("apis.woocommerce.consumerkey"),
                plugin.getPVConfig().getApiRoot().getString("apis.woocommerce.consumersecret"));
        wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);
    }

    @Override
    public String getPayName() {
        return "WooCommerce";
    }

    @Override
    public boolean checkTransaction(Player player, String transCode) {
        boolean test = _plugin.getPVConfig().getApiRoot().getBoolean("apis.in-test");

        // check if used
        if (_plugin.getPVConfig().transExist(getPayName(), transCode)) {
            player.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                    .getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayName())));
            return true;
        }

        try {
            int id = Integer.parseInt(transCode);
            Map list = wooCommerce.get(EndpointBaseType.ORDERS.getValue(), id);
            if (!list.isEmpty()) {

                if (test){
                    _plugin.getPVLogger().severe("Order id: " + list.get("id").toString());
                    _plugin.getPVLogger().severe("Order status: " + list.get("status").toString());
                    _plugin.getPVLogger().severe("Order date completed: " + list.get("date_completed").toString());
                }

                // check status
                if (!Objects.equals(list.get("status").toString(), _plugin.getPVConfig().getApiRoot().getString("apis.woocommerce.ordercompleted"))) {
                    player.sendMessage(_plugin.getUtil()
                            .toColor(_plugin.getPVConfig().getLang("_pluginTag", "payment.waiting")
                                    .replace("{payment}", getPayName())));
                    return true;
                }

                // check if expired
                LocalDateTime dtConfig = LocalDateTime.parse(Objects.requireNonNull(_plugin.getPVConfig().getApiRoot()
                        .getString("apis.woocommerce.ignoreOldest")), FORMATTER);
                LocalDateTime dtOrder = LocalDateTime.parse(list.get("date_completed").toString());
                if (dtOrder.isBefore(dtConfig)){
                    player.sendMessage(_plugin.getUtil()
                            .toColor(_plugin.getPVConfig().getLang("_pluginTag", "payment.expired")
                                    .replace("{payment}", getPayName())));
                    return true;
                }

                // handle items
                List<Map> items = (List<Map>)list.get("line_items");
                if (!items.isEmpty()){
                    for (Map item : items){
                        String idItem = item.get("product_id").toString();
                        int qtdItem = (int)item.get("quantity");
                        String nameItem = item.get("name").toString();

                        // no items delivered
                        if (qtdItem == 0) {
                            player.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                    .getLang("_pluginTag", "payment.noitems")
                                    .replace("{payment}", getPayName())
                                    .replace("{transaction}", idItem)
                            ));
                            continue;
                        }

                        if (test){
                            _plugin.getPVLogger().severe("Item id: " + idItem);
                            _plugin.getPVLogger().severe("Item name: " + nameItem);
                            _plugin.getPVLogger().severe("Item quantity: " + qtdItem);
                        }

                        String cmd = _plugin.getPVConfig().getApiRoot().getString("apis.commandIds." + idItem);
                        if (cmd != null){
                            _plugin.getPVLogger().info("Running \""+ cmd +"\" for player " + player.getName() + " from E-Commerce integration: " + getPayName());
                            for (int i = 0; i < qtdItem; i++) {
                                _plugin.getUtil().ExecuteCmd(cmd, player);
                                player.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                        .getLang("_pluginTag", "payment.delivered")
                                        .replace("{payment}", getPayName())
                                        .replace("{item}", nameItem)
                                ));
                            }
                        } else {
                            _plugin.getPVLogger().info("Item not found on config with id " + idItem + " from player " + player.getName() + " for order " + transCode);
                            player.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                    .getLang("_pluginTag", "payment.notfound")
                                    .replace("{payment}", getPayName())
                                    .replace("{id}", idItem)
                                    .replace("{item}", nameItem)
                            ));
                        }
                    }
                }
                _plugin.getPVConfig().addTrans(getPayName(), transCode, player.getName());
                return true;
            }
        } catch (Exception ex){
            _plugin.getPVLogger().warning("Error on process WooCommerce transaction: " + ex.getMessage());
        }
        return false;
    }
}
