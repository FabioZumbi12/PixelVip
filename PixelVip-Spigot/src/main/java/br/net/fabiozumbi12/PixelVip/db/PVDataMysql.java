package br.net.fabiozumbi12.PixelVip.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	private String colKComment;	
	
	private String vipTable;
	private String colVUUID;	
	private String colVVip;
	private String colVPGroup;
	private String colVDuration;
	private String colVNick;
	private String colVExpires;
	private String colVActive;
	private String colVKits;
	private String colVComment;

	private String transTable;

	private String colTID;

	private String colTNick;

	
		
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
        this.colKComment = plugin.getConfig().getString("configs.database.mysql.keys.columns.comments");
		
		this.vipTable = plugin.getConfig().getString("configs.database.mysql.vips.table-name");
		this.colVUUID = plugin.getConfig().getString("configs.database.mysql.vips.columns.uuid");
		this.colVVip = plugin.getConfig().getString("configs.database.mysql.vips.columns.vip");
        this.colVPGroup = plugin.getConfig().getString("configs.database.mysql.vips.columns.playerGroup");
        this.colVDuration = plugin.getConfig().getString("configs.database.mysql.vips.columns.duration");
        this.colVNick = plugin.getConfig().getString("configs.database.mysql.vips.columns.nick");
        this.colVExpires = plugin.getConfig().getString("configs.database.mysql.vips.columns.expires-on-exact");
        this.colVActive = plugin.getConfig().getString("configs.database.mysql.vips.columns.active");
        this.colVKits = plugin.getConfig().getString("configs.database.mysql.vips.columns.kits");
        this.colVComment = plugin.getConfig().getString("configs.database.mysql.vips.columns.comments");
		
        this.transTable = plugin.getConfig().getString("configs.database.mysql.transactions.table-name");
		this.colTID = plugin.getConfig().getString("configs.database.mysql.transactions.columns.idt");
		this.colTNick = plugin.getConfig().getString("configs.database.mysql.transactions.columns.nick");
		
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e2) {
            plugin.getPVLogger().severe("Couldn't find the driver for MySQL: com.mysql.jdbc.Driver.");
            setError();
            return;
        }
                
		try {
			//create connection
			tryConnect();
		} catch (SQLException e1) {
			e1.printStackTrace();
			setError();
			return;
		}
        
		//vips connection
		if (!checkVipTable()){
			try {				
				PreparedStatement st = this.con.prepareStatement("CREATE TABLE `"+vipTable+"` ("
						+"uuid_bin binary(16), "
						+colVUUID+" varchar(36) generated always as (insert(insert(insert(insert(hex(uuid_bin),9,0,?),14,0,?),19,0,?),24,0,?)) virtual, "
	        			+colVVip+" varchar(128), "
	        			+colVPGroup+" varchar(128), "
	        			+colVDuration+" bigint(13), "
	        			+colVNick+" varchar(128), "
	        			+colVExpires+" varchar(128), "
	        			+colVActive+" tinyint(1), "
	        			+colVKits+" bigint(13), "
	        			+colVComment+" varchar(255))");
				st.setString(1, "-");
				st.setString(2, "-");
				st.setString(3, "-");
				st.setString(4, "-");
	            st.executeUpdate();
	            st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
        }
        //keys connection
        if (!checkKeyTable()){
			try {
				PreparedStatement st = this.con.prepareStatement("CREATE TABLE `"+keyTable+"` ("
	        			+colKey+" varchar(128) PRIMARY KEY, "
	        			+colKGroup+" varchar(128), "
	        			+colKDuration+" bigint(13), "
	        			+colKUses+" int(13), "
	        			+colKInfo+" varchar(128), "
	        			+colKCmds+" varchar(255), "	        			
	        			+colKComment+" varchar(255))");
	            st.executeUpdate();
	            st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
        }
        //transactions connection
        if (!checkTransTable()){
			try {
				PreparedStatement st = this.con.prepareStatement("CREATE TABLE `"+transTable+"` ("
	        			+colTID+" varchar(128) PRIMARY KEY, "
	        			+colTNick+" varchar(128))");
	            st.executeUpdate();
	            st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}        	
        }
	}
	
	private void tryConnect() throws SQLException{
		if (this.con != null && !this.con.isClosed()){
			this.con.close();
		}
		this.con = DriverManager.getConnection(this.host + this.dbname + "?autoReconnect=true", this.username, this.password);
	}
	
	private void setError(){		
		plugin.getConfig().set("configs.database.type", "file");
		plugin.getPVLogger().severe("Database set back to file!");
        plugin.saveConfig();
        plugin.reloadCmd();
	}
	
	private boolean checkKeyTable() {     
        try {
        	this.con = DriverManager.getConnection(this.host + this.dbname, this.username, this.password);
            DatabaseMetaData meta = this.con.getMetaData();
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
            DatabaseMetaData meta = this.con.getMetaData();
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
	
	private boolean checkTransTable() {     
        try {
            DatabaseMetaData meta = this.con.getMetaData();
            ResultSet rs = meta.getTables(null, null, transTable, null);
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
	
	@Override
	public boolean containsVip(String uuid, String group){
		int total = 0;
		try {
            PreparedStatement st = this.con.prepareStatement("SELECT COUNT(*) FROM `"+vipTable+"` WHERE "+colVUUID+" = ? AND "+colVVip+" = ?");
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
	public void addRawVip(String group, String uuid, String pgroup, long duration, String nick, String expires, boolean active) {
		if (!containsVip(uuid, group)){
			try {
				PreparedStatement st = this.con.prepareStatement("INSERT INTO `"+vipTable+"` (uuid_bin,"
						+colVVip+","
			            +colVPGroup+","
						+colVDuration+", "
			            +colVNick+", "
						+colVExpires+","
						+colVActive+") VALUES (unhex(replace(?,'-','')),?,?,?,?,?,?)");
				st.setString(1, uuid);
				st.setString(2, group);
				st.setString(3, pgroup);
				st.setLong(4, duration);
				st.setString(5, nick);
				st.setString(6, expires);
				st.setBoolean(7, active);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void addRawVip(String group, String uuid, String pgroup, long duration, String nick, String expires) {
		if (!containsVip(uuid, group)){
			try {
				PreparedStatement st = this.con.prepareStatement("INSERT INTO `"+vipTable+"` (uuid_bin,"
						+colVVip+","
			            +colVPGroup+","
						+colVDuration+", "
			            +colVNick+", "
						+colVExpires+") VALUES (unhex(replace(?,'-','')),?,?,?,?,?)");
				st.setString(1, uuid);
				st.setString(2, group);
				st.setString(3, pgroup);
				st.setLong(4, duration);
				st.setString(5, nick);
				st.setString(6, expires);
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
            PreparedStatement st = this.con.prepareStatement("SELECT COUNT(*) FROM `"+keyTable+"` WHERE "+colKey+" = ?");
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
		try {
			PreparedStatement st = this.con.prepareStatement("INSERT INTO `"+keyTable+"` ("
		            +colKey+","
					+colKGroup+","
		            +colKDuration+","
					+colKUses+", "
		            +colKInfo+
		            ") VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE "
					+colKGroup+"=?, "
		            +colKDuration+"=?, "
		            +colKUses+"=?, "
		            +colKInfo+"=?");
			st.setString(1, key);
			st.setString(2, group);
			st.setLong(3, duration);
			st.setInt(4, uses);
			st.setString(5, String.valueOf(plugin.getUtil().millisToDay(duration))+plugin.getConfig().getString("strings.days"));
			st.setString(6, group);
			st.setLong(7, duration);
			st.setInt(8, uses);
			st.setString(9, String.valueOf(plugin.getUtil().millisToDay(duration))+plugin.getConfig().getString("strings.days"));
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void addRawItemKey(String key, List<String> cmds) {
		try {
			PreparedStatement st = this.con.prepareStatement("INSERT INTO `"+keyTable+"` ("+colKey+","+colKCmds+") VALUES (?,?) ON DUPLICATE KEY UPDATE "+colKCmds+"=?");		
			StringBuilder strBuilder = new StringBuilder();
			String[] cmdsArr = cmds.toArray(new String[cmds.size()]);
			for (int i = 0; i < cmdsArr.length; i++) {
				strBuilder.append(cmdsArr[i]+",");
			}
			String cmdsB = strBuilder.toString().substring(0, strBuilder.toString().length()-1);
			st.setString(1, key);
			st.setString(2, cmdsB);			
			st.setString(3, cmdsB);
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public HashMap<String, List<String[]>> getActiveVipList() {
		HashMap<String, List<String[]>> activeVips = new HashMap<String, List<String[]>>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT "+colVUUID+" FROM `"+vipTable+"`");
			ResultSet rs = st.executeQuery();
			while (rs.next()){
				String uuid = rs.getString(colVUUID);
				List<String[]> actives = new ArrayList<String[]>();
				getVipInfo(uuid).stream().filter(vip->vip[3].equals("true")).forEach(vipInfos->{
					actives.add(vipInfos);
				});		
				activeVips.put(uuid, actives);
			}			
			st.close();
			rs.close();
		} catch (SQLException e) {			
			e.printStackTrace();
			try {
				tryConnect();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} 
		return activeVips;
	}
		
	@Override
	public HashMap<String, List<String[]>> getAllVipList() {
		HashMap<String, List<String[]>> activeVips = new HashMap<String, List<String[]>>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT "+colVUUID+" FROM `"+vipTable+"`");
			ResultSet rs = st.executeQuery();
			while (rs.next()){
				String uuid = rs.getString(colVUUID);
				activeVips.put(uuid, getVipInfo(uuid));
			}			
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return activeVips;
	}
	
	@Override
	public List<String[]> getVipInfo(String puuid) {
		List<String[]> vips = new ArrayList<String[]>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT "
					+colVVip+", "
        			+colVPGroup+", "
        			+colVDuration+", "
        			+colVNick+", "
        			+colVActive+
        			" FROM `"+vipTable+"` WHERE "+colVUUID+"=?");
			st.setString(1, puuid);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				String[] info = new String[]{
						String.valueOf(rs.getLong(colVDuration)),
						rs.getString(colVVip),
						rs.getString(colVPGroup),
						String.valueOf(rs.getBoolean(colVActive)),
						rs.getString(colVNick)};
				vips.add(info);
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vips;
	}
	
	@Override
	public List<String> getItemKeyCmds(String key) {
		List<String> cmds = new ArrayList<String>();
		if (keyExist(key)){			
			try {
				PreparedStatement st = this.con.prepareStatement("SELECT "+colKCmds+" FROM `"+keyTable+"` WHERE "+colKey+"=? AND "+colKCmds+" IS NOT NULL");
				st.setString(1, key);
				ResultSet rs = st.executeQuery();
				if (rs.next()){
					cmds.addAll(Arrays.asList(rs.getString(colKCmds).replace(", ", ",").split(",")));
				}
				st.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}			
		return cmds;
	}
	
	@Override
	public void removeKey(String key) {
		if (keyExist(key)){
			try {
				String query = "";
				if (getItemKeyCmds(key).size() > 0){
					query = "UPDATE `"+keyTable+"` SET "
		        			+colKGroup+"=NULL, "
		        			+colKDuration+"=NULL, "
		        			+colKUses+"=NULL, "
		        			+colKInfo+"=NULL WHERE "+colKey+"=?";
				} else {
					query = "DELETE FROM `"+keyTable+"` WHERE "+colKey+"=?";
				}
				PreparedStatement st = this.con.prepareStatement(query);
				st.setString(1, key);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public void removeItemKey(String key) {
		try {
			String query = "";
			if (getKeyInfo(key).length == 3){
				query = "UPDATE `"+keyTable+"` SET "+colKCmds+"=NULL WHERE "+colKey+"=?";
			} else {
				query = "DELETE FROM `"+keyTable+"` WHERE "+colKey+"=?";
			}
			PreparedStatement st = this.con.prepareStatement(query);
			st.setString(1, key);
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<String> getListKeys() {
		Set<String> keys = new HashSet<String>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT "+colKey+" FROM `"+keyTable+"` WHERE "+colKGroup+" IS NOT NULL");
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				keys.add(rs.getString(colKey));
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keys;
	}
	
	@Override
	public Set<String> getItemListKeys() {
		Set<String> keys = new HashSet<String>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT "+colKey+" FROM `"+keyTable+"` WHERE "+colKCmds+" IS NOT NULL");
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				keys.add(rs.getString(colKey));
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keys;
	}
	
	@Override
	public String[] getKeyInfo(String key) {
		String[] info = new String[0];
		if (keyExist(key)){
			try {
				PreparedStatement st = this.con.prepareStatement("SELECT "+colKGroup+", "+colKDuration+", "+colKUses+" FROM `"+keyTable+"` WHERE "+colKey+"=? AND "+colKGroup+" IS NOT NULL");
				st.setString(1, key);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					info = new String[]{rs.getString(colKGroup), String.valueOf(rs.getLong(colKDuration)), String.valueOf(rs.getInt(colKUses))};
				}
				st.close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
		return info;
	}
	
	@Override
	public void setKeyUse(String key, int uses) {
		if (keyExist(key)){
			try {
				PreparedStatement st = this.con.prepareStatement("UPDATE `"+keyTable+"` SET "+colKUses+"=? WHERE "+colKey+"=? AND "+colKGroup+" IS NOT NULL");
				st.setInt(1, uses);
				st.setString(2, key);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	@Override
	public void setVipActive(String uuid, String vip, boolean active) {
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("UPDATE `"+vipTable+"` SET "+colVActive+"=? WHERE "+colVUUID+"=? AND "+colVVip+"=?");
				st.setBoolean(1, active);
				st.setString(2, uuid);
				st.setString(3, vip);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setVipDuration(String uuid, String vip, long duration) {
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("UPDATE `"+vipTable+"` SET "+colVDuration+"=? WHERE "+colVUUID+"=? AND "+colVVip+"=?");
				st.setLong(1, duration);
				st.setString(2, uuid);
				st.setString(3, vip);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setVipKitCooldown(String uuid, String vip, long cooldown) {
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("UPDATE `"+vipTable+"` SET "+colVKits+"=? WHERE "+colVUUID+"=? AND "+colVVip+"=?");
				st.setLong(1, cooldown);
				st.setString(2, uuid);
				st.setString(3, vip);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void removeVip(String uuid, String vip) {
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("DELETE FROM `"+vipTable+"` WHERE "+colVUUID+"=? AND "+colVVip+"=?");				
				st.setString(1, uuid);
				st.setString(2, vip);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public long getVipCooldown(String uuid, String vip) {
		long count = 0;
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("SELECT "+colVKits+" FROM `"+vipTable+"` WHERE "+colVUUID+"=? AND "+colVVip+"=?");				
				st.setString(1, uuid);
				st.setString(2, vip);
				ResultSet rs = st.executeQuery();
				if (rs.next()){
					count = rs.getLong(colVKits);
				}
				rs.close();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	
	@Override
	public long getVipDuration(String uuid, String vip) {
		long count = 0;
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("SELECT "+colVDuration+" FROM `"+vipTable+"` WHERE "+colVUUID+"=? AND "+colVVip+"=?");				
				st.setString(1, uuid);
				st.setString(2, vip);
				ResultSet rs = st.executeQuery();
				if (rs.next()){
					count = rs.getLong(colVDuration);
				}
				rs.close();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	
	@Override
	public boolean isVipActive(String uuid, String vip) {
		boolean active = true;
		if (containsVip(uuid, vip)){
			try {
				PreparedStatement st = this.con.prepareStatement("SELECT "+colVActive+" FROM `"+vipTable+"` WHERE "+colVUUID+"=? AND "+colVVip+"=? AND "+colVActive+" IS NOT NULL");				
				st.setString(1, uuid);
				st.setString(2, vip);
				ResultSet rs = st.executeQuery();
				if (rs.next()){
					active = rs.getBoolean(colVActive);
				}
				rs.close();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return active;
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

	@Override
	public String getVipUUID(String player) {
		String uuid = null;
		try {			
			PreparedStatement st = this.con.prepareStatement("SELECT "+colVUUID+" FROM `"+vipTable+"` WHERE "+colVNick+"=?");				
			st.setString(1, player);
			ResultSet rs = st.executeQuery();
			if (rs.next()){
				uuid = rs.getString(colVUUID);
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return uuid;
	}

	@Override
	public void removeTrans(String trans) {
		if (transactionExist(trans)){
			try {
				PreparedStatement st = this.con.prepareStatement("DELETE FROM `"+transTable+"` WHERE "+colTID+"=?");				
				st.setString(1, trans);
				st.executeUpdate();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void addTras(String trans, String player) {
		try {
			PreparedStatement st = this.con.prepareStatement("INSERT INTO `"+transTable+"` ("+colTID+","+colTNick+") VALUES (?,?) ON DUPLICATE KEY UPDATE "+colTNick+"=?");
			st.setString(1, trans);
			st.setString(2, player);			
			st.setString(3, player);
			st.executeUpdate();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean transactionExist(String trans) {
		int total = 0;
		try {
            PreparedStatement st = this.con.prepareStatement("SELECT COUNT(*) FROM `"+transTable+"` WHERE "+colTID+" = ?");
            st.setString(1, trans);
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
	public HashMap<String, String> getAllTrans(){
		HashMap<String, String> trans = new HashMap<String, String>();
		try {
			PreparedStatement st = this.con.prepareStatement("SELECT * FROM `"+transTable+"`");
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				trans.put(rs.getString(colTID), rs.getString(colTNick));
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return trans;
	}
	
}