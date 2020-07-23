/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.log4j.Logger;
import ussd.etranzact.glo.model.UssdConfig;
import ussd.etranzact.glo.model.UssdMobileBorrowmeSubscriber;
import ussd.etranzact.glo.model.UssdMobileSubscriber;
import ussd.etranzact.glo.model.UssdRequest;
import ussd.etranzact.glo.model.UssdMobileTransactionLog;
import ussd.etranzact.glo.utils.Crypto;
import ussd.etranzact.glo.utils.DateTimeUtil;

/**
 *
 * @author Damilola.Omowaye
 */
public class GloService {

    private static GloService INSTANCE = GloService.getInstance();
    private final Logger LOG = Logger.getLogger(GloService.class);
    public EntityManager em = Persistence.createEntityManagerFactory("GloPU").createEntityManager();
//    private static final String appCode = Utils.getAPP_DI();
    //private EntityTransaction transaction;

//    public EntityTransaction getTransaction() {
//        return transaction;
//    }
//
//    public void setTransaction(EntityTransaction transaction) {
//        this.transaction = transaction;
//    }
    public static String loadParam(String param) {
        String outt = "";
        try {
            TypedQuery<UssdConfig> query = getInstance().em.createQuery("SELECT c FROM UssdConfig c WHERE c.parameterName = :parameterName ", UssdConfig.class);

            query.setParameter("parameterName", param);
            UssdConfig ussdConfig = (UssdConfig) query.getSingleResult();

            outt = ussdConfig.getParameterValue();
            System.out.println("Load Rate outt " + param + " :::::::::::::::::::::: " + outt);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return outt;
    }

    public static GloService getInstance() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new GloService();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return INSTANCE;
    }

    public EntityManager getEm() {
        return this.em;
    }

