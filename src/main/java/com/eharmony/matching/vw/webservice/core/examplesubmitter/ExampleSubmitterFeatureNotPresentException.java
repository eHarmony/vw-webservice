/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when a feature isn't present.
 */
public class ExampleSubmitterFeatureNotPresentException extends Exception {

    private final String featureKey;

    public ExampleSubmitterFeatureNotPresentException(String featureKey) {

	super("Missing feature key: " + featureKey);

	checkNotNull(featureKey);

	this.featureKey = featureKey;
    }

    public String getMissingFeatureKey() {
	return featureKey;
    }
}
