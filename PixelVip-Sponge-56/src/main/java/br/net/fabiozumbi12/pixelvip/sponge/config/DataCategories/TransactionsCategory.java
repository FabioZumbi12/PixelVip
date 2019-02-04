package br.net.fabiozumbi12.pixelvip.sponge.config.DataCategories;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class TransactionsCategory {

    @Setting
    public Map<String, Transactions> transactions = new HashMap<>();

    @ConfigSerializable
    public static class Transactions {
        @Setting
        public Map<String, String> payment;

        public Transactions() {
        }

        public Transactions(Map<String, String> payment) {
            this.payment = payment;
        }
    }
}
