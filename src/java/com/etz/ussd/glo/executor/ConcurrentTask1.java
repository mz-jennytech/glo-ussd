/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.ussd.glo.executor;

import com.etz.ussd.session.USSDSession;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import ussd.etranzact.glo.dto.VasGateResponse;
import ussd.etranzact.glo.service.GloService;
import ussd.etranzact.glo.utils.FlowProcessor;
import ussd.etranzact.glo.utils.Utility;

/**
 *
 * @author Omowaye Damilola
 */
public class ConcurrentTask1 implements Runnable {

    private static final Logger L = Logger.getLogger(ConcurrentTask1.class);
    GloService service = GloService.getInstance();
    
    private JsonObject jsonObject;
    private USSDSession sessionData;
    private String bankCode;
    private String accountNo;
    private String amount;
    private String reference;
    private String desc;

    public ConcurrentTask1() {
    }

    public ConcurrentTask1(JsonObject jsonObject, USSDSession sessionData) {
        this.jsonObject = jsonObject;
        this.sessionData = sessionData;
    }

    public ConcurrentTask1(JsonObject jsonObject, USSDSession sessionData, String bankCode, String accountNo, String amount, String reference, String desc) {
        this.jsonObject = jsonObject;
        this.sessionData = sessionData;
        this.bankCode = bankCode;
        this.accountNo = accountNo;
        this.amount = amount;
        this.reference = reference;
        this.desc = desc;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public USSDSession getSessionData() {
        return sessionData;
    }

    public void setSessionData(USSDSession sessionData) {
        this.sessionData = sessionData;
    }

    public void run(JsonObject json, USSDSession session, String bankCode, String accountNo, String amount, String reference, String desc) {
        System.out.println("Concurrent Task Runner called!!!");
        String response = null;
        VasGateResponse resp = Utility.postToVasgate(session.getMsisdn(), json.toString());
        service.updateTransaction(reference,resp.getError());
        if (resp.getError().startsWith("0")) {
            if (desc.startsWith("VT:")) {
                response = "Your Airtime purchase was successful. Thank you.";
            } else if (desc.startsWith("DT:")) {
                response = "Your Data bundle purchase was successful. Thank you.";
            } else {
                response = "Your transaction was successful. Thank you.";
            }            
        } else if (resp.getResponseCode().equals("503")) {
            if (desc.startsWith("VT:")) {
                response = "Your Airtime purchase was successful. Thank you.";
            } else if (desc.startsWith("DT:")) {
                response = "Your Data bundle purchase was successful. Thank you.";
            } else {
                response = "Your transaction was successful. Thank you.";
            }
        } else {
            FlowProcessor.reverseTransaction(bankCode, accountNo, session, amount, reference, desc);
            if (desc.startsWith("VT:")) {
                response = "Your Airtime purchase was not successful. You will be contacted soon. Thank you. ";
            } else if (desc.startsWith("DT:")) {
                response = "Your Data bundle purchase was not successful.  You will be contacted soon. Thank you. ";
            } else {
                response = "Your transaction was not successful. You will be contacted soon. Thank you. ";
            }
        }
        System.out.println("Response after Concurrent Task Runner finished!!");
        System.out.println(response);
//         if (response.equals("0")) {
//            System.out.println("SMS not sent because transaction was successful!");
//         } else {
//            try {
//                sendSMS(sessionData.getMsisdn(), "Sorry! Your transaction was not successful. It will be reversed shortly. Please try again later. Thank you");
//            } catch (Exception ex) {
//                java.util.logging.Logger.getLogger(ConcurrentTask1.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            System.out.println("SMS is sent");
//         }
    }

    private static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        System.out.println("\nSending 'GET' request to URL : " + url);
        int responseCode = con.getResponseCode();

        System.out.println("Response Code : " + responseCode);
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("RESPONSE FROM SERVICE:: " + response.toString());
        } catch (Exception ex) {

        }
        return response.toString();
    }

    private void printObj(Object obj, Class clazz) {
        try {
            L.info(clazz.getSimpleName() + "{");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(obj);
                L.info("\t" + name + " : " + value);
                //System.out.printf("Field name: %s, Field value: %s%n", name, value);
            }
            L.info("}");
        } catch (SecurityException securityException) {
        } catch (IllegalArgumentException illegalArgumentException) {
        } catch (IllegalAccessException illegalAccessException) {
        }
    }

    @Override
    public void run() {
        System.out.println("+++++++++++++++++++++++++++++++Running");
        run(jsonObject, sessionData, bankCode, accountNo, amount, reference, desc);
        System.out.println("+++++++++++++++++++++++++++++++Done");
    }

    private static void sendSMS(String msisdn, String message) throws Exception {
        StringBuilder sb = new StringBuilder("http://172.16.10.45/?mobile=");
        sb.append(msisdn);
        sb.append("&message=");
        sb.append(URLEncoder.encode(message));
        sb.append("&header=GLOeService");
        sb.append("&ticketid=");
        sb.append("");
        System.out.println(sb.toString());
        sendGet(sb.toString());
    }

    public static void main(String[] args) {
        try {
            sendSMS("2348052772499", "Confirm SMS reception.");
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ConcurrentTask1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
