/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.db.DAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.patronus.fractal.core.graph.EdgeListDataService;
import org.patronus.fractal.db.JPA.EdgelistJpaController;
import org.patronus.fractal.db.entities.Dataseries;
import org.patronus.fractal.db.entities.Edgelist;
import org.patronus.response.FractalResponseCode;

/**
 *
 * @author dgrfi
 */
public class EdgeListDAO extends EdgelistJpaController {

    public EdgeListDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public int importVGGraphEdgeListReal(String psvgResultsTermInstanceSlug, String graphTermInstanceSlug) {

        String nativeQueryString = "insert into edgelist (graphslug, node, adjnode, edgelength) \n"
                + "select ?1, node, adjnode, realedgelength from vgadjacency where\n"
                + "psvgresultsslug = ?2";

        EntityManager em = getEntityManager();

        EntityTransaction entr = em.getTransaction();
        Query query = em.createNativeQuery(nativeQueryString);
        query.setParameter(1, graphTermInstanceSlug);
        query.setParameter(2, psvgResultsTermInstanceSlug);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        entr.commit();
        int response;
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }

        return response;

    }
    public int importVGGraphEdgeListHorizontal(String psvgResultsTermInstanceSlug, String graphTermInstanceSlug) {

        String nativeQueryString = "insert into edgelist (graphslug, node, adjnode, edgelength) \n"
                + "select ?1, node, adjnode, hedgelength from vgadjacency where\n"
                + "psvgresultsslug = ?2";

        EntityManager em = getEntityManager();

        EntityTransaction entr = em.getTransaction();
        Query query = em.createNativeQuery(nativeQueryString);
        query.setParameter(1, graphTermInstanceSlug);
        query.setParameter(2, psvgResultsTermInstanceSlug);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        entr.commit();
        int response;
        if (executeUpdate == 0) {
            response = FractalResponseCode.DB_NON_EXISTING;
        } else {
            response = FractalResponseCode.SUCCESS;
        }

        return response;

    }
    public int deleteEdgeList(String graphTermInstanceSlug) {
        int response;
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        Query query = em.createNamedQuery("Edgelist.deleteBySlug");
        query.setParameter("graphTermInstanceSlug", graphTermInstanceSlug);
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
    public String getmaxUploadSlug() {
        EntityManager em = getEntityManager();
        TypedQuery<String> query = em.createNamedQuery("Edgelist.getMaxSlug", String.class);
        
        String maxUploadSlug = query.getSingleResult();
        if (maxUploadSlug == null) {
            maxUploadSlug = "00000";
        }
        return maxUploadSlug;
    }
    public int loadGraphEdges(String fileName) {
        
        EntityManager em = getEntityManager();
        EntityTransaction entr = em.getTransaction();
        String nativeQueryString = "LOAD DATA LOCAL INFILE '" + fileName + "' INTO TABLE edgelist FIELDS TERMINATED BY ',' ";
        Query query = em.createNativeQuery(nativeQueryString);
        //query.setParameter("fileName", fileName);
        entr.begin();
        int executeUpdate = query.executeUpdate();
        
        
        entr.commit();
        return executeUpdate;
    }
    public void dumpGraphEdgeList(String outputFileName,String networkStatsTermInstanceSlug) {
        EntityManager em = getEntityManager();
        TypedQuery<Edgelist> query = em.createNamedQuery("Edgelist.findByTermInstanceSlug", Edgelist.class);
        query.setParameter("graphTermInstanceSlug", networkStatsTermInstanceSlug);
        List<Edgelist> EdgelistList = query.getResultList();
        PrintWriter writer;
        try { 
            writer = new PrintWriter(outputFileName, "UTF-8");
            for (Edgelist e : EdgelistList) {
                String outPutLine = e.getEdgelistPK().getNode()+","+e.getEdgelistPK().getAdjnode()+","+e.getEdgelength();
                writer.println(outPutLine);
            }
            writer.close();
        } catch (IOException e) {
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.SEVERE, null, e);
            
        }
            
    }
}
