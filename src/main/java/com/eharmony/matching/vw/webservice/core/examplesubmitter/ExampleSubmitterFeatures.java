/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import java.util.Map;

/**
 * @author vrahimtoola Features and other stuff describing the example
 *         submitter. Feature keys cannot be null, but feature values may be
 *         null.
 */
public interface ExampleSubmitterFeatures {

    /*
	 * Whether or not the example submitter's submitExamples() method will
	 * execute synchronously.
	 * 
	 * @returns True if the example submitter is asynchronous, false otherwise.
	 */
    boolean isAsync();

    /*
     * Returns all the features applicable to this example submitter.
     * 
     * @returns All the features that this submitter provides. None of the keys
     * can be null, but values may be. The types of the values should be
     * documented by the example submitter. The returned map should never be
     * null, but can be empty.
     */
    Map<String, Object> getAllFeatures();

}
