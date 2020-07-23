/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Damilola.Omowaye
 */
@Entity
@Table(name = "ussd_mobile_subscriber")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UssdMobileSubscriber.findAll", query = "SELECT u FROM UssdMobileSubscriber u")
    , @NamedQuery(name = "UssdMobileSubscriber.findById", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.id = :id")
    , @NamedQuery(name = "UssdMobileSubscriber.findByAccountNo", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.accountNo = :accountNo")
    , @NamedQuery(name = "UssdMobileSubscriber.findByMobileNo", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.mobileNo = :mobileNo")
    , @NamedQuery(name = "UssdMobileSubscriber.findByName", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.name = :name")
    , @NamedQuery(name = "UssdMobileSubscriber.findByActive", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.active = :active")
    , @NamedQuery(name = "UssdMobileSubscriber.findByBankCode", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.bankCode = :bankCode")
    , @NamedQuery(name = "UssdMobileSubscriber.findByPin", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.pin = :pin")
    , @NamedQuery(name = "UssdMobileSubscriber.findByCreated", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.created = :created")
    , @NamedQuery(name = "UssdMobileSubscriber.findByModified", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.modified = :modified")
    , @NamedQuery(name = "UssdMobileSubscriber.findByLastTranTime", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.lastTranTime = :lastTranTime")
    , @NamedQuery(name = "UssdMobileSubscriber.findByAppcode", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.appcode = :appcode")
    , @NamedQuery(name = "UssdMobileSubscriber.findByFId", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.fId = :fId")
    , @NamedQuery(name = "UssdMobileSubscriber.findByEmailId", query = "SELECT u FROM UssdMobileSubscriber u WHERE u.emailId = :emailId")})
public class UssdMobileSubscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "account_no")
    private String accountNo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "mobile_no")
    private String mobileNo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "appcode")
    private String appcode;

    @Size(max = 200)
    @Column(name = "name")
    private String name;
    @Column(name = "active")
    private Boolean active;
    @Size(max = 255)
    @Column(name = "bank_code")
    private String bankCode;
    @Size(max = 100)
    @Column(name = "pin")
    private String pin;   
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Column(name = "lastTranTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastTranTime;
    @Size(max = 255)
    @Column(name = "f_id")
    private String fId;
    @Size(max = 100)
    @Column(name = "email_id")
    private String emailId;

    public UssdMobileSubscriber() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }

    public String getfId() {
        return fId;
    }

    public void setfId(String fId) {
        this.fId = fId;
    }

     

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

   
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getLastTranTime() {
        return lastTranTime;
    }

    public void setLastTranTime(Date lastTranTime) {
        this.lastTranTime = lastTranTime;
    }

    public String getFId() {
        return fId;
    }

    public void setFId(String fId) {
        this.fId = fId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 71 * hash + Objects.hashCode(this.accountNo);
        hash = 71 * hash + Objects.hashCode(this.mobileNo);
        hash = 71 * hash + Objects.hashCode(this.appcode);
        hash = 71 * hash + Objects.hashCode(this.bankCode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UssdMobileSubscriber other = (UssdMobileSubscriber) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.accountNo, other.accountNo)) {
            return false;
        }
        if (!Objects.equals(this.mobileNo, other.mobileNo)) {
            return false;
        }
        if (!Objects.equals(this.appcode, other.appcode)) {
            return false;
        }
        if (!Objects.equals(this.bankCode, other.bankCode)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UssdMobileSubscriber{" + "id=" + id + ", accountNo=" + accountNo + ", mobileNo=" + mobileNo + ", appcode=" + appcode + ", name=" + name + ", active=" + active + ", bankCode=" + bankCode + ", pin=" + pin + ", created=" + created + ", modified=" + modified + ", lastTranTime=" + lastTranTime + ", fId=" + fId + ", emailId=" + emailId + '}';
    }

     

     

}
