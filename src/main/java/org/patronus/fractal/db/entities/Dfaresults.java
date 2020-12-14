/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.db.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author bhaduri
 */
@Entity
@Table(name = "dfaresults")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Dfaresults.findAll", query = "SELECT d FROM Dfaresults d")})
public class Dfaresults implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DfaresultsPK dfaresultsPK;
    @Basic(optional = false)
    @Column(name = "scale")
    private double scale;
    @Basic(optional = false)
    @Column(name = "fluctuation")
    private double fluctuation;

    public Dfaresults() {
    }

    public Dfaresults(DfaresultsPK dfaresultsPK) {
        this.dfaresultsPK = dfaresultsPK;
    }

    public Dfaresults(DfaresultsPK dfaresultsPK, double scale, double fluctuation) {
        this.dfaresultsPK = dfaresultsPK;
        this.scale = scale;
        this.fluctuation = fluctuation;
    }

    public Dfaresults(String mfdfaresultsslug, int resultid) {
        this.dfaresultsPK = new DfaresultsPK(mfdfaresultsslug, resultid);
    }

    public DfaresultsPK getDfaresultsPK() {
        return dfaresultsPK;
    }

    public void setDfaresultsPK(DfaresultsPK dfaresultsPK) {
        this.dfaresultsPK = dfaresultsPK;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getFluctuation() {
        return fluctuation;
    }

    public void setFluctuation(double fluctuation) {
        this.fluctuation = fluctuation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dfaresultsPK != null ? dfaresultsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dfaresults)) {
            return false;
        }
        Dfaresults other = (Dfaresults) object;
        if ((this.dfaresultsPK == null && other.dfaresultsPK != null) || (this.dfaresultsPK != null && !this.dfaresultsPK.equals(other.dfaresultsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.Dfaresults[ dfaresultsPK=" + dfaresultsPK + " ]";
    }
    
}
