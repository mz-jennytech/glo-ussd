/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.handler;

import com.etz.http.etc.XResponse;
import com.etz.ussd.dto.Data;
import com.etz.ussd.glo.executor.ConcurrencyManager;
import com.etz.ussd.glo.executor.ConcurrentTask1;
import com.etz.ussd.handler.ShortCodeInterface;
import com.etz.ussd.session.USSDSession;
import com.etz.ussd.session.USSDSessionManager;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import static ussd.etranzact.glo.handler.AirtimeSelfHandler.convertToInteger;
import ussd.etranzact.glo.model.UssdMobileSubscriber;
import ussd.etranzact.glo.service.GloService; 
import ussd.etranzact.glo.utils.App3DDetails;
import ussd.etranzact.glo.utils.Crypto;
import ussd.etranzact.glo.utils.DateTimeUtil;
import ussd.etranzact.glo.utils.FlowProcessor;
import ussd.etranzact.glo.utils.Utility;
import ussd.etranzact.glo.utils.Utils;
import ussd.etranzact.glo.web.GloUSSDWeb;

/**
 *
 * @author damilola.omowaye
 */
public class DataDirectThirdPartyHandler implements ShortCodeInterface {

    private final Logger LOG = Logger.getLogger(DataDirectThirdPartyHandler.class);
    private String actionName = "Data-Direct-Third_Party";
    private static String appCode;

