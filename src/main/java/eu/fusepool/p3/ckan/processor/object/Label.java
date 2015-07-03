package eu.fusepool.p3.ckan.processor.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the labels of distributions. A distribution can have a single
 * transformer and multiple LDPCs.
 *
 * @author Gabor
 */
public class Label {

    public String transformer;
    public List<String> LDPCs;

    public Label() {
        LDPCs = new ArrayList<>();
    }

}
