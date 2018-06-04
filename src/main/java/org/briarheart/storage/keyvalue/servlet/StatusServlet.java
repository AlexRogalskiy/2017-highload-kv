package org.briarheart.storage.keyvalue.servlet;

import org.briarheart.storage.keyvalue.ReplicatedKeyValueStorageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Roman Chigvintsev
 */
public class StatusServlet extends HttpServlet {
    public static final String NAME = "statusServlet";

    private final ReplicatedKeyValueStorageService storageService;

    public StatusServlet(ReplicatedKeyValueStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (storageService.isAvailable())
            resp.setStatus(HttpServletResponse.SC_OK);
        else
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
}
