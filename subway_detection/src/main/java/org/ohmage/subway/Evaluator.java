package org.ohmage.subway;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Evaluator {
	public ArrayList<Attribute> createAttrs(Set<String> wifis){
		ArrayList<Attribute> ret = new ArrayList<Attribute> ();
		 ArrayList<String> fvSubway = new  ArrayList<String> ();
		 fvSubway.add("true");
		 fvSubway.add("false");
		 ret.add(new Attribute("subway", fvSubway));
		 
		 ArrayList<String> weekday = new  ArrayList<String> ();
		 weekday.add("weekday");
		 weekday.add("weekend");
		 ret.add(new Attribute("weekday/weekend", weekday ));
		 
		 ArrayList<String> timeslice = new  ArrayList<String> ();
		 for(Integer i=0; i<Constants.TIME_SLICES_IN_A_DAY; i++){
			 timeslice.add(i.toString());
		 }
		 ret.add(new Attribute("timeslice", timeslice ));
		 
		 for(String wifi: wifis){
			 ret.add( new Attribute(wifi));
		 }
		 for(String wifi: wifis){
			 ret.add( new Attribute("DELTA:"+wifi));
		 }
		 return ret;
		 
		
	}

	public Instances createDataset(List<DataPoint> allActivePoints,List<DataPoint> allActivePointsBeforeSubway, ArrayList<Attribute> attrs, Set<String> wifis){
		 // get the 5 strongest wifi
		if(wifis == null){
			wifis = FeatureExtraction.getStrongestNWiFiInAllPoints(allActivePoints, NUM_OF_WIFI_AP);
			attrs = createAttrs(wifis);
		}
		 Instances dat = new Instances("Dataset", attrs, allActivePoints.size());
		 // Create an empty training set        
		 // Set class index
		 dat.setClassIndex(0);
		 for(DataPoint dp: allActivePoints){
			 dat.add(Utils.createInstance(dp, allActivePointsBeforeSubway.contains(dp), attrs, wifis));
		 }
		 return dat;
	}
	final int NUM_OF_WIFI_AP;
	final List<DataPoint> allActivePoints; 
	final List<DataPoint> allActivePointsBeforeSubway;
	public Evaluator(List<DataPoint> allActivePoints, List<DataPoint> allActivePointsBeforeSubway, int NUM_OF_WIFI_AP){
		this.NUM_OF_WIFI_AP = NUM_OF_WIFI_AP;
		this.allActivePoints = allActivePoints;
		this.allActivePointsBeforeSubway = allActivePointsBeforeSubway;
		
	}
	
	void evaluateModelByLastNDaysCV(Classifier cModel, int n) throws Exception{
		 DateTime mostRecent = allActivePoints.iterator().next().getTime();
		 for(DataPoint point: allActivePoints){
			 if(point.getTime().isAfter(mostRecent)){
				 mostRecent = point.getTime();
			 }
		 }
		 List<DataPoint> subsetOfActivePoints = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsBeforeSubway = new ArrayList<DataPoint>();
		 
		 for(DataPoint dp: this.allActivePoints){
			 if(dp.getTime().isAfter(mostRecent.minusDays(n))){
				 subsetOfActivePoints.add(dp);
			 }
		 }
		 for(DataPoint dp: this.allActivePointsBeforeSubway){
			 if(dp.getTime().isAfter(mostRecent.minusDays(n))){
				 subsetOfActivePointsBeforeSubway.add(dp);
			 }
		 }
		 Instances dataset = this.createDataset(subsetOfActivePoints, subsetOfActivePointsBeforeSubway, null, null);
		 Evaluation evaluation = new Evaluation(dataset);
		 evaluation.crossValidateModel(cModel, dataset, Constants.CV_FOLDS, new Random(Constants.SEED));
		 System.out.println(evaluation.toClassDetailsString("********************" + cModel.toString() + " CV for last " + n));
		 System.out.println(evaluation.toMatrixString());
		 System.out.println(evaluation.toSummaryString());
	};
	void evaluateRunningPerformance(Classifier cModel, int lastNDays, int NDaysBefore) throws Exception{
		 DateTime mostRecent = allActivePoints.iterator().next().getTime();
		 for(DataPoint point: allActivePoints){
			 if(point.getTime().isAfter(mostRecent)){
				 mostRecent = point.getTime();
			 }
		 }
		 List<DataPoint> subsetOfActivePointsForTesting = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsBeforeSubwayForTesting = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsForTraining = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsBeforeSubwayForTraining = new ArrayList<DataPoint>();
		 for(DataPoint dp: this.allActivePoints){
			 if(dp.getTime().isAfter(mostRecent.minusDays(lastNDays))){
				 subsetOfActivePointsForTesting.add(dp);
			 }
			 else if(dp.getTime().isAfter(mostRecent.minusDays(lastNDays + NDaysBefore))){
				 subsetOfActivePointsForTraining.add(dp);
			 }
		 }
		 for(DataPoint dp: this.allActivePointsBeforeSubway){
			 if(dp.getTime().isAfter(mostRecent.minusDays(lastNDays))){
				 subsetOfActivePointsBeforeSubwayForTesting.add(dp);
			 }
			 else if(dp.getTime().isAfter(mostRecent.minusDays(lastNDays + NDaysBefore))){
				 subsetOfActivePointsBeforeSubwayForTraining.add(dp);
			 }
		 }
		 Set<String> wifis = FeatureExtraction.getStrongestNWiFiInAllPoints(subsetOfActivePointsForTraining, NUM_OF_WIFI_AP);
		 ArrayList<Attribute> attrs = createAttrs(wifis);
		 Instances testingDataset = this.createDataset(subsetOfActivePointsForTesting, subsetOfActivePointsBeforeSubwayForTesting, attrs, wifis);
		 Instances trainDataset = this.createDataset(subsetOfActivePointsForTraining, subsetOfActivePointsBeforeSubwayForTraining, attrs, wifis);
		 cModel.buildClassifier(trainDataset);
		 Evaluation evaluation = new Evaluation(trainDataset);
		 double[] predictions = evaluation.evaluateModel(cModel, testingDataset);
		 DataPoint prevDp = subsetOfActivePointsForTesting.get(0);
		 for(int i=0; i<predictions.length; i++){
			 DataPoint dp = subsetOfActivePointsForTesting.get(i);
			 if(subsetOfActivePointsBeforeSubwayForTesting.contains(dp)){
				 if(dp.getTime().minusSeconds(Constants.LOOKBACK_TIME).isAfter(prevDp.getTime())){
					 System.out.println("===============================================");
				 }
				 prevDp = dp;
				 if(predictions[i] == 0.0 ){
					 System.out.println("Hit "+ dp.getTime().toString());
				 }
				 if(predictions[i] != 0.0){
					 System.out.println("Miss "+ dp.getTime().toString());
				 }
			 }
		 }
		 Utils.OutputWiFiSetAndClassifier(wifis, cModel, attrs);
		 
		 System.out.println(evaluation.toClassDetailsString("********************" + cModel.toString() + " last " + lastNDays + " " + NDaysBefore + "days before that."));
		 System.out.println(evaluation.toMatrixString());
		 System.out.println(evaluation.toSummaryString());
	};
	void evaluatePerformanceByInterval(Classifier cModel, Interval testInterval, Interval trainInterval) throws Exception{
		 List<DataPoint> subsetOfActivePointsForTesting = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsBeforeSubwayForTesting = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsForTraining = new ArrayList<DataPoint>();
		 List<DataPoint> subsetOfActivePointsBeforeSubwayForTraining = new ArrayList<DataPoint>();
		 for(DataPoint dp: this.allActivePoints){
			 if(testInterval.contains(dp.getTime())){
				 subsetOfActivePointsForTesting.add(dp);
			 }
			 else if(trainInterval.contains(dp.getTime())){
				 subsetOfActivePointsForTraining.add(dp);
			 }
		 }
		 for(DataPoint dp: this.allActivePointsBeforeSubway){
			 if(testInterval.contains(dp.getTime())){
				 subsetOfActivePointsBeforeSubwayForTesting.add(dp);
			 }
			 else if(trainInterval.contains(dp.getTime())){
				 subsetOfActivePointsBeforeSubwayForTraining.add(dp);
			 }
		 }
		 Set<String> wifis = FeatureExtraction.getStrongestNWiFiInAllPoints(subsetOfActivePointsForTraining, NUM_OF_WIFI_AP);
		 ArrayList<Attribute> attrs = createAttrs(wifis);
		 Instances testingDataset = this.createDataset(subsetOfActivePointsForTesting, subsetOfActivePointsBeforeSubwayForTesting, attrs, wifis);
		 Instances trainDataset = this.createDataset(subsetOfActivePointsForTraining, subsetOfActivePointsBeforeSubwayForTraining, attrs, wifis);
		 cModel.buildClassifier(trainDataset);
		 Evaluation evaluation = new Evaluation(trainDataset);
		 double[] predictions = evaluation.evaluateModel(cModel, testingDataset);
		 DataPoint prevDp = subsetOfActivePointsForTesting.get(0);
		 for(int i=0; i<predictions.length; i++){
			 DataPoint dp = subsetOfActivePointsForTesting.get(i);
			 if(subsetOfActivePointsBeforeSubwayForTesting.contains(dp)){
				 if(dp.getTime().minusSeconds(Constants.LOOKBACK_TIME).isAfter(prevDp.getTime())){
					 System.out.println("===============================================");
				 }
				 prevDp = dp;
				 if(predictions[i] == 0.0 ){
					 System.out.println("Hit "+ dp.getTime().toString());
				 }
				 if(predictions[i] != 0.0){
					 System.out.println("Miss "+ dp.getTime().toString());
				 }
			 }
		 }
		 System.out.println(evaluation.toClassDetailsString("********************" + cModel.toString() + testInterval.toString() + trainInterval.toString()));
		 System.out.println(evaluation.toMatrixString());
		 System.out.println(evaluation.toSummaryString());
	};
	
	
}
