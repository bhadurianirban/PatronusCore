/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.db.JPA;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.patronus.fractal.db.JPA.exceptions.NonexistentEntityException;
import org.patronus.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.patronus.fractal.db.entities.Dfaresults;
import org.patronus.fractal.db.entities.DfaresultsPK;

/**
 *
 * @author bhaduri
 */
public class DfaresultsJpaController implements Serializable {

    public DfaresultsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Dfaresults dfaresults) throws PreexistingEntityException, Exception {
        if (dfaresults.getDfaresultsPK() == null) {
            dfaresults.setDfaresultsPK(new DfaresultsPK());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(dfaresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findDfaresults(dfaresults.getDfaresultsPK()) != null) {
                throw new PreexistingEntityException("Dfaresults " + dfaresults + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Dfaresults dfaresults) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            dfaresults = em.merge(dfaresults);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                DfaresultsPK id = dfaresults.getDfaresultsPK();
                if (findDfaresults(id) == null) {
                    throw new NonexistentEntityException("The dfaresults with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(DfaresultsPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Dfaresults dfaresults;
            try {
                dfaresults = em.getReference(Dfaresults.class, id);
                dfaresults.getDfaresultsPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The dfaresults with id " + id + " no longer exists.", enfe);
            }
            em.remove(dfaresults);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Dfaresults> findDfaresultsEntities() {
        return findDfaresultsEntities(true, -1, -1);
    }

    public List<Dfaresults> findDfaresultsEntities(int maxResults, int firstResult) {
        return findDfaresultsEntities(false, maxResults, firstResult);
    }

    private List<Dfaresults> findDfaresultsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Dfaresults.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Dfaresults findDfaresults(DfaresultsPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Dfaresults.class, id);
        } finally {
            em.close();
        }
    }

    public int getDfaresultsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Dfaresults> rt = cq.from(Dfaresults.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
