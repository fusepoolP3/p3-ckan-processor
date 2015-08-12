package eu.fusepool.p3.ckan.processor.object;

import eu.fusepool.p3.transformer.commons.Entity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.activation.MimeType;
import org.apache.commons.io.IOUtils;

/**
 * It stores the inputstream of the original Entity in a way that it can be read
 * multiple times.
 *
 * @author Gabor
 */
public class PersitentEntity implements Entity {

    final private MimeType mimeType;
    final private URI contentLocation;
    final private byte[] data;

    public PersitentEntity(MimeType mimeType, InputStream inputStream) throws IOException {
        this.mimeType = mimeType;
        this.contentLocation = null;
        this.data = IOUtils.toByteArray(inputStream);
    }

    public PersitentEntity(Entity entity) throws IOException {
        this.mimeType = entity.getType();
        this.contentLocation = entity.getContentLocation();
        this.data = IOUtils.toByteArray(entity.getData());
    }

    @Override
    public MimeType getType() {
        return mimeType;
    }

    @Override
    public URI getContentLocation() {
        return contentLocation;
    }

    @Override
    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public void writeData(OutputStream outputStream) throws IOException {
        IOUtils.copy(getData(), outputStream);
    }

}
