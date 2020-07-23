/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.etz.app3d.client.core.App3DClient;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 *
 * @author Damilola.Omowaye
 */
public class NewApp3DDetails {

    private static Logger logger = Logger.getLogger(NewApp3DDetails.class);

    public static final List<String> BANK = new ArrayList();
    public static final List<String> BANK_CODE = new ArrayList();
    public static final Map<String, String> BANKCODE = new HashMap();
    private static String[] newApp3DClient = Utils.getConfigDetails("NEW_APP3D_CLIENT_DETAILS").split("~");
    private static String apiGatewayEndpoint = Utils.getConfigDetails("API_GATEWAY_URL");
    
    
    public static void main(String[] args) {
        //System.out.println("Banks =================== "+getBankList());
        //System.out.println(getBank(5));
       System.out.println(getBankCode(getBank(5)));
       System.out.println("Bank :::::::::"+BANK_CODE.contains(getBankCode(getBank(5))));

       //System.out.println("Bank :::::::::"+BANK_CODE.toString());
        //System.out.println("list :::::::: "+getBankList());

    }

    static {
        for (String app3DClient1 : newApp3DClient) {
            String[] tmp = app3DClient1.split(":");
            BANKCODE.put(tmp[1], tmp[0]);
            BANK.add(tmp[1]);
            BANK_CODE.add(tmp[0]);
        }
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

    public static boolean validateAccount(String bankCode, String accountNumber, String mobile, String reference) {
        boolean phoneMatch = false;
        App3DClient app3DClient = new App3DClient(apiGatewayEndpoint, 20);
        JsonObject responseObj = app3DClient.doAccountQuery(bankCode, accountNumber, reference, mobile);
        logger.info("App3D Client Response: " + responseObj.toString());
        logger.info("Trace Id: " + responseObj.get("traceId").getAsString());

        if (responseObj.get("statusCode").getAsInt() == 200) {
            JsonObject app3DResponse = responseObj.get("responseBody").getAsJsonObject();
            if (app3DResponse.get("responseCode").getAsInt() == 0) {
                phoneMatch = app3DResponse.get("phoneMatch").getAsBoolean();
                if (!phoneMatch) {
                    logger.info("Customer phone doesn't match");
                } else {
                    logger.info("Customer phone matches");
                }
            } else {
                logger.info("Failed Request: " + app3DResponse.get("message").getAsString());
            }
        }
        return phoneMatch;
    }
    
    

}
