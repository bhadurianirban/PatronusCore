/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.core.IPSVG;

import java.util.ArrayList;
import java.util.List;
import org.patronus.fractal.core.PSVG.VisibilityGraphUniformTS;

/**
 *
 * @author dgrfv
 */
public class ImpVisibilityDegree {

    private List<Double> InputTimeSeries;
    private List<IPSVGDetails> ImpPSVGList;
    private int PSVGRequiredStart;
    private Double PSVGDataPartFromStart;
    private int maxNodesForCalc;
    private int maxGap;
    private boolean includePSVGInterCept;
    private Double improvedPSVG;
    private Double logBase;
    private String psvgResultsTermInstanceSlug;

    public ImpVisibilityDegree(List<Double> InputTimeSeries, int PSVGRequiredStart, Double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double logBase, int maxGap, String psvgResultsTermInstanceSlug) {
        this.InputTimeSeries = InputTimeSeries;
        this.PSVGRequiredStart = PSVGRequiredStart;
        this.PSVGDataPartFromStart = PSVGDataPartFromStart;
        this.maxNodesForCalc = maxNodesForCalc;
        this.maxGap = maxGap;
        this.includePSVGInterCept = includePSVGInterCept;
        this.logBase = logBase;
        this.psvgResultsTermInstanceSlug = psvgResultsTermInstanceSlug;

    }

    public List<IPSVGDetails> getImpPSVGList() {
        return ImpPSVGList;
    }

    public Double getImprovedPSVG() {
        return improvedPSVG;
    }

    public void calculateVisibilityDegree() {
        ArrayList<Double> ImpPSVGValue = new ArrayList<>();
        ImpPSVGList = new ArrayList<>();
        Double[] PSVGofBrokenTS = new Double[maxGap];
        for (int gapCounter = 0; gapCounter < maxGap; gapCounter++) {
            List<Double> brokenTimeSeries = getGappedTimeSeries(InputTimeSeries, gapCounter + 1);
            VisibilityGraphUniformTS visDegree = new VisibilityGraphUniformTS(brokenTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, 0.0, logBase, psvgResultsTermInstanceSlug);
            visDegree.calculateVisibilityDegree();
            PSVGofBrokenTS[gapCounter] = visDegree.getPSVGFractalDimension();
        }
        for (int gapCounter = 1; gapCounter <= maxGap; gapCounter++) {

            ArrayList<Double> PSVGForGaps = new ArrayList<>();
            for (int moveInGapCounter = 1; moveInGapCounter <= gapCounter; moveInGapCounter++) {
                int storedPSVGCounter = moveInGapCounter - 1;
                if (Double.isNaN(PSVGofBrokenTS[storedPSVGCounter])) {
                    break;
                }
                PSVGForGaps.add(PSVGofBrokenTS[storedPSVGCounter]);

            }

            //Double meanPSVGForAllGaps = calcMeanOfTimeSeries(PSVGForGaps);
            Double meanPSVGForAllGaps = PSVGForGaps.stream().mapToDouble(a -> a).average().getAsDouble();
            IPSVGDetails ImpPSVGdet = new IPSVGDetails(gapCounter, meanPSVGForAllGaps);
            ImpPSVGValue.add(meanPSVGForAllGaps);
            PSVGForGaps.clear();
            ImpPSVGList.add(ImpPSVGdet);
        }
        improvedPSVG = ImpPSVGValue.stream().mapToDouble(a->a).average().getAsDouble();
        //improvedPSVG = calcMeanOfTimeSeries(ImpPSVGValue);
    }

//    private Double calcMeanOfTimeSeries(ArrayList<Double> timeSeries) {
//        Double aggregate = 0.0;
//        for (int i = 0; i < timeSeries.size(); i++) {
//            aggregate = aggregate + timeSeries.get(i);
//        }
//        Double meanOfTimeSeries = aggregate / timeSeries.size();
//
//        return meanOfTimeSeries;
//    }

    private List<Double> getGappedTimeSeries(List<Double> InputTimeSeriesData, int gap) {
        List<Double> gappedTimeSeries = new ArrayList<>();
        int lengthToTraverse = InputTimeSeriesData.size();
        for (int i = 0; i < lengthToTraverse; i = i + gap) {
            gappedTimeSeries.add(InputTimeSeriesData.get(i));
        }
        return gappedTimeSeries;
    }

}
