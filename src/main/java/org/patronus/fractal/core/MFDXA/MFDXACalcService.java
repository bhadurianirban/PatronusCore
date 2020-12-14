/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.core.MFDXA;


import org.patronus.termmeta.MFDXAResultsMeta;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.patronus.response.FractalResponseCode;
import org.leviosa.core.driver.LeviosaClientService;
import org.hedwig.leviosa.constants.CMSConstants;
import org.hedwig.cms.dto.TermInstanceDTO;
import org.patronus.fluctuations.core.Fluctuations;
import org.patronus.fluctuations.core.FluctuationsX;
import org.patronus.fluctuations.results.MFDFAResults;
import org.patronus.fractal.db.DAO.DataSeriesDAO;
import org.patronus.termmeta.MFDFAParamMeta;
import org.patronus.core.dto.FractalDTO;
import org.patronus.fractal.core.util.DatabaseConnection;
import org.patronus.constants.PatronusConstants;
import org.patronus.fluctuations.utils.LogUtil;

/**
 *
 * @author dgrfv
 */
public class MFDXACalcService {

    

    public FractalDTO calculateMFDXA(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String firstDataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String secondDataSeriesSlug = fractalDTO.getDataSeriesSlugSecond();
        
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(firstDataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedFirstDataSeries = termInstanceDTO.getTermInstance();

        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_DATASERIES);
        termInstanceDTO.setTermInstanceSlug(secondDataSeriesSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        Map<String, Object> selectedSecondDataSeries = termInstanceDTO.getTermInstance();

        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug(PatronusConstants.TERM_SLUG_MFDFA_PARAM);
        termInstanceDTO.setTermInstanceSlug(mfdfaParamSlug);
        termInstanceDTO = cmscs.getTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }

        Map<String, Object> selectedmfdfaParamData = termInstanceDTO.getTermInstance();

        int dataseriesIdFirst = Integer.parseInt((String) selectedFirstDataSeries.get("id"));
        int dataseriesIdSecond = Integer.parseInt((String) selectedSecondDataSeries.get("id"));
        DataSeriesDAO dataSeriesDao = new DataSeriesDAO(DatabaseConnection.EMF);

        Integer scaleMin;
        Integer scaleMax;
        int scaleNumber;
        Double rejectCut;
        Double logBase;

        scaleMin = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MIN));
        scaleMax = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_MAX));
        scaleNumber = Integer.parseInt((String) selectedmfdfaParamData.get(MFDFAParamMeta.SCALE_NUMBER));
        rejectCut = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.REJECT_CUT));
        logBase = Double.parseDouble((String) selectedmfdfaParamData.get(MFDFAParamMeta.LOG_BASE));
        LogUtil.setLogBase(logBase);
        //get cumulative series
        List<Double> InputTimeSeriesFirst = dataSeriesDao.getDataSeriesById(dataseriesIdFirst).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        List<Double> InputTimeSeriesSecond = dataSeriesDao.getDataSeriesById(dataseriesIdSecond).stream().map(ds -> ds.getYcumulative()).collect(Collectors.toList());
        //Starting MFDXA Calculation...................
        /**
         * Main method for calculating the MFDFA. 1) First this calculates the
         * overall q-order RMS : Fq (matrix of all the q order RMS for different
         * scales) and the monofractal RMS F (vector of all the second order
         * RMS) 2) The q-order Hurst exponent Hq (from singularity exponent
         * hq[]) 3) Singularity dimension Dq
         */
//        CalculateMFDXAFq FqVal = new CalculateMFDXAFq(scaleMax, scaleMin, scaleNumber, InputTimeSeriesFirst, InputTimeSeriesSecond);
//        CalculateMFDXAFD FDVal = new CalculateMFDXAFD(FqVal.getFVector(), rejectCut);
        Fluctuations mfdxa = new FluctuationsX(InputTimeSeriesFirst, InputTimeSeriesSecond, scaleMax, scaleMin, rejectCut, scaleNumber);
        //Ending MFDFA Calculation...................
        //Retrieve MFDFA Calculation results................
        MFDFAResults mfdfaResults = mfdxa.getMFDFAResults();
        Double hurstExponent = mfdfaResults.getHurstExponent();
        Double hurstExponentSE = mfdfaResults.getHurstExponentSE();
        Double rSquaredVal = mfdfaResults.getHurstExponentRSquare();
        Double chiSquaredVal = mfdfaResults.getHurstExponentChiSquare();
        Double gammaX = mfdfaResults.getGammaX();
