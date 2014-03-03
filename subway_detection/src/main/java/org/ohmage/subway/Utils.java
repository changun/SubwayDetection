package org.ohmage.subway;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.ohmage.models.OhmageStream;
import org.ohmage.models.OhmageUser;
import org.ohmage.models.OhmageUser.OhmageAuthenticationError;
import org.ohmage.sdk.OhmageStreamClient;
import org.ohmage.sdk.OhmageStreamIterator;
import org.ohmage.sdk.OhmageStreamIterator.SortOrder;
import org.ohmage.subway.WiFi;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.JsonNode;

public class Utils {
	static public List<DataPoint> getDataFor(OhmageUser user, DateTime pointer) throws OhmageAuthenticationError, IOException{
		  // targeted ohmage stream
        OhmageStream stream = new OhmageStream.Builder()
                .observerId("edu.ucla.cens.Mobility")
                .observerVer("2012061300")
                .streamId("extended")
                .streamVer("2012050700").build();

        // requester should have access to the requestee' data streams
        OhmageUser requester = new OhmageUser("https://test.ohmage.org", "changun", "1qaz@WSX");
        // create a ohmage stream client
        OhmageStreamClient client = new OhmageStreamClient(requester);


        
        List<DataPoint> ret = new ArrayList<DataPoint>();
        DataPoint prev = null;
        while(pointer.isBefore(DateTime.now())){
	        OhmageStreamIterator streamIterator = client.getOhmageStreamIteratorBuilder(stream, user)
	            .order(SortOrder.Chronological)
	            .startDate(pointer)
	            .endDate(pointer.plusDays(1)).columnList("wifi_data,mode") // order is optional, it is in Chronological order by default
	            .build(); 
	        pointer = pointer.plusDays(1);
	        System.out.println("Download data " + pointer.toLocalDate().toString() + " for " + user);
	        while(streamIterator.hasNext()){
	            // the iterator returns each data point as a json node
	            JsonNode node = streamIterator.next();
	            JsonNode dataNode = node.get("data");
	            DataPoint dp = new DataPoint();
	            dp.setMode(MobilityState.valueOf(dataNode.get("mode").asText().toUpperCase()));
	            if(dataNode.has("wifi_data")){
	            	Iterator<JsonNode> iter = dataNode.get("wifi_data").get("scan").iterator();
	            	while(iter.hasNext()){
	            		JsonNode scan = iter.next();
	            		dp.getWifis().put(new WiFi(scan.get("ssid").asText()), scan.get("strength").asDouble());
	            	}
	            }
	            else{
	            	continue;
	            }
	            dp.setTime(new DateTime(node.get("metadata").get("timestamp").asText()));
	            dp.setPrev(prev);
	            dp.setUser(user);
	            if(node.get("metadata").has("location")){
	            	dp.setLat(node.get("metadata").get("location").get("latitude").asDouble());
		            dp.setLng(node.get("metadata").get("location").get("longitude").asDouble());
	            }
	            if(prev != null)
	            	prev.setNext(dp);
	            prev = dp;
	            ret.add(dp);
	        }
        }
        return ret;
	}
	static public List<DataPoint> getDataFromFile(String filename) throws OhmageAuthenticationError, IOException{
		// targeted ohmage stream
		
	
	    Input input = new Input(new FileInputStream(filename));
	    ArrayList<DataPoint> data = ExportData.getInstance().readObject(input, new ArrayList<DataPoint>().getClass());
	    input.close();
	    DataPoint prev = null;
	    for(int i=0; i<data.size(); i++){
	    	data.get(i).setPrev(prev);
	    	if(prev != null){
	    		prev.setNext(data.get(i));
	    	}
	    	prev = data.get(i);
	    }
	    return data;
	}
	static public List<LabeledDataPoint> getLabelDataForRawData(List<DataPoint> rawData){
		List<DataPoint> activePointsBeforeSubway = FeatureExtraction.getActivePointsBeforeSubway(rawData);
		List<DataPoint> activePoints = FeatureExtraction.getAllActivePoints(rawData);
		List<LabeledDataPoint> labeledData = new ArrayList<LabeledDataPoint>(activePoints.size());
		for(DataPoint dp: activePoints){
			labeledData.add(new LabeledDataPoint(dp, activePointsBeforeSubway.contains(dp)));
		}
		return labeledData;
		
	}
	

	
}
