package eu.fusepool.p3.ckan.processor.server;

import eu.fusepool.p3.ckan.processor.Processor;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;

@Path("")
public class EndPoint {

    private static final String sparqlEndPoint = "http://fusepool.openlinksw.com/sparql";

    public EndPoint() {
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public String get() {
        return "test";
    }

    @POST
    @Path("/process")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response process(@FormParam("dataSet") String dataSet) {
        try {
            // check if data set was provided
            if (StringUtils.isEmpty(dataSet)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Parameter \"dataSet\" cannot be empty!").build();
            }
            // create processor
            Processor processor = new Processor(sparqlEndPoint, dataSet);
            // create backgroud thread
            Thread thread = new Thread(processor);
            // start tasks in the backgroud
            thread.start();
            // return accepted
            return Response.status(Response.Status.ACCEPTED).entity("Request was accepted and process started in backgroud.").build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
