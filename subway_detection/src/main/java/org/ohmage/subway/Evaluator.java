package org.ohmage.subway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Seconds;
import org.ohmage.subway.WiFi;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;

import weka.core.Instances;

public class Evaluator {
	


	final List<LabeledDataPoint> data;
	public Evaluator(List<LabeledDataPoint> data){
		this.data = data;
		
		
	}
	void evaluatePerformanceByInterval(Classifier cModel, Interval testInterval, Interval trainInterval) throws Exception{
		 List<LabeledDataPoint> testingData = new ArrayList<LabeledDataPoint>();
		 List<LabeledDataPoint> trainData = new ArrayList<LabeledDataPoint>();
		 boolean containBeforeSubwayInstance = false;
		 for(LabeledDataPoint dp: this.data){
			 if(testInterval.contains(dp.getTime())){
				 testingData.add(dp);
			 }
			 else if(trainInterval.contains(dp.getTime())){
				 trainData.add(dp);
				 if(dp.isBeforeSubway())
					 containBeforeSubwayInstance = true;
			 }
		 }
		 if(testingData.size() == 0 || !containBeforeSubwayInstance)
			 return;
		 DatasetCreator datasetCreator = new DatasetCreator(trainData);
		 Instances testingDataset = datasetCreator.createDataset(testingData);
		 Instances trainDataset = datasetCreator.createDataset(trainData);
		 cModel.buildClassifier(trainDataset);
		 Evaluation evaluation = new Evaluation(trainDataset);
		 double[] predictions = evaluation.evaluateModel(cModel, testingDataset);

		 
		 Performance.eventBasedPerformance(testingData, predictions);
		 //Performance.instanceBasedPerformance(testingData, predictions);
		 
	}
	void evaluateModelByLastNDaysCV(Classifier cModel, int n) throws Exception{
		 DateTime mostRecent = data.get(data.size()-1).getTime();
		 List<LabeledDataPoint> trainData = new ArrayList<LabeledDataPoint>();
		 
		 for(LabeledDataPoint dp: data){
			 if(dp.getTime().isAfter(mostRecent.minusDays(n))){
				 trainData.add(dp);
			 }
		 }
		 DatasetCreator datasetCreator = new DatasetCreator(trainData);
		 Instances trainDataset = datasetCreator.createDataset(trainData);
		 Evaluation evaluation = new Evaluation(trainDataset);
		 evaluation.crossValidateModel(cModel, trainDataset, Constants.CV_FOLDS, new Random(Constants.SEED));
		 System.out.println(evaluation.toClassDetailsString("********************" + cModel.toString() + " CV for last " + n));
		 System.out.println(evaluation.toMatrixString());
		 System.out.println(evaluation.toSummaryString());
	};
	
}
