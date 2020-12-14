/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.core.MFDFA;

import org.patronus.termmeta.MFDFAResultsMeta;
import org.patronus.termmeta.MFDFAParamMeta;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.patronus.response.FractalResponseCode;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermInstanceDTO;

import org.patronus.core.dto.FractalDTO;
import org.patronus.fractal.db.DAO.DataSeriesDAO;
import org.patronus.fractal.db.DAO.MfdfaresultsDAO;
import org.patronus.fractal.db.JPA.exceptions.PreexistingEntityException;
import org.patronus.fractal.db.entities.Mfdfaresults;
import org.patronus.fractal.db.entities.MfdfaresultsPK;
import org.patronus.fractal.core.util.DatabaseConnection;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.dto.MFDFAResultDTO;
import org.patronus.fluctuations.utils.LogUtil;
import org.patronus.fluctuations.results.MFDFAResults;
import org.patronus.fluctuations.data.MultiFractalSpectrum;
import org.patronus.fluctuations.data.ScaleMappedFluctuations;
import org.patronus.fluctuations.core.Fluctuations;
import org.patronus.fluctuations.core.Fluctuations1D;
import org.patronus.core.dto.DFAResultDTO;
import org.patronus.fractal.db.DAO.DFAResultsDAO;
import org.patronus.fractal.db.entities.Dfaresults;
import org.patronus.fractal.db.entities.DfaresultsPK;

/**
 *
 * @author bhaduri
 */
public class MFDFACalcService {

    public MFDFACalcService() {

    }

    public FractalDTO deleteMFDFAResults(FractalDTO fractalDTO) {
        //delete mfdfa results data
        Map<String, Object> selectedMFDFATermInstance = fractalDTO.getFractalTermInstance();
        String mfdfaTermInstanceSlug = (String) selectedMFDFATermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        int response = mfdfaresultsDAO.deleteMfdfaResultsById(mfdfaTermInstanceSlug);

        fractalDTO.setResponseCode(response);
        if ((response != FractalResponseCode.SUCCESS) && (response != FractalResponseCode.DB_NON_EXISTING)) {
            return fractalDTO;
        }
        DFAResultsDAO dfaResultsDAO = new DFAResultsDAO(DatabaseConnection.EMF);
        response = dfaResultsDAO.deleteDfaResultsById(mfdfaTermInstanceSlug);
        fractalDTO.setResponseCode(response);
        if ((response != FractalResponseCode.SUCCESS) && (response != FractalResponseCode.DB_NON_EXISTING)) {
            return fractalDTO;
        }
        
        //delete mfdfa term instance
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        Map<String, Object> fractalTermInstance = fractalDTO.getFractalTermInstance();
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        String termSlug = (String) fractalTermInstance.get(CMSConstants.TERM_SLUG);
        String termInstanceSlug = (String) fractalTermInstance.get(CMSConstants.TERM_INSTANCE_SLUG);
        termInstanceDTO.setTermSlug(termSlug);
        termInstanceDTO.setTermInstanceSlug(termInstanceSlug);
        termInstanceDTO = cmscs.deleteTermInstance(termInstanceDTO);

        response = termInstanceDTO.getResponseCode();
        fractalDTO.setResponseCode(response);
        if (response != FractalResponseCode.SUCCESS) {
            return fractalDTO;
        }
        return fractalDTO;
    }

    public FractalDTO calculateMFDFA(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(dataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedDataSeries = termInstanceDTO.getTermInstance();

        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_MFDFA_PARAM);
        termInstanceDTO.setTermInstanceSlug(mfdfaParamSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedmfdfaParamData = termInstanceDTO.getTermInstance();

        Map<String, Object> mfdfaCalcResults = new HashMap<>();
        int dataseriesId = Integer.parseInt((String) selectedDataSeries.get("id"));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        Integer scaleMin;
        Integer scaleMax;
        Integer scaleNumber;
        Double rejectCut;
        Double logBase;

        scaleMin = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MIN));
        scaleMax = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MAX));
        scaleNumber = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_NUMBER));
        rejectCut = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.REJECT_CUT));
        logBase = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.LOG_BASE));
        LogUtil.setLogBase(logBase);
        //get cumulative series
        List<Double> InputTimeSeries = dataSeriesDao.getDataSeriesById(dataseriesId).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        //Starting MFDFA Calculation...................
