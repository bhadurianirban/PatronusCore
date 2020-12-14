package org.patronus.fractal.core.PSVG;

import java.util.List;
import org.patronus.fractal.db.entities.Vgadjacency;

public class VisibilityGraphUniformTS extends VisibilityGraph {

    public VisibilityGraphUniformTS(List<?> InputTimeSeries, int PSVGRequiredStart, double PSVGDataPartFromStart, boolean includePSVGInterCept, int maxNodesForCalc, Double rejectCut, double logBase, String psvgResultsTermInstanceSlug) {
        super(InputTimeSeries, PSVGRequiredStart, PSVGDataPartFromStart, includePSVGInterCept, maxNodesForCalc, rejectCut, logBase, psvgResultsTermInstanceSlug);
    }

//    @Override
//    void createVGEdges() {
//        int totalNodes = InputTimeSeries.size();
//        int maxNodesForCalc = MAX_NODES_FOR_CALC;
//        if (InputTimeSeries.size() < MAX_NODES_FOR_CALC) {
//            maxNodesForCalc = InputTimeSeries.size();
//        }
//
//        for (int nodeGap = 1; nodeGap < maxNodesForCalc; nodeGap++) {
//            for (int currentNodeIndex = 0; currentNodeIndex < (totalNodes - nodeGap); currentNodeIndex++) {
//                int nodeToCompareIndex = currentNodeIndex + nodeGap;
//
//                Vgadjacency vgadjacency = checkVisibility(currentNodeIndex, nodeToCompareIndex);
//                if (vgadjacency != null) {
//                    //PSVGGraphStore.storeVisibilityGraphInFile(currentNodeIndex, nodeToCompareIndex);
//                    insertNewEdge(vgadjacency);
//                }
//
//            }
//        }
//    }
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
            Double inBetweenNodeXVal = Double.valueOf(i);
            Double inBetweenNodeYVal = (Double) InputTimeSeries.get(i);

            Double baseRatio = (inBetweenNodeXVal - currentNodeXVal) / (nodeToCompareXVal - currentNodeXVal);
            Double inBetweenHeight = (baseRatio * (nodeToCompareYVal - currentNodeYVal)) + currentNodeYVal;

            if (inBetweenNodeYVal >= inBetweenHeight) {
                return null;
            }
        }
        Vgadjacency vgadjacency = gatherEdgeDetails(currentNodeIndex, nodeToCompareIndex, currentNodeXVal, currentNodeYVal, nodeToCompareXVal, nodeToCompareYVal);
        return vgadjacency;
    }

}
