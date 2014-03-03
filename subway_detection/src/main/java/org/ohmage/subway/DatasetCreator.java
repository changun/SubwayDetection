package org.ohmage.subway;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DatasetCreator implements KryoSerializable{

	private Instance createInstance(LabeledDataPoint dp, ArrayList<Attribute> attrs, Set<WiFi> wifis){
		// Create the instance
		 Instance instance = new DenseInstance(attrs.size());
		 instance.setValue(attrs.get(0), dp.isBeforeSubway()? "true": "false");
		 instance.setValue(attrs.get(1), FeatureExtraction.isWeekday(dp)? "weekday": "weekend");
		 instance.setValue(attrs.get(2), FeatureExtraction.getTime(dp));
		 int i = 1;
		 Map<WiFi, Double> strength_fatures = FeatureExtraction.getWiFiStrengthFeatures(dp, wifis);
		 for(WiFi wifi: wifis){
			 instance.setValue(attrs.get(i++), strength_fatures.get(wifi));
		 }
		 Map<WiFi, Double> strength_delta_fatures = FeatureExtraction.getWiFiStrengthDifferenceFeatures(dp, wifis);
		 for(WiFi wifi: wifis){
			 instance.setValue(attrs.get(i++), strength_delta_fatures.get(wifi));
		 }
		 return instance;
	}
	
	private ArrayList<Attribute> createAttrs(Set<WiFi> wifis){
		ArrayList<Attribute> ret = new ArrayList<Attribute> ();
		 ArrayList<String> fvSubway = new  ArrayList<String> ();
		 fvSubway.add("true");
		 fvSubway.add("false");
		 ret.add(new Attribute("subway", fvSubway));
		 
		 ArrayList<String> weekday = new  ArrayList<String> ();
		 weekday.add("weekday");
		 weekday.add("weekend");
		 ret.add(new Attribute("weekday/weekend", weekday ));
		 
		 ret.add(new Attribute("time"));
		 
		 for(WiFi wifi: wifis){
			 ret.add( new Attribute(wifi.toString()));
		 }
		 for(WiFi wifi: wifis){
			 ret.add( new Attribute("DELTA:"+wifi));
		 }
		 return ret;
	}
	Set<WiFi> wifis;
	ArrayList<Attribute> attrs;
	
	public Instances createDataset(List<LabeledDataPoint> data){
		Instances dat = new Instances("", attrs, data.size());     
		 // Set class index
		 dat.setClassIndex(0);
		 for(LabeledDataPoint dp: data){
			 dat.add(createInstance(dp, attrs, wifis));
		 }
		 return dat;
	}
	public Instance createTestInstance(DataPoint data){
		Instances dat = new Instances("TestInstance", attrs, 1);     
		 // Set class index
		 dat.setClassIndex(0);
		 LabeledDataPoint labeledDp = new LabeledDataPoint(data, false);
		 Instance instance = createInstance(labeledDp, attrs, wifis);
		 dat.add(instance);
		 instance.setDataset(dat);
		 return instance;
	}
	
	public DatasetCreator(List<LabeledDataPoint> trainData){
		 // get the N strongest wifis for each data point
		 wifis = FeatureExtraction.getStrongestNWiFiInAllPoints(trainData, Constants.NUM_STRONGEST_WIFI);
		 attrs = createAttrs(wifis);
	}
	public DatasetCreator(Set<WiFi> wifis, ArrayList<Attribute> attrs){
		this.wifis = wifis;
		this.attrs = attrs;
	}

	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, this.wifis);
		try {
			ObjectOutputStream jvmObjectOutput = new ObjectOutputStream(output);
			jvmObjectOutput.writeObject(this.attrs);
			jvmObjectOutput.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void read(Kryo kryo, Input input) {
		this.wifis = kryo.readObject(input, new HashSet<WiFi>().getClass());
		try {
			ObjectInputStream jvmObjectInput = new ObjectInputStream(input);
			this.attrs = (ArrayList<Attribute>) jvmObjectInput.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}
