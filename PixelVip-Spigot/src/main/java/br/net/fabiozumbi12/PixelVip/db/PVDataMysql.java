package br.net.fabiozumbi12.PixelVip.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import br.net.fabiozumbi12.PixelVip.PixelVip;

public class PVDataMysql implements PVDataManager {
	
	private PixelVip plugin;	
	
	private String host;
	private String dbname;
	private String username;
	private String password;
	private Connection con;
	
	private String keyTable;
	private String colKey;
	private String colKGroup;
	private String colKDuration;
	private String colKUses;
	private String colKInfo;
	private String colKCmds;
	private String colKKits;
	private String colKComment;	
	
	private String vipTable;
	private String colVUUID;	
	private String colVVip;
	private String colVPGroup;
	private String colVDuration;
	private String colVNick;
	private String colVExpires;
	private String colVActive;
	private String colVComment;

	
		
	public PVDataMysql(PixelVip plugin){
		this.plugin = plugin;
		
		this.host = plugin.getConfig().getString("configs.database.mysql.host");
		this.dbname = plugin.getConfig().getString("configs.database.mysql.db-name");
		this.username = plugin.getConfig().getString("configs.database.mysql.username");
		this.password = plugin.getConfig().getString("configs.database.mysql.password");
		
		this.keyTable = plugin.getConfig().getString("configs.database.mysql.keys.table-name");
        this.colKey = plugin.getConfig().getString("configs.database.mysql.keys.columns.key");
        this.colKGroup = plugin.getConfig().getString("configs.database.mysql.keys.columns.group");        
        this.colKDuration = plugin.getConfig().getString("configs.database.mysql.keys.columns.duration");
        this.colKUses = plugin.getConfig().getString("configs.database.mysql.keys.columns.uses");
        this.colKInfo = plugin.getConfig().getString("configs.database.mysql.keys.columns.info");
        this.colKCmds = plugin.getConfig().getString("configs.database.mysql.keys.columns.cmds");
        this.colKKits = plugin.getConfig().getString("configs.database.mysql.keys.columns.kits");
        this.colKComment = plugin.getConfig().getString("configs.database.mysql.keys.columns.comment");
		
		this.vipTable = plugin.getConfig().getString("configs.database.mysql.vips.table-name");
		this.colVUUID = plugin.getConfig().getString("configs.database.mysql.vips.columns.uuid");
		this.colVVip = plugin.getConfig().getString("configs.database.mysql.vips.columns.vip");
        this.colVPGroup = plugin.getConfig().getString("configs.database.mysql.vips.columns.playerGroup");
        this.colVDuration = plugin.getConfig().getString("configs.database.mysql.vips.columns.duration");
        this.colVNick = plugin.getConfig().getString("configs.database.mysql.vips.columns.nick");
        this.colVExpires = plugin.getConfig().getString("configs.database.mysql.vips.columns.expires-on-exact");
        this.colVActive = plugin.getConfig().getString("configs.database.mysql.vips.columns.active");
        this.colVComment = plugin.getConfig().getString("configs.database.mysql.vips.columns.comment");
		
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e2) {
            plugin.getPVLogger().severe("Couldn't find the driver for MySQL: com.mysql.jdbc.Driver.");
            setError();
            return;
        }
                
		try {
			//create connection
			con = DriverManager.getConnection(this.host + this.dbname + "?autoReconnect=true", this.username, this.password);
		} catch (SQLException e1) {
			e1.printStackTrace();
			setError();
			return;
		}
        
