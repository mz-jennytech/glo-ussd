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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Damilola.Omowaye
 */
@Entity
@Table(name = "ussd_mobile_borrowme_subscriber")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findAll", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findById", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.id = :id")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByMobileNo", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.mobileNo = :mobileNo")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByMinAmount", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.minAmount = :minAmount")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByMaxAmount", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.maxAmount = :maxAmount")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByActive", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.active = :active")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByBvn", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.bvn = :bvn")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByBankCode", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.bankCode = :bankCode")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByCreated", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.created = :created")
    , @NamedQuery(name = "UssdMobileBorrowmeSubscriber.findByModified", query = "SELECT u FROM UssdMobileBorrowmeSubscriber u WHERE u.modified = :modified")})
public class UssdMobileBorrowmeSubscriber implements Serializable {

    @Size(max = 25)
    @Column(name = "bvn")
    private String bvn;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 25)
    @Column(name = "mobile_no")
    private String mobileNo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "min_amount")
    private BigDecimal minAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "max_amount")
    private BigDecimal maxAmount;
    @Column(name = "active")
    private Boolean active;
    @Size(max = 3)
    @Column(name = "bank_code")
    private String bankCode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Basic(optional = false)
    @NotNull
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public UssdMobileBorrowmeSubscriber() {
    }

    public UssdMobileBorrowmeSubscriber(Long id) {
        this.id = id;
    }

    public UssdMobileBorrowmeSubscriber(Long id, BigDecimal minAmount, BigDecimal maxAmount, Date created, Date modified) {
        this.id = id;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.created = created;
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UssdMobileBorrowmeSubscriber)) {
            return false;
        }
        UssdMobileBorrowmeSubscriber other = (UssdMobileBorrowmeSubscriber) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ussd.etranzact.glo.model.UssdMobileBorrowmeSubscriber[ id=" + id + " ]";
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

}
