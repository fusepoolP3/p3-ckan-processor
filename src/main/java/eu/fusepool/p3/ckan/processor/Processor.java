package eu.fusepool.p3.ckan.processor;

import eu.fusepool.p3.ckan.processor.client.LDPClient;
import eu.fusepool.p3.ckan.processor.client.TransformerClient;
import eu.fusepool.p3.ckan.processor.object.Label;
import eu.fusepool.p3.transformer.commons.Entity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Gabor
 */
public class Processor implements Runnable {

    private final String sparqlEndPoint;
    private final String dataSet;

    public Processor(String sparqlEndPoint, String dataSet) {
        this.sparqlEndPoint = sparqlEndPoint;
        this.dataSet = dataSet;
    }

    @Override
    public void run() {
        final String query
                = "SELECT distinct ?distribution ?label ?value "
                + "FROM <" + dataSet + "> "
                + "WHERE "
                + "{ "
                + "     ?dataset a dcat:Dataset ; "
                + "     <http://www.w3.org/ns/dcat#distribution> ?distribution ; "
                + "     <http://purl.org/dc/terms/relation> ?r . "
                + "     ?r <http://www.w3.org/2000/01/rdf-schema#label> ?label ; "
                + "     <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> ?value . "
                + "}";

        try {
            Map<String, Label> distributions = LDPClient.getDistributions(sparqlEndPoint, query);

            for (Map.Entry<String, Label> entry : distributions.entrySet()) {
                String tentativeName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                // get URI if distribution
                String distribution = entry.getKey();
                // get labels of distribution
                Label label = entry.getValue();
                // distributions must have at least one LDPC
                if (label.LDPCs.size() > 0) {
                    // fix LDPC paths
                    for (int i = 0; i < label.LDPCs.size(); i++) {
                        String fixedPath = LDPClient.getFixedURI(label.LDPCs.get(i));
                        label.LDPCs.set(i, fixedPath);
                    }
                    // get the content of the distribution
                    Entity originalData = LDPClient.getDistributionContent(distribution);
                    // write distribution to LDPC
                    for (String LDPC : label.LDPCs) {
                        LDPClient.sendToLDPC(LDPC, tentativeName, originalData);
                    }
                    // if distribution has a transformer label
                    if (StringUtils.isNotEmpty(label.transformer)) {
                        // transform distribution
                        Entity transformerData = TransformerClient.transform(label.transformer, originalData);
                        // write transformed distribution to LDPC
                        for (String LDPC : label.LDPCs) {
                            LDPClient.sendToLDPC(LDPC, tentativeName + "-transformed", transformerData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO handle exception in some way
            e.printStackTrace();
        }
    }

}
