package br.net.fabiozumbi12.pixelvip.bukkit.PaymentsAPI;

import org.bukkit.entity.Player;

public interface PaymentModel {

    String getPayname();

    boolean checkTransaction(Player player, String transCode);
}
