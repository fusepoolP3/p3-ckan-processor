package eu.fusepool.p3.ckan.processor;

/**
 *
 * @author Gabor
 */
public class Test {

    private static final String sparqlEndPoint = "http://fusepool.openlinksw.com/sparql";
    private static final String dataSet = "http://cot-test.infotn.it/dataset";

    public static void main(String[] args) {
        System.out.println("Main thread started...");
        Processor processor = new Processor(sparqlEndPoint, dataSet);
        // create backgroud thread
        Thread thread = new Thread(processor);
        // start tasks in the backgroud
        thread.start();
        System.out.println("Main thread exited...");
    }
}
