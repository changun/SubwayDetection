package org.ohmage.subway;

import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.ohmage.models.OhmageServer;
import org.ohmage.models.OhmageUser;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;

public class OnPhoneClassifier implements KryoSerializable {

	DatasetCreator datCreator;
	Classifier classifier;
	OnPhoneClassifier(DatasetCreator datCreator, Classifier classifier){
		this.datCreator = datCreator;
		this.classifier = classifier;
	}
	// return if the data point is a before-subway event
	boolean classify(DataPoint dp) throws Exception{
		Instance instance = datCreator.createTestInstance(dp);
		return classifier.classifyInstance(instance) == 0.0;	
	}
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, this.datCreator);
		try {
			ObjectOutputStream jvmObjectOutput = new ObjectOutputStream(output);
			jvmObjectOutput.writeObject(this.classifier);
			jvmObjectOutput.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void read(Kryo kryo, Input input) {
		this.datCreator = kryo.readObject(input, DatasetCreator.class);
		try {
			ObjectInputStream jvmObjectInput = new ObjectInputStream(input);
			this.classifier =(Classifier) jvmObjectInput.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static Kryo getKryoInstance(){
		Kryo kryo = new Kryo();
	
		kryo.setRegistrationRequired(false);
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
	
		return kryo;
	}
	
	
	
}
