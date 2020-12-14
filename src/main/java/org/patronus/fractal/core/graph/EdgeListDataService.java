/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.core.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hedwig.leviosa.constants.CMSConstants;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.dto.FractalDTO;
import org.patronus.fractal.core.util.DatabaseConnection;
import org.patronus.fractal.db.DAO.EdgeListDAO;
import org.patronus.response.FractalResponseCode;
import org.patronus.networkcore.AssortitivityCoefficient;
import org.patronus.networkcore.AverageClusteringCoefficient;
import org.patronus.networkcore.AverageShortestPath;
import org.patronus.termmeta.GraphMeta;
import org.patronus.termmeta.NetworkStatsMeta;

/**
 *
 * @author dgrfi
 */
public class EdgeListDataService {

    public FractalDTO importPSVGGraph(FractalDTO fractalDTO) {

        Map<String, Object> graphTermInstance = fractalDTO.getFractalTermInstance();
        String edgeLengthType = (String) graphTermInstance.get("edgeLengthTypeForImport");
        String importFromVGSlug = (String) graphTermInstance.get("importFromVGInstanceSlug");

        //generate graph terminstance slug for import 
        String graphTermInstanceSlug = importFromVGSlug.replace(PatronusConstants.TERM_INSTANCE_SLUG_PSVG_EXT, PatronusConstants.TERM_INSTANCE_SLUG_GRAPH_EXT);
        //copy graph data from VG
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);

        int response = edgeListDAO.deleteEdgeList(graphTermInstanceSlug);

        if (edgeLengthType.equals("real")) {
            response = edgeListDAO.importVGGraphEdgeListReal(importFromVGSlug, graphTermInstanceSlug);
            if (response != FractalResponseCode.SUCCESS) {
                fractalDTO.setResponseCode(FractalResponseCode.DB_SEVERE);
                return fractalDTO;
            }
        } else {
            response = edgeListDAO.importVGGraphEdgeListHorizontal(importFromVGSlug, graphTermInstanceSlug);
            if (response != FractalResponseCode.SUCCESS) {
                fractalDTO.setResponseCode(FractalResponseCode.DB_SEVERE);
                return fractalDTO;
            }
        }
        //we do not need the edge length type of the visibility graph any more.
        // and also from where it is imported.

