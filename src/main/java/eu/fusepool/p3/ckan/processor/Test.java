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
public class Test {

    private static final String sparqlEndPoint = "http://fusepool.openlinksw.com/sparql";
    private static final String dataSet = "http://cot-test.infotn.it/dataset";
    private static final String query
            = "SELECT distinct ?distribution ?label ?value "
            + "WHERE "
            + "{ "
            + "     ?dataset a dcat:Dataset ; "
            + "     <http://www.w3.org/ns/dcat#distribution> ?distribution ; "
            + "     <http://purl.org/dc/terms/relation> ?r . "
            + "     ?r <http://www.w3.org/2000/01/rdf-schema#label> ?label ; "
            + "     <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> ?value . "
            + "}";

    public static void main(String[] args) {
        long start, end;
        try {
            start = System.currentTimeMillis();
            Map<String, Label> distributions = LDPClient.getDistributions(sparqlEndPoint, dataSet, query);
            end = System.currentTimeMillis();
            System.out.println("Get distributions from Virtuoso [" + Double.toString((double) (end - start) / 1000) + " sec] .");

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
                        start = System.currentTimeMillis();
                        String fixedPath = LDPClient.getFixedURI(label.LDPCs.get(i));
                        label.LDPCs.set(i, fixedPath);
                        end = System.currentTimeMillis();
                        System.out.println("Fix LDPC path from <" + label.LDPCs.get(i) + "> to <" + fixedPath + "> [" + Double.toString((double) (end - start) / 1000) + " sec] .");
                    }
                    // get the content of the distribution
                    Entity originalData = LDPClient.getDistributionContent(distribution);
                    // write distribution to LDPC
                    for (String LDPC : label.LDPCs) {
                        start = System.currentTimeMillis();
                        LDPClient.sendToLDPC(LDPC, tentativeName, originalData);
                        end = System.currentTimeMillis();
                        System.out.println("Add original distribution to <" + LDPC + "> [" + Double.toString((double) (end - start) / 1000) + " sec] .");
                    }
                    // if distribution has a transformer label
                    if (StringUtils.isNotEmpty(label.transformer)) {
                        start = System.currentTimeMillis();
                        // transform distribution
                        Entity transformerData = TransformerClient.transform(label.transformer, originalData);
                        end = System.currentTimeMillis();
                        System.out.println("Transform distribution with <" + label.transformer + "> [" + Double.toString((double) (end - start) / 1000) + " sec] .");
                        // write transformed distribution to LDPC
                        for (String LDPC : label.LDPCs) {
                            start = System.currentTimeMillis();
                            LDPClient.sendToLDPC(LDPC, tentativeName + "-transformed", transformerData);
                            end = System.currentTimeMillis();
                            System.out.println("Add transformed distribution to <" + LDPC + "> [" + Double.toString((double) (end - start) / 1000) + " sec] .");
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
