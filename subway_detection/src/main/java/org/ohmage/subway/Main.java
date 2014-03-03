package org.ohmage.subway;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.ohmage.models.OhmageUser;
import org.ohmage.models.OhmageUser.OhmageAuthenticationError;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;


public class Main {
	
	
	public static void main(String[] args) throws Exception {
        // requestee's password is not required
     
		List<DataPoint> rawData = Utils.getDataFromFile("ohmage.cameron_since_2013-11-01.bin");
		// do use the first point as it does not have prev point
		rawData.remove(0);
		
		List<LabeledDataPoint> data = Utils.getLabelDataForRawData(rawData);
		Evaluator evaluator = new Evaluator(data);
		// test the running performance
		DateTime start = new DateTime(2013,11,1,0,0);
		DateTime date = start.plusDays(1);
		
		CostSensitiveClassifier model = new CostSensitiveClassifier();
		model.setCostMatrix(Constants.COST_MATRIX);
		IBk KNN = new IBk();
		KNN.setCrossValidate(true);
		model.setClassifier(KNN);
		
		List<LabeledDataPoint> lastMonthdata = new  ArrayList<LabeledDataPoint> ();
		for(LabeledDataPoint dp: data){
			if(dp.getTime().isAfter(DateTime.now().minusWeeks(2)))
				lastMonthdata.add(dp);
		}
		DatasetCreator datasetCreator = new DatasetCreator(lastMonthdata);
		model.buildClassifier(datasetCreator.createDataset(lastMonthdata));
		
		Output output = new Output(new FileOutputStream("classifier.bin"));
		OnPhoneClassifier onphone = new OnPhoneClassifier(datasetCreator, model);
		onphone.classify(rawData.get(0));
		
		OnPhoneClassifier.getKryoInstance().writeObject(output,onphone);
		output.close();
		
		
		// incrementally train the model
		while(date.isBefore(DateTime.now())){
			evaluator.evaluatePerformanceByInterval(model, 
					new Interval(date, date.plusDays(1)), new Interval(start, date));
			date = date.plusDays(1);
		}
		
        
	}

}
