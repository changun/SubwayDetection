package org.ohmage.subway_detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

abstract public class BaseClassifier {
	public ArrayList<Attribute> createAttrs(Set<String> wifis){
		ArrayList<Attribute> ret = new ArrayList<Attribute> ();
		 ArrayList<String> fvSubway = new  ArrayList<String> ();
		 fvSubway.add("true");
		 fvSubway.add("false");
		 ret.add(new Attribute("subway", fvSubway));
		 
		 ArrayList<String> weekday = new  ArrayList<String> ();
		 weekday .add("weekday");
		 weekday .add("weekend");
		 ret.add(new Attribute("weekday/weekend", weekday ));
		 
		 for(String wifi: wifis){
			 ret.add( new Attribute(wifi));
		 }
		 for(String wifi: wifis){
			 ret.add( new Attribute("DELTA:"+wifi));
		 }
		 return ret;
		 
		
	}
	public Instance createInstance(DataPoint dp, Boolean subway, ArrayList<Attribute> attrs, Set<String> wifis){
		// Create the instance
		 Instance instance = new DenseInstance(attrs.size());
		 instance.setValue(attrs.get(0), subway? "true": "false");
		 instance.setValue(attrs.get(1), FeatureExtraction.isWeekday(dp)? "weekday": "weekend");
		 int i = 2;
		 Map<String, Double> strength_fatures = FeatureExtraction.getWiFiSignalFeatures(dp, wifis);
		 for(String wifi: wifis){
			 instance.setValue(attrs.get(i++), strength_fatures.get(wifi));
		 }
		 Map<String, Double> strength_delta_fatures = FeatureExtraction.getWiFiSignalDifferenceFeatures(dp, wifis);
		 for(String wifi: wifis){
			 instance.setValue(attrs.get(i++), strength_delta_fatures.get(wifi));
		 }
		 return instance;
	}
	public Instances createDataset(Set<DataPoint> allActivePoints,Set<DataPoint> allActivePointsBeforeSubway){
		 // get the 5 strongest wifi
		 Set<String> wifis = FeatureExtraction.getStrongestNWiFiInAllPoints(allActivePoints, NUM_OF_WIFI_AP);
		 ArrayList<Attribute> attrs = createAttrs(wifis);
		 Instances dat = new Instances("Dataset", attrs, allActivePoints.size());
		 // Create an empty training set        
		 // Set class index
		 dat.setClassIndex(0);
		 for(DataPoint dp: allActivePoints){
			 dat.add(createInstance(dp, allActivePointsBeforeSubway.contains(dp), attrs, wifis));
		 }
		 return dat;
	}
	final int NUM_OF_WIFI_AP;
	final Instances dataset;
	public BaseClassifier(Set<DataPoint> allActivePoints, Set<DataPoint> allActivePointsBeforeSubway, int NUM_OF_WIFI_AP){
		this.NUM_OF_WIFI_AP = NUM_OF_WIFI_AP;
		dataset = this.createDataset(allActivePoints, allActivePointsBeforeSubway);
	}
	
	abstract Classifier getModel();
	void evaluateModel() throws Exception{
		 Classifier cModel = getModel();
		 Evaluation evaluation = new Evaluation(dataset);
		 evaluation.crossValidateModel(cModel, dataset, 10, new Random(Constants.SEED));
		 System.out.print(evaluation.toSummaryString());
	};
	
}
