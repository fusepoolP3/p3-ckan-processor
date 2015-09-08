package eu.fusepool.p3.ckan.processor.client;

import eu.fusepool.p3.ckan.processor.object.Label;
import eu.fusepool.p3.ckan.processor.object.LabelType;
import eu.fusepool.p3.ckan.processor.object.PersitentEntity;
import eu.fusepool.p3.transformer.commons.Entity;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Gabor
 */
public class LDPClient {

    /**
     * Queries the distributions from the target URI.
     *
     * @param targetURI
     * @param graphURI
     * @param query
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static Map<String, Label> getDistributions(String targetURI, String graphURI, String query) throws IOException, JSONException {

        HttpURLConnection connection = null;

        final String urlParameters = "default-graph-uri=" + URLEncoder.encode(graphURI, "UTF-8") + "&query=" + URLEncoder.encode(query, "UTF-8");

        try {
            URL url = new URL(targetURI);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(urlParameters);
                outputStream.flush();
            }

            String jsonString;
            try (InputStream inputStream = connection.getInputStream()) {
                jsonString = IOUtils.toString(inputStream, "UTF-8");
            }

            JSONObject jsonResult = new JSONObject(jsonString);
            JSONArray jsonArray = jsonResult.getJSONObject("results").getJSONArray("bindings");

            Map<String, Label> distributions = new HashMap<>();
            JSONObject jsonItem, jsonDistribution, jsonLabel, jsonValue;
            String distribution, label, value;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonItem = jsonArray.getJSONObject(i);

                jsonDistribution = jsonItem.getJSONObject("distribution");
                jsonLabel = jsonItem.getJSONObject("label");
                jsonValue = jsonItem.getJSONObject("value");

                distribution = jsonDistribution.getString("value");
                label = jsonLabel.getString("value");
                value = jsonValue.getString("value");

                Label temp = distributions.get(distribution);
                if (temp == null) {
                    temp = new Label();
                }

                switch (label) {
                    case LabelType.P3_Transformer:
                        temp.transformer = value;
                        break;
                    case LabelType.P3_LDPC:
                        temp.LDPCs.add(value);
                        break;
                    default:
                        break;
                }

                distributions.put(distribution, temp);
            }

            return distributions;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Gets the distribution content from the target URI.
     *
     * @param targetURI
     * @return
     * @throws IOException
     * @throws MimeTypeParseException
     */
    public static Entity getDistributionContent(String targetURI) throws IOException, MimeTypeParseException {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetURI);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoInput(true);

            final MimeType mimeType = new MimeType(connection.getContentType());
            final InputStream inputStream = connection.getInputStream();
            //final InputStream inputStream = IOUtils.toBufferedInputStream(readLines(connection.getInputStream(), 200)); // for testing only

            return new PersitentEntity(mimeType, inputStream);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Sends a request to the target LDPC URI using the supplied tentative
     * naming.
     *
     * @param targetURI
     * @param tentativeName
     * @param entity
     * @return
     * @throws IOException
     */
    public static String sendToLDPC(String targetURI, String tentativeName, Entity entity) throws IOException {

        HttpURLConnection connection = null;

        try {
            byte[] content = IOUtils.toByteArray(entity.getData());

            URL url = new URL(targetURI);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Slug", tentativeName);
            connection.setRequestProperty("Content-Type", entity.getType().toString());
            connection.setRequestProperty("Content-Length", Integer.toString(content.length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(content);
                outputStream.flush();
            }

            String response;
            try (InputStream inputStream = connection.getInputStream()) {
                response = IOUtils.toString(inputStream, "UTF-8");
            }

            int status = connection.getResponseCode();
            String location = "";
            if (status == HttpURLConnection.HTTP_CREATED) {
                location = connection.getHeaderField("Location");
            }

            return location;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Checks out the supplied LDPC URI and created the missing containers if
     * needed.
     *
     * @param targetURI
     * @return
     * @throws IOException
     */
    public static String getFixedURI(String targetURI) throws IOException {
        if (!isValid(targetURI)) {
            String baseURI = getBaseURI(targetURI);
            if (targetURI.equals(baseURI)) {
                return targetURI;
            }

            String path = targetURI.substring(baseURI.length(), targetURI.length());
            String[] containers = path.split("/");

            String tempPath = baseURI;
            for (String container : containers) {
                String tryPath = tempPath + container + "/";
                if (!isValid(tryPath)) {
                    tryPath = createContainer(tempPath, container);
                }
                tempPath = tryPath;
            }
            return tempPath;
        }
        return targetURI;
    }

    // --------------- Private Methods -------------- //
    /**
     * Checks if the supplied URI exists.
     *
     * @param targetURI
     * @return
     */
    private static boolean isValid(String targetURI) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetURI);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Sends a basic container to the target URI using the supplied tentative
     * naming.
     *
     * @param targetURI
     * @param tentativeName
     * @return
     * @throws IOException
     */
    private static String createContainer(String targetURI, String tentativeName) throws IOException {
        HttpURLConnection connection = null;

        final String basicContainer
                = "@prefix dct: <http://purl.org/dc/terms/> . "
                + "@prefix ldp: <http://www.w3.org/ns/ldp#>. "
                + "<> a ldp:BasicContainer ; "
                + "dct:title \"Container title\" .";

        try {
            URL url = new URL(targetURI);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Slug", tentativeName);
            connection.setRequestProperty("Content-Type", "text/turtle");
            connection.setRequestProperty("Content-Length", Integer.toString(basicContainer.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(basicContainer);
                outputStream.flush();
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return connection.getHeaderField("Location");
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Extracts the base URI from the suppied URI.
     *
     * @param targetURI
     * @return
     */
    private static String getBaseURI(String targetURI) {
        Pattern p = Pattern.compile("^([a-zA-Z]+://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]+(:\\d+)?/");
        Matcher m = p.matcher(targetURI);
        if (m.find()) {
            return m.group(0);
        }
        return targetURI;
    }

    // --------------- Test -------------- //
    /**
     * Read the specified number of lines from the input stream as text.
     *
     * @param inputSteam
     * @param numberOfLines
     * @return
     * @throws IOException
     */
    private static InputStream readLines(InputStream inputSteam, int numberOfLines) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputSteam));
        String line;
        StringBuilder data = new StringBuilder();

        for (int i = 0; i < numberOfLines; i++) {
            line = in.readLine();
            data.append(line);
            data.append("\n");
        }
        return new ByteArrayInputStream(data.toString().getBytes());
    }
}
