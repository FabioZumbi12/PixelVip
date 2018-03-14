package br.net.fabiozumbi12.pixelvip.bukkit;

import br.com.uol.pagseguro.domain.AccountCredentials;
import br.com.uol.pagseguro.domain.Credentials;
import br.com.uol.pagseguro.domain.Item;
import br.com.uol.pagseguro.domain.Transaction;
import br.com.uol.pagseguro.exception.PagSeguroServiceException;
import br.com.uol.pagseguro.service.TransactionSearchService;
import org.bukkit.command.CommandSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PVPagSeguro {
	private Transaction trans;
	private PixelVip plugin;
	private Credentials accCred;
	
	public PVPagSeguro(PixelVip plugin){
		this.plugin = plugin;
		try {
			this.accCred = new AccountCredentials(plugin.getConfig().getString("apis.pagseguro.email"), plugin.getConfig().getString("apis.pagseguro.token"));
		} catch (PagSeguroServiceException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkTransaction(CommandSender player, String transCode){
		try {
			trans = TransactionSearchService.searchByCode(accCred, transCode);
		} catch (Exception e) {
			plugin.processTrans.remove(transCode);
			return false;
		}
		
		if (trans == null){
			plugin.processTrans.remove(transCode);
			return false;
		}
		
		if (trans.getStatus().getValue() != 3 && trans.getStatus().getValue() != 4){
			plugin.processTrans.remove(transCode);
			player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","pagseguro.waiting")));
			return true;
		}
				
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
			Date oldCf = sdf.parse(plugin.getConfig().getString("apis.pagseguro.ignoreOldest"));			
			if (trans.getLastEventDate().compareTo(oldCf) < 0){
				player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","pagseguro.expired")));
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//debug
        boolean debug = plugin.getPVConfig().getBoolean(true, "apis.pagseguro.debug");

		if (debug) printTransaction(trans);

		int log = 0;
		for (Item item:trans.getItems()){
            if (debug) System.out.println("item ID: >" + item.getId() + "<");
            String[] ids = item.getId().split(" ");

            if (!item.getDescription().isEmpty()){
                ids = item.getDescription().split(" ");
                if (debug) System.out.println("item Description: >" + item.getDescription() + "<");
            }
            int amount = item.getQuantity();
            for (String id:ids){
            	// description "id:<id from config>"
                if (debug) System.out.println("Command ID: >" + id + "<");
            	if (id.startsWith("id:")){
            		String cmdId = id.replace("id:", "");
            		String command = plugin.getConfig().getString("apis.commandIds."+cmdId);
            		if (command != null){
                        command = command.replace("{p}", player.getName());
            			for (int i = 0; i < amount; i++){
            				plugin.serv.dispatchCommand(plugin.serv.getConsoleSender(), command);
            			}            			
            			plugin.addLog("API:Pagseguro | "+player.getName()+" | Item Cmd:"+command+" | Transaction Code: "+trans);
            			log++;
            		}            		
            	}
            }            
        }
		
		if (log == 0){
			player.sendMessage(plugin.getUtil().toColor(plugin.getPVConfig().getLang("_pluginTag","pagseguro.noitems")));
		}
		//if success
		plugin.getPVConfig().addTrans(transCode, player.getName());
		plugin.processTrans.remove(transCode);
		return true;
	}
	

	private static void printTransaction(Transaction transaction) {
        System.out.println("code: " + transaction.getCode());
        System.out.println("date: " + transaction.getDate());
        System.out.println("discountAmount: " + transaction.getDiscountAmount());
        System.out.println("extraAmount: " + transaction.getExtraAmount());
        System.out.println("feeAmount: " + transaction.getFeeAmount());
        System.out.println("grossAmount: " + transaction.getGrossAmount());
        System.out.println("installmentCount: " + transaction.getInstallmentCount());
        System.out.println("itemCount: " + transaction.getItemCount());
        for (int i = 0; i < transaction.getItems().size(); i++) {
            System.out.println("item[" + (i + 1) + "]: " + transaction.getItems().get(i).getId());
            System.out.println("item[" + (i + 1) + "]: " + transaction.getItems().get(i).getDescription());
            System.out.println("item[" + (i + 1) + "]: " + transaction.getItems().get(i).getQuantity());
            System.out.println("item[" + (i + 1) + "]: " + transaction.getItems().get(i).getAmount());
            
            
        }
        System.out.println("lastEventDate: " + transaction.getLastEventDate());
                
        System.out.println("netAmount: " + transaction.getNetAmount());
        System.out.println("paymentMethodType: " + transaction.getPaymentMethod().getCode().getValue());
        System.out.println("paymentMethodcode: " + transaction.getPaymentMethod().getType().getValue());
        System.out.println("reference: " + transaction.getReference());
        System.out.println("senderEmail: " + transaction.getSender().getEmail());
        if (transaction.getSender() != null) {
            System.out.println("senderPhone: " + transaction.getSender().getPhone());
        }
        if (transaction.getShipping() != null) {
            System.out.println("shippingType: " + transaction.getShipping().getType().getValue());
            System.out.println("shippingCost: " + transaction.getShipping().getCost());
            if (transaction.getShipping().getAddress() != null) {
                System.out.println("shippingAddressCountry: " + transaction.getShipping().getAddress().getCountry());
                System.out.println("shippingAddressState: " + transaction.getShipping().getAddress().getState());
                System.out.println("shippingAddressCity: " + transaction.getShipping().getAddress().getCity());
                System.out.println("shippingAddressPostalCode: "
                        + transaction.getShipping().getAddress().getPostalCode());
                System.out.println("shippingAddressDistrict: " + transaction.getShipping().getAddress().getDistrict());
                System.out.println("shippingAddressStreet: " + transaction.getShipping().getAddress().getStreet());
                System.out.println("shippingAddressNumber: " + transaction.getShipping().getAddress().getNumber());
                System.out.println("shippingAddressComplement: "
                        + transaction.getShipping().getAddress().getComplement());
            }
        }
        System.out.println("status: " + transaction.getStatus().getValue());
        System.out.println("type: " + transaction.getType().getValue());
    }
}
