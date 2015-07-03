package eu.fusepool.p3.ckan.processor.client;

import eu.fusepool.p3.transformer.client.Transformer;
import eu.fusepool.p3.transformer.client.TransformerClientImpl;
import eu.fusepool.p3.transformer.commons.Entity;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 *
 * @author Gabor
 */
public class TransformerClient {

    /**
     * Sends a request to the supplied transformer URI using the transformer client library.
     *
     * @param transformerURI
     * @param data
     * @return
     * @throws javax.activation.MimeTypeParseException
     */
    public static Entity transform(String transformerURI, Entity data) throws MimeTypeParseException {
        Transformer transformer = new TransformerClientImpl(transformerURI);
        return transformer.transform(data, new MimeType("*/*"));
    }

}
