package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import br.net.fabiozumbi12.pixelvip.bukkit.PixelVip;
import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.WooCommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WooCommerceHook implements PaymentModel {
    private final WooCommerce wooCommerce;
    private final PixelVip _plugin;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss");

    public WooCommerceHook(PixelVip plugin) {
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
        if (player == null) {
            return false;
        }
        boolean test = _plugin.getPVConfig().getApiRoot().getBoolean("apis.in-test");
        String playerName = player.getName();
        String playerId = player.getUniqueId().toString();

        // check if used
        if (_plugin.getPVConfig().transExist(getPayName(), transCode)) {
            notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                    .getLang("_pluginTag", "payment.codeused").replace("{payment}", getPayName()))));
            return true;
        }

        try {
            Integer.parseInt(transCode);
        } catch (NumberFormatException ex) {
            return false;
        }

        _plugin.getScheduler().runAsync(() -> processTransaction(playerId, playerName, transCode, test));
        return true;
    }

    private void processTransaction(String playerId, String playerName, String transCode, boolean test) {
        try {
            int id = Integer.parseInt(transCode);
            Map list = wooCommerce.get(EndpointBaseType.ORDERS.getValue(), id);
            if (!list.isEmpty()) {

                if (test) {
                    _plugin.getPVLogger().severe("Order id: " + list.get("id").toString());
                    _plugin.getPVLogger().severe("Order status: " + list.get("status").toString());
                    _plugin.getPVLogger().severe("Order date completed: " + list.get("date_completed").toString());
                }

                // check status
                if (!Objects.equals(list.get("status").toString(), _plugin.getPVConfig().getApiRoot().getString("apis.woocommerce.ordercompleted"))) {
                    notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil()
                            .toColor(_plugin.getPVConfig().getLang("_pluginTag", "payment.waiting")
                                    .replace("{payment}", getPayName()))));
                    return;
                }

                // check if expired
                LocalDateTime dtConfig = LocalDateTime.parse(Objects.requireNonNull(_plugin.getPVConfig().getApiRoot()
                        .getString("apis.woocommerce.ignoreOldest")), FORMATTER);
                LocalDateTime dtOrder = LocalDateTime.parse(list.get("date_completed").toString());
                if (dtOrder.isBefore(dtConfig)) {
                    notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil()
                            .toColor(_plugin.getPVConfig().getLang("_pluginTag", "payment.expired")
                                    .replace("{payment}", getPayName()))));
                    return;
                }

                // handle items
                List<Map> items = (List<Map>) list.get("line_items");
                if (!items.isEmpty()) {
                    for (Map item : items) {
                        String idItem = item.get("product_id").toString();
                        int qtdItem = (int) item.get("quantity");
                        String nameItem = item.get("name").toString();

                        // no items delivered
                        if (qtdItem == 0) {
                            notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                    .getLang("_pluginTag", "payment.noitems")
                                    .replace("{payment}", getPayName())
                                    .replace("{transaction}", idItem)
                            )));
                            continue;
                        }

                        if (test) {
                            _plugin.getPVLogger().severe("Item id: " + idItem);
                            _plugin.getPVLogger().severe("Item name: " + nameItem);
                            _plugin.getPVLogger().severe("Item quantity: " + qtdItem);
                        }

                        String cmd = _plugin.getPVConfig().getApiRoot().getString("apis.commandIds." + idItem);
                        if (cmd != null) {
                            _plugin.getPVLogger().info("Running \"" + cmd + "\" for player " + playerName + " from E-Commerce integration: " + getPayName());
                            for (int i = 0; i < qtdItem; i++) {
                                String resolved = cmd.replace("{p}", playerName).replace("{player}", playerName);
                                _plugin.getUtil().ExecuteCmd(resolved, null);
                                notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                        .getLang("_pluginTag", "payment.delivered")
                                        .replace("{payment}", getPayName())
                                        .replace("{item}", nameItem)
                                )));
                            }
                        } else {
                            _plugin.getPVLogger().info("Item not found on config with id " + idItem + " from player " + playerName + " for order " + transCode);
                            notifyPlayer(playerId, p -> p.sendMessage(_plugin.getUtil().toColor(_plugin.getPVConfig()
                                    .getLang("_pluginTag", "payment.notfound")
                                    .replace("{payment}", getPayName())
                                    .replace("{id}", idItem)
                                    .replace("{item}", nameItem)
                            )));
                        }
                    }
                }
                _plugin.getPVConfig().addTrans(getPayName(), transCode, playerName);
            }
        } catch (Exception ex) {
            _plugin.getPVLogger().warning("Error on process WooCommerce transaction: " + ex.getMessage());
        }
    }

    private void notifyPlayer(String playerId, java.util.function.Consumer<Player> action) {
        _plugin.getScheduler().runSync(() -> {
            Player player = Bukkit.getPlayer(java.util.UUID.fromString(playerId));
            if (player != null) {
                _plugin.getScheduler().runEntity(player, () -> action.accept(player));
            }
        });
    }
}
