package org.ohmage.subway_detection;

import java.util.List;
import java.util.Random;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

public class DecisionTree extends BaseClassifier{

	public DecisionTree(Set<DataPoint> allActivePoints,
			Set<DataPoint> allActivePointsBeforeSubway, int NUM_OF_WIFI_AP) {
		super(allActivePoints, allActivePointsBeforeSubway, NUM_OF_WIFI_AP);
	}

	@Override
	void evaluateModel() throws Exception {
		 Classifier cModel = getModel();
		 Evaluation evaluation = new Evaluation(dataset);
		 evaluation.crossValidateModel(cModel, dataset, 10, new Random(Constants.SEED));
		 System.out.print(evaluation.toSummaryString());
	}

	@Override
	Classifier getModel() {
		
		return (Classifier)new J48() ;
	}

}