//        CalculateFq FqVal = new CalculateFq(scaleMax, scaleMin, scaleNumber, InputTimeSeries);
//        CalculateFD FDVal = new CalculateFD(FqVal.getFqMatrix(), FqVal.getFVector(), FqVal.getQLinSpace(), FqVal.getExpLinSpace(), rejectCut);
//        CalculateDq DqVal = new CalculateDq(FDVal.gethq(), FDVal.getTq(), FqVal.getQLinSpace());
        Fluctuations fq = new Fluctuations1D(InputTimeSeries, scaleMax, scaleMin, rejectCut, scaleNumber);
        MFDFAResults mfdfaResults = fq.getMFDFAResults();
        //Ending MFDFA Calculation...................
        //Retrieve MFDFA Calculation results................
        Double hurstExponent = mfdfaResults.getHurstExponent();
        Double hurstExponentSE = mfdfaResults.getHurstExponentSE();
        Double rSquaredVal = mfdfaResults.getHurstExponentRSquare();
        Double chiSquaredVal = mfdfaResults.getHurstExponentChiSquare();
        Double multiFractalWidth = mfdfaResults.getMultiFractalSpectrumWidth();
        List<ScaleMappedFluctuations> scaleMappedFluctuations = mfdfaResults.getScaleMappedFluctuationsList();

        //scaleMappedFluctuations.forEach(scm -> System.out.println(scm.getScale() + "," + scm.getQuadraticMeanOfFluctuations()));
        DecimalFormat df = new DecimalFormat("####0.00");
        mfdfaCalcResults.put(MFDFAResultsMeta.DATASERIES, dataSeriesSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.QUEUED, "No");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT, df.format(hurstExponent));
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT_SE, df.format(hurstExponentSE));
        mfdfaCalcResults.put(MFDFAResultsMeta.MUILTI_FRACTAL_WIDTH, df.format(multiFractalWidth));
        mfdfaCalcResults.put(MFDFAResultsMeta.R_SQUARED_VAL, df.format(rSquaredVal));
        mfdfaCalcResults.put(MFDFAResultsMeta.CHI_SQUARED_VAL, df.format(chiSquaredVal));
        String mfdfaTermInstanceSlug = dataSeriesSlug + PatronusConstants.TERM_INSTANCE_SLUG_MFDFA_EXT;
        mfdfaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, mfdfaTermInstanceSlug);
        mfdfaCalcResults.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_MFDFA_CALC);
        //List<Map<String, Object>> mfdfaCalcMeta = mts.getTermMetaList(PatronusConstants.TERM_SLUG_MFDFA_CALC);

        int response = storeDFAResults(scaleMappedFluctuations, mfdfaTermInstanceSlug);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        }
        response = storeMFDFAResults(mfdfaResults.getMultiFractalSpectrumList(), mfdfaTermInstanceSlug);
        if (response != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(response);
            return fractalDTO;
        }
        //dBResponse = mts.saveTermInstance(mfdfaCalcMeta, mfdfaCalcResults);
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdfaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdfaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO queueMFDFACalculation(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String dataSeriesSlug = fractalDTO.getDataSeriesSlug();
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        Map<String, Object> mfdfaCalcResults = new HashMap<>();
        mfdfaCalcResults.put(MFDFAResultsMeta.DATASERIES, dataSeriesSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdfaCalcResults.put(MFDFAResultsMeta.QUEUED, "Yes");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.HURST_EXPONENT_SE, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.MUILTI_FRACTAL_WIDTH, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.R_SQUARED_VAL, "--");
        mfdfaCalcResults.put(MFDFAResultsMeta.CHI_SQUARED_VAL, "--");
        String mfdfaTermInstanceSlug = dataSeriesSlug + PatronusConstants.TERM_INSTANCE_SLUG_MFDFA_EXT;
        mfdfaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, mfdfaTermInstanceSlug);
        mfdfaCalcResults.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_MFDFA_CALC);
        //List<Map<String, Object>> mfdfaCalcMeta = mts.getTermMetaList(PatronusConstants.TERM_SLUG_MFDFA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdfaCalcMeta, mfdfaCalcResults);
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdfaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdfaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        fractalDTO.setFractalTermInstance(mfdfaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    private int storeDFAResults(List<ScaleMappedFluctuations> scaleMappedFluctuations, String mfdfaTermInstanceSlug) {
        int response;
        DFAResultsDAO dfaresultsDAO = new DFAResultsDAO(DatabaseConnection.EMF);
        for (int counter = 0; counter < scaleMappedFluctuations.size(); counter++) {
            DfaresultsPK dfaresultsPK = new DfaresultsPK(mfdfaTermInstanceSlug, counter);
            Dfaresults dfaresults = new Dfaresults(dfaresultsPK);
            dfaresults.setScale(scaleMappedFluctuations.get(counter).getScale());
            dfaresults.setFluctuation(scaleMappedFluctuations.get(counter).getQuadraticMeanOfFluctuations());

            try {
                dfaresultsDAO.create(dfaresults);
            } catch (PreexistingEntityException ex) {
                try {
                    dfaresultsDAO.edit(dfaresults);
                } catch (Exception ex1) {
                    Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex1);
                    response = FractalResponseCode.DB_SEVERE;
                    return response;
                }
            } catch (Exception ex) {
                Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex);
                response = FractalResponseCode.DB_SEVERE;
                return response;
            }
        }
        return FractalResponseCode.SUCCESS;
    }

    private int storeMFDFAResults(List<MultiFractalSpectrum> multiFractalSpectrums, String mfdfaTermInstanceSlug) {
        int response;
        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        for (int counter = 0; counter < multiFractalSpectrums.size(); counter++) {
            MfdfaresultsPK mfdfaresultsPK = new MfdfaresultsPK(mfdfaTermInstanceSlug, counter);
            Mfdfaresults mfdfaresults = new Mfdfaresults(mfdfaresultsPK);

            mfdfaresults.setHq(multiFractalSpectrums.get(counter).getHq());
            mfdfaresults.setDq(multiFractalSpectrums.get(counter).getDq());
            try {
                mfdfaresultsDAO.create(mfdfaresults);
            } catch (PreexistingEntityException ex) {
                try {
                    mfdfaresultsDAO.edit(mfdfaresults);
                } catch (Exception ex1) {
                    Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex1);
                    response = FractalResponseCode.DB_SEVERE;
                    return response;
                }
            } catch (Exception ex) {
                Logger.getLogger(MFDFACalcService.class.getName()).log(Level.SEVERE, null, ex);
                response = FractalResponseCode.DB_SEVERE;
                return response;
            }
        }
        return FractalResponseCode.SUCCESS;
    }

    public FractalDTO getMfdfaResults(FractalDTO fractalDTO) {
        Map<String, Object> mfdfaResultInstance = fractalDTO.getFractalTermInstance();
        String mfdfaTermInstanceSlug = (String) mfdfaResultInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        MfdfaresultsDAO mfdfaresultsDAO = new MfdfaresultsDAO(DatabaseConnection.EMF);
        List<Mfdfaresults> mfdfaresultsList = mfdfaresultsDAO.findResultsByID(mfdfaTermInstanceSlug);
        List<MFDFAResultDTO> mfdfaResultDTOList = mfdfaresultsList.stream().map(mfdfaresults -> {
            MFDFAResultDTO mfdfaResultDTO = new MFDFAResultDTO();
            mfdfaResultDTO.setMfdfaresulsslug(mfdfaresults.getMfdfaresultsPK().getMfdfaresultsslug());
            mfdfaResultDTO.setResultid(mfdfaresults.getMfdfaresultsPK().getResultid());
            mfdfaResultDTO.setHq(mfdfaresults.getHq());
            mfdfaResultDTO.setDq(mfdfaresults.getDq());
            return mfdfaResultDTO;
        }).collect(Collectors.toList());
        fractalDTO.setMfdfaResultDTOs(mfdfaResultDTOList);
        return fractalDTO;
    }

    public FractalDTO getDfaResults(FractalDTO fractalDTO) {
        Map<String, Object> mfdfaResultInstance = fractalDTO.getFractalTermInstance();
        String mfdfaTermInstanceSlug = (String) mfdfaResultInstance.get(CMSConstants.TERM_INSTANCE_SLUG);

        DFAResultsDAO dfaresultsDAO = new DFAResultsDAO(DatabaseConnection.EMF);
        List<Dfaresults> dfaresultsList = dfaresultsDAO.findResultsByID(mfdfaTermInstanceSlug);
        List<DFAResultDTO> dfaResultDTOList = dfaresultsList.stream().map(dfaresults -> {
            DFAResultDTO dfaResultDTO = new DFAResultDTO();
            dfaResultDTO.setMfdfaresulsslug(dfaresults.getDfaresultsPK().getMfdfaresultsslug());
            dfaResultDTO.setResultid(dfaresults.getDfaresultsPK().getResultid());
            dfaResultDTO.setScale(dfaresults.getScale());
            dfaResultDTO.setFluctuations(dfaresults.getFluctuation());
            return dfaResultDTO;
        }).collect(Collectors.toList());
        fractalDTO.setDfaResultDTOs(dfaResultDTOList);
        return fractalDTO;
    }
}
