package br.net.fabiozumbi12.pixelvip.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class DatabaseCategory {

    @Setting
    public String type = "file";

    @Setting
    public MysqlCat mysql = new MysqlCat();

    @ConfigSerializable
    public static class MysqlCat {

        @Setting
        public String host = "jdbc:mysql://localhost:3306/";
        @Setting(value = "db-name")
        public String db_name = "pixelvip_epic";
        @Setting
        public String username = "username";
        @Setting
        public String password = "password";

        @Setting
        public VipKeysCat keys = new VipKeysCat();
        @Setting
        public VipCat vips = new VipCat();
        @Setting
        public TransCat transactions = new TransCat();

        @ConfigSerializable
        public static class VipKeysCat {

            @Setting(value = "table-name")
            public String table_name = "pixelvip_keys";

            @Setting
            public ColumsKeysCat columns = new ColumsKeysCat();

            @ConfigSerializable
            public static class ColumsKeysCat {

                @Setting
                public String key = "col_key";
                @Setting
                public String group = "col_group";
                @Setting
                public String duration = "col_duration";
                @Setting
                public String uses = "col_uses";
                @Setting
                public String cmds = "col_cmds";
                @Setting
                public String info = "col_info";
                @Setting
                public String comments = "col_comments";
            }
        }

        @ConfigSerializable
        public static class VipCat {

            @Setting(value = "table-name")
            public String table_name = "pixelvip_vips";

            @Setting
            public ColumsVipCat columns = new ColumsVipCat();

            @ConfigSerializable
            public static class ColumsVipCat {

                @Setting
                public String uuid = "col_uuid";
                @Setting
                public String vip = "col_vip";
                @Setting
                public String playerGroup = "col_playerGroup";
                @Setting
                public String duration = "col_duration";
                @Setting
                public String nick = "col_nick";
                @Setting(value = "expires-on-exact")
                public String expires_on_exact = "col_expires";
                @Setting
                public String active = "col_active";
                @Setting
                public String kits = "col_kits";
                @Setting
                public String comments = "col_comments";

            }
        }

        @ConfigSerializable
        public static class TransCat {

            @Setting(value = "table-name")
            public String table_name = "pixelvip_transactions";

            @Setting
            public ColumsTransCat columns = new ColumsTransCat();

            @ConfigSerializable
            public static class ColumsTransCat {

                @Setting
                public String idt = "col_idt";
                @Setting
                public String nick = "col_nick";
                @Setting
                public String payment = "col_payment";
            }
        }
    }
}
