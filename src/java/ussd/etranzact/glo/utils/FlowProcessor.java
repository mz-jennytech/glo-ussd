/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.utils;

import com.etz.http.etc.Card;
import com.etz.http.etc.HttpHost;
import com.etz.http.etc.TransCode;
import com.etz.http.etc.XProcessor;
import com.etz.http.etc.XRequest;
import com.etz.http.etc.XResponse;
import com.etz.ussd.dto.App3D;
import com.etz.ussd.dto.Bank;
import com.etz.ussd.session.USSDSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ussd.etranzact.glo.dto.App3DResponse;
import ussd.etranzact.glo.model.UssdMobileTransactionLog;
import ussd.etranzact.glo.service.GloService;

/**
 *
 * @author Omowaye Damilola
 */
public class FlowProcessor {

    private final static Logger LOG = Logger.getLogger(FlowProcessor.class);
    //private static final String SWITCHIP = "172.17.10.101";//Demo autoswitch
    private static final String SWITCHIP = Utils.getConfigDetails("SWITCH_IP");//Prod autoswitch
    private static final String PORT = Utils.getConfigDetails("SWITCH_PORT");
    private static final String app3DURL = Utils.getConfigDetails("APP3D_URL"); //Live
    // private static final String app3DURL = "http://172.17.10.101/3DApp/invoke.jsp?"; //Demo

    private static int sequence;

    private static final String key = Utils.getConfigDetails("KEY"); // 128 bit key
    private static final String initVector = Utils.getConfigDetails("INIT_VECTOR"); // 16 bytes IV

    private static final String callerID = Utils.getConfigDetails("CALLER_ID");
    private static final String clientName = Utils.getConfigDetails("CLIENT_NAME");
    private static final String password = Utils.getConfigDetails("PASSWORD");
    private static final String customBillerID = Utils.getConfigDetails("CUSTOM_BILLERID");

