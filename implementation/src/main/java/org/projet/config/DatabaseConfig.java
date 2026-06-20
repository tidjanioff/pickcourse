package org.projet.config;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

/**
 * Configuration centralisée de la connexion PostgreSQL.
 */
public final class DatabaseConfig {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/pickcourse";
    private static final String DEFAULT_USER = "pickcourse";
    private static final String DEFAULT_PASSWORD = "devpassword";

    private DatabaseConfig() {
    }

    public static String dbUrl() {
        return getenvOrDefault("PICKCOURSE_DB_URL", DEFAULT_URL);
    }

    public static String dbUser() {
        return getenvOrDefault("PICKCOURSE_DB_USER", DEFAULT_USER);
    }

    public static String dbPassword() {
        return getenvOrDefault("PICKCOURSE_DB_PASSWORD", DEFAULT_PASSWORD);
    }

    public static Jdbi createJdbi() {
        return Jdbi.create(dbUrl(), dbUser(), dbPassword());
    }

    public static void migrate() {
        Flyway.configure()
                .dataSource(dbUrl(), dbUser(), dbPassword())
                .load()
                .migrate();
    }

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
