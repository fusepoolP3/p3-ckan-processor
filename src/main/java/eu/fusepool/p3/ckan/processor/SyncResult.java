package eu.fusepool.p3.ckan.processor;

import eu.fusepool.p3.ckan.processor.object.Result;
import eu.fusepool.p3.ckan.processor.object.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabor
 */
public class SyncResult {

    private static final Map<String, Result> syncResults = Collections.synchronizedMap(new HashMap<String, Result>());

    public static Result getStatus(final String requestId) {
        synchronized (syncResults) {
            return syncResults.get(requestId);
        }
    }

    public static void setStatusProcessing(final String requestId) {
        synchronized (syncResults) {
            syncResults.put(requestId, new Result(Status.PROCESSING, "/status/" + requestId));
        }
    }

    public static void setStatusSuccess(final String requestId, final String[] contentLocations) {
        synchronized (syncResults) {
            syncResults.put(requestId, new Result(Status.SUCCESS, Arrays.toString(contentLocations)));
        }
    }

    public static void setStatusFailure(final String requestId, final Exception exception) {
        synchronized (syncResults) {
            syncResults.put(requestId, new Result(Status.FAILURE, exception.getMessage()));
        }
    }

}
