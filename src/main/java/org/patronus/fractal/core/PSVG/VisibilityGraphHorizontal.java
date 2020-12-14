package org.patronus.fractal.core.PSVG;

import java.util.List;
import org.patronus.fractal.db.entities.Vgadjacency;

public class VisibilityGraphHorizontal extends VisibilityGraph {

    public VisibilityGraphHorizontal(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }

    @Override
    Vgadjacency checkVisibility(int currentNodeIndex, int nodeToCompareIndex) {
        Double currentNodeXVal = Double.valueOf(currentNodeIndex);
        Double currentNodeYVal = (Double) InputTimeSeries.get(currentNodeIndex);

        Double nodeToCompareXVal = Double.valueOf(nodeToCompareIndex);
        Double nodeToCompareYVal = (Double) InputTimeSeries.get(nodeToCompareIndex);
        int inBetweenStartIndex = currentNodeIndex + 1;
        int inBetweenEndIndex = nodeToCompareIndex;
        //List<?> seriesInBetween = InputTimeSeries.subList(currentNodeIndex + 1, nodeToCompareIndex);
        //মাঝখানের গ্যাপ যদি 1 হয় তাহলে এই লিস্টের সাইজ ০ হবে সেক্ষেত্রে এই লুপের মধ্যে না ঢুকেই ট্রু রিটার্ন করবে 

        for (int i = inBetweenStartIndex; i < inBetweenEndIndex; i++) {
            //Double inBetweenNodeXVal = Double.valueOf(currentNodeIndex + i + 1);
            Double inBetweenNodeYVal = (Double) InputTimeSeries.get(i);

            //Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            //Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;
            if ((inBetweenNodeYVal < currentNodeYVal) && (inBetweenNodeYVal < nodeToCompareYVal)) {

            } else {
                return null;
            }
        }
        Vgadjacency vgadjacency = gatherEdgeDetails(currentNodeIndex, nodeToCompareIndex, currentNodeXVal, currentNodeYVal, nodeToCompareXVal, nodeToCompareYVal);
        return vgadjacency;
    }

}