		//vips connection
		if (!checkVipTable()){
			try {				
				PreparedStatement st = con.prepareStatement("CREATE TABLE `"+vipTable+"` ("
						+colVUUID+" varchar(255), "
	        			+colVVip+" varchar(255), "
	        			+colVPGroup+" varchar(255), "
	        			+colVDuration+" int(13), "
	        			+colVNick+" varchar(255), "
	        			+colVExpires+" varchar(255), "
	        			+colVActive+" tinyint(1), "
	        			+colVComment+" varchar(255))");
	            st.executeUpdate();
	            st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
        }
        //keys connection
        if (!checkKeyTable()){
			try {
				PreparedStatement st = con.prepareStatement("CREATE TABLE `"+keyTable+"` ("
	        			+colKey+" varchar(255) PRIMARY KEY NOT NULL, "
	        			+colKGroup+" varchar(255), "
	        			+colKDuration+" int(13), "
	        			+colKUses+" int(10), "
	        			+colKInfo+" varchar(255), "
	        			+colKCmds+" varchar(255), "
	        			+colKKits+" int(13), "
	        			+colKComment+" varchar(255))");
	            st.executeUpdate();
	            st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
        }
	}
	
	private void setError(){		
		plugin.getConfig().set("configs.database.type", "file");
		plugin.getPVLogger().severe("Database set back to file!");
        plugin.saveConfig();
        plugin.reloadCmd();
	}
	
	private boolean checkKeyTable() {     
        try {
        	con = DriverManager.getConnection(this.host + this.dbname, this.username, this.password);
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, keyTable, null);
            if (rs.next()) {
            	rs.close();
            	return true;               
            }
        	rs.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
	
	private boolean checkVipTable() {     
        try {
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, vipTable, null);
            if (rs.next()) {
            	rs.close();
            	return true;               
            }
        	rs.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
	
	@Override
	public void saveVips() {}
	
	@Override
	public void saveKeys() {}
	
	private boolean vipExist(String uuid, String group){
		int total = 0;
		try {
            PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM `"+vipTable+"` WHERE "+colVUUID+" = ? AND "+colVVip+" = ?");
            st.setString(1, uuid);
            st.setString(2, group);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return total > 0;
	}
	
	@Override
	public void addRawVip(String group, String uuid, String pgroup, long duration, String nick, String expires) {
		if (!vipExist(uuid, group)){
			try {
				PreparedStatement st = con.prepareStatement("INSERT INTO `"+vipTable+"` VALUES (?,?,?,?,?,?,?)");
				st.setString(1, uuid);
				st.setString(2, group);
				st.setString(3, pgroup);
				st.setLong(4, duration);
				st.setString(5, nick);
				st.setString(6, expires);
				st.setString(7, "");
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private boolean keyExist(String key){
		int total = 0;
		try {
            PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM `"+keyTable+"` WHERE "+colKey+" = ?");
            st.setString(1, key);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return total > 0;
	}
	
	@Override
	public void addRawKey(String key, String group, long duration, int uses) {
		if (!keyExist(key)){
			try {
				PreparedStatement st = con.prepareStatement("INSERT INTO `"+keyTable+"` VALUES (?,?,?,?,?,?)");
				st.setString(1, key);
				st.setString(2, group);
				st.setLong(3, duration);
				st.setInt(4, uses);
				st.setString(5, "");
				st.setString(6, "");
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public void addRawItemKey(String key, List<String> cmds) {
		String query;
		if (!keyExist(key)){
			query = "INSERT INTO `"+keyTable+"` ("+colKCmds+","+colKey+") VALUES (?,?)";
		} else {
			query = "UPDATE `"+keyTable+"` SET "+colKCmds+" = ? WHERE "+colKey+" = ?";
		}
		
		try {
			PreparedStatement st = con.prepareStatement(query);				
			st.setString(1, cmds.toArray(new String[cmds.size()]).toString());
			st.setString(2, key);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public HashMap<String, List<String[]>> getActiveVipList() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public HashMap<String, List<String[]>> getAllVipList() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String[]> getVipInfo(String puuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> getItemKeyCmds(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void removeKey(String key) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeItemKey(String key) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Set<String> getListKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> getItemListKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] getKeyInfo(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setKeyUse(String key, int uses) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setVipActive(String id, String vip, boolean active) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setVipDuration(String id, String vip, long duration) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean containsVip(String id, String vip) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setVipKitCooldown(String id, String vip, long cooldown) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeVip(String id, String vip) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public long getVipCooldown(String id, String vip) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public long getVipDuration(String id, String vip) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isVipActive(String id, String vip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void closeCon() {
		try {
			if (this.con != null && !this.con.isClosed()){
				this.con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}