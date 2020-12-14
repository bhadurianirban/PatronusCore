/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.db.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.patronus.fractal.db.JPA.DfaresultsJpaController;
import org.patronus.fractal.db.entities.Dfaresults;
import org.patronus.response.FractalResponseCode;

/**
 *
 * @author bhaduri
 */
public class DFAResultsDAO extends DfaresultsJpaController{

    public DFAResultsDAO(EntityManagerFactory emf) {
        super(emf);
    }
    public int deleteDfaResultsById(String mfdfaTermInstanceSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Dfaresults.deleteResultsByID");
        query.setParameter("mfdfaresultsslug", mfdfaTermInstanceSlug);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }
        entr.commit();
        return response;
    }
    public List<Dfaresults> findResultsByID (String mfdfaTermInstanceSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<Dfaresults> query = em.createNamedQuery("Dfaresults.findResultsByID", Dfaresults.class);
        query.setParameter("mfdfaresultsslug", mfdfaTermInstanceSlug);
        List<Dfaresults> dfaResultsList = query.getResultList();
        return dfaResultsList;
    }
}
