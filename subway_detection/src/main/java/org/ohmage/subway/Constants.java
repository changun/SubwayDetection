package org.ohmage.subway;

import org.joda.time.DateTime;

public class Constants {
	final public static DateTime SINCE = new DateTime("2013-11-1");
	// how many seconds to look ahead to decide if a point is in subway
	final public static int LOOKAHEAD_TIME = 600;
	// how many seconds' data before the subway events to be used as features
	final public static int LOOKBACK_TIME = 600;
	// the magic strength that represents no signal
	final public static double NO_SIGNAL_STRENGTH = -1000;
	
	final public static int SEED = 1000;
	final public static int TIME_SLICES_IN_A_DAY = 4;
	final public static int CV_FOLDS = 2;
}