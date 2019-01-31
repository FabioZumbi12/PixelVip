package br.net.fabiozumbi12.pixelvip.sponge.PaymentsAPI;

import org.spongepowered.api.entity.living.player.Player;

public interface PaymentModel {

    String getPayname();

    boolean checkTransaction(Player player, String transCode);
}
