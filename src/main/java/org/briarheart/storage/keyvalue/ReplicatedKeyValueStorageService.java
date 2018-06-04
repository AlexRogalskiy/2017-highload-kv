package org.briarheart.storage.keyvalue;

import org.briarheart.storage.keyvalue.event.StorageEventListenerSupport;
import org.briarheart.storage.keyvalue.event.SynchronizeEvent;
import org.briarheart.storage.keyvalue.replication.ReplicationListener;
import org.briarheart.storage.keyvalue.servlet.EntityServlet;
import org.briarheart.storage.keyvalue.servlet.StateTransferServlet;
import org.briarheart.storage.keyvalue.servlet.StatusServlet;
import org.briarheart.storage.keyvalue.synchronization.StateSynchronizer;
import org.briarheart.storage.keyvalue.util.Arguments;
import org.briarheart.storage.keyvalue.util.Exceptions;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.polis.KVService;

import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Roman Chigvintsev
 */
public class ReplicatedKeyValueStorageService extends StorageEventListenerSupport implements KVService {
    private static final Logger log = LoggerFactory.getLogger(ReplicatedKeyValueStorageService.class);

    private static final String API_VERSION = "v0";

    private final int port;
    private final KeyValueStorage storage;
    private final StateSynchronizer stateSynchronizer;
    private final Server server;
    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);
    private final CountDownLatch startLatch = new CountDownLatch(1);

    public ReplicatedKeyValueStorageService(int port, KeyValueStorage storage, Set<String> endpoints) {
        Arguments.assertNonNegative(port, "Port cannot be negative");
        Arguments.assertNotNull(storage, "Storage cannot be null");
        Arguments.assertNotNullOrEmpty(endpoints, "Endpoint set cannot be null or empty");

        this.port = port;
        this.storage = storage;

        Set<URL> peerEndpoints = getPeerEndpoints(endpoints);
        this.stateSynchronizer = new StateSynchronizer(storage, peerEndpoints, API_VERSION);

        this.server = new Server();

        ServerConnector connector = new ServerConnector(this.server);
        connector.setPort(port);
        this.server.setConnectors(new Connector[] {connector});

        ServletHandler servletHandler = new ServletHandler();

        StatusServlet statusServlet = new StatusServlet(this);
        ServletHolder statusServletHolder = new ServletHolder(StatusServlet.NAME, statusServlet);
        servletHandler.addServletWithMapping(statusServletHolder, "/" + API_VERSION + "/status");

        StateTransferServlet stateTransferServlet = new StateTransferServlet(storage);
        ServletHolder stateTransferServletHolder = new ServletHolder(StateTransferServlet.NAME, stateTransferServlet);
        servletHandler.addServletWithMapping(stateTransferServletHolder, "/" + API_VERSION + "/state");

        ReplicationListener replicationListener = new ReplicationListener(peerEndpoints, API_VERSION);
        EntityServlet entityServlet = new EntityServlet(storage, replicationListener, startLatch);
        ServletHolder entityServletHolder = new ServletHolder(EntityServlet.NAME, entityServlet);
        servletHandler.addServletWithMapping(entityServletHolder, "/" + API_VERSION + "/entity");

        this.server.setHandler(servletHandler);
    }

    @Override
    public void start() {
        if (state.compareAndSet(State.STOPPED, State.STARTING))
            try {
                storage.open();
                log.debug("Storage opened");

                server.start();
                log.debug("Server started on port {}", port);

                stateSynchronizer.synchronize(this);
            } catch (Exception e) {
                throw Exceptions.createRuntimeException(e);
            }
    }

    @Override
    public void stop() {
        if (state.compareAndSet(State.STARTED, State.STOPPING))
            try {
                server.stop();
                log.debug("Server stopped");

                storage.close();
                log.debug("Storage closed");

                state.set(State.STOPPED);
            } catch (Exception e) {
                throw Exceptions.createRuntimeException(e);
            }
    }

    @Override
    public void onSynchronize(SynchronizeEvent event) throws StorageException {
        state.set(State.STARTED);
        startLatch.countDown();
    }

    public boolean isAvailable() {
        return state.get() == State.STARTED;
    }

    private Set<URL> getPeerEndpoints(Set<String> endpoints) {
        Set<URL> urls = new HashSet<>();
        for (String endpoint : endpoints)
            try {
                URL url = new URL(endpoint);
                if (isLocalEndpoint(url)) {
                    if (url.getPort() != port)
                        urls.add(url);
                } else
                    urls.add(url);
            } catch (MalformedURLException | UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        return urls;
    }

    private boolean isLocalEndpoint(URL endpoint) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(endpoint.getHost());
        if (address.isAnyLocalAddress() || address.isLoopbackAddress())
            return true;

        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    private enum State {STARTING, STARTED, STOPPING, STOPPED}
}