    @Override
    public String process(Data data) {
        String response = "";
        USSDSessionManager instance = USSDSessionManager.getInstance();
        GloService service = GloService.getInstance();
        USSDSession session = instance.getSession(data.getSessionId());
        StringBuilder sb = new StringBuilder();
        String message = data.getMessage();

        if (session == null) {

            // LOG.info(data.getSessionId() + " Data3rPartyHandler message ::::::::::::::::::::::::::::::::::::::::::::::" + message);
            session = new USSDSession(data.getMsisdn(), data.getSessionId());
            session.getQueue().add(data.getMessage());
            USSDSessionManager.getInstance().add(session);

            long ssStart = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") StartedAt ==  " + ssStart);

            String[] msgLength = session.getQueue().get(0).split("\\*");// *805*1*Amount*msisdn#

            int dataLen = msgLength.length;
            //String bank_Code = msgLength[dataLen - 5];
            String data_Code = msgLength[dataLen - 3];
            String amount = msgLength[dataLen - 2];
            String mobile3rdParty = msgLength[dataLen - 1];
            // LOG.info(data.getSessionId() + " bank_Code >>::::::::::::::::::::: " + bank_Code);
            LOG.info(data.getSessionId() + " amount >>::::::::::::::::::::: " + amount);
            LOG.info(data.getSessionId() + " data_Code >>::::::::::::::::::::: " + data_Code);
            LOG.info(data.getSessionId() + " mobile3rdParty >>::::::::::::::::::::: " + mobile3rdParty);

            //========================================================================================================================================================
            /**
             * *
             * Build limit message parameters here Call method getLimitValue to
             * fetch message
             *
             ***
             */
            appCode = Utils.getAPP_ID(session.getQueue().get(0));
            int responseCode2 = 0;
            String errorMessageSingle2 = null,
                    errorMessageCum2 = null;
            boolean useToken2 = false,
                    allowSingle2 = false,
                    allowTotal2 = false;
            double singleValue2 = 0.0,
                    totalValue2 = 0.0;
            Map limitMap2 = Utils.getLimitValue(session.getMsisdn(), session.getQueue().get(0), Double.valueOf(amount), appCode);
            LOG.info("getLimitValue1 size :::::::::::::::::::  " + limitMap2.size());

            if (limitMap2.isEmpty()) {
                sb.append("Transaction status is undefined. Please try again later.Thank you.").append("~");
                response = sb.toString();
            } else {

                try {
                    responseCode2 = (int) limitMap2.get("responseCode");
                    errorMessageSingle2 = (String) limitMap2.get("errorMessageSingle");
                    errorMessageCum2 = (String) limitMap2.get("errorMessageCum");
                    useToken2 = (boolean) limitMap2.get("useToken");
                    allowSingle2 = (boolean) limitMap2.get("allowSingle");
                    allowTotal2 = (boolean) limitMap2.get("allowTotal");
                    singleValue2 = (double) limitMap2.get("singleValue");
                    totalValue2 = (double) limitMap2.get("totalValue");
                } catch (Exception x) {
                    x.printStackTrace();
                }

                if (responseCode2 == 200) {

                    if (useToken2) {
                        /**
                         * *TO DO TOKEN IMPLEMENTATTION HERE Token is required
                         * here
                         */
                        sb.append("Use of Token is not activated for this service").append("~");
                        response = sb.toString();
                    } else {
                       String msg = null;

                        if (!allowTotal2) {
                            msg = errorMessageCum2;
                        }
                        if (!allowSingle2) {
                            msg = errorMessageSingle2;
                        }
                        if (msg != null) {
                            sb.append(msg).append("~");
                            response = sb.toString();
                            instance.destroySession(session.getSessionId());
                        } else {
                            // session.getSessionData().put("bank_Code", bank_Code);
                            session.getSessionData().put("amount", amount);
                            session.getSessionData().put("mobile", mobile3rdParty);

                            List<UssdMobileSubscriber> userAccounts = service.findAllAccount(data.getMsisdn(), appCode);

                            if (data_Code.equals("1")) {

                                //String jsonString = "{\"alias\":\"GLO\",\"reference\":\"02QC78415NM31\",\"clientRef\":\"02QC78415NM31\",\"amount\":0,\"action\":\"query\",\"type\":\"1\",\"error\":\"00\",\"fault\":\"zz{\\\"glo-dataplans\\\":[{\\\"amount\\\":\\\"25\\\",\\\"size\\\":\\\"12.5MB\\\",\\\"validity\\\":\\\"1 Day\\\",\\\"productID\\\":\\\"DATA-32\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N25 for 12.5MB 1 Day\\\"},{\\\"amount\\\":\\\"50\\\",\\\"size\\\":\\\"27.5MB\\\",\\\"validity\\\":\\\"1 Day\\\",\\\"productID\\\":\\\"DATA-18\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N50 for 27.5MB 1 Day\\\"},{\\\"amount\\\":\\\"100\\\",\\\"size\\\":\\\"100MB\\\",\\\"validity\\\":\\\"1 Day\\\",\\\"productID\\\":\\\"DATA-21\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N100 for 100MB 1 Day\\\"},{\\\"amount\\\":\\\"200\\\",\\\"size\\\":\\\"262MB\\\",\\\"validity\\\":\\\"5 Days\\\",\\\"productID\\\":\\\"DATA-28\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N200 for 262MB 5 Days\\\"},{\\\"amount\\\":\\\"500\\\",\\\"size\\\":\\\"1GMB\\\",\\\"validity\\\":\\\"14 Days\\\",\\\"productID\\\":\\\"DATA-27\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N500 for 1GB 14 Days\\\"},{\\\"amount\\\":\\\"1000\\\",\\\"size\\\":\\\"2GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-2\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N1000 for 2GB 30 Days\\\"},{\\\"amount\\\":\\\"2000\\\",\\\"size\\\":\\\"4.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-25\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N2000 for 4.5GB 30 days\\\"},{\\\"amount\\\":\\\"2500\\\",\\\"size\\\":\\\"7.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-19\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N2500 for 7.5GB 30 Days\\\"},{\\\"amount\\\":\\\"3000\\\",\\\"size\\\":\\\"8.75GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-23\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N3000 for 8.75GB 30 Days\\\"},{\\\"amount\\\":\\\"4000\\\",\\\"size\\\":\\\"12.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-12\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N4000 for 12.5GB 30 Days\\\"},{\\\"amount\\\":\\\"5000\\\",\\\"size\\\":\\\"15.6GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-5\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N5000 for 15.6GB 30 Days\\\"},{\\\"amount\\\":\\\"8000\\\",\\\"size\\\":\\\"25GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-4\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N8000 for 25GB 30 Days\\\"},{\\\"amount\\\":\\\"10000\\\",\\\"size\\\":\\\"32.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-10\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N10000 for 32.5GB 30Days\\\"},{\\\"amount\\\":\\\"15000\\\",\\\"size\\\":\\\"52.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-11\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N15000 for 52.5GB 30 Days\\\"},{\\\"amount\\\":\\\"18000\\\",\\\"size\\\":\\\"62.5GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-20\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N18000 for 62.5GB 30 Days\\\"},{\\\"amount\\\":\\\"20000\\\",\\\"size\\\":\\\"78.7GB\\\",\\\"validity\\\":\\\"30 Days\\\",\\\"productID\\\":\\\"DATA-33\\\",\\\"category\\\":\\\" \\\",\\\"description\\\":\\\"N20000 for 78.7GB 30 Days\\\"}]}\",\"subscriber\":\"\",\"client\":\"USSD2\",\"channel\":\"02\",\"ip\":\"172.16.10.38\",\"date\":\"Feb 12, 2018 8:52:53 AM\"}";
                                String jsonString = null;
                                try {
                                    jsonString = Utility.verifyGLODataPlan("GLO", "1", "USSD2", Utility.genKEY(11));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    sb.append("Data bundle plans are not available at the moment.~ Please try it again. Thank you.");
                                    instance.destroySession(session.getSessionId());
                                }

                                LOG.info(data.getSessionId() + " Data bundle plan dataPlans :::::::::::::::::" + jsonString);
                                if (jsonString == null) {
                                    sb.append("Data bundle plans are not available at the moment.~ Please try it again. Thank you.");
                                    instance.destroySession(session.getSessionId());
                                } else {
                                    // LOG.info(data.getSessionId() + " Data plan jsonString <<<::::::::::::::::::::::::::::::::::::::::::::::>>> " + jsonString);
                                    JSONObject json = new JSONObject(jsonString);
                                    String responseCode = json.getString("error");

                                    if (responseCode.equals("00")) {
                                        String strArr = json.getString("fault");
                                        if (strArr.startsWith("zz")) {
                                            strArr = strArr.replace("zz", "");
                                        }

                                        //LOG.info(data.getSessionId() + " Data plan jArray <<<::::::::::::::::::::::::::::::::::::::::::::::>>> " + strArr);
                                        JSONObject jsonn = new JSONObject(strArr);
                                        JSONArray jArray = jsonn.optJSONArray("glo-dataplans");
                                        session.getSessionData().put("dataPlanArray", jArray);

                                        // LOG.info(data.getSessionId() + " Current Message &&&& <<<::::::::::::::::::::::::::::::::::::::::::::::>>> " + message);
                                    }

                                    JSONArray jArray = (JSONArray) session.getSessionData().get("dataPlanArray");
                                    for (int x = 0; x < jArray.length(); x++) {
                                        JSONObject jsn = jArray.getJSONObject(x);
                                        if (Double.parseDouble((String) jsn.get("amount")) == Double.parseDouble(amount)) {
                                            session.getSessionData().put("chosenPlanInJSONObject", jsn);
                                        }
                                    }

                                    JSONObject jsn = (JSONObject) session.getSessionData().get("chosenPlanInJSONObject");
                                    if (jsn == null) {
                                        sb.append("Sorry, no data plan package found for amount specified: N").append(Double.parseDouble(amount)).append(". Please enter any appropriate amount specific to GLO data plans. Thank you.");
                                        instance.destroySession(session.getSessionId());
                                    } else {
                                        //.append("You are purchasing GLO Data Bundle ").append(jsn.get("size")).append(" for ").append(mobile3rdParty).append(".Total cost N").append(jsn.get("amount")).append("~~");
                                        if (userAccounts == null || userAccounts.isEmpty()) {
                                            if (session.getSessionData().get("bankCounter") == null) {
                                                String banks = App3DDetails.getBankList();
                                                String bankListing[] = banks.split("~");

                                                sb.append("Choose bank").append("~");
                                                StringBuilder sb1 = new StringBuilder();
                                                for (int t = 0; t < bankListing.length; t++) {
                                                    if (t < 7) {
                                                        sb.append(bankListing[t]).append("~ ");
                                                        sb1.append(bankListing[t]).append("~ ");
                                                    }
                                                }
                                                session.getSessionData().put("BANK_LIST1", sb1.toString());
                                                sb.append("~");
                                                sb.append("8. ").append("Next");
                                                session.getSessionData().put("bankCounter", "1");
                                                session.getSessionData().put("process", "DATA_3TH_DIRECT-NUBAN");
                                                session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                            }
                                        } else {
                                            session.getSessionData().put("userAccounts", userAccounts);
                                            if (userAccounts.size() == 1) {
                                                sb.append("You are purchasing GLO Data Bundle ").append(jsn.get("size")).append(" for ").append(mobile3rdParty).append(".Total cost N").append(jsn.get("amount")).append("~~");
                                                sb.append("Enter your PIN").append("~");
                                                session.getSessionData().put("process", "DATA_3TH_DIRECT-pin");
                                                session.getSessionData().put("ASDB-myPinIndex", session.getQueue().size());
                                            } else if (userAccounts.size() > 1) {
                                                sb.append(" Choose bank").append("~");
                                                //int counter = 1;
                                                for (int v = 0; v < userAccounts.size(); v++) {
                                                    sb.append(v + 1).append(". ").append(App3DDetails.getBank(userAccounts.get(v).getBankCode())).append(" (").append(userAccounts.get(v).getAccountNo().replace(userAccounts.get(v).getAccountNo().substring(0, 6), "******")).append(")~");
                                                    //counter++;
                                                }
//                                                sb.append("~").append(counter).append(".Opt-out of Service~");
//                                                session.getSessionData().put("maxAcctSize", counter);
                                                session.getSessionData().put("process", "DATA_3TH_DIRECT-NUBAN");
                                                session.getSessionData().put("chosenBankIndex", session.getQueue().size());
                                            }

                                        }
                                    }
                                }
                            } else {
                                sb.append("You have dailed an Invalid code! Check and dial again. Thank you").append("~");
                                instance.destroySession(session.getSessionId());
                            }

                            //}
                            response = sb.toString();
                        }
                    }
                } else {
                    response = "Transaction cannot be completed at this time.Try again later~}Thank you"; //
                }

            }
            FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", mobile3rdParty, "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, "MainMenu", null);

