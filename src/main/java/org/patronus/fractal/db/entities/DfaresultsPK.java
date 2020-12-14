/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.db.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author bhaduri
 */
@Embeddable
public class DfaresultsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "mfdfaresultsslug")
    private String mfdfaresultsslug;
    @Basic(optional = false)
    @Column(name = "resultid")
    private int resultid;

    public DfaresultsPK() {
    }

    public DfaresultsPK(String mfdfaresultsslug, int resultid) {
        this.mfdfaresultsslug = mfdfaresultsslug;
        this.resultid = resultid;
    }

    public String getMfdfaresultsslug() {
        return mfdfaresultsslug;
    }

    public void setMfdfaresultsslug(String mfdfaresultsslug) {
        this.mfdfaresultsslug = mfdfaresultsslug;
    }

    public int getResultid() {
        return resultid;
    }

    public void setResultid(int resultid) {
        this.resultid = resultid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mfdfaresultsslug != null ? mfdfaresultsslug.hashCode() : 0);
        hash += (int) resultid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DfaresultsPK)) {
            return false;
        }
        DfaresultsPK other = (DfaresultsPK) object;
        if ((this.mfdfaresultsslug == null && other.mfdfaresultsslug != null) || (this.mfdfaresultsslug != null && !this.mfdfaresultsslug.equals(other.mfdfaresultsslug))) {
            return false;
        }
        if (this.resultid != other.resultid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.fractal.db.entities.DfaresultsPK[ mfdfaresultsslug=" + mfdfaresultsslug + ", resultid=" + resultid + " ]";
    }
    
}
