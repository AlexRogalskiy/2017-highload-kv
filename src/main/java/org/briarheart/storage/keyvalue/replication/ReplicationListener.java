package org.briarheart.storage.keyvalue.replication;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.briarheart.storage.keyvalue.StorageException;
import org.briarheart.storage.keyvalue.event.RemoveEvent;
import org.briarheart.storage.keyvalue.event.StorageEventListenerSupport;
import org.briarheart.storage.keyvalue.event.UpsertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * @author Roman Chigvintsev
 */
public class ReplicationListener extends StorageEventListenerSupport {
    private static final Logger log = LoggerFactory.getLogger(ReplicationListener.class);

    private final Set<URL> peerEndpoints;
    private final String apiVersion;

    public ReplicationListener(Set<URL> peerEndpoints, String apiVersion) {
        this.peerEndpoints = peerEndpoints;
        this.apiVersion = apiVersion;
    }

    @Override
    public void onUpsert(UpsertEvent event) throws StorageException {
        int minNumberOfVotes = event.getQuorum().getMinNumberOfVotes();
        if (minNumberOfVotes < 1) {
            log.debug("Minimum number of votes is not specified or already reached for event '{}'", event);
            return;
        }

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(500).build();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig).build()) {
            int votesNumber = 1;

            for (URL endpoint : peerEndpoints)
                try {
                    URIBuilder uriBuilder = new URIBuilder(endpoint.toURI());
                    uriBuilder.setPath(apiVersion + "/entity");
                    uriBuilder.addParameter("id", event.getId());

                    HttpPut put = new HttpPut(uriBuilder.build());
                    put.setEntity(new ByteArrayEntity(event.getValue()));

                    try (CloseableHttpResponse response = httpClient.execute(put)) {
                        if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_CREATED) {
                            votesNumber++;
                            log.debug("Event '{}' is successfully replicated to endpoint {}", event, endpoint);
                        }
                    }
                } catch (IOException | URISyntaxException e) {
                    if (log.isErrorEnabled())
                        log.error("Replication of event '" + event + "' to endpoint " + endpoint
                                + " finished with error", e);
                }

            if (votesNumber != minNumberOfVotes)
                throw new ReplicationException("Minimum number of votes (" + minNumberOfVotes + ") is not reached "
                        + "for event '" + event + "'. Only " + votesNumber + " nodes reported success.");
            log.debug("Minimum number of votes ({}) is reached for event '{}'", minNumberOfVotes, event);
        } catch (IOException e) {
            log.error("Failed to close HTTP client", e);
        }
    }

    @Override
    public void onRemove(RemoveEvent event) throws StorageException {
        // TODO: implement me!
    }
}
