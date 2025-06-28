package me.bombardeen.autoproxyshutdown;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@Plugin(
        id = "autoproxyshutdown",
        name = "AutoProxyShutdown",
        version = "1.0.0",
        description = "Automatically shutdowns the proxy after a defined time when there are no players.")
public class AutoShutdownPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Instant startTime;
    private Duration restartAfter;

    @Inject
    public AutoShutdownPlugin(ProxyServer server, Logger logger, Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadConfig();

        this.startTime = Instant.now();
        logger.info("[AutoShutdown] Proxy start time recorded.");

        server.getScheduler().buildTask(this, this::checkAndShutdown)
                .repeat(Duration.ofMinutes(1))
                .schedule();
    }

    private void checkAndShutdown() {
        Duration uptime = Duration.between(startTime, Instant.now());

        if (uptime.compareTo(restartAfter) >= 0) {
            int playersOnline = server.getAllPlayers().size();

            if (playersOnline == 0) {
                logger.info("[AutoShutdown] {} passed and no players online. Shutting down...", restartAfter);
                System.exit(0);
            } else {
                logger.info("[AutoShutdown] {} passed but {} player(s) online. Shutdown skipped.", restartAfter, playersOnline);
            }
        }
    }

    private void loadConfig() {
        try {
            Path configPath = dataDirectory.resolve("config.yml");

            if (!Files.exists(configPath)) {
                Files.createDirectories(dataDirectory);
                Files.writeString(configPath, "RESTART_AFTER: \"12h\"\n");
                logger.info("[AutoShutdown] Created default config.yml");
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            ConfigurationNode root = loader.load();
            String timeStr = root.node("RESTART_AFTER").getString("12h");
            this.restartAfter = parseDuration(timeStr);

        } catch (IOException e) {
            logger.error("[AutoShutdown] Failed to load config.yml, using default 12h.", e);
            this.restartAfter = Duration.ofHours(12);
        }
    }

    private Duration parseDuration(String input) {
        input = input.toLowerCase().trim();
        if (input.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(input.replace("h", "").trim()));
        } else if (input.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(input.replace("m", "").trim()));
        } else if (input.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(input.replace("s", "").trim()));
        } else {
            logger.warn("[AutoShutdown] Invalid RESTART_AFTER format '{}', defaulting to 12h", input);
            return Duration.ofHours(12);
        }
    }
}
