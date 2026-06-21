package org.projet.controller;

import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projet.service.CatalogSyncService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminControllerTest {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private Javalin app;
    private CatalogSyncService syncService;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        syncService = mock(CatalogSyncService.class);
        port = findFreePort();

        Executor directExecutor = Runnable::run;
        AdminController controller = new AdminController(syncService, () -> "test-token", directExecutor);

        app = Javalin.create().start(port);
        app.post("/admin/sync", controller::syncCatalog);
    }

    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void syncAdminEndpointAcceptsValidToken() throws Exception {
        HttpResponse<String> response = sendSyncRequest("test-token");

        assertEquals(202, response.statusCode());
        assertEquals("Sync started", response.body());
        verify(syncService).syncAll();
    }

    @Test
    void syncAdminEndpointRejectsMissingToken() throws Exception {
        HttpResponse<String> response = sendSyncRequest(null);

        assertEquals(401, response.statusCode());
        assertEquals("Unauthorized", response.body());
        verify(syncService, never()).syncAll();
    }

    @Test
    void syncAdminEndpointRejectsIncorrectToken() throws Exception {
        HttpResponse<String> response = sendSyncRequest("wrong-token");

        assertEquals(401, response.statusCode());
        assertEquals("Unauthorized", response.body());
        verify(syncService, never()).syncAll();
    }

    private HttpResponse<String> sendSyncRequest(String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/admin/sync"))
                .POST(HttpRequest.BodyPublishers.noBody());

        if (token != null) {
            builder.header("X-Admin-Token", token);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