//        DBResponse dBResponse = storeMFDFAResults(Arrays.asList(FDVal.gethq()), Arrays.asList(DqVal.getDq()), dataseriesId);
//        if (dBResponse.getResponseCode() != DBResponse.SUCCESS) {
//            return null;
//        }
        DecimalFormat df = new DecimalFormat("####0.00");
        Map<String, Object> mfdxaCalcResults = new HashMap<>();
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_FIRST, firstDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_SECOND, secondDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.QUEUED, "No");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT, df.format(hurstExponent));
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT_SE, df.format(hurstExponentSE));
        mfdxaCalcResults.put(MFDXAResultsMeta.GAMMA_X, df.format(gammaX));
        mfdxaCalcResults.put(MFDXAResultsMeta.R_SQUARED_VAL, df.format(rSquaredVal));
        mfdxaCalcResults.put(MFDXAResultsMeta.CHI_SQUARED_VAL, df.format(chiSquaredVal));
        mfdxaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, firstDataSeriesSlug + secondDataSeriesSlug + PatronusConstants.TERM_INSTANCE_SLUG_MFDXA_EXT);
        mfdxaCalcResults.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_MFDXA_CALC);
        //List<Map<String, Object>> mfdxaCalcResultsMeta = mts.getTermMetaList(PatronusConstants.TERM_SLUG_MFDXA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdxaCalcResultsMeta, mfdxaCalcResults);
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdxaCalcResults);
        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdxaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }

    public FractalDTO queueMFDXACalculation(FractalDTO fractalDTO) {
        String mfdfaParamSlug = fractalDTO.getParamSlug();
        String firstDataSeriesSlug = fractalDTO.getDataSeriesSlug();
        String secondDataSeriesSlug = fractalDTO.getDataSeriesSlugSecond();
        Map<String, Object> mfdxaCalcResults = new HashMap<>();
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_FIRST, firstDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.DATASERIES_SECOND, secondDataSeriesSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.MFDFA_PARAM, mfdfaParamSlug);
        mfdxaCalcResults.put(MFDXAResultsMeta.QUEUED, "Yes");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.HURST_EXPONENT_SE, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.GAMMA_X, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.R_SQUARED_VAL, "--");
        mfdxaCalcResults.put(MFDXAResultsMeta.CHI_SQUARED_VAL, "--");
        mfdxaCalcResults.put(CMSConstants.TERM_INSTANCE_SLUG, firstDataSeriesSlug + secondDataSeriesSlug + PatronusConstants.TERM_INSTANCE_SLUG_MFDXA_EXT);
        mfdxaCalcResults.put(CMSConstants.TERM_SLUG, PatronusConstants.TERM_SLUG_MFDXA_CALC);
        LeviosaClientService cmscs = new LeviosaClientService(fractalDTO.getHedwigAuthCredentials().getHedwigServer(),fractalDTO.getHedwigAuthCredentials().getHedwigServerPort());
        //List<Map<String, Object>> mfdxaCalcResultsMeta = mts.getTermMetaList(PatronusConstants.TERM_SLUG_MFDXA_CALC);
        //DBResponse dBResponse = mts.saveTermInstance(mfdxaCalcResultsMeta, mfdxaCalcResults);
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(fractalDTO.getHedwigAuthCredentials());
        termInstanceDTO.setTermSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_SLUG));
        termInstanceDTO.setTermInstanceSlug((String) mfdxaCalcResults.get(CMSConstants.TERM_INSTANCE_SLUG));
        termInstanceDTO.setTermInstance(mfdxaCalcResults);

        termInstanceDTO = cmscs.saveTermInstance(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            fractalDTO.setResponseCode(termInstanceDTO.getResponseCode());
            return fractalDTO;
        }
        fractalDTO.setFractalTermInstance(mfdxaCalcResults);
        fractalDTO.setResponseCode(FractalResponseCode.SUCCESS);
        return fractalDTO;
    }
}
