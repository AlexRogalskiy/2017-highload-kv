package org.briarheart.storage.keyvalue.servlet;

import org.briarheart.storage.keyvalue.KeyValueStorage;
import org.briarheart.storage.keyvalue.Quorum;
import org.briarheart.storage.keyvalue.StorageException;
import org.briarheart.storage.keyvalue.StorageSettings;
import org.briarheart.storage.keyvalue.event.StorageEvent;
import org.briarheart.storage.keyvalue.event.StorageEventListener;
import org.briarheart.storage.keyvalue.event.UpsertEvent;
import org.briarheart.storage.keyvalue.replication.ReplicationException;
import org.briarheart.storage.keyvalue.util.Arguments;
import org.briarheart.storage.keyvalue.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Chigvintsev
 */
public class EntityServlet extends HttpServlet {
    public static final String NAME = "entityServlet";

    private static final Logger log = LoggerFactory.getLogger(EntityServlet.class);

    private final KeyValueStorage storage;
    private final StorageEventListener eventListener;

    private CountDownLatch startLatch;

    public EntityServlet(KeyValueStorage storage, StorageEventListener eventListener, CountDownLatch startLatch) {
        Arguments.assertNotNull(storage, "Storage cannot be null");
        this.storage = storage;
        this.eventListener = eventListener;
        this.startLatch = startLatch;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!waitStart()) {
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            return;
        }

        String id = req.getParameter("id");
        if (Strings.isNullOrEmpty(id)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id is not specified");
            return;
        }

        byte[] value = storage.get(id);
        if (value == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentLength(value.length);
        resp.getOutputStream().write(value);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!waitStart()) {
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            return;
        }

        String id = req.getParameter("id");
        if (Strings.isNullOrEmpty(id)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id is not specified");
            return;
        }

        String replicas = req.getParameter("replicas");
        Quorum quorum = Quorum.parse(replicas);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        InputStream in = new BufferedInputStream(req.getInputStream());
        int b;
        while ((b = in.read()) != -1)
            buf.write(b);

        byte[] value = buf.toByteArray();
        storage.upsert(id, value);

        try {
            dispatchEvent(new UpsertEvent(id, value, quorum));
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ReplicationException e) {
            log.error(e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!waitStart()) {
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            return;
        }

        String id = req.getParameter("id");
        if (Strings.isNullOrEmpty(id)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "id is not specified");
            return;
        }

        storage.remove(id);
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    private boolean waitStart() {
        try {
            return startLatch.await(StorageSettings.getSyncWaitTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void dispatchEvent(StorageEvent event) {
        if (eventListener != null)
            event.dispatch(eventListener);
    }
}
