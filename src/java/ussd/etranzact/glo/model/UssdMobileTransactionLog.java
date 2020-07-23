/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Damilola.Omowaye
 */
@Entity
@Table(name = "ussd_mobile_transaction_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UssdMobileTransactionLog.findAll", query = "SELECT u FROM UssdMobileTransactionLog u")
    , @NamedQuery(name = "UssdMobileTransactionLog.findById", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.id = :id")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByActionType", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.actionType = :actionType")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByAmount", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.amount = :amount")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByAppid", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.appid = :appid")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByBankCode", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.bankCode = :bankCode")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByMobileNo", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.mobileNo = :mobileNo")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByModified", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.modified = :modified")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByProvider", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.provider = :provider")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByResponseCode", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.responseCode = :responseCode")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByResponseMessage", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.responseMessage = :responseMessage")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByShortCode", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.shortCode = :shortCode")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByTransDate", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.transDate = :transDate")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByUniqueTransid", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.uniqueTransid = :uniqueTransid")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByUserBankCode", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.userBankCode = :userBankCode")
    , @NamedQuery(name = "UssdMobileTransactionLog.findBySessionid", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.sessionid = :sessionid")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByBeneficiaryMobileNo", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.beneficiaryMobileNo = :beneficiaryMobileNo")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByDirection", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.direction = :direction")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByUssdText", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.ussdText = :ussdText")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByRate", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.rate = :rate")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByActionName", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.actionName = :actionName")
    , @NamedQuery(name = "UssdMobileTransactionLog.findByVasResponse", query = "SELECT u FROM UssdMobileTransactionLog u WHERE u.vasResponse = :vasResponse")})
public class UssdMobileTransactionLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Size(max = 50)
    @Column(name = "action_type")
    private String actionType;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "amount")
    private BigDecimal amount;
    @Size(max = 100)
    @Column(name = "appid")
    private String appid;
    @Size(max = 15)
    @Column(name = "bank_code")
    private String bankCode;
    @Size(max = 25)
    @Column(name = "mobile_no")
    private String mobileNo;
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Size(max = 255)
    @Column(name = "provider")
    private String provider;
    @Size(max = 5)
    @Column(name = "response_code")
    private String responseCode;
    @Size(max = 255)
    @Column(name = "response_message")
    private String responseMessage;
    @Size(max = 100)
    @Column(name = "short_code")
    private String shortCode;
    @Column(name = "trans_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transDate;
    @Size(max = 100)
    @Column(name = "unique_transid")
    private String uniqueTransid;
    @Size(max = 25)
    @Column(name = "user_bank_code")
    private String userBankCode;
    @Size(max = 100)
    @Column(name = "sessionid")
    private String sessionid;
    @Size(max = 25)
    @Column(name = "beneficiary_mobile_no")
    private String beneficiaryMobileNo;
    @Column(name = "direction")
    private Character direction;
    @Size(max = 255)
    @Column(name = "ussd_text")
    private String ussdText;
    @Column(name = "rate")
    private Double rate;
    @Size(max = 100)
    @Column(name = "actionName")
    private String actionName;
    @Size(max = 10)
    @Column(name = "vasResponse")
    private String vasResponse;

    public UssdMobileTransactionLog() {
    }

    public UssdMobileTransactionLog(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    public String getUniqueTransid() {
        return uniqueTransid;
    }

    public void setUniqueTransid(String uniqueTransid) {
        this.uniqueTransid = uniqueTransid;
    }

    public String getUserBankCode() {
        return userBankCode;
    }

    public void setUserBankCode(String userBankCode) {
        this.userBankCode = userBankCode;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getBeneficiaryMobileNo() {
        return beneficiaryMobileNo;
    }

    public void setBeneficiaryMobileNo(String beneficiaryMobileNo) {
        this.beneficiaryMobileNo = beneficiaryMobileNo;
    }

    public Character getDirection() {
        return direction;
    }

    public void setDirection(Character direction) {
        this.direction = direction;
    }

    public String getUssdText() {
        return ussdText;
    }

    public void setUssdText(String ussdText) {
        this.ussdText = ussdText;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getVasResponse() {
        return vasResponse;
    }

    public void setVasResponse(String vasResponse) {
        this.vasResponse = vasResponse;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UssdMobileTransactionLog)) {
            return false;
        }
        UssdMobileTransactionLog other = (UssdMobileTransactionLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UssdMobileTransactionLog{" + "id=" + id + ", actionType=" + actionType + ", amount=" + amount + ", appid=" + appid + ", bankCode=" + bankCode + ", mobileNo=" + mobileNo + ", modified=" + modified + ", provider=" + provider + ", responseCode=" + responseCode + ", responseMessage=" + responseMessage + ", shortCode=" + shortCode + ", transDate=" + transDate + ", uniqueTransid=" + uniqueTransid + ", userBankCode=" + userBankCode + ", sessionid=" + sessionid + ", beneficiaryMobileNo=" + beneficiaryMobileNo + ", direction=" + direction + ", ussdText=" + ussdText + ", rate=" + rate + ", actionName=" + actionName + ", vasResponse=" + vasResponse + '}';
    }

    
}
