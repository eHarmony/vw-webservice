/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vrahimtoola A basic implementation of ExampleSubmitterFeatures.
 */
public class ExampleSubmitterFeaturesImpl implements ExampleSubmitterFeatures {

    private final Map<String, Object> featuresMap;
    private final boolean isAsync;

    public ExampleSubmitterFeaturesImpl(boolean isAsync,
	    Map<String, Object> featuresMap) {

	this.isAsync = isAsync;
	this.featuresMap = getShallowCopyOfMap(featuresMap);
    }

    /* (non-Javadoc)
     * @see com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFeatures#getAllFeatures()
     */
    @Override
    public Map<String, Object> getAllFeatures() {
	return getShallowCopyOfMap(featuresMap);
    }


    private Map<String, Object> getShallowCopyOfMap(
	    Map<String, Object> mapToCopy) {
	Map<String, Object> copy = new HashMap<String, Object>();

	if (mapToCopy != null)
	    for (Map.Entry<String, Object> entry : mapToCopy.entrySet()) {
		if (entry.getKey() != null) // skip over null keys.
		    copy.put(entry.getKey(), entry.getValue());
	    }

	return copy;
    }

    @Override
    public boolean isAsync() {
	return isAsync;
    }
}