            long ssEnds = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") EndedAt ==  " + ssEnds);
            LOG.info("Total TimeTaken for session (" + data.getSessionId() + ") ==  " + (ssEnds - ssStart));
        } else {

            long ssStart = System.currentTimeMillis();
            LOG.info(" GLO805 Existing Session (" + data.getSessionId() + ") StartedAt ==  " + ssStart);

            session.getQueue().add(data.getMessage());
            appCode = Utils.getAPP_ID(session.getQueue().get(0));

            String mobile3rdParty = (String) session.getSessionData().get("mobile");
            List<UssdMobileSubscriber> userAccounts = (List<UssdMobileSubscriber>) session.getSessionData().get("userAccounts");

            // String bank_Code = (String) session.getSessionData().get("bank_Code");
            if (session.getSessionData().get("process") != null && session.getSessionData().get("process1") == null) {
                String process = (String) session.getSessionData().get("process");
                LOG.info(data.getSessionId() + " process :::::::::::::::::::" + process);

                //  if (session.getSessionData().get("chosenPlanInJSONObject") != null) {
                JSONObject jsn = (JSONObject) session.getSessionData().get("chosenPlanInJSONObject");

                if (userAccounts == null || userAccounts.isEmpty()) {

                    if (message.equals("0") || message.equals("8")) {
                        int dataPlanMessage = Integer.parseInt(message);
                        LOG.info(data.getSessionId() + " dataPlan-Message ::::::::::::::::::::::::::::::::::::::::::::::" + dataPlanMessage);
                        switch (dataPlanMessage) {

                            case 0:
                                if (session.getSessionData().get("bankCounter").equals("2")) {
                                    session.getSessionData().remove("BANK_LIST1");
                                    String banks = App3DDetails.getBankList();
                                    String bankListing[] = banks.split("~");

                                    sb.append("Choose bank").append("~");
                                    StringBuilder sb1 = new StringBuilder();
                                    for (int t = 0; t < bankListing.length; t++) {
                                        if (t < 7) {
                                            sb.append(bankListing[t]).append("~ ");
                                            sb1.append(bankListing[t]).append("~ ");
                                        }
                                    }
                                    session.getSessionData().put("BANK_LIST1", sb1.toString());
                                    sb.append("~");
                                    sb.append("8. ").append("Next");
                                    session.getSessionData().put("bankCounter", "1");
                                    //session.getSessionData().put("process", "DATA_3TH_DIRECT-BankSelect");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                } else if (session.getSessionData().get("bankCounter").equals("3")) {
                                    session.getSessionData().remove("BANK_LIST1");
                                    String lastMessage = (String) session.getQueue().get(session.getQueue().size() - 1);
                                    LOG.info(data.getSessionId() + " 1ai lastMessage on Queue ============================ " + lastMessage);

                                    String banks = App3DDetails.getBankList();
                                    String bankListing[] = banks.split("~");

                                    List<String> aList = new ArrayList();

                                    for (int y = 7; y < bankListing.length; y++) {
                                        if (y < 14) {
                                            aList.add(bankListing[y]);
                                        }
                                    }

                                    LOG.info(data.getSessionId() + "  banks remaining listing2 ::::::::::::::::::::::::::::::::::::::::::::::" + aList);

                                    StringBuilder sb1 = new StringBuilder();
                                    sb.append("Choose bank").append("~");
                                    for (int x = 0; x < aList.size(); x++) {
                                        sb.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                        sb1.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                    }

                                    sb.append("~");
                                    if (bankListing.length > 14) {
                                        sb.append("8. Next").append("~");
                                    }
                                    sb.append("0. Back").append("~");
                                    session.getSessionData().put("BANK_LIST1", sb1.toString());
                                    session.getSessionData().put("bankCounter", "2");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                }
                                break;

                            case 8:
                                if (session.getSessionData().get("bankCounter").equals("1")) {

                                    session.getSessionData().remove("BANK_LIST1");
                                    String lastMessage = (String) session.getQueue().get(session.getQueue().size() - 1);
                                    LOG.info(data.getSessionId() + " 1ai lastMessage on Queue ============================ " + lastMessage);

                                    String banks = App3DDetails.getBankList();
                                    String bankListing[] = banks.split("~");

                                    List<String> aList = new ArrayList();

                                    for (int y = 7; y < bankListing.length; y++) {
                                        if (y < 14) {
                                            aList.add(bankListing[y]);
                                        }
                                    }

                                    LOG.info(data.getSessionId() + "  banks remaining listing2 ::::::::::::::::::::::::::::::::::::::::::::::" + aList);

                                    StringBuilder sb1 = new StringBuilder();
                                    sb.append("Choose bank").append("~");
                                    for (int x = 0; x < aList.size(); x++) {
                                        sb.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                        sb1.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                    }

                                    sb.append("~");
                                    if (bankListing.length > 14) {
                                        sb.append("8. Next").append("~");
                                    }
                                    sb.append("0. Back").append("~");
                                    session.getSessionData().put("BANK_LIST1", sb1.toString());
                                    session.getSessionData().put("bankCounter", "2");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                } else if (session.getSessionData().get("bankCounter").equals("2")) {

                                    session.getSessionData().remove("BANK_LIST1");
                                    String lastMessage = (String) session.getQueue().get(session.getQueue().size() - 1);
                                    LOG.info(data.getSessionId() + " 1ai lastMessage on Queue ============================ " + lastMessage);

                                    String banks = App3DDetails.getBankList();
                                    String bankListing[] = banks.split("~");

                                    List<String> aList = new ArrayList();

                                    for (int y = 14; y < bankListing.length; y++) {
                                        if (y < 21) {
                                            aList.add(bankListing[y]);
                                        }
                                    }

                                    LOG.info(data.getSessionId() + "  banks remaining listing2 ::::::::::::::::::::::::::::::::::::::::::::::" + aList);

                                    StringBuilder sb1 = new StringBuilder();
                                    sb.append("Choose bank").append("~");
                                    for (int x = 0; x < aList.size(); x++) {
                                        sb.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                        sb1.append(x + 1).append(". ").append(aList.get(x).split("\\.")[1]).append("~ ");
                                    }
                                    sb.append("0. Back").append("~");
                                    session.getSessionData().put("BANK_LIST1", sb1.toString());
                                    session.getSessionData().put("bankCounter", "3");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                }
                                break;

                            default:
                                break;
                        }
                    } else if (!"8".equals(message) && session.getSessionData().get("BANK_LIST1") != null) {
                        String bankPos = (String) session.getQueue().get((Integer) session.getSessionData().get("ASDB-newUserChosenBankIndex"));

                        LOG.info(data.getSessionId() + "  DATA_3TH_DIRECT-BankSlect-newUser-size bankPos :::::::::::::::::::" + bankPos);

                        String list1 = (String) session.getSessionData().get("BANK_LIST1");
                        String listing1[] = list1.split("~");
                        LOG.info(data.getSessionId() + " 1st banks listing1 ::::::::::::::::::::::::::::::::::::::::::::::" + Arrays.toString(listing1));

                        int enterMsg = convertToInteger(bankPos);
                        String bankName = listing1[enterMsg - 1];
                        LOG.info(data.getSessionId() + " DATA_3TH_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        bankName = bankName.split("\\.")[1].trim();
                        LOG.info(data.getSessionId() + " DATA_3TH_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        session.getSessionData().put("DATA_3TH_DIRECT-BankSlect-newUser-bankCode", App3DDetails.getBankCode(bankName));

                        sb.append("Enter your new ").append(bankName).append(" 10-digit Account Number").append("~");

                        session.getSessionData().put("process1", "DATA_3TH_DIRECT-NoAccount-accountNumber");
                        session.getSessionData().put("DATA_3TH_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex", session.getQueue().size());
                    }

                } else {

//                    int maxAcctSize1 = (int) session.getSessionData().get("maxAcctSize");
//                    int chosenAcctIndex1 = Integer.parseInt(message);
//                    if (maxAcctSize1 == chosenAcctIndex1) {
//                        //Do Out out here
//                        sb.append("Opt-out of Service!~~~Are you sure you want to Opt-out of GLO 805?~1. Yes~2. No~");
//                        session.getSessionData().put("optoutIndex", message);
//                    } else {
                    String amount = String.valueOf(jsn.get("amount"));
                    String productID = String.valueOf(jsn.get("productID"));
                    if (userAccounts.size() == 1) {
                        UssdMobileSubscriber subscriber = userAccounts.get(0);
                        String pina = subscriber.getPin();
                        LOG.info(data.getSessionId() + " DATA_3TH_DIRECT pina >>>>>============================>> " + pina);

                        String userPin = session.getQueue().get((Integer) session.getSessionData().get("ASDB-myPinIndex"));

                        if (session.getSessionData().get("pinSetup") == null && (pina == "" || pina == null || pina.isEmpty() || pina.equalsIgnoreCase("null"))) {
                            sb.append("You do not have a PIN setup on this account~Kindly create a 4-digit PIN~");
                            session.getSessionData().put("ASDB-myPinIndex", session.getQueue().size());
                            session.getSessionData().put("pinSetup", "ON");
                        } else {
                            if (userPin == null) {
                                try {
                                    userPin = session.getQueue().get((Integer) session.getSessionData().get("ASDB-myPinIndex"));
                                } catch (Exception e) {
                                }
                            }
                            LOG.info(data.getSessionId() + " DATA_3TH_DIRECT userPin >>>>>============================>> " + userPin);

                            if (pina == "" || pina == null || pina.isEmpty() || pina.equalsIgnoreCase("null")) {
                                service.updatePIN(data.getMsisdn(), subscriber.getAccountNo(), userPin, appCode);
                            }

                            if (service.isAccountLocked(data.getMsisdn(), subscriber.getBankCode(), subscriber.getAccountNo(),appCode)) {
                                sb = new StringBuilder();
                                sb.append("Your account has been suspended  due to several failed authentication attempts.~You will be reactivated within an hour.~ Thank you.");
                                instance.destroySession(session.getSessionId());
                            } else {
//                                Boolean verifyAccount = FlowProcessor.verifyAccountWithBankCode(subscriber.getBankCode(), subscriber.getAccountNo(), session.getMsisdn());
//                                if (verifyAccount) {
                                if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), userPin,appCode)) {

                                    String initialDial = "1";//session.getQueue().get(1);
                                    String resp1 = doDirect3rdPartyDATAProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, userPin);
                                    sb.append(resp1);
                                    instance.destroySession(session.getSessionId());

                                } else {
                                    if (session.getSessionData().get("lock") == null) {
                                        session.getSessionData().put("lock", "2");
                                    }

                                    if (session.getSessionData().get("lock").equals("2") && session.getSessionData().get("lockCounter") == null) {
                                        sb.append("Wrong PIN ~You have only 2 attempts left.~Enter PIN").append("~");
                                        session.getSessionData().put("lock", "3");
                                        session.getSessionData().put("lockCounter", "ON");
                                    } else if (session.getSessionData().get("lock").equals("3") && session.getSessionData().get("lockCounter2") == null) {
                                        if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), message,appCode)) {
                                            /**
                                             * Process Transaction here
                                             *
                                             */
                                            String initialDial = session.getQueue().get(0);
                                            String resp = doDirect3rdPartyDATAProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, userPin);
                                            sb.append(resp);
                                            instance.destroySession(session.getSessionId());
                                        } else {
                                            sb.append("Wrong PIN ~You have only 1 attempt left.~Enter PIN").append("~");
                                            session.getSessionData().put("lock", "4");
                                            session.getSessionData().put("lockCounter2", "ON");
                                        }
                                    } else if (session.getSessionData().get("lock").equals("4")) {
                                        if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), message,appCode)) {
                                            /**
                                             * Process Transaction here
                                             *
                                             */
                                            String initialDial = session.getQueue().get(0);
                                            String resp = doDirect3rdPartyDATAProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, userPin);
                                            sb.append(resp);
                                            instance.destroySession(session.getSessionId());
                                        } else {
                                            sb = new StringBuilder();
                                            service.lockSubscriberAccount(data.getMsisdn(), subscriber.getBankCode(), subscriber.getAccountNo(),appCode);
                                            sb.append("For your security, your account has been suspended. ~ Please try again after an hour.~ Thank you ");
                                            instance.destroySession(session.getSessionId());
                                        }
                                    }
                                }

