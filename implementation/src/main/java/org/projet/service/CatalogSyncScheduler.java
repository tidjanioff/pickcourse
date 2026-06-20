package org.projet.service;

import org.projet.repository.CatalogCacheRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Planifie la synchronisation périodique du cache catalogue.
 */
public class CatalogSyncScheduler {
    private static final Logger LOGGER = Logger.getLogger(CatalogSyncScheduler.class.getName());
    private static final long DEFAULT_INTERVAL_HOURS = 12;

    private final CatalogSyncService syncService;
    private final CatalogCacheRepository cacheRepository;
    private final ScheduledExecutorService executor;

    public CatalogSyncScheduler() {
        this(new CatalogSyncService(), new CatalogCacheRepository());
    }

    CatalogSyncScheduler(CatalogSyncService syncService, CatalogCacheRepository cacheRepository) {
        this.syncService = syncService;
        this.cacheRepository = cacheRepository;
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "catalog-sync");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        long interval = syncIntervalHours();

        try {
            if (cacheRepository.isEmpty()) {
                executor.execute(syncService::syncAll);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to check catalog cache state before startup sync", e);
        }

        executor.scheduleAtFixedRate(syncService::syncAll, interval, interval, TimeUnit.HOURS);
    }

    private long syncIntervalHours() {
        String value = System.getenv("PICKCOURSE_SYNC_INTERVAL_HOURS");
        if (value == null || value.isBlank()) {
            return DEFAULT_INTERVAL_HOURS;
        }

        try {
            long interval = Long.parseLong(value);
            return interval > 0 ? interval : DEFAULT_INTERVAL_HOURS;
        } catch (NumberFormatException e) {
            return DEFAULT_INTERVAL_HOURS;
        }
    }
}
