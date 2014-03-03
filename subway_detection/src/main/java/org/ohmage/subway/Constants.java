package org.ohmage.subway;

import org.joda.time.DateTime;

import weka.classifiers.CostMatrix;

public class Constants {
	final public static DateTime SINCE = new DateTime("2013-11-1");
	// how many seconds to look ahead to decide if a point is in subway
	final public static int LOOKAHEAD_TIME = 600;
	// how many seconds' data before the subway events to be used as features
	final public static int LOOKBACK_TIME = 600;
	final public static double MINIMUN_AVAILABLE_WIFI_FOR_NON_SUBWAY = 1;
	// the magic strength that represents no signal
	final public static double NO_SIGNAL_STRENGTH = -200;
	final public static int SEED = 1000;
	final public static int TIME_SLICES_IN_A_DAY = 4;
	final public static int CV_FOLDS = 2;
	final public static CostMatrix COST_MATRIX;
	final public static int NUM_STRONGEST_WIFI = 4;
	static{
		
		COST_MATRIX = new CostMatrix(2);
		COST_MATRIX.setCell(0, 0,0.0); // hit cost (should be 0 or negative)
		COST_MATRIX.setCell(0, 1, 10.0); // miss cost 
		COST_MATRIX.setCell(1, 0, 1.0); // false alarm cost (
		COST_MATRIX.setCell(1, 1, 0.0); // true negative cost (should be 0 or negative)
	};
}
