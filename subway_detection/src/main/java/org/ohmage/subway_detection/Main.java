package org.ohmage.subway_detection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.models.OhmageUser;
import org.ohmage.models.OhmageUser.OhmageAuthenticationError;


public class Main {
	
	
	public static void main(String[] args) throws Exception {
        // requestee's password is not required
        OhmageUser requestee = new OhmageUser("https://test.ohmage.org", "ohmage.cameron", null);
		List<DataPoint> data = Utils.getDataFor(requestee, Constants.SINCE);
		// do use the first point as it does not have prev point
		data.remove(0);
		Set<DataPoint> activePointsBeforeSubway = FeatureExtraction.getActivePointsBeforeSubway(data);
		Set<DataPoint> activePoints = FeatureExtraction.getAllActivePoints(data);
		DecisionTree tree = new DecisionTree(activePoints, activePointsBeforeSubway, 5);
		tree.evaluateModel();
	}

}