        graphTermInstance.remove("edgeLengthTypeForImport");
        graphTermInstance.remove("importFromVGInstanceSlug");

        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());

        graphTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_GRAPH);
        graphTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, graphTermInstanceSlug);

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_GRAPH);
        termInstanceDTO.setTermInstanceSlug(graphTermInstanceSlug);
        termInstanceDTO.setTermInstance(graphTermInstance);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(graphTermInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO deleteGraph(FractalDTO fractalDTO) {
        Map<String, Object> graphTermInstance = fractalDTO.getFractalTermInstance();
        String graphTermInstanceSlug = (String) graphTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_GRAPH);
        termInstanceDTO.setTermInstanceSlug(graphTermInstanceSlug);
        termInstanceDTO.setTermInstance(graphTermInstance);

        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);
        int response = edgeListDAO.deleteEdgeList(graphTermInstanceSlug);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        }
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO calculateNetworkStats(FractalDTO fractalDTO) {
        System.out.println("Gheu gheu");
        Map<String, Object> networkStatsTermInstance = fractalDTO.getFractalTermInstance();
        String graphTermInstanceSlug = (String) networkStatsTermInstance.get(NetworkStatsMeta.GRAPH);
        String networkCalculationType = (String) networkStatsTermInstance.get("calctype");
        String networkStatsTermInstanceSlug = graphTermInstanceSlug.replaceAll(PatronusConstants.TERM_INSTANCE_SLUG_GRAPH_EXT, PatronusConstants.TERM_INSTANCE_SLUG_NETWORK_STATS_EXT);
        

        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        //check if term instance is already existing
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_NETWORK_STATS);
        termInstanceDTO.setTermInstanceSlug(networkStatsTermInstanceSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);

        if (termInstanceDTO.getResponseCode() == FractalResponseCode.SUCCESS) {
            Map<String, Object> existingNetworkStatsTermInstance = termInstanceDTO.getTermInstance();
            if (existingNetworkStatsTermInstance.get(NetworkStatsMeta.AVERAGE_CLUSTERING_COEFF) != null) {
                networkStatsTermInstance.put(NetworkStatsMeta.AVERAGE_CLUSTERING_COEFF, existingNetworkStatsTermInstance.get(NetworkStatsMeta.AVERAGE_CLUSTERING_COEFF));
            }
            if (existingNetworkStatsTermInstance.get(NetworkStatsMeta.HETEROGENEITY) != null) {
                networkStatsTermInstance.put(NetworkStatsMeta.HETEROGENEITY, existingNetworkStatsTermInstance.get(NetworkStatsMeta.HETEROGENEITY));
            }
            if (existingNetworkStatsTermInstance.get(NetworkStatsMeta.ASSORTITIVITY_COEFFICIENT) != null) {
                networkStatsTermInstance.put(NetworkStatsMeta.ASSORTITIVITY_COEFFICIENT, existingNetworkStatsTermInstance.get(NetworkStatsMeta.ASSORTITIVITY_COEFFICIENT));
            }
            if (existingNetworkStatsTermInstance.get(NetworkStatsMeta.AVERAGE_SHORTEST_PATH) != null) {
                networkStatsTermInstance.put(NetworkStatsMeta.AVERAGE_SHORTEST_PATH, existingNetworkStatsTermInstance.get(NetworkStatsMeta.AVERAGE_SHORTEST_PATH));
            }

        }

        networkStatsTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_NETWORK_STATS);
        networkStatsTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, networkStatsTermInstanceSlug);
        if (networkCalculationType.equals(NetworkStatsMeta.AVERAGE_CLUSTERING_COEFF)) {
            String edgeListFilePath  = dumpEdgeListToFile(graphTermInstanceSlug);
            Double acc = new AverageClusteringCoefficient(edgeListFilePath).getAverageClustertingCoeffient();
            networkStatsTermInstance.put(NetworkStatsMeta.AVERAGE_CLUSTERING_COEFF, Double.toString(acc));
            deleteFile(edgeListFilePath);
        } else if (networkCalculationType.equals(NetworkStatsMeta.ASSORTITIVITY_COEFFICIENT)) {
            String edgeListFilePath  = dumpEdgeListToFile(graphTermInstanceSlug);
            Double acc = new AssortitivityCoefficient(edgeListFilePath).getAssortitivityCoeffient();
            networkStatsTermInstance.put(NetworkStatsMeta.ASSORTITIVITY_COEFFICIENT, Double.toString(acc));
            deleteFile(edgeListFilePath);
        } else if (networkCalculationType.equals(NetworkStatsMeta.AVERAGE_SHORTEST_PATH)) {
            String edgeListFilePath  = dumpEdgeListToFile(graphTermInstanceSlug);
            Double acc = new AverageShortestPath(edgeListFilePath).getAverageShortestPath();
            networkStatsTermInstance.put(NetworkStatsMeta.AVERAGE_SHORTEST_PATH, Double.toString(acc));
        } else if (networkCalculationType.equals(NetworkStatsMeta.HETEROGENEITY)) {
            networkStatsTermInstance.put(NetworkStatsMeta.HETEROGENEITY, "0.6");
        }

        networkStatsTermInstance.remove("calctype");

        termInstanceDTO.setTermInstance(networkStatsTermInstance);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(networkStatsTermInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }
    private String dumpEdgeListToFile(String networkStatsTermInstanceSlug) {
        String presentWorkingDirectory = System.getProperty("user.dir");
        String outputFileName = presentWorkingDirectory+File.separator+networkStatsTermInstanceSlug;
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);
        edgeListDAO.dumpGraphEdgeList(outputFileName, networkStatsTermInstanceSlug);
        return outputFileName;
    }
    private void deleteFile (String filePath) {
        File file = new File(filePath);
        if(file.delete()){
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.SEVERE, "Temp file {0} is deleted", filePath);
        } else {
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.SEVERE, "Temp file {0} does not exist", filePath);
        }
    }
    public FractalDTO deleteNetworkStats(FractalDTO fractalDTO) {
        Map<String, Object> networkStatsTermInstance = fractalDTO.getFractalTermInstance();

        String networkStatsTermInstanceSlug = (String) networkStatsTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        String networkStatsTermSlug = (String) networkStatsTermInstance.get(CMSConstants.TERM_SLUG);

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(networkStatsTermSlug);
        termInstanceDTO.setTermInstanceSlug(networkStatsTermInstanceSlug);

        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);

        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO uploadGraph(FractalDTO fractalDTO) {
        String uploadedFilePath = fractalDTO.getCsvFilePath();
        Map<String, Object> graphTermInstance = fractalDTO.getFractalTermInstance();
        String graphTermInstanceSlug = createGraphTermSlug();
        String graphName = (String) graphTermInstance.get(GraphMeta.NAME);
        System.out.println("Gheuuuu " + uploadedFilePath + " " + graphName + " " + graphTermInstanceSlug);
        String dbLoadFileName = createGraphUploadFile(uploadedFilePath, graphTermInstanceSlug);
        if (dbLoadFileName == null) {
            fractalDTO.setResponseCode(FractalResponseCode.DATA_SERIES_SEVERE);
            return fractalDTO;
        }
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);
        int noOfInsertedRows = edgeListDAO.loadGraphEdges(dbLoadFileName);
        if (noOfInsertedRows == 0) {
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.SEVERE, "Problem loading table from temp file");
            fractalDTO.setResponseCode(FractalResponseCode.DATA_SERIES_UPDATE_ERROR);
            return fractalDTO;
        } else {
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.INFO, "Loaded {0} edges", noOfInsertedRows);
        }
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());

        graphTermInstance.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_GRAPH);
        graphTermInstance.put(CMSConstants.TERM_INSTANCE_SLUG, graphTermInstanceSlug);

        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_GRAPH);
        termInstanceDTO.setTermInstanceSlug(graphTermInstanceSlug);
        termInstanceDTO.setTermInstance(graphTermInstance);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(graphTermInstance);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
        
    }

    private String createGraphTermSlug() {
        //String graphTermInstanceSlug = "00001"+PatronusConstants.TERM_INSTANCE_SLUG_GRAPH_EXT+"UPLD";
        //String graphSeqPrefix = graphTermInstanceSlug.substring(0, 5);
        EdgeListDAO edgeListDAO = new EdgeListDAO(DatabaseConnection.EMF);
        String graphMaxPrefix = edgeListDAO.getmaxUploadSlug();
        Integer graphMaxId = Integer.parseInt(graphMaxPrefix);
        String graphSeqPrefix = String.format("%05d", (graphMaxId + 1));
        String graphTermInstanceSlug = graphSeqPrefix + PatronusConstants.TERM_INSTANCE_SLUG_GRAPH_EXT + "UPLD";
        System.out.println(graphTermInstanceSlug);
        return graphTermInstanceSlug;
    }

    private String createGraphUploadFile(String inputFileName, String graphTermInstanceSlug) {
        String outputFileName = inputFileName + "bak";
        BufferedReader reader;
        PrintWriter writer;
        try {
            reader = new BufferedReader(new FileReader(inputFileName));
            writer = new PrintWriter(outputFileName, "UTF-8");
            String line = reader.readLine();
            String outPutLine;
            while (line != null) {
                outPutLine = graphTermInstanceSlug + "," + line;
                writer.println(outPutLine);
                // read next line
                line = reader.readLine();
                
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            Logger.getLogger(EdgeListDataService.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        return outputFileName;
    }
}
