package org.ohmage.subway_detection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;



import org.joda.time.DateTime;
import org.ohmage.models.OhmageServer;
import org.ohmage.models.OhmageUser;
import org.ohmage.models.OhmageUser.OhmageAuthenticationError;

import com.esotericsoftware.kryo.Kryo;
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

/**
 * Hello world!
 *
 */
public class ExportData 
{
	public static Kryo getInstance(){
		Kryo kryo = new Kryo();
		kryo.register( Arrays.asList( "" ).getClass(), new ArraysAsListSerializer() );
		kryo.register( Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer() );
		kryo.register( Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer() );
		kryo.register( Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer() );
		kryo.register( Collections.singletonList( "" ).getClass(), new CollectionsSingletonListSerializer(  ) );
		kryo.register( Collections.singleton( "" ).getClass(), new CollectionsSingletonSetSerializer(  ) );
		kryo.register( Collections.singletonMap( "", "" ).getClass(), new CollectionsSingletonMapSerializer(  ) );
		kryo.register( GregorianCalendar.class, new GregorianCalendarSerializer() );
		kryo.register(EnumMap.class, new EnumMapSerializer());
		kryo.register(BitSet.class, new BitSetSerializer());
		kryo.register(Pattern.class, new RegexSerializer());
		UnmodifiableCollectionsSerializer.registerSerializers( kryo );
		SynchronizedCollectionsSerializer.registerSerializers( kryo );

		// custom serializers for non-jdk libs

		// joda datetime
		kryo.register( DateTime.class, new JodaDateTimeSerializer() );
		
		//kryo.setRegistrationRequired(false);
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
		
		kryo.register(ArrayList.class);
		kryo.register(LinkedList.class);
		kryo.register(HashMap.class);
		kryo.register(HashSet.class);
		
		
		
		kryo.register(OhmageUser.class);
		kryo.register(OhmageServer.class);
		
		kryo.register(DataPoint.class);

		return kryo;
	}
    public static void main( String[] args ) throws OhmageAuthenticationError, IOException
    {	
    	for(String user: new String[]{"ohmage.cameron", "ohmage.josh", "ohmage.estrin", "sink.thaw" }){
        // requestee's password is not required
    		OhmageUser requestee = new OhmageUser("https://test.ohmage.org", user, null);
    		
			List<DataPoint> data = Utils.getDataFor(requestee, Constants.SINCE);
			for(DataPoint dp: data){
				dp.prev = null;
				dp.next = null;
			}
		 	Kryo kryo = getInstance();
		 	Output output = new Output(new FileOutputStream(user + "_since_" +Constants.SINCE.toLocalDate().toString()+ ".bin"));
		 	kryo.writeObject(output, data);
		 	output.close();
    	}
    }
}
