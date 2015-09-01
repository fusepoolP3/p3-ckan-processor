package eu.fusepool.p3.ckan.processor.server;

import eu.fusepool.p3.ckan.processor.Processor;
import eu.fusepool.p3.ckan.processor.SyncResult;
import eu.fusepool.p3.ckan.processor.object.Result;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;

@Path("")
public class EndPoint {

    private static final String sparqlEndPoint = "http://fusepool.openlinksw.com/sparql";

    public EndPoint() {
    }

    @GET
    @Path("/status/{requestId}")
    @Produces(MediaType.TEXT_HTML)
    public Response get(@PathParam("requestId") String requestId) {
        Result result = SyncResult.getStatus(requestId);
        if (result.isProcessing()) {
            return Response.status(Response.Status.ACCEPTED).entity("Status: PROCESSING").build();
        } else {
            if (result.isSuccess()) {
                return Response.status(Response.Status.OK).entity(result.getSuccessData()).build();
            } else if (result.isFailure()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result.getErrorData()).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Status: UNKNOWN").build();
            }
        }
    }

    @POST
    @Path("/start")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response process(@Context HttpServletResponse response, @FormParam("dataSet") String dataSet) {
        try {
            // check if data set was provided
            if (StringUtils.isEmpty(dataSet)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Parameter \"dataSet\" cannot be empty!").build();
            }
            final String requestId = UUID.randomUUID().toString();
            SyncResult.setStatusProcessing(requestId);
            // create processor
            Processor processor = new Processor(sparqlEndPoint, dataSet);
            // create backgroud thread
            Thread thread = new Thread(processor, requestId);
            // start tasks in the backgroud
            thread.start();
            // return accepted
            return Response.status(Response.Status.ACCEPTED).entity("/status/" + requestId).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
