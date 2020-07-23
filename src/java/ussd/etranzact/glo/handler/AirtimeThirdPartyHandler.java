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
import static ussd.etranzact.glo.handler.AirtimeSelfHandler.convertToInteger;
import ussd.etranzact.glo.model.UssdMobileSubscriber;
import ussd.etranzact.glo.service.GloService; 
import ussd.etranzact.glo.utils.App3DDetails;
import ussd.etranzact.glo.utils.Crypto;
import ussd.etranzact.glo.utils.DateTimeUtil;
import ussd.etranzact.glo.utils.FlowProcessor;
import ussd.etranzact.glo.utils.Utility;
import ussd.etranzact.glo.utils.Utils;

/**
 *
 * @author Damilola.Omowaye
 */
public class AirtimeThirdPartyHandler implements ShortCodeInterface {

    private final Logger LOG = Logger.getLogger(AirtimeThirdPartyHandler.class);
    private String actionName = "Airtime-Third Party";
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
            // LOG.info(data.getSessionId() + " AirtimeSelfHandler message ::::::::::::::::::::::::::::::::::::::::::::::" + message);
            session = new USSDSession(data.getMsisdn(), data.getSessionId());
            session.getQueue().add(data.getMessage());
            USSDSessionManager.getInstance().add(session);

