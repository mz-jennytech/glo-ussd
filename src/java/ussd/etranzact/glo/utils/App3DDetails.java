/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.etz.ussd.dto.App3D;
import com.etz.ussd.dto.Bank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Omowaye Damilola <damilola.omowaye@etranzact.com>
 */
public class App3DDetails {

    private static final List<String> BANK = new ArrayList();
    private static final Map<String, String> BANKCODE = new HashMap();
    private static String[] app3DClient = Utils.getConfigDetails("APP3D_CLIENT_DETAILS").split("-");
    private static String[] bankDetails = Utils.getConfigDetails("NEW_APP3D_CLIENT_DETAILS").split("~");

    public static void main(String[] args) {
        System.out.println("Banks:: "+getBankListing());
        System.out.println(BANK.toString());
        System.out.println(getBankCode(getBank(1)));
        System.out.println(getBank("044"));

//        List<Bank> allBanks = getAllBanks();
//        for (Bank allBank : allBanks) {
//            System.out.println(allBank);
//        }
    }

    static {

        for (String app3DClient1 : bankDetails) {
            String[] tmp = app3DClient1.split(":");
            //String[] tmp = temp[1].split("~");
            BANKCODE.put(tmp[1], tmp[0]);
            BANK.add(tmp[1]);
        }
    }

//    public static List<Bank> getAllBanks() {
//        List<Bank> banks = new ArrayList();
//        try {
//
//            for (int i = 0; i < app3DClient.length; i++) {
//                String temp[] = app3DClient[i].split(":");
//                String[] tmp = temp[1].split("~");
//                //for (int j = 0; j < tmp.length; j++) {                    
//                Bank b = new Bank(i + 1, tmp[2], temp[0]);
//                banks.add(b);
//                //}
//            }
//        } catch (Exception x) {
//            x.printStackTrace();
//        }
//        return banks;
//    }

    public static App3D getClientDetails(String bankCode) {
        App3D app = null;
        try {
            String[] app3DClient = Utils.getConfigDetails("APP3D_CLIENT_DETAILS").split("-");

            for (int i = 0; i < app3DClient.length; i++) {
                String temp[] = app3DClient[i].split(":");
                for (int j = 0; j < temp.length; j++) {
                    if (bankCode.equals(temp[0])) {
                        String[] tmp = temp[1].split("~");
                        app = new App3D(tmp[1], tmp[0]);
                    }
                }

            }

        } catch (Exception x) {
            x.printStackTrace();
        }

        return app;
    }

    public static String getBank(int position) {
        return (String) BANK.get(position - 1);
    }

    public static String getBankCode(String bankAcronym) {
        return (String) BANKCODE.get(bankAcronym);
    }

    public static String getBankList() {
        String retVal = "";
        int counter = 1;
        for (String value : BANK) {
            retVal = retVal + counter + ". " + value + "~";
            counter++;
        }
        return retVal;
    }
    
    public static String getBankListing() {
        String retVal = "";        
        for (String value : BANK) {
            retVal = retVal + value + "~";           
        }
        return retVal;
    }


    public static String getBank(String bankCode) {
        String app = null;

        try {
            String[] app3DClient = Utils.getConfigDetails("NEW_APP3D_CLIENT_DETAILS").split("~");

            for (int i = 0; i < app3DClient.length; i++) {
                String temp[] = app3DClient[i].split(":");
                for (int j = 0; j < temp.length; j++) {
                    if (bankCode.equals(temp[0])) {
                       // String[] tmp = temp[1].split("~");
                        app = temp[1];
                        break;
                    }
                }

            }

        } catch (Exception x) {
            x.printStackTrace();
        }

        return app;
    }
}
