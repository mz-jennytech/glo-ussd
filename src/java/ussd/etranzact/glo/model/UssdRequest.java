/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.model;

import com.etz.ussd.dto.Data;
import com.etz.ussd.session.USSDSession;
import java.io.Serializable;
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
import ussd.etranzact.glo.utils.DateTimeUtil;

/**
 *
 * @author Damilola.Omowaye
 */
@Entity
@Table(name = "ussd_request")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UssdRequest.findAll", query = "SELECT u FROM UssdRequest u")
    , @NamedQuery(name = "UssdRequest.findById", query = "SELECT u FROM UssdRequest u WHERE u.id = :id")
    , @NamedQuery(name = "UssdRequest.findByAppid", query = "SELECT u FROM UssdRequest u WHERE u.appid = :appid")
    , @NamedQuery(name = "UssdRequest.findByCreated", query = "SELECT u FROM UssdRequest u WHERE u.created = :created")
    , @NamedQuery(name = "UssdRequest.findByMessage", query = "SELECT u FROM UssdRequest u WHERE u.message = :message")
    , @NamedQuery(name = "UssdRequest.findByMobileNo", query = "SELECT u FROM UssdRequest u WHERE u.mobileNo = :mobileNo")
    , @NamedQuery(name = "UssdRequest.findByProvider", query = "SELECT u FROM UssdRequest u WHERE u.provider = :provider")
    , @NamedQuery(name = "UssdRequest.findByUniqueTransid", query = "SELECT u FROM UssdRequest u WHERE u.uniqueTransid = :uniqueTransid")})
public class UssdRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Size(max = 255)
    @Column(name = "appid")
    private String appid;
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Size(max = 255)
    @Column(name = "message")
    private String message;
    @Size(max = 255)
    @Column(name = "mobile_no")
    private String mobileNo;
    @Size(max = 50)
    @Column(name = "provider")
    private String provider;
    @Size(max = 50)
    @Column(name = "unique_transid")
    private String uniqueTransid;

    public UssdRequest() {
    }
    
    
    
   public UssdRequest(USSDSession session, Data data) {
      this.mobileNo = session.getMsisdn();
      this.provider = data.getProvider();
      this.message = session.getSessionId() + "|" + data.getMessage();
      this.created = DateTimeUtil.getCurrentDate();
      this.appid = "GLO805";
   }

   public UssdRequest(USSDSession session, Data data, String ref) {
      this.mobileNo = session.getMsisdn();
      this.provider = data.getProvider();
      this.message = session.getSessionId() + "|" + data.getMessage() + "|" + ref;
      this.created = DateTimeUtil.getCurrentDate();
      this.appid = "GLO805";

   }

    public UssdRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUniqueTransid() {
        return uniqueTransid;
    }

    public void setUniqueTransid(String uniqueTransid) {
        this.uniqueTransid = uniqueTransid;
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
        if (!(object instanceof UssdRequest)) {
            return false;
        }
        UssdRequest other = (UssdRequest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ussd.etranzact.glo.model.UssdRequest[ id=" + id + " ]";
    }
    
}
