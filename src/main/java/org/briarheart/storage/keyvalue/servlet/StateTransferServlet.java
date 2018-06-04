package org.briarheart.storage.keyvalue.servlet;

import org.briarheart.storage.keyvalue.KeyValueStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Chigvintsev
 */
public class StateTransferServlet extends HttpServlet {
    public static final String NAME = "stateTransferServlet";

    private final KeyValueStorage storage;

    public StateTransferServlet(KeyValueStorage storage) {
        this.storage = storage;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OutputStream out = resp.getOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        Map<String, byte[]> state = new HashMap<>();
        for (String key : storage.keySet())
            state.put(key, storage.get(key));
        objectOut.writeObject(state);
        objectOut.flush();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
