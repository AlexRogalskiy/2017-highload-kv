package org.briarheart.storage.keyvalue.synchronization;

import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.briarheart.storage.keyvalue.KeyValueStorage;
import org.briarheart.storage.keyvalue.event.StorageEventListener;
import org.briarheart.storage.keyvalue.event.SynchronizeEvent;
import org.briarheart.storage.keyvalue.util.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Chigvintsev
 */
public class StateSynchronizer {
    private static final Logger log = LoggerFactory.getLogger(StateSynchronizer.class);

    private final KeyValueStorage storage;
    private final Set<URL> peerEndpoints;
    private final String apiVersion;

    public StateSynchronizer(KeyValueStorage storage, Set<URL> peerEndpoints, String apiVersion) {
        this.storage = storage;
        this.peerEndpoints = peerEndpoints;
        this.apiVersion = apiVersion;
    }

    @SuppressWarnings("unchecked")
    public void synchronize(StorageEventListener listener) {
        URL endpoint = Iterables.firstItem(peerEndpoints);
        if (endpoint != null) {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(500).build();
            try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig).build()) {
                URIBuilder uriBuilder = new URIBuilder(endpoint.toURI());
                uriBuilder.setPath(apiVersion + "/state");

                HttpGet get = new HttpGet(uriBuilder.build());

                try (CloseableHttpResponse response = httpClient.execute(get)) {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() != HttpServletResponse.SC_OK)
                        throw new SynchronizationException(statusLine.toString());

                    InputStream content = response.getEntity().getContent();
                    ObjectInputStream objectIn = new ObjectInputStream(content);
                    Map<String, byte[]> state = (Map<String, byte[]>) objectIn.readObject();

                    storage.clear();
                    for (Map.Entry<String, byte[]> entry : state.entrySet())
                        storage.upsert(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                if (log.isErrorEnabled())
                    log.error("Synchronization with endpoint " + endpoint + " finished with error", e);
            }
        }

        if (listener != null)
            listener.onSynchronize(new SynchronizeEvent());
    }
}