//                                } else {
//                                    sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
//                                    instance.destroySession(session.getSessionId());
//                                }
                            }
                        }

                    } else if (userAccounts.size() > 1) {
                        String chosenBankPosition = session.getQueue().get((Integer) session.getSessionData().get("chosenBankIndex"));

                        UssdMobileSubscriber ums = userAccounts.get(Integer.parseInt(chosenBankPosition) - 1);
                        LOG.info(data.getSessionId() + " DATA_3TH_DIRECT ums ::::::::::::::::::::::::::::::::::::::::::::::" + ums.toString());
                        session.getSessionData().put("subscriber", ums);

                        sb.append("You are purchasing GLO Data Bundle ").append(jsn.get("size")).append(" for ").append(mobile3rdParty).append(".Total cost N").append(jsn.get("amount")).append("~~");
                        sb.append("Enter your PIN").append("~");
                        session.getSessionData().put("process1", "DATA_3TH_DIRECT-pin");
                        session.getSessionData().put("multipleAcctPinIndex", session.getQueue().size());

                    }

                    //}
                }
                response = sb.toString();
                String mobile = (String) session.getSessionData().get("mobile");
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", mobile, "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);
            } else if (session.getSessionData().get("process1") != null && session.getSessionData().get("process2") == null) {
                String process = (String) session.getSessionData().get("process1");
                LOG.info(data.getSessionId() + " process1 :::::::::::::::::::" + process);

                JSONObject jsn1 = (JSONObject) session.getSessionData().get("chosenPlanInJSONObject");
                LOG.info(data.getSessionId() + " jsn1 :::::::::::::::::::" + jsn1);
                String amount = String.valueOf(jsn1.get("amount"));
                String productID = String.valueOf(jsn1.get("productID"));

                if (userAccounts == null || userAccounts.isEmpty()) {
                    String bankCode = (String) session.getSessionData().get("DATA_3TH_DIRECT-BankSlect-newUser-bankCode");
                    String accountNo = (String) session.getQueue().get((Integer) session.getSessionData().get("DATA_3TH_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex"));

                    LOG.info(data.getSessionId() + " bankCode >> :::::::::::::::::::" + bankCode);
                    LOG.info(data.getSessionId() + " accountNo :::::::::::::::::::" + accountNo);

                    if (session.getSessionData().get("pinn1") == null) {
                        sb.append("Kindly create a PIN").append("~");
                        session.getSessionData().put("pinn1", "NO");
                        session.getSessionData().put("pinIndex1", session.getQueue().size());
                    } else if (session.getSessionData().get("pinn2") == null) {
                        sb.append("Kindly confirm your PIN").append("~");
                        session.getSessionData().put("pinn2", "NO");
                        session.getSessionData().put("pinIndex2", session.getQueue().size());
                    } else {
                        String pinn1 = session.getQueue().get((Integer) session.getSessionData().get("pinIndex1"));
                        String pinn2 = session.getQueue().get((Integer) session.getSessionData().get("pinIndex2"));

                        if (pinn1.equals(pinn2)) {
                            Boolean verifyAccountA = FlowProcessor.verifyAccountWithBankCode(bankCode, accountNo, session.getMsisdn(), FlowProcessor.generateReference(session.getMsisdn()));
                            LOG.info(data.getSessionId() + " verifyAccountA :::::::::::::::::::" + verifyAccountA);
                            if (verifyAccountA) {
                                UssdMobileSubscriber subscriberA = new UssdMobileSubscriber();
                                subscriberA.setAccountNo(accountNo);
                                subscriberA.setBankCode(bankCode);
                                subscriberA.setActive(true);
                                subscriberA.setCreated(DateTimeUtil.getCurrentDate());
                                subscriberA.setMobileNo(session.getMsisdn());
                                //subscriberA.setAppcode(appCode);
                                subscriberA.setModified(DateTimeUtil.getCurrentDate());
                                subscriberA.setPin(pinn1);
                                service.createSubscriber(subscriberA,appCode);

                                String initialDial = "1";//session.getQueue().get(1);
                                String resp1 = doDirect3rdPartyDATAProcessing(session, bankCode, accountNo, mobile3rdParty, amount, initialDial, productID, 0.0, pinn1);
                                sb.append(resp1);
                                instance.destroySession(session.getSessionId());
                            } else {
                                sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
                                instance.destroySession(session.getSessionId());
                            }
                        } else {
                            sb.append("PINs do not match!~Kindly re-enter the PIN").append("~");
                            session.getSessionData().remove("pinn2");
                        }

                    }

                } else if (userAccounts.size() > 1) {

//                    String optout1 = (String) session.getSessionData().get("optoutIndex");
//                    LOG.info("1 outOfOfService optout1 A ::::::::::::::::::::::::::::::::::::  " + optout1);
//                    if (optout1.equals("1") || optout1.equals("2")) {
//                        LOG.info("1 outOfOfService optout1 B ::::::::::::::::::::::::::::::::::::  " + optout1);
//                        if (optout1.equals("1")) {
//                            Map optOut = Utils.optOutOfService(session.getMsisdn(), true);
//                            LOG.info("1 outOfOfService optOut ::::::::::::::::::::::::::::::::::::  " + optOut);
//                            if ((int) optOut.get("responseCode") == 200) {
//                                sb.append("Opt-out of Service!~~").append((String) optOut.get("message")).append("~Thank you.");
//                            } else {
//                                sb.append("Transaction cannot be completed at this time.Please try again later.~Thank you.");
//                            }
//                        } else if (optout1.equals("2")) {
//                            sb.append("Opt-out of Service!~~~Congratulations! You remain an active valid subscriber on GLO 805~Thank you.");
//                        } else {
//                            sb.append("Opt-out of Service!~~~Opt-out operation failed! Please try again later~Thank you.");
//                        }
//                        instance.destroySession(session.getSessionId());
//                    } else {
                    int pinIndex = (Integer) session.getSessionData().get("multipleAcctPinIndex");
                    // LOG.info(data.getSessionId() + " DATA_3TH_DIRECT-PIN pinIndex ::::::::::::::::::::: " + pinIndex);
                    String userPin = session.getQueue().get(pinIndex);
                    // LOG.info(data.getSessionId() + " DATA_3TH_DIRECT-PIN pin :::::::::::::::::::: " + pinn);
                    UssdMobileSubscriber ums = (UssdMobileSubscriber) session.getSessionData().get("subscriber");

                    String pina = ums.getPin();

                    if (session.getSessionData().get("pinSetup") == null && (pina == "" || pina == null || pina.isEmpty() || pina.equalsIgnoreCase("null"))) {
                        sb.append("You do not have a PIN setup on this account~Kindly create a 4-digit PIN~");
                        session.getSessionData().put("ASDB-pinIndex1", session.getQueue().size());
                        session.getSessionData().put("pinSetup", "ON");
                    } else {
                        if (userPin == null) {
                            try {
                                userPin = session.getQueue().get((Integer) session.getSessionData().get("ASDB-pinIndex1"));
                            } catch (Exception e) {
                            }
                        }

                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT userPin ::::::::::::::::::::::::::::::::: " + userPin);

                        if (pina == "" || pina == null || pina.isEmpty() || pina.equalsIgnoreCase("null")) {
                            service.updatePIN(data.getMsisdn(), ums.getAccountNo(), userPin, appCode);
                        }
                        if (service.isAccountLocked(data.getMsisdn(), userAccounts.get(0).getBankCode(), userAccounts.get(0).getAccountNo(), appCode)) {
                            sb = new StringBuilder();
                            service.lockSubscriberAccount(data.getMsisdn(), ums.getBankCode(), ums.getAccountNo(), appCode);
                            sb.append("Your account has been suspended  due to several failed authentication attempts.~You will be reactivated within an hour.~ Thank you.");
                            instance.destroySession(session.getSessionId());

                        } else {
//                            Boolean verifyAccount = FlowProcessor.verifyAccountWithBankCode(ums.getBankCode(), ums.getAccountNo(), session.getMsisdn());
//                            if (verifyAccount) {
                            if (service.verifyUserPIN(data.getMsisdn(), ums.getAccountNo(), userPin, appCode)) {
                                /**
                                 * Process Transaction here *
                                 */
                                String initialDial = "1";//session.getQueue().get(1);
                                String resp1 = doDirect3rdPartyDATAProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, "");
                                sb.append(resp1);
                                instance.destroySession(session.getSessionId());
                            } else {

                                if (session.getSessionData().get("lock") == null) {
                                    session.getSessionData().put("lock", "2");
                                }

                                if (session.getSessionData().get("lock").equals("2") && session.getSessionData().get("lockCounter") == null) {
                                    sb.append("Wrong PIN ~You have only 2 attempts left.~Enter PIN").append("~");
                                    session.getSessionData().put("lock", "3");
                                    session.getSessionData().put("lockCounter", "ON");

                                } else if (session.getSessionData().get("lock").equals("3") && session.getSessionData().get("lockCounter2") == null) {
                                    if (service.verifyUserPIN(data.getMsisdn(), ums.getAccountNo(), message, appCode)) {
                                        /**
                                         * **
                                         * Process Transaction here *
                                         */
                                        // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                                        // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                                        String initialDial = session.getQueue().get(0);
                                        // sb.append("Your Transaction was successful. ~ You can add more Account(s) using the Manage Profile Menu. ~ Thank you.");
                                        String resp = doDirect3rdPartyDATAProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, "");
                                        sb.append(resp);
                                        instance.destroySession(session.getSessionId());
                                    } else {
                                        sb.append("Wrong PIN ~You have only 1 attempt left.~Enter PIN").append("~");
                                        session.getSessionData().put("lock", "4");
                                        session.getSessionData().put("lockCounter2", "ON");
                                    }
                                } else if (session.getSessionData().get("lock").equals("4")) {
                                    if (service.verifyUserPIN(data.getMsisdn(), ums.getAccountNo(), message, appCode)) {
                                        /**
                                         * **
                                         * Process Transaction here *
                                         */
                                        // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                                        // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                                        String initialDial = session.getQueue().get(0);
                                        // sb.append("Your Transaction was successful. ~ You can add more Account(s) using the Manage Profile Menu. ~ Thank you.");
                                        String resp = doDirect3rdPartyDATAProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, productID, 0.0, "");
                                        sb.append(resp);
                                        instance.destroySession(session.getSessionId());
                                    } else {
                                        sb = new StringBuilder();
                                        service.lockSubscriberAccount(data.getMsisdn(), ums.getBankCode(), ums.getAccountNo(), appCode);
                                        sb.append("For your security, your account has been suspended. ~ Please try again after an hour.~ Thank you ");
                                        instance.destroySession(session.getSessionId());
                                    }
                                }
                            }
//                            } else {
//                                sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
//                                instance.destroySession(session.getSessionId());
//                            }
                        }
                    }
                    // }
                }

                response = sb.toString();
                // String mobile = (String) session.getSessionData().get("mobile");
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", mobile3rdParty, "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);
            }
            long ssEnds = System.currentTimeMillis();
            LOG.info(" GLO805 Existing Session (" + data.getSessionId() + ") EndedAt ==  " + ssEnds);
            LOG.info("Total TimeTaken for session (" + data.getSessionId() + ") ==  " + (ssEnds - ssStart));

        }

        return response;
    }

    public static String generateMac(String alias, String reference, String mobile, String type) {
        String mac = alias + reference + mobile + type;
        System.out.println(mac);
        return Crypto.encodeMD5NoSalt(mac);
    }

    /**
     * ***************************************************************************************************************
     * PROCESS TRNSACTION VIA AUTO SWITCH AND GIVE VALUE VIA VASGATE PROCESS
     * //****************************************************************************************************************
     */
    private String doDirect3rdPartyDATAProcessing(USSDSession session, String bankCode, String accountNo, String phoneNumber, String amount, String initialDial, String productID, double rate, String ussdPin) {
        phoneNumber = phoneNumber.replaceAll(" ", "");
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "234" + phoneNumber.substring(1);
        }

        System.out.println("doDirect3rdPartyDATAProcessing Details of doProcessing....");
        System.out.println("doDirect3rdPartyDATAProcessing MSISDN : " + session.getMsisdn());
        System.out.println("doDirect3rdPartyDATAProcessing 3rd Party MSISDN : " + phoneNumber);
        System.out.println("doDirect3rdPartyDATAProcessing Account Number : " + accountNo);
        System.out.println("doDirect3rdPartyDATAProcessing Bank Code : " + bankCode);
        System.out.println("doDirect3rdPartyDATAProcessing Amount : " + amount);
        System.out.println("doDirect3rdPartyDATAProcessing ProductID : " + productID);
        System.out.println("doDirect3rdPartyDATAProcessing Initial Dial : " + initialDial);
        LOG.info("doDirect3rdPartyDATAProcessing Calling doProcessing!!!!");
        String response, typeOfPayment;
        StringBuilder sb = new StringBuilder();
        String type = "0";//0 - prepared, 1 - postpaid
        String type2 = "1";
        String alias = "GLO805";
        String actionType = "DATA";
        String actionName = "DATA-DIRECT-THIRD_PARTY";
        String appId = "" ;
        if(appCode.equals("125")){
            appId = "805";
        }else{
            appId = "777";
        }
        System.out.println("doDirect3rdPartyDATAProcessing appId : " + appId);

        typeOfPayment = "DT:" + alias + ":1";

        //VT:MTN:0:2348067567811:2348067567811
        String desc = typeOfPayment + ":" + session.getMsisdn() + ":" + session.getMsisdn();

        //Prepare Subscriber activity chain
        StringBuilder sbr = new StringBuilder();
        List<String> textStream = session.getQueue();
        for (String s : textStream) {
            sbr.append(s).append(" : ");
        }
        String ussdText = sbr.toString();

        //Call Auto Switch here
        XResponse xResponse = FlowProcessor.process(bankCode, accountNo, session, amount, desc, ussdPin);

        if (xResponse == null) {// Report that payment was not success
            sb = new StringBuilder();
            sb.append("No response from server! ~ Your Data bundle purchase request was not successful. Please try again later.~");
            response = sb.toString();
            FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", phoneNumber, "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
        } else {
            LOG.info("AutoSwitch as returned!!!");
            LOG.info("Response code returned is " + xResponse.getResponse());
            try {
                switch (xResponse.getResponse()) {
                    case 0:
                        LOG.info("AutoSwitch returned successful!!!");

                        LOG.info("Transaction logged. Proceeding to call VasGate");
                        String client = "USSD2";
                        JsonObject json = new JsonObject();
                        String merchantCode = "JT_" + bankCode + "_"+ appId;//GloService.getInstance().findMerchantCode(bankCode);
                        json.addProperty("reference", xResponse.getReference());
                        json.addProperty("amount", amount);
                        json.addProperty("alias", alias);
                        json.addProperty("action", "process");
                        json.addProperty("merchant", merchantCode);
                        json.addProperty("account", phoneNumber); //Destination
                        json.addProperty("mobile", session.getMsisdn());//Source
                        json.addProperty("name", "USSD2");
                        json.addProperty("type", type);
                        json.addProperty("type2", type2);
                        //alias + reference + mobile + type                     
                        json.addProperty("mac", generateMac(alias, xResponse.getReference(), phoneNumber, type));
                        json.addProperty("mode", "1");
                        json.addProperty("client", client);
                        json.addProperty("otherinfo", productID);
                        json.addProperty("channel", "04");
                        LOG.info("vasgate request (3rdDATA) ---- " + json.toString());

                        response = "Your data request is processing. You will be notified via SMS at completion. Thank you.";
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, phoneNumber, "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);
                        LOG.info("Final Response to User is ");
                        LOG.info(response);
                        ConcurrencyManager.execute(
                                new ConcurrentTask1(json,
                                        session,
                                        bankCode,
                                        accountNo,
                                        amount,
                                        xResponse.getReference(),
                                        desc));
                        break;
                    default:
                        sb.append(Utility.getAutoSwitchMessage(xResponse.getResponse()));
                        response = sb.toString();
                        if (response == null || "".equals(response) || response.isEmpty()) {
                            response = "Unknown error occurred! We could not complete your request at the moment. Please try again later.~Thank you.";
                        }
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, phoneNumber, "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);

                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "System error! ~ We could not complete your data request at the moment. Please try again later.~Thank you.";
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", phoneNumber, "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
            }
        }
        return response;
    }
}
