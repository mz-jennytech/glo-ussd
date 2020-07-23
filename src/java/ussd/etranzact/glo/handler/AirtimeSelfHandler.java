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
public class AirtimeSelfHandler implements ShortCodeInterface {

    private final Logger LOG = Logger.getLogger(AirtimeSelfHandler.class);
    private String actionName = "Airtime-Self";
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

            session = new USSDSession(data.getMsisdn(), data.getSessionId());
            session.getQueue().add(data.getMessage());
            USSDSessionManager.getInstance().add(session);

            long ssStart = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") StartedAt ==  " + ssStart);

            String[] msgLength = session.getQueue().get(0).split("\\*");// *389*805*<AMOUNT>*805#

            int dataLen = msgLength.length;
            //String bank_Code = msgLength[dataLen - 3];
            String amount = msgLength[dataLen - 2];
            //LOG.info(data.getSessionId() + " bank_Code >>::::::::::::::::::::: " + bank_Code);
            LOG.info(data.getSessionId() + " amount >>::::::::::::::::::::: " + amount);
            //session.getSessionData().put("bank_Code", bank_Code);
            appCode = Utils.getAPP_ID(session.getQueue().get(0));
            //========================================================================================================================================================
            /**
             * *
             * Build limit message parameters here Call method getLimitValue to
             * fetch message
             *
             ***
             */

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

                            List<UssdMobileSubscriber> userAccounts = service.findAllAccount(data.getMsisdn(), appCode);
                            // LOG.info(data.getSessionId() + " userAccounts size >>::::::::::::::::::::: " + userAccounts.size());
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
                                    session.getSessionData().put("process", "AIRTIME_SELF_DIRECT-BankSelect");
                                    session.getSessionData().put("ASDB-newUserChosenBankIndex", session.getQueue().size());
                                }

                            } else {
                                session.getSessionData().put("userAccounts", userAccounts);
                                if (userAccounts.size() == 1) {

                                    UssdMobileSubscriber ums = userAccounts.get(0);

                                    if (service.isAccountLocked(data.getMsisdn(), ums.getBankCode(), ums.getAccountNo(), appCode)) {
                                        sb = new StringBuilder();
                                        sb.append("Your account has been suspended  due to several failed authentication attempts.~You will be reactivated within an hour.~ Thank you.");
                                        instance.destroySession(session.getSessionId());
                                    } else {
//                            Boolean verifyAccountA = FlowProcessor.verifyAccountWithBankCode(ums.getBankCode(), ums.getAccountNo(), session.getMsisdn());
//                            LOG.info(data.getSessionId() + " verifyAccountA :::::::::::::::::::" + verifyAccountA);
//                            if (verifyAccountA) {
                                        UssdMobileSubscriber subscriberA = new UssdMobileSubscriber();
                                        subscriberA.setAccountNo(ums.getAccountNo());
                                        subscriberA.setBankCode(ums.getBankCode());
                                        subscriberA.setActive(true);
                                        subscriberA.setCreated(DateTimeUtil.getCurrentDate());
                                        subscriberA.setMobileNo(session.getMsisdn());
                                        //subscriberA.setAppcode(appCode);
                                        subscriberA.setModified(DateTimeUtil.getCurrentDate());
                                        //subscriberA.setPin(pinn1);
                                        service.createSubscriber(subscriberA, appCode);

                                        /**
                                         * Process Transaction here
                                         */
                                        // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                                        // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                                        String initialDial1 = session.getQueue().get(0);
                                        String resp = doMenubasedVTUProcessing(session, ums.getBankCode(), ums.getAccountNo(), amount, initialDial1, 0.0, "");
                                        sb.append(resp);
                                        instance.destroySession(session.getSessionId());
//                            } else {
//                                sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
//                                instance.destroySession(session.getSessionId());
//                            }
                                    }

                                } else if (userAccounts.size() > 1) {
                                    sb.append(" Choose bank").append("~");
                                    //int counter = 1;
                                    for (int v = 0; v < userAccounts.size(); v++) {
                                        sb.append(v + 1).append(". ").append(App3DDetails.getBank(userAccounts.get(v).getBankCode())).append(" (").append(userAccounts.get(v).getAccountNo().replace(userAccounts.get(v).getAccountNo().substring(0, 6), "******")).append(")~");
                                        //counter++;
                                    }
//                                    sb.append("~").append(counter).append(".Opt-out of Service~");
//                                    session.getSessionData().put("maxAcctSize", counter);
                                    session.getSessionData().put("process", "AIRTIME_SELF_DIRECT-BankSelect");
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
            FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", data.getMsisdn(), "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, "MainMenu", null);

            long ssEnds = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") EndedAt ==  " + ssEnds);
            LOG.info("Total TimeTaken for session (" + data.getSessionId() + ") ==  " + (ssEnds - ssStart));

        } else {
            long ssStart = System.currentTimeMillis();
            LOG.info(" GLO805 Existing Session (" + data.getSessionId() + ") StartedAt ==  " + ssStart);

            session.getQueue().add(data.getMessage());
            appCode = Utils.getAPP_ID(session.getQueue().get(0));

            String amount = (String) session.getSessionData().get("amount");
            List<UssdMobileSubscriber> userAccounts = (List<UssdMobileSubscriber>) session.getSessionData().get("userAccounts");

            LOG.info(data.getSessionId() + " message :::::::::::::::::::::: " + message);
            LOG.info(data.getSessionId() + " amount :::::::::::::::::::: " + amount);

            if (session.getSessionData().get("process") != null && session.getSessionData().get("processA") == null) {
                String actionName = "Airtime-Self";
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
                                    session.getSessionData().put("process", "AIRTIME_SELF_DIRECT-BankSelect");
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

                                    sb.append("~");
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

                        LOG.info(data.getSessionId() + "  AIRTIME_SELF_DIRECT-BankSlect-newUser-size bankPos :::::::::::::::::::" + bankPos);

                        String list1 = (String) session.getSessionData().get("BANK_LIST1");
                        String listing1[] = list1.split("~");
                        LOG.info(data.getSessionId() + " 1st banks listing1 ::::::::::::::::::::::::::::::::::::::::::::::" + Arrays.toString(listing1));

                        int enterMsg = convertToInteger(bankPos);
                        String bankName = listing1[enterMsg - 1];
                        LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        bankName = bankName.split("\\.")[1].trim();
                        LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-BankSlect-newUser- bankName >>>>>============================>> " + bankName);
                        session.getSessionData().put("AIRTIME_SELF_DIRECT-BankSlect-newUser-bankCode", App3DDetails.getBankCode(bankName));

                        sb.append("Enter your new ").append(bankName).append(" 10-digit Account Number").append("~");

                        session.getSessionData().put("processA", "AIRTIME_SELF_DIRECT-NoAccount-accountNumber");
                        session.getSessionData().put("AIRTIME_SELF_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex", session.getQueue().size());
                    }

                } else {

//                    int maxAcctSize1 = (int) session.getSessionData().get("maxAcctSize");
//                    int chosenAcctIndex1 = Integer.parseInt(message);
//                    if (maxAcctSize1 == chosenAcctIndex1) {
//                        //Do Out out here
//                        sb.append("Opt-out of Service!~~~Are you sure you want to Opt-out of GLO 805?~1. Yes~2. No~");
//                        session.getSessionData().put("optoutIndex", message);
//                    } else {
                    if (userAccounts.size() > 1) {

                        int chosenBankIndex = (Integer) session.getSessionData().get("ASDB-chosenBankIndex");
                        LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT chosenBankIndex ::::::::::::::::::::::::::::::::::::::::::::::" + chosenBankIndex);
                        String chosenBankPosition = session.getQueue().get(chosenBankIndex);
                        LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT chosenBankPosition ::::::::::::::::::::::::::::::::::::::::::::::" + chosenBankPosition);

                        UssdMobileSubscriber ums = userAccounts.get(Integer.parseInt(chosenBankPosition) - 1);
                        if (service.isAccountLocked(data.getMsisdn(), ums.getBankCode(), ums.getAccountNo(), appCode)) {
                            sb = new StringBuilder();
                            sb.append("Your account has been suspended  due to several failed authentication attempts.~You will be reactivated within an hour.~ Thank you.");
                            instance.destroySession(session.getSessionId());
                        } else {

//                            Boolean verifyAccountA = FlowProcessor.verifyAccountWithBankCode(ums.getBankCode(), ums.getAccountNo(), session.getMsisdn());
//                            LOG.info(data.getSessionId() + " verifyAccountA :::::::::::::::::::" + verifyAccountA);
//                            if (verifyAccountA) {
                            UssdMobileSubscriber subscriberA = new UssdMobileSubscriber();
                            subscriberA.setAccountNo(ums.getAccountNo());
                            subscriberA.setBankCode(ums.getBankCode());
                            subscriberA.setActive(true);
                            subscriberA.setCreated(DateTimeUtil.getCurrentDate());
                            subscriberA.setMobileNo(session.getMsisdn());
                            subscriberA.setModified(DateTimeUtil.getCurrentDate());
                            //subscriberA.setPin(pinn1);
                            service.createSubscriber(subscriberA, appCode);
                            /**
                             * Process Transaction here
                             */
                            // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                            // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                            String initialDial1 = session.getQueue().get(0);
                            String resp = doMenubasedVTUProcessing(session, ums.getBankCode(), ums.getAccountNo(), amount, initialDial1, 0.0, "");
                            sb.append(resp);
                            instance.destroySession(session.getSessionId());
//                            } else {
//                                sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
//                                instance.destroySession(session.getSessionId());
//                            }

                        }

                    }

                    //}
                }
                response = sb.toString();
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", data.getMsisdn(), "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);

            } else if (session.getSessionData().get("processA") != null && session.getSessionData().get("processB") == null) {
                String process = (String) session.getSessionData().get("processA");
                LOG.info(data.getSessionId() + " processA :::::::::::::::::::" + process);

                if (userAccounts == null || userAccounts.isEmpty()) {
                    String bankCode1 = (String) session.getSessionData().get("AIRTIME_SELF_DIRECT-BankSlect-newUser-bankCode");
                    LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-BANKSELECT-NEWUSER-account-accountIndex ::::::::::::::::::::::::::::::::::::::::::::::" + bankCode1);

                    int acctIndexu = (Integer) session.getSessionData().get("AIRTIME_SELF_DIRECT-BANKSELECT-NEWUSER-accountNumberIndex");
                    LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-BANKSELECT-NEWUSER-account-accountIndex ::::::::::::::::::::::::::::::::::::::::::::::" + acctIndexu);
                    String acctNum1 = session.getQueue().get(acctIndexu);
                    session.getSessionData().put("AIRTIME_SELF_DIRECT-accountNumber", acctNum1);
                    LOG.info(data.getSessionId() + " AIRTIME_SELF_DIRECT-accountNumber ::::::::::::::::::::::::::::::::::::::::::::::" + acctNum1);

                    Boolean verifyAccountA = FlowProcessor.verifyAccountWithBankCode(bankCode1, acctNum1, session.getMsisdn(), FlowProcessor.generateReference(session.getMsisdn()));
                    LOG.info(data.getSessionId() + " verifyAccountA :::::::::::::::::::" + verifyAccountA);
                    if (verifyAccountA) {
                        UssdMobileSubscriber subscriberA = new UssdMobileSubscriber();
                        subscriberA.setAccountNo(acctNum1);
                        subscriberA.setBankCode(bankCode1);
                        subscriberA.setActive(true);
                        subscriberA.setCreated(DateTimeUtil.getCurrentDate());
                        subscriberA.setMobileNo(session.getMsisdn());
                        //subscriberA.setAppcode(appCode);
                        subscriberA.setModified(DateTimeUtil.getCurrentDate());
                        //subscriberA.setPin(pinn1);
                        service.createSubscriber(subscriberA, appCode);
                        /**
                         * Process Transaction here
                         */
                        // session.getSessionData().put("process", "AIRTIME_SELF-Completed");
                        // sb.append("You are recharging ").append(data.getMsisdn()).append(" with N").append(amount).append("~");
                        String initialDial1 = session.getQueue().get(1);
                        String resp = doMenubasedVTUProcessing(session, bankCode1, acctNum1, amount, initialDial1, 0.0, "");
                        sb.append(resp);
                        instance.destroySession(session.getSessionId());
                    } else {
                        sb.append("This mobile number is NOT tied to your bank account! ~ Please contact your bank for assistance.~ Thank you.").append("~");
                        instance.destroySession(session.getSessionId());
                    }

                }
//                else {
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
//                    }
//                }
                response = sb.toString();
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Request", data.getMsisdn(), "0", message + "|" + sb.toString(), session.getSessionId(), 0.0, actionName, null);

            }

            long ssEnds = System.currentTimeMillis();
            LOG.info(" GLO805 New Session (" + data.getSessionId() + ") EndedAt ==  " + ssEnds);
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
     * PROCESS TRANSACTION VIA AUTO SWITCH AND GIVE VALUE VIA VASGATE PROCESS
     * //****************************************************************************************************************
     */
    private String doMenubasedVTUProcessing(USSDSession session, String bankCode, String accountNo, String amount, String initialDial, double rate, String ussdPin) {
        System.out.println("doMenubasedVTUProcessing Details of doProcessing....");
        System.out.println("doMenubasedVTUProcessing MSISDN : " + session.getMsisdn());
        System.out.println("doMenubasedVTUProcessing Account Number : " + accountNo);
        System.out.println("doMenubasedVTUProcessing Bank Code : " + bankCode);
        System.out.println("doMenubasedVTUProcessing appCode : " + appCode);
        System.out.println("doMenubasedVTUProcessing Amount : " + amount);
        System.out.println("doMenubasedVTUProcessing Initial Dial : " + initialDial);
        LOG.info("doMenubasedVTUProcessing Calling doProcessing!!!!");
        String response, typeOfPayment;
        StringBuilder sb = new StringBuilder();
        String type = "0";//0 - prepared, 1 - postpaid
        String type2 = "0";
        String alias = "GLO805";
        String actionType = "AIRTIME";
        String actionName = "AIRTIME-SELF";
        String appId = "";
        if (appCode.equals("125")) {
            appId = "805";
        } else {
            appId = "777";
        }
        System.out.println("doMenubasedVTUProcessing appId : " + appId);

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
            sb.append("No response from server! ~ Your request was not successful. Please try again later.~ Thank you.");
            response = sb.toString();
            FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", session.getMsisdn(), "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
            //FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, session.getMsisdn(), "0", ussdText + " - " + response, session.getSessionId(), rate);
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
                        json.addProperty("account", session.getMsisdn());
                        json.addProperty("mobile", session.getMsisdn());
                        json.addProperty("name", "USSD2");
                        json.addProperty("type", type);
                        json.addProperty("type2", type2);
                        //alias + reference + mobile + type                     
                        json.addProperty("mac", generateMac(alias, xResponse.getReference(), session.getMsisdn(), type));
                        json.addProperty("mode", "1");
                        json.addProperty("client", client);
                        json.addProperty("otherinfo", "USSD2");
                        json.addProperty("channel", "04");
                        LOG.info("vasgate request (VTU) ---- " + json.toString());

                        sb.append("Your airtime request is processing. You will be notified via SMS at completion.~ Thank you.");
                        response = sb.toString();
                        LOG.info("Final Response to User is ");
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, session.getMsisdn(), "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);

                        //LOG.info(response);
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
                        FlowProcessor.logtransaction(xResponse, session, bankCode, typeOfPayment, amount, appCode, Utility.provider, actionType, session.getMsisdn(), "1", ussdText + " - " + response, session.getSessionId(), rate, actionName, null);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "System error! ~ We could not complete your airtime request at the moment. Please try again later.~ Thank you.";
                FlowProcessor.logtransaction(null, null, session, "", "", "0.0", appCode, Utility.provider, "Response", session.getMsisdn(), "1", ussdText + " - " + response, session.getSessionId(), 0.0, actionName, null);
            }
        }
        return response;
    }

    //*************************************************************
    public static int convertToInteger(String number) {
        int num = 0;
        try {
            num = Integer.parseInt(number);
        } catch (Exception e) {

        }
        return num;
    }

    private boolean isDecimal(String number) {
        try {
            Double.parseDouble(number);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isInteger(String number) {
        try {
            Integer.parseInt(number);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
