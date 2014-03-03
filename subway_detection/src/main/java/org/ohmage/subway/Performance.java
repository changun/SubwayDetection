package org.ohmage.subway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;
import org.joda.time.Interval;

public class Performance {
	static final Duration NO_DETECTION = new Duration(0);
	static void eventBasedPerformance(List<LabeledDataPoint> subsetOfActivePointsForTesting, double[] predictions){
		 double beforeSubwayPrediction = 0.0;
		 
		 Map<DataPoint, Duration> detectionTimeForSubwayEvents = new HashMap<DataPoint, Duration>();
		 int numFalseAlarm = 0;
		 
		 int index=0;
		 for(LabeledDataPoint dp: subsetOfActivePointsForTesting){
			 if(dp.isBeforeSubway()){
				 /* first, compute the hit rate and detection time */
				 
				 // add the subway event associated with this data point to the set
				 DataPoint associatedSubwayEvent = dp.getSubwayEventInNextNSeconds(Constants.LOOKBACK_TIME);
				 if(!detectionTimeForSubwayEvents.containsKey(associatedSubwayEvent)){
					 detectionTimeForSubwayEvents.put(associatedSubwayEvent, NO_DETECTION);
				 }
				 // whether we successfully predict the subway event
				 boolean hit = predictions[index] == beforeSubwayPrediction;
				 if(hit){
					 // if so, store the longest detection time the event
					 Duration detectionTime = new Interval(dp.getTime() ,associatedSubwayEvent.getTime()).toDuration();
					 if(detectionTime.isLongerThan(detectionTimeForSubwayEvents.get(associatedSubwayEvent))){
						 detectionTimeForSubwayEvents.put(associatedSubwayEvent, detectionTime);
					 }
				 }
			 }
			 else if(predictions[index] == beforeSubwayPrediction){
				 /* second, compute false alarm */
				 numFalseAlarm ++;
			 }
			 index++;
		 }
		 
		 int numMiss = 0;
		 int numHit = 0;
		 Duration avgDurationTime = new Duration(0);
		 for(Duration detectionTime: detectionTimeForSubwayEvents.values()){
			 if(detectionTime.equals(NO_DETECTION)){
				 numMiss++;
			 }
			 else{
				 numHit++;
				 avgDurationTime = avgDurationTime.plus(detectionTime);
			 }
		 }
		 Double hitRate = ((double)numHit) / detectionTimeForSubwayEvents.size();
		 Double avgDetectionTimeInSec = (avgDurationTime.getMillis()/1000) / (double)numHit;
		 Double falseAlarmRate = numFalseAlarm / (double)subsetOfActivePointsForTesting.size();
		 System.out.println(subsetOfActivePointsForTesting.get(0).getTime().toLocalDate().toString() 
				 + " HIT:" + numHit 
				 + " MISS:" + numMiss 
				 + " HITRATE:" + hitRate 
				 + " DETECTIONTime: " + avgDetectionTimeInSec 
				 + " FAlarm:" + numFalseAlarm 
				 + " FAlarmRate:" + falseAlarmRate);
	}
	static void  instanceBasedPerformance(List<LabeledDataPoint> subsetOfActivePointsForTesting,  double[] predictions){
		 DataPoint prevDp = subsetOfActivePointsForTesting.get(0);
		 for(int i=0; i<predictions.length; i++){
			 LabeledDataPoint dp = subsetOfActivePointsForTesting.get(i);
			 if(dp.isBeforeSubway()){
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
		 
	}
}