    public static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setConnectTimeout(2000);
        con.setReadTimeout(5000);
        int responseCode = con.getResponseCode();
        LOG.info("\nSending 'GET' request to URL : " + url);
        LOG.info("Response Code : " + responseCode);
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            LOG.info("RESPONSE FROM SERVICE:: " + response.toString());
        } catch (Exception ex) {

        }
        return response.toString();
    }

    private static App3DResponse getAccountDetails(String clientId, String providerId, String accountNumber, String phone) {
        App3DResponse resp = null;
        StringBuilder sb = new StringBuilder(FlowProcessor.app3DURL);
        sb.append("clientid=").append(clientId).append("&");
        sb.append("providerid=").append(providerId).append("&");
        sb.append("xmlinfo=<Request>");
        sb.append("<clientID>").append(clientId).append("</clientID>");
        sb.append("<providerID>").append(providerId).append("</providerID>");
        sb.append("<providerName>").append(providerId).append("</providerName>");
        sb.append("<accountNumber>").append(accountNumber).append("</accountNumber>");
        sb.append("<otherInfo>").append(phone).append("</otherInfo>");
        sb.append("</Request>");
        try {
            String sendGet = sendGet(sb.toString());
            resp = convert2Response(sendGet);
        } catch (Exception ex) {
            LOG.info(ex);
        }
        return resp;
    }

    private static App3DResponse getAccountDetailsDiamond(String clientId, String providerId, String accountNumber, String phone) {
        App3DResponse resp = null;
        StringBuilder sb = new StringBuilder(FlowProcessor.app3DURL);
        sb.append("clientid=").append(clientId).append("&");
        sb.append("providerid=").append(providerId).append("&");
        sb.append("xmlinfo=<Request>");
        sb.append("<clientID>").append(clientId).append("</clientID>");
        sb.append("<providerID>").append(providerId).append("</providerID>");
        sb.append("<providerName>").append(providerId).append("</providerName>");
        sb.append("<accountNumber>").append(accountNumber).append("</accountNumber>");
        //" + mobile + ":" + amount + ":" + reference + ":GLO
        sb.append("<otherInfo>").append(phone).append(":1:").append(new Date().getTime()).append("</otherInfo>");
        sb.append("</Request>");
        try {
            String sendGet = sendGet(sb.toString());
            resp = convert2Response(sendGet);
        } catch (Exception ex) {
            LOG.info(ex);
        }
        return resp;
    }

    private static App3DResponse convert2Response(String responseXml) {
        App3DResponse resp = null;
        try {
            resp = new App3DResponse();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseXml));
            Document doc = db.parse(is);

            NodeList responseCode = doc.getElementsByTagName("responseCode");
            Element rc = (Element) responseCode.item(0);
            String rcs = getCharacterDataFromElement(rc);
            resp.setResponseCode(rcs);
            NodeList otherInfo = doc.getElementsByTagName("otherInfo");
            Element oi = (Element) otherInfo.item(0);
            String ois = getCharacterDataFromElement(oi);
            resp.setOtherInfo(ois);
            NodeList accountNumber = doc.getElementsByTagName("accountNumber");
            Element an = (Element) accountNumber.item(0);
            String ans = getCharacterDataFromElement(an);
            resp.setAccountNumber(ans);
            NodeList providerName = doc.getElementsByTagName("providerName");
            Element pn = (Element) providerName.item(0);
            String pns = getCharacterDataFromElement(pn);
            resp.setProviderName(pns);
            NodeList providerID = doc.getElementsByTagName("providerID");
            Element pi = (Element) providerID.item(0);
            String pis = getCharacterDataFromElement(pi);
            resp.setProviderID(pis);
            NodeList clientID = doc.getElementsByTagName("clientID");
            Element ci = (Element) clientID.item(0);
            String cis = getCharacterDataFromElement(ci);
            resp.setClientID(cis);
            LOG.info("Raw Response ::: " + resp.toString());
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException pce) {
            pce.printStackTrace();
        } catch (IOException pce) {
            pce.printStackTrace();
        }
        return resp;
    }

    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static String getAmount(String initialCode) {
        String[] split = initialCode.split("\\*");
        return split[3].replace("#", "");
    }

    //Combined App3D service - both Old and New App3D services on a single method
    public static Boolean verifyAccountWithBankCode(String bankCode, String accountNumber, String mobile, String reference) {
        boolean status = false;
        try {
            List bankList = NewApp3DDetails.BANK_CODE;
            if (bankList.contains(bankCode)) {
                status = NewApp3DDetails.validateAccount(bankCode, accountNumber, mobile, reference);
            } else {
                status = verifyAccountWithBankCodeX(bankCode, accountNumber, mobile);
            }
        } catch (Exception ex) {
            LOG.info(ex);
        }

        return status;
    }

    public static App3DResponse verifyZenithUser(String accountNumber, String callerReference, String mobileNumber) {
        App3DResponse resp = new App3DResponse();
        try {
            StringBuilder strB = new StringBuilder();
            strB.append(callerReference).append(":").append(mobileNumber).append(":").append("1").append(":").append("").append(":").append("");

            String url = FlowProcessor.app3DURL + "clientid=ZenithAirtel444&providerid=ZenithAirtel444&xmlinfo=<Request><clientID>ZenithAirtel444</clientID>"
                    + "<providerID>ZenithAirtel444</providerID><providerName>ZenithAirtel444</providerName><accountNumber>" + accountNumber.trim() + "</accountNumber><otherInfo>" + strB.toString().trim() + "</otherInfo></Request>";
            System.out.println("Verify USSD ZENITH Url :: " + url);
            try {
                String sendGet = sendGet(url);
                resp = convert2Response(sendGet);
            } catch (Exception ex) {
                LOG.info(ex);
            }

        } catch (Exception ex) {
            LOG.info(ex);
        }
        return resp;
    }

    public static App3DResponse registerUssdSubscriberZenith(String accountNumber, String callerReference, String last4CardDigit, String mobileNumber, String ussdPIN) {
        App3DResponse resp = new App3DResponse();
        try {
            //<otherInfo>ZCDT537x:08063263523:2:7697:1234</otherInfo>
            StringBuilder strB = new StringBuilder();
            strB.append(callerReference).append(":").append(mobileNumber).append(":").append("2").append(":").append(last4CardDigit).append(":").append(ussdPIN);

            String url = FlowProcessor.app3DURL + "clientid=ZenithAirtel444&providerid=ZenithAirtel444&xmlinfo=<Request><clientID>ZenithAirtel444</clientID>"
                    + "<providerID>ZenithAirtel444</providerID><providerName>ZenithAirtel444</providerName><accountNumber>" + accountNumber.trim() + "</accountNumber><otherInfo>" + strB.toString().trim() + "</otherInfo></Request>";
            System.out.println("Register USSD ZENITH Url :: " + url);

            try {
                String sendGet = sendGet(url);
                resp = convert2Response(sendGet);
            } catch (Exception exception) {
            }

        } catch (Exception ex) {
            System.out.println("Error occured checking zenith bank ussd subscriber exist :" + ex.getMessage());
            ex.printStackTrace();
        }
        return resp;
    }

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.toBase64String(encrypted));

            return Base64.toBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Boolean verifyAccountWithBankCodeX(String bankCode, String accountNumber, String phone) {
        phone = phone.replaceAll(" ", "");
        if (phone.startsWith("0")) {
            phone = "234" + phone.substring(1);
        }
        LOG.info("Inside verifyAccountWithBankCodeX..... ");
        LOG.info("Bank Code ::: " + bankCode);
        LOG.info("Account Number " + accountNumber);
        LOG.info("Phone " + phone);
        Boolean retVal = false;
        App3D detail = App3DDetails.getClientDetails(bankCode);
        LOG.info("The default value for verification is " + retVal);
        App3DResponse resp;
        if (bankCode.equals("063")) {
            resp = getAccountDetailsDiamond(detail.getClientId(), detail.getProviderId(), accountNumber, phone);
        } else {
            resp = getAccountDetails(detail.getClientId(), detail.getProviderId(), accountNumber, phone);
        }
        if (resp != null) {
            LOG.info("Response is not null and details are as follows");
            LOG.info("Response Code " + resp.getResponseCode());
            LOG.info("Account Number : " + resp.getAccountNumber());
            LOG.info("Other Info : " + resp.getOtherInfo());
            LOG.info("Client Id " + resp.getClientID());
            switch (resp.getResponseCode()) {
                case "0":
                    retVal = true;
                    break;
                case "1":
                    retVal = false;
                    break;
                case "-1":
                    retVal = false;
                    break;
                default:
                    retVal = false;
                    break;
            }
        }
        LOG.info("Returning resp ::::::::::::::::::::::::::::: " + resp);
        LOG.info("Returning :: " + retVal);
        return retVal;
    }

    public static void main(String[] args) {
        //http://www.etranzact.net/3DApp/invoke.jsp?clientid=fcmbclient0001&providerid=FCMBClient&xmlinfo=<Request><clientID>fcmbclient0001</clientID><providerID>FCMBClient</providerID><providerName>FCMBClient</providerName><accountNumber>0438752011</accountNumber><otherInfo>2349096110413</otherInfo></Request>
        Boolean verifyAccountWithBankCode = verifyAccountWithBankCode("069", "0053072018", "2348034000029", "02J80803808080");
        LOG.info(verifyAccountWithBankCode);
    }

    public int process(String provider,
            String reference,
            String bankCode,
            String mobile,
            String account_no,
            String amount) {
        String pan = "";
        String expiration = "777777";
        String pin = "7777";
        int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + account_no;
            } else {
                pan = bankCode + "ZZZ" + account_no;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.PAYMENT);
            String merchantCode = GloService.getInstance().findMerchantCode(bankCode);
            xrequest.setMerchantCode(merchantCode);
            //xrequest.setDescription(serviceId + ":R:" + paymentType + ";" + serviceId);
            xrequest.setReference(reference);
            xrequest.setTransAmount(Double.parseDouble(amount));
            xrequest.setMobileNumber(mobile);

            xResponse = processor.process(httpHost, xrequest);

            if (xResponse != null) {
                response = xResponse.getResponse();
                if (response == 0) {
                    response = 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public int processZenith(String provider,
            String reference,
            String bankCode,
            String mobile,
            String account_no,
            String amount, String ussdPin) {
        String pan = "";
        String expiration = "777777";
        String pin = "7777";
        int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + account_no;
            } else {
                pan = bankCode + "ZZZ" + account_no;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.PAYMENT);
            String merchantCode = GloService.getInstance().findMerchantCode(bankCode);
            xrequest.setMerchantCode(merchantCode);
            //xrequest.setDescription(serviceId + ":R:" + paymentType + ";" + serviceId);
            xrequest.setReference(reference);
            xrequest.setTransAmount(Double.parseDouble(amount));
            xrequest.setMobileNumber(mobile);

            if (bankCode.equals("057")) {
                //debit customer [transactionRef, customBillerID, transactionAmount,  description,  passCode,  mobileNumber]
                String xml = "<passCode>" + encrypt(key, initVector, ussdPin) + "</passCode><mobileNumber>" + mobile + "</mobileNumber><customBillerID>" + customBillerID + "</customBillerID><merchantCode>" + merchantCode + "</merchantCode><callerID>" + callerID + "</callerID><clientName>" + clientName + "</clientName><password>" + password + "</password>";
                xrequest.setXmlString(xml);
            }

            xResponse = processor.process(httpHost, xrequest);

            if (xResponse != null) {
                response = xResponse.getResponse();
                if (response == 0) {
                    response = 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static XResponse process(String bankCode, String accountNo, String paymentType, USSDSession session) {
        String pan;
        String expiration = "777777";
        String pin = "7777";
        //int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + accountNo;
            } else {
                pan = bankCode + "ZZZ" + accountNo;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.PAYMENT);

            xrequest.setMerchantCode("0443241306");

            xrequest.setDescription(paymentType + ":R:WINNERS;");
            xrequest.setReference(generateReference(session.getMsisdn()));
            xrequest.setTransAmount(Double.parseDouble(getAmount(session.getQueue().get(0))));
            xrequest.setMobileNumber(session.getMsisdn());
            printObj(xrequest, XRequest.class);
            xResponse = processor.process(httpHost, xrequest);

//         if (xResponse != null) {
//            response = xResponse.getResponse();
//            if (response == 0) {
//               response = 0;
//            }
//         }
            return xResponse;
        } catch (Exception e) {
            return xResponse;
        }
    }

    public static XResponse process(String bankCode, String accountNo, USSDSession session, String amount, String description) {

        String merchantCode = GloService.getInstance().findMerchantCode(bankCode);
        String pan;
        String expiration = "777777";
        String pin = "7777";
        //int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + accountNo;
            } else {
                pan = bankCode + "ZZZ" + accountNo;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.PAYMENT);

            xrequest.setMerchantCode(merchantCode);
            xrequest.setDescription(description);
            //xrequest.setDescription("ETISALAT:R:DATA;");
            xrequest.setReference(generateReference(session.getMsisdn()));
            xrequest.setTransAmount(Double.parseDouble(amount));
            xrequest.setMobileNumber(session.getMsisdn());
            LOG.info("Just before posting to AutoSwitch!!!");
            LOG.info("Reference :: " + xrequest.getReference());
            LOG.info("Transaction Amount :: " + xrequest.getTransAmount());
            LOG.info("Mobile Number :: " + xrequest.getMobileNumber());
            LOG.info("Merchant Code :: " + xrequest.getMerchantCode());
            LOG.info("Description :: " + xrequest.getDescription());
            xResponse = processor.process(httpHost, xrequest);
            LOG.info("Auto Switch Response Code :::: " + xResponse.getResponse());
            return xResponse;
        } catch (Exception e) {
            return xResponse;
        }
    }

    public static XResponse process(String bankCode, String accountNo, USSDSession session, String amount, String description, String ussdPin) {

        String merchantCode = GloService.getInstance().findMerchantCode(bankCode);
        String pan;
        String expiration = "777777";
        String pin = "7777";
        //int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + accountNo;
            } else {
                pan = bankCode + "ZZZ" + accountNo;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.PAYMENT);

            xrequest.setMerchantCode(merchantCode);
            xrequest.setDescription(description);
            xrequest.setReference(generateReference(session.getMsisdn()));
            xrequest.setTransAmount(Double.parseDouble(amount));
            xrequest.setMobileNumber(session.getMsisdn());
            if (bankCode.equals("057")) {
                //debit customer [transactionRef, customBillerID, transactionAmount,  description,  passCode,  mobileNumber]
                String xml = "<passCode>" + encrypt(key, initVector, ussdPin) + "</passCode><mobileNumber>" + session.getMsisdn() + "</mobileNumber><customBillerID>" + customBillerID + "</customBillerID><merchantCode>" + merchantCode + "</merchantCode><callerID>" + callerID + "</callerID><clientName>" + clientName + "</clientName><password>" + password + "</password>";
                xrequest.setXmlString(xml);
            }
            LOG.info(bankCode + "_Request");
            LOG.info("Before posting to AutoSwitch!!!");
            LOG.info("Reference :: " + xrequest.getReference());
            LOG.info("Transaction Amount :: " + xrequest.getTransAmount());
            LOG.info("Mobile Number :: " + xrequest.getMobileNumber());
            LOG.info("Merchant Code :: " + xrequest.getMerchantCode());
            LOG.info("Description :: " + xrequest.getDescription());
            xResponse = processor.process(httpHost, xrequest);
            return xResponse;
        } catch (Exception e) {
            return xResponse;
        }
    }

    public static XResponse reverseTransaction(String bankCode, String accountNo, USSDSession session, String amount, String reference, String description) {

        String merchantCode = GloService.getInstance().findMerchantCode(bankCode);
        String pan;
        String expiration = "777777";
        String pin = "7777";
        //int response = -1;

        HttpHost httpHost = getHttpHost(SWITCHIP, PORT);

        XProcessor processor = new XProcessor();
        XResponse xResponse = null;
        try {
            if (bankCode.equals("063") || bankCode.equals("039")) {
                pan = bankCode + "KKK" + accountNo;
            } else {
                pan = bankCode + "ZZZ" + accountNo;
            }

            Card card = new Card();
            card.setCardNumber(pan);
            card.setCardExpiration(expiration);
            card.setAccountType("CA");
            card.setCardPin(pin);

            XRequest xrequest = new XRequest();
            xrequest.setCard(card);
            xrequest.setTransCode(TransCode.REVERSAL);

            xrequest.setMerchantCode(merchantCode);

            xrequest.setDescription(description);
            xrequest.setReference(reference);
            xrequest.setTransAmount(Double.parseDouble(amount));
            xrequest.setMobileNumber(session.getMsisdn());
            printObj(xrequest, XRequest.class);
            xResponse = processor.process(httpHost, xrequest);

//         if (xResponse != null) {
//            response = xResponse.getResponse();
//            if (response == 0) {
//               response = 0;
//            }
//         }
            return xResponse;
        } catch (Exception e) {
            return xResponse;
        }
    }

    public static HttpHost getHttpHost(String autoSwitchUrl, String port) {
        HttpHost httpHost = null;
        try {
            httpHost = new HttpHost();
            httpHost.setServerAddress(autoSwitchUrl);
            httpHost.setPort(Integer.parseInt(port));
            String key = "123456";
            httpHost.setSecureKey(key);
        } catch (Exception ex) {
            LOG.info("could not connected autoswitch " + ex.getMessage());
            ex.printStackTrace();
        }
        return httpHost;
    }