    public void create(Object t) {
        try {

            EntityManager entityManager = this.em.getEntityManagerFactory().createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(t);
            entityManager.getTransaction().commit();
            entityManager.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean delete(Object t) {
        boolean status = false;
        try {
            EntityTransaction tx = this.em.getTransaction();
            tx.begin();
            this.em.merge(t);
            this.em.remove(t);
            tx.commit();
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }
//    public <T> T merge(T t) {
//        T merge;
//        try {
//            this.em = this.em.getEntityManagerFactory().createEntityManager();
//            if (!transaction.isActive()) {
//                transaction.begin();
//            }
//            merge = em.merge(t);
//            transaction.commit();
//            return merge;
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
//    public <T> void delete(T t) {
//        try {
//            this.em = this.em.getEntityManagerFactory().createEntityManager();
//            this.em.remove(t);
//        } catch (Exception e) {
//
//        } finally {
//            this.em.close();
//        }
//    }
//    public boolean delete(Object t) {
//        //EntityManager entityManager = null;
//        boolean status = false;
//        try {
//            EntityManager entityManager = this.em.getEntityManagerFactory().createEntityManager();
//            entityManager.getTransaction().begin();
//            this.em.merge(t);
//            this.em.remove(t);
//            entityManager.getTransaction().commit();
//            status = true;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return status;
//    }

    public void update(Object t) {
        try {
            EntityManager entityManager = this.em.getEntityManagerFactory().createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.merge(t);
            entityManager.getTransaction().commit();
            entityManager.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createSubcriberRequest(UssdRequest ussdRequest) {
        UssdRequest uRequest = null;
        try {
            create(ussdRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findMerchantCode(String bankCode) {
        TypedQuery<UssdConfig> query = this.em.createQuery("SELECT u FROM UssdConfig u WHERE u.parameterName = :parameterName", UssdConfig.class);
        query.setParameter("parameterName", "GLO_MERCHANT_ACCT");
        List<UssdConfig> results = (List<UssdConfig>) query.getResultList();
        UssdConfig result;
        String retVal;
        switch (results.size()) {
            case 0:
                result = (UssdConfig) results.get(0);
                retVal = result.getParameterValue();
                break;
            default:
                TypedQuery<UssdConfig> singleQuery = this.em.createQuery("SELECT u FROM UssdConfig u WHERE u.parameterName = :parameterName", UssdConfig.class);

                singleQuery.setParameter("parameterName", "GLO_MERCHANT_ACCT");
                result = (UssdConfig) singleQuery.getSingleResult();
                retVal = result.getParameterValue();
        }
        return retVal;
    }

    public void createTxnLog(UssdMobileTransactionLog txnLog) {
        try {
            create(txnLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSubscriber(UssdMobileSubscriber subscriber, String appCode) {
        this.LOG.info("Creating subscriber.......");
        try {
            if (findSubscriberByMobileAccount(subscriber.getMobileNo(), subscriber.getAccountNo(), appCode) == null) {
                if (subscriber.getPin() != null) {
                    subscriber.setPin(Crypto.encodeMD5NoSalt(subscriber.getPin()));
                }
                subscriber.setAppcode(appCode);
                create(subscriber);
                this.LOG.info("Subscriber created ::::::: " + subscriber);
            } else {
                this.LOG.info("Subscriber already exists ::::::: " + subscriber);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteSubscriberAccount(UssdMobileSubscriber ussdMobileSubscriber) {
        boolean status = false;
        UssdMobileSubscriber rr = (UssdMobileSubscriber) this.em.merge(ussdMobileSubscriber);
        if (delete(rr)) {
            status = true;
        }
        this.LOG.info("Delete status ::::::: " + status);
        return status;
    }

    public UssdMobileSubscriber findSubscriberByMobileAccount(String mobileNo, String accountNumber, String appCode) {
        this.LOG.info("Searching for previous accounts with ....");
        this.LOG.info("Mobile Number ::: " + mobileNo);
        this.LOG.info("Account Number ::: " + accountNumber);
        this.LOG.info("App Code ::: "+appCode);
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.appcode = :appcode AND u.accountNo = :accountNo", UssdMobileSubscriber.class);

        query.setParameter("mobileNo", mobileNo);
        query.setParameter("appcode", appCode);
        query.setParameter("accountNo", accountNumber);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            this.LOG.info("Could not find subscriber. Returning null!!!!!");
            return null;
        }
        this.LOG.info("Account found for User.");
        return (UssdMobileSubscriber) uss.get(0);
    }

    public boolean updatePIN(String mobileNo, String accountNo, String pin, String appCode) {
        boolean status = false;
        try {
            this.LOG.info("Mobile Number ::: " + mobileNo);
            this.LOG.info("Account Number ::: " + accountNo);
            this.LOG.info("App Code ::: "+appCode);
            pin = Crypto.encodeMD5NoSalt(pin);

            UssdMobileSubscriber user = findSubscriberByMobileAccount(mobileNo, accountNo,appCode);
            user.setModified(DateTimeUtil.getCurrentDate());
            user.setPin(pin);
            update(user);

            this.LOG.info("Subscriber Updated!!!!!");
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }

//    public UssdMobileSubscriber findMobileSubscriber(String mobileNo) {
//        this.LOG.info("Searching for previous accounts with ....");
//        this.LOG.info("Mobile Number ::: " + mobileNo);
//        //this.LOG.info("App Code ::: "+appCode);
//        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo", UssdMobileSubscriber.class);
//
//        query.setParameter("mobileNo", mobileNo);
//        //query.setParameter("appcode", appCode);
//        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
//        if (uss.isEmpty()) {
//            this.LOG.info("Could not find subscriber. Returning null!!!!!");
//            return null;
//        }
//        this.LOG.info("Account found for User.");
//        return (UssdMobileSubscriber) uss.get(0);
//    }

    public boolean verifyUserPIN(String mobileNo, String account, String bankCode, String pin, String appCode) {
        boolean status = false;
        // this.LOG.info("Raw PIN :::::::::::: " + pin);
        pin = Crypto.encodeMD5NoSalt(pin);
        this.LOG.info("appCode PIN :::::::::::: " + appCode);
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.accountNo = :accountNo AND u.bankCode = :bankCode AND u.appcode = :appcode AND u.pin = :pin", UssdMobileSubscriber.class);

        query.setParameter("mobileNo", mobileNo);
        query.setParameter("accountNo", account);
        query.setParameter("bankCode", bankCode);
        query.setParameter("appcode", appCode);
        query.setParameter("pin", pin);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            status = false;
        } else {
            this.LOG.info("Account found for User.");
            status = true;
        }
        this.LOG.info("Verified? :::::::::::: " + status);
        return status;
    }

    public boolean verifyUserPIN(String mobileNo, String accountNo, String pin, String appCode) {
        boolean status = false;
        //this.LOG.info("Raw PIN :::::::::::: " + pin);
        pin = Crypto.encodeMD5NoSalt(pin);
        //this.LOG.info("Encoded PIN :::::::::::: " + pin);
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.accountNo = :accountNo AND u.appcode = :appcode AND u.pin = :pin", UssdMobileSubscriber.class);

        query.setParameter("mobileNo", mobileNo);
        query.setParameter("accountNo", accountNo);
        query.setParameter("appcode", appCode);
        query.setParameter("pin", pin);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            status = false;
        } else {
            this.LOG.info("Account found for User.");
            status = true;
        }
        return status;
    }

    public boolean userExist(String mobileNo) {
        boolean status = false;
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo", UssdMobileSubscriber.class);

        query.setParameter("mobileNo", mobileNo);

        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            status = false;
        } else {
            this.LOG.info("Account found for User.");
            status = true;
        }
        return status;
    }

    public List<UssdMobileSubscriber> findAllAccount(String mobileNo) {
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo", UssdMobileSubscriber.class);

        query.setParameter("mobileNo", mobileNo);

        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();

        return uss;
    }

    public List<UssdMobileBorrowmeSubscriber> checkBorrowMeProfile(String mobileNo) {
        TypedQuery<UssdMobileBorrowmeSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.mobileNo = :mobileNo", UssdMobileBorrowmeSubscriber.class);

        query.setParameter("mobileNo", mobileNo);

        return (List<UssdMobileBorrowmeSubscriber>) query.getResultList();
    }

    public UssdMobileBorrowmeSubscriber checkBorrowMeProfile(String mobileNo, String bankCode) {
        TypedQuery<UssdMobileBorrowmeSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.mobileNo = :mobileNo AND u.bankCode = :bankCode", UssdMobileBorrowmeSubscriber.class);

        return (UssdMobileBorrowmeSubscriber) query.setParameter("mobileNo", mobileNo).setParameter("bankCode", bankCode).getSingleResult();
    }

    public boolean lockSubscriberAccount(String mobileNo, String bankCode, String accountNo, String appCode) {
        boolean status = false;
        try {
            this.LOG.info("Mobile Number ::: " + mobileNo);
            this.LOG.info("Account Number ::: " + accountNo);
            this.LOG.info("Bank Code ::: " + bankCode);
            this.LOG.info("App Code ::: "+appCode);

            UssdMobileSubscriber user = findSubscriberByMobileAccountBankCode(mobileNo, accountNo, bankCode, appCode);
            user.setModified(DateTimeUtil.getCurrentDate());
            user.setActive(false);
            update(user);

            this.LOG.info("Subscriber Updated!!!!!");
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public static String getTimeDiff(Date dateOne, Date dateTwo) {
        String diff = "";
        long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime());
        diff = String.format("%d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(timeDiff)), Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)))});
        return diff;
    }

    public boolean isAccountLocked(String mobileNo, String bankCode, String accountNo, String appCode) {
        boolean status = false;
        try {
            this.LOG.info("Mobile Number ::: " + mobileNo);
            this.LOG.info("Account Number ::: " + accountNo);
            this.LOG.info("Bank Code ::: " + bankCode);
           // this.LOG.info("App Code ::: "+appCode);

            UssdMobileSubscriber user = findSubscriberByMobileAccountBankCode(mobileNo, accountNo, bankCode,appCode);
            if (user != null) {
                if (user.getActive()) {
                    status = false;
                } else {
                    status = true;
                    Calendar now = Calendar.getInstance();
                    now.setTime(user.getModified()); // sets calendar time/date                
                    System.out.println("Diff. Time ::::::::>> " + getTimeDiff(new Date(), now.getTime()));
                    this.LOG.info("Suspended ::: " + now.getTime());
                    if (Integer.parseInt(getTimeDiff(new Date(), now.getTime())) >= 1) { //Unluck account after 1hr
                        user.setModified(DateTimeUtil.getCurrentDate());
                        user.setActive(true);
                        update(user);
                        //status = true;
                        this.LOG.info("Subscriber reactivated :::>> " + user.toString());
                    }
                }
                //this.LOG.info("Subscriber Account is Locked!!!!!");
            } else {
                status = false;
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        System.out.println("Subscriber Account locked? :::::::::>> " + status);
        return status;
    }

    public UssdMobileSubscriber findSubscriberByMobileAccountBankCode(String mobileNo, String accountNumber, String bankCode, String appCode) {
        this.LOG.info("Searching for previous accounts with ....");
        this.LOG.info("Bank Code ::: " + bankCode);
        this.LOG.info("Mobile Number ::: " + mobileNo);
        this.LOG.info("Account Number ::: " + accountNumber);
        this.LOG.info("App Code ::: "+appCode);
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.appcode = :appcode AND u.accountNo = :accountNo AND u.bankCode = :bankCode", UssdMobileSubscriber.class);

        query.setParameter("bankCode", bankCode);
        query.setParameter("mobileNo", mobileNo);
        query.setParameter("appcode", appCode);
        query.setParameter("accountNo", accountNumber);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            this.LOG.info("Could not find subscriber. Returning null!!!!!");
            return null;
        }
        this.LOG.info("Account found for User.");
        return (UssdMobileSubscriber) uss.get(0);
    }

    public boolean updateTransaction(String reference, String responseCode) {
        boolean status = false;
        try {
            this.LOG.info("Unique TransID ::: " + reference);
            this.LOG.info("Vasgate responseCode ::: " + responseCode);

            TypedQuery<UssdMobileTransactionLog> query = this.em.createQuery("SELECT u FROM UssdMobileTransactionLog u WHERE u.uniqueTransid = :uniqueTransid", UssdMobileTransactionLog.class);
            UssdMobileTransactionLog ud = (UssdMobileTransactionLog) query.setParameter("uniqueTransid", reference).getSingleResult();

            if (ud != null) {
                ud.setVasResponse(responseCode);
                update(ud);
                this.LOG.info("UssdMobileTransactionLog Updated!!!!!");
                status = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public List<UssdMobileSubscriber> findSubscriberByMobileBankCode(String mobileNo, String bankCode, String appCode) {

        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.appcode = :appcode AND u.bankCode = :bankCode", UssdMobileSubscriber.class);
        query.setParameter("bankCode", bankCode);
        query.setParameter("mobileNo", mobileNo);
        query.setParameter("appcode", appCode);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        if (uss.isEmpty()) {
            this.LOG.info("Could not find subscriber. Returning null!!!!!");
            return null;
        }
        this.LOG.info(uss.size() + " Accounts found for User.");
        return uss;
    }

    public List<UssdMobileSubscriber> findAllAccount(String mobileNo, String appCode) {
        TypedQuery<UssdMobileSubscriber> query = this.em.createQuery("SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo AND u.appcode = :appcode", UssdMobileSubscriber.class);
        query.setParameter("mobileNo", mobileNo);
        query.setParameter("appcode", appCode);
        List<UssdMobileSubscriber> uss = (List<UssdMobileSubscriber>) query.getResultList();
        return uss;
    }

}
