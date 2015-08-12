package eu.fusepool.p3.ckan.processor.object;

/**
 *
 * @author Gabor
 */
public class Result {

    public Status status;
    public String data;

    public Result(Status status) {
        this.status = status;
        this.data = "";
    }

    public Result(Status status, String data) {
        this.status = status;
        this.data = data;
    }

    public boolean isProcessing() {
        return status.equals(Status.PROCESSING);
    }

    public boolean isSuccess() {
        return status.equals(Status.SUCCESS);
    }

    public boolean isFailure() {
        return status.equals(Status.FAILURE);
    }

    public String getSuccessData() {
        return "Status: SUCEESS, Content locations: " + data;
    }

    public String getErrorData() {
        return "Status: FAILURE, Cause: " + data;
    }
}