//    public static String getPaymentType(String number) {
//        String retVal = "OTHERS";
//        if (number.equals("1")) {
//            retVal = "OFFERING";
//        } else if (number.equals("2")) {
//            retVal = "TITHE";
//        } else if (number.equals("3")) {
//            retVal = "THANKSGIVING";
//        } else if (number.equals("4")) {
//            retVal = "PROPHETIC_GIVING";
//        } else if (number.equals("5")) {
//            retVal = "SHILOH_SACRIFICE";
//        } else if (number.equals("6")) {
//            retVal = "OTHERS";
//        }
//
//        return retVal;
//    }
    public static synchronized String generateReference(String mobile) {
        String[] alphabets = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        String uniqueId = "02JT" + alphabets[new Random(System.currentTimeMillis()).nextInt(26)] + alphabets[new Random(System.nanoTime()).nextInt(26)] + mobile.substring(5, 6);

        int pos = new Random(System.currentTimeMillis()).nextInt(mobile.length());
        uniqueId += mobile.substring(pos, pos + 1);
        java.text.DecimalFormat sequ = new java.text.DecimalFormat("000");
        long nxtSeq = sequence++;
        if (nxtSeq > 998) {
            sequence = 0;
        }
        String inCnt = sequ.format(nxtSeq);
        uniqueId += inCnt;
        String hash = md5(mobile + new SimpleDateFormat("yyyyMMddHHmmssS").format(new Date()));
        pos = new Random(System.nanoTime()).nextInt(32);
        uniqueId += hash.substring(pos, pos + 1).toUpperCase();
        pos = new Random(System.currentTimeMillis()).nextInt(32);
        uniqueId += hash.substring(pos, pos + 1).toUpperCase();
        String randomNo = new Random(System.nanoTime()).nextInt(10000) + "";
        while (randomNo.length() < 4) {
            randomNo = "0" + randomNo;
        }
        uniqueId += randomNo;
        return uniqueId;
    }

    public static String md5(String value) {
        String macValue = "";

        try {
            MessageDigest mdEnc = MessageDigest.getInstance("MD5"); // Encryption
            // algorithm
            mdEnc.update(value.getBytes(), 0, value.length());
            macValue = new BigInteger(1, mdEnc.digest()).toString(16);
            int len = 32 - macValue.length();
            for (int i = 0; i < len; i++) {
                macValue = "0" + macValue;
            }
        } catch (Exception e) {
            LOG.info("Error generating Check Value :: "
                    + e.getMessage());
            macValue = "";
        }

        return macValue;
    }

    public static void logtransaction(XResponse xr, USSDSession session, String bankCode, String paymentType, String amount, String appCode, String provider, String actionType, String beneficiaryMobileNo, String direction, String ussdText, String sessionid, double rate, String actionName, String vasResponse) {
        if (!beneficiaryMobileNo.startsWith("234")) {
            beneficiaryMobileNo = "234" + beneficiaryMobileNo.substring(1);
        }
        beneficiaryMobileNo = beneficiaryMobileNo.replaceAll(" ", "");

        UssdMobileTransactionLog log = new UssdMobileTransactionLog();
        log.setUniqueTransid(xr.getReference());
        log.setTransDate(DateTimeUtil.getCurrentDate());
        log.setAmount(new BigDecimal(amount));
        log.setAppid(appCode);
        log.setBankCode(bankCode);
        log.setProvider(provider);
        log.setUserBankCode(bankCode);
        log.setMobileNo(session.getMsisdn());
        log.setResponseMessage(paymentType);
        log.setShortCode(session.getQueue().get(0));
        log.setResponseCode(xr.getResponse() + "");
        log.setActionType(actionType);
        log.setBeneficiaryMobileNo(beneficiaryMobileNo);
        log.setDirection(direction.charAt(0));
        log.setUssdText(ussdText);
        log.setSessionid(sessionid);
        log.setRate(rate);
        log.setActionName(actionName);
        log.setVasResponse(vasResponse);
        LOG.info(log);
        GloService.getInstance().createTxnLog(log);
    }

    public static void logtransaction(String responseCode, String reference, USSDSession session, String bankCode, String paymentType, String amount, String appCode, String provider, String actionType, String beneficiaryMobileNo, String direction, String ussdText, String sessionid, double rate, String actionName, String vasResponse) {
        if (!beneficiaryMobileNo.startsWith("234")) {
            beneficiaryMobileNo = "234" + beneficiaryMobileNo.substring(1);
        }
        beneficiaryMobileNo = beneficiaryMobileNo.replaceAll(" ", "");

        UssdMobileTransactionLog log = new UssdMobileTransactionLog();
        log.setUniqueTransid(reference);
        log.setTransDate(DateTimeUtil.getCurrentDate());
        log.setAmount(new BigDecimal(amount));
        log.setAppid(appCode);
        log.setBankCode(bankCode);
        log.setProvider(provider);
        log.setUserBankCode(bankCode);
        log.setMobileNo(session.getMsisdn());
        log.setResponseMessage(paymentType);
        log.setShortCode(session.getQueue().get(0));
        log.setResponseCode(responseCode);
        log.setActionType(actionType);
        log.setBeneficiaryMobileNo(beneficiaryMobileNo);
        log.setDirection(direction.charAt(0));
        log.setUssdText(ussdText);
        log.setSessionid(sessionid);
        log.setRate(rate);
        log.setActionName(actionName);
        log.setVasResponse(vasResponse);
        LOG.info(log);
        GloService.getInstance().createTxnLog(log);
    }

    public static void printObj(Object obj, Class clazz) {
        try {
            LOG.info(clazz.getSimpleName() + "{");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(obj);
                LOG.info("\t" + name + " : " + value);
                //System.out.printf("Field name: %s, Field value: %s%n", name, value);
            }
            LOG.info("}");
        } catch (SecurityException securityException) {
        } catch (IllegalArgumentException illegalArgumentException) {
        } catch (IllegalAccessException illegalAccessException) {
        }
    }

}
