package org.projet.controller;

import io.javalin.http.Context;
import org.projet.service.CatalogSyncService;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Admin routes for manual catalog maintenance.
 */
public class AdminController {
    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final CatalogSyncService syncService;
    private final Supplier<String> adminTokenSupplier;
    private final Executor executor;

    public AdminController() {
        this(new CatalogSyncService(), () -> System.getenv("PICKCOURSE_ADMIN_TOKEN"), daemonExecutor());
    }

    AdminController(CatalogSyncService syncService, Supplier<String> adminTokenSupplier, Executor executor) {
        this.syncService = Objects.requireNonNull(syncService, "syncService");
        this.adminTokenSupplier = Objects.requireNonNull(adminTokenSupplier, "adminTokenSupplier");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public void syncCatalog(Context ctx) {
        if (!isAuthorized(ctx)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        executor.execute(syncService::syncAll);
        ctx.status(202).result("Sync started");
    }

    private boolean isAuthorized(Context ctx) {
        String expectedToken = adminTokenSupplier.get();
        String providedToken = ctx.header(ADMIN_TOKEN_HEADER);
        return expectedToken != null
                && !expectedToken.isBlank()
                && providedToken != null
                && expectedToken.equals(providedToken);
    }

    private static Executor daemonExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "catalog-sync-admin");
            thread.setDaemon(true);
            return thread;
        });
    }
}