            long ssStart = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") StartedAt ==  " + ssStart);

            String[] msgLength = session.getQueue().get(0).split("\\*");// *389*063*1500*2348045567890*805# 

            int dataLen = msgLength.length;
            // String bank_Code = msgLength[dataLen - 4];
            String amount = msgLength[dataLen - 3];
            String mobile3rdParty = msgLength[dataLen - 2];
            //LOG.info(data.getSessionId() + " bank_Code >>::::::::::::::::::::: " + bank_Code);
            LOG.info(data.getSessionId() + " amount >>::::::::::::::::::::: " + amount);
            LOG.info(data.getSessionId() + " mobile3rdParty  >>::::::::::::::::::::: " + mobile3rdParty);

            //========================================================================================================================================================
            /**
             * *
             * Build limit message parameters here Call method getLimitValue to
             * fetch message
             *
             ***
             */
            appCode = Utils.getAPP_ID(session.getQueue().get(0));
            LOG.info("AirtimeThirdPartyHandler appCode :::::::::::::::::::  " + appCode);
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
                            session.getSessionData().put("amount", amount);
                            session.getSessionData().put("mobile3rdParty", mobile3rdParty);

                            List<UssdMobileSubscriber> userAccounts = service.findAllAccount(data.getMsisdn(), appCode);
                            LOG.info(data.getSessionId() + " userAccounts >>::::::::::::::::::::: " + userAccounts.size());
                            if (userAccounts == null || userAccounts.isEmpty()) {
                                if (session.getSessionData().get("bankCounter") == null) {
                                    String banks = App3DDetails.getBankList();
                                    String bankListing[] = banks.split("~");

                                    sb.append("Choose bank").append("~");
                                    StringBuilder sb1 = new StringBuilder();
                                    for (int t = 0; t < bankListing.length; t++) {
                                        if (t < 7) {
                                            sb.append(bankListing[t]).append("~");
                                            sb1.append(bankListing[t]).append("~");
                                        }
                                    }
                                    session.getSessionData().put("BANK_LIST1", sb1.toString());
                                    sb.append("~");
                                    sb.append("8. ").append("Next");
                                    session.getSessionData().put("bankCounter", "1");
                                    session.getSessionData().put("process", "AIRTIME_3TH_DIRECT-BankSelect");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                }
                            } else {
                                session.getSessionData().put("userAccounts", userAccounts);
                                if (userAccounts.size() == 1) {
                                    sb.append("You are recharging ").append(mobile3rdParty).append(" with airtime worth N").append(amount).append("~");
                                    sb.append("Enter your PIN").append("~");
                                    session.getSessionData().put("process", "AIRTIME_3TH_DIRECT-pin");
                                    session.getSessionData().put("ASDB-myPinIndex", session.getQueue().size());
                                } else if (userAccounts.size() > 1) {
                                    sb.append(" Choose bank").append("~");
                                    //int counter = 1;
                                    for (int v = 0; v < userAccounts.size(); v++) {
                                        sb.append(v + 1).append(". ").append(App3DDetails.getBank(userAccounts.get(v).getBankCode())).append(" (").append(userAccounts.get(v).getAccountNo().replace(userAccounts.get(v).getAccountNo().substring(0, 6), "******")).append(")~");
                                        //counter++;
                                    }
//                                    sb.append("~").append(counter).append(".Opt-out of Service~");
//                                    session.getSessionData().put("maxAcctSize", counter);
                                    session.getSessionData().put("process", "AIRTIME_3TH_DIRECT-BankSelect");
                                    session.getSessionData().put("ASDB-chosenBankIndex", session.getQueue().size());

                                }

                            }

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

            String amount = (String) session.getSessionData().get("amount");
            String mobile3rdParty = (String) session.getSessionData().get("mobile3rdParty");
            List<UssdMobileSubscriber> userAccounts = (List<UssdMobileSubscriber>) session.getSessionData().get("userAccounts");

            if (session.getSessionData().get("process") != null && session.getSessionData().get("process1") == null) {
                String process = (String) session.getSessionData().get("process");
                LOG.info(data.getSessionId() + " process :::::::::::::::::::" + process);

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
                                    session.getSessionData().put("process", "AIRTIME_3TH_DIRECT-BankSelect");
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

                                    //sb.append("~");
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

                        LOG.info(data.getSessionId() + "  AIRTIME_3TH_DIRECT-BankSlect-newUser-size bankPos :::::::::::::::::::" + bankPos);

                        String list1 = (String) session.getSessionData().get("BANK_LIST1");
                        String listing1[] = list1.split("~");
                        LOG.info(data.getSessionId() + " 1st banks listing1 ::::::::::::::::::::::::::::::::::::::::::::::" + Arrays.toString(listing1));

                        int enterMsg = convertToInteger(bankPos);
                        String bankName = listing1[enterMsg - 1];
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        bankName = bankName.split("\\.")[1].trim();
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        session.getSessionData().put("AIRTIME_3TH_DIRECT-BankSlect-newUser-bankCode", App3DDetails.getBankCode(bankName));

                        sb.append("Enter your new ").append(bankName).append(" 10-digit Account Number").append("~");

                        session.getSessionData().put("process1", "AIRTIME_3TH_DIRECT-NoAccount-accountNumber");
                        session.getSessionData().put("AIRTIME_3TH_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex", session.getQueue().size());
                    }

                } else {
//                    int maxAcctSize1 = (int) session.getSessionData().get("maxAcctSize");
//                    int chosenAcctIndex1 = Integer.parseInt(message);
//                    if (maxAcctSize1 == chosenAcctIndex1) {
//                        //Do Out out here
//                        sb.append("Opt-out of Service!~~~Are you sure you want to Opt-out of GLO 805?~1. Yes~2. No~");
//                        session.getSessionData().put("optoutIndex", message);
//                    } else {
                    if (userAccounts.size() == 1) {

                        UssdMobileSubscriber subscriber = userAccounts.get(0);
                        String pina = subscriber.getPin();
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT pina >>>>>============================>> " + pina);

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
                            LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT userPin >>>>>============================>> " + userPin);

                            if (pina == "" || pina == null || pina.isEmpty() || pina.equalsIgnoreCase("null")) {
                                service.updatePIN(data.getMsisdn(), subscriber.getAccountNo(), userPin, appCode);
                            }

                            if (service.isAccountLocked(data.getMsisdn(), subscriber.getBankCode(), subscriber.getAccountNo(), appCode)) {
                                sb = new StringBuilder();
                                sb.append("Your account has been suspended  due to several failed authentication attempts.~You will be reactivated within an hour.~ Thank you.");
                                instance.destroySession(session.getSessionId());
                            } else {
//                                Boolean verifyAccount = FlowProcessor.verifyAccountWithBankCode(subscriber.getBankCode(), subscriber.getAccountNo(), session.getMsisdn());
//
//                                if (verifyAccount) {
                                if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), userPin, appCode)) {

                                    String initialDial = session.getQueue().get(0);
                                    String resp = doMenubased3rdPartyVTUProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
                                    sb.append(resp);
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
                                        if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), message, appCode)) {
                                            /**
                                             * Process Transaction here
                                             *
                                             */
                                            String initialDial = session.getQueue().get(0);
                                            String resp = doMenubased3rdPartyVTUProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
                                            sb.append(resp);
                                            instance.destroySession(session.getSessionId());
                                        } else {
                                            sb.append("Wrong PIN ~You have only 1 attempt left.~Enter PIN").append("~");
                                            session.getSessionData().put("lock", "4");
                                            session.getSessionData().put("lockCounter2", "ON");
                                        }
                                    } else if (session.getSessionData().get("lock").equals("4")) {
                                        if (service.verifyUserPIN(data.getMsisdn(), subscriber.getAccountNo(), subscriber.getBankCode(), message, appCode)) {
                                            /**
                                             * Process Transaction here
                                             *
                                             */
                                            String initialDial = session.getQueue().get(0);
                                            String resp = doMenubased3rdPartyVTUProcessing(session, subscriber.getBankCode(), subscriber.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
                                            sb.append(resp);
                                            instance.destroySession(session.getSessionId());
                                        } else {
                                            sb = new StringBuilder();
                                            service.lockSubscriberAccount(data.getMsisdn(), subscriber.getBankCode(), subscriber.getAccountNo(), appCode);
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
                        int chosenBankIndex = (Integer) session.getSessionData().get("ASDB-chosenBankIndex");
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT chosenBankIndex ::::::::::::::::::::::::::::::::::::::::::::::" + chosenBankIndex);
                        String chosenBankPosition = session.getQueue().get(chosenBankIndex);
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT chosenBankPosition ::::::::::::::::::::::::::::::::::::::::::::::" + chosenBankPosition);

                        UssdMobileSubscriber ums = userAccounts.get(Integer.parseInt(chosenBankPosition) - 1);
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT ums ::::::::::::::::::::::::::::::::::::::::::::::" + ums.toString());
                        session.getSessionData().put("subscriber", ums);

                        sb.append("You are recharging ").append(mobile3rdParty).append(" with airtime worth N").append(amount).append("~");
                        LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT ums ::::::::::::::::::::::::::::::::::::::::::::::" + ums.toString());
                        sb.append(" Enter your PIN").append("~");
                        session.getSessionData().put("process1", "AIRTIME_3TH_DIRECT-pin");
                        session.getSessionData().put("ASDB-pinIndex1", session.getQueue().size());
                    }
                    //}
                }
                response = sb.toString();
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", mobile3rdParty, "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);
            } else if (session.getSessionData().get("process1") != null && session.getSessionData().get("process2") == null) {
                String process = (String) session.getSessionData().get("process1");
                LOG.info(data.getSessionId() + " process1 :::::::::::::::::::" + process);

                if (userAccounts == null || userAccounts.isEmpty()) {

                    String bankCode = (String) session.getSessionData().get("AIRTIME_3TH_DIRECT-BankSlect-newUser-bankCode");
                    String accountNo = (String) session.getQueue().get((Integer) session.getSessionData().get("AIRTIME_3TH_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex"));

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
                                service.createSubscriber(subscriberA, appCode);

                                String initialDial = session.getQueue().get(0);
                                String resp = doMenubased3rdPartyVTUProcessing(session, bankCode, accountNo, mobile3rdParty, amount, initialDial, 0.0, pinn1);
                                sb.append(resp);
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
                    int pinIndex1 = (Integer) session.getSessionData().get("ASDB-pinIndex1");
                    String userPin = session.getQueue().get(pinIndex1);
                    LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-pinn ::::::::::::::::::::::::::::::::::::::::::::::" + userPin);
                    UssdMobileSubscriber ums = (UssdMobileSubscriber) session.getSessionData().get("subscriber");
                    LOG.info(data.getSessionId() + " AIRTIME_3TH_DIRECT subscriber :::::::::::::::::::::::::::::: " + ums);
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
//                            Boolean verifyAccountA = FlowProcessor.verifyAccountWithBankCode(ums.getBankCode(), ums.getAccountNo(), session.getMsisdn());
//                            if (verifyAccountA) {
                            if (service.verifyUserPIN(data.getMsisdn(), ums.getAccountNo(), userPin, appCode)) {
                                /**
                                 * **
                                 * Process Transaction here *
                                 */
                                // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                                // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                                String initialDial = session.getQueue().get(0);
                                // sb.append("Your Transaction was successful. ~ You can add more Account(s) using the Manage Profile Menu. ~ Thank you.");
                                String resp = doMenubased3rdPartyVTUProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
                                sb.append(resp);
                                instance.destroySession(session.getSessionId());
                            } else {
                                //sb.append("PIN verification failed! Please try again").append("~");
                                //session.getSessionData().remove("process1");

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
                                        String resp = doMenubased3rdPartyVTUProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
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
                                        String resp = doMenubased3rdPartyVTUProcessing(session, ums.getBankCode(), ums.getAccountNo(), mobile3rdParty, amount, initialDial, 0.0, userPin);
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
                    //}
                }
                response = sb.toString();
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", mobile3rdParty, "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);

            }

            long ssEnds = System.currentTimeMillis();
            LOG.info(" GLO805 Existing Session (" + data.getSessionId() + ") EndedAt ==  " + ssEnds);
            LOG.info("Total TimeTaken for session (" + data.getSessionId() + ") ==  " + (ssEnds - ssStart));

        }

        return response;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    private String doMenubased3rdPartyVTUProcessing(USSDSession session, String bankCode, String accountNo, String phone2, String amount, String initialDial, double rate, String ussdPin) {
        phone2 = phone2.replaceAll(" ", "");
        if (phone2.startsWith("0")) {
            phone2 = "234" + phone2.substring(1);
        }

        System.out.println("doMenubased3rdPartyVTUProcessing Details of doProcessing....");
        System.out.println("doMenubased3rdPartyVTUProcessing MSISDN : " + session.getMsisdn());
        System.out.println("doMenubased3rdPartyVTUProcessing 3rd Party MSISDN : " + phone2);
        System.out.println("doMenubased3rdPartyVTUProcessing Account Number : " + accountNo);
        System.out.println("doMenubased3rdPartyVTUProcessing Bank Code : " + bankCode);
        System.out.println("doMenubased3rdPartyVTUProcessing appCode : " + appCode);
        System.out.println("doMenubased3rdPartyVTUProcessing Amount : " + amount);
        System.out.println("doMenubased3rdPartyVTUProcessing Initial Dial : " + initialDial);
        LOG.info("doMenubased3rdPartyVTUProcessing Calling doProcessing!!!!");
        String response, typeOfPayment;
        StringBuilder sb = new StringBuilder();
        String type = "0";//0 - prepared, 1 - postpaid
        String type2 = "0";
        String alias = "GLO805";
        String actionType = "AIRTIME";
        String actionName = "AIRTIME-THIRD PARTY";
        String appId = "";
        if (appCode.equals("125")) {
            appId = "805";
        } else {
            appId = "777";
        }
        System.out.println("doMenubased3rdPartyVTUProcessing appId : " + appId);

        typeOfPayment = "VT:" + alias + ":0";
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
            sb.append("No response from server! ~Your request was not successful. Please try again later.~Thank you");
            response = sb.toString();
            FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", phone2, "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
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
                        String merchantCode = "JT_" + bankCode + "_" + appId;//GloService.getInstance().findMerchantCode(bankCode);
                        json.addProperty("reference", xResponse.getReference());
                        json.addProperty("amount", amount);
                        json.addProperty("alias", alias);
                        json.addProperty("action", "process");
                        json.addProperty("merchant", merchantCode);
                        json.addProperty("account", phone2); //Destination
                        json.addProperty("mobile", session.getMsisdn());//Source
                        json.addProperty("name", "USSD2");
                        json.addProperty("type", type);
                        json.addProperty("type2", type2);
                        //alias + reference + mobile + type                     
                        json.addProperty("mac", generateMac(alias, xResponse.getReference(), phone2, type));
                        json.addProperty("mode", "1");
                        json.addProperty("client", client);
                        json.addProperty("otherinfo", "USSD2");
                        json.addProperty("channel", "04");
                        LOG.info("vasgate request(3rdVTU) ---- " + json.toString());

                        response = "Your airtime request is processing. You will be notified via SMS at completion. Thank you.";
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, phone2, "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);
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
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, phone2, "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "System error! ~ We could not complete your airtime request at the moment. Please try again later.~ Thank you.";
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", phone2, "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
            }
        }
        return response;
    }

    public static String generateMac(String alias, String reference, String mobile, String type) {
        String mac = alias + reference + mobile + type;
        System.out.println(mac);
        return Crypto.encodeMD5NoSalt(mac);
    }

}
