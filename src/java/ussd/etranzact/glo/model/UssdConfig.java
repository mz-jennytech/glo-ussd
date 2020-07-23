/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author HP
 */
@Entity
@Table(name = "ussd_config")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UssdConfig.findAll", query = "SELECT u FROM UssdConfig u"),
    @NamedQuery(name = "UssdConfig.findById", query = "SELECT u FROM UssdConfig u WHERE u.id = :id"),
    @NamedQuery(name = "UssdConfig.findByParameterName", query = "SELECT u FROM UssdConfig u WHERE u.parameterName = :parameterName"),
    @NamedQuery(name = "UssdConfig.findByParameterValue", query = "SELECT u FROM UssdConfig u WHERE u.parameterValue = :parameterValue")})
public class UssdConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 255)
    @Column(name = "parameter_name")
    private String parameterName;
    @Size(max = 255)
    @Column(name = "parameter_value")
    private String parameterValue;

    public UssdConfig() {
    }

    public UssdConfig(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
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
        if (!(object instanceof UssdConfig)) {
            return false;
        }
        UssdConfig other = (UssdConfig) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ussd.etranzact.fuel.model.UssdConfig[ id=" + id + " ]";
    }
    
}
