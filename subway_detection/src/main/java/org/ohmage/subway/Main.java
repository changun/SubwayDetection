package org.ohmage.subway;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.models.OhmageUser;
import org.ohmage.models.OhmageUser.OhmageAuthenticationError;

import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;


public class Main {
	
	
	public static void main(String[] args) throws Exception {
        // requestee's password is not required
        OhmageUser requestee = new OhmageUser("https://test.ohmage.org", "ohmage.cameron", null);
		List<DataPoint> data = Utils.getDataFromFile("ohmage.cameron_since_2013-11-01.bin");
		// do use the first point as it does not have prev point
		data.remove(0);
		
		List<DataPoint> activePointsBeforeSubway = FeatureExtraction.getActivePointsBeforeSubway(data);
		List<DataPoint> activePoints = FeatureExtraction.getAllActivePoints(data);
		Evaluator evaluator = new Evaluator(activePoints, activePointsBeforeSubway, 4);
		//evaluator.evaluateModelByLastNDaysCV(new Logistic(), 14);
		//evaluator.evaluateModelByLastNDaysCV(new LogitBoost(), 14);
		evaluator.evaluateRunningPerformance(new J48(), 1, 30);
		//evaluator.evaluateRunningPerformance(new AdaBoostM1(), 2, 30);
		//evaluator.evaluateRunningPerformance(new RandomForest(), 2, 30);
		//evaluator.evaluateModelByLastNDaysCV(new J48(), 14);

		//evaluator.evaluateModelByLastNDaysCV(new RandomForest(), 14);
		
		
        
	}

}
