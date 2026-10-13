package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.seed.types.SeedMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("!test")
@Order(Ordered.LOWEST_PRECEDENCE)
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger LOG = LogManager.getLogger(DemoDataSeeder.class);

    private final SeedApplier seedApplier;
    private final SeedRollback seedRollback;
    private final ResourceLoader resourceLoader;
    private final String mode;
    private final String fileLocation;
    private final boolean allowExamplePasswords;

    public DemoDataSeeder(
        SeedApplier seedApplier,
        SeedRollback seedRollback,
        ResourceLoader resourceLoader,
        @Value("${app.seed.mode:}") String mode,
        @Value("${app.seed.file:}") String fileLocation,
        @Value("${app.seed.allow-example-passwords:false}") boolean allowExamplePasswords
    ) {
        this.seedApplier = seedApplier;
        this.seedRollback = seedRollback;
        this.resourceLoader = resourceLoader;
        this.mode = mode;
        this.fileLocation = fileLocation;
        this.allowExamplePasswords = allowExamplePasswords;
    }

    @Override
    public void run(String... args) {
        if (mode.isBlank()) {
            return;
        }
        switch (SeedMode.fromProperty(mode)) {
            case APPLY -> apply();
            case ROLLBACK_DRY_RUN -> seedRollback.dryRun();
            case ROLLBACK -> seedRollback.rollback();
        }
    }

    private void apply() {
        if (fileLocation.isBlank()) {
            throw new SeedException("app.seed.file is required when app.seed.mode=apply");
        }
        var content = read(resolve(fileLocation));
        try {
            seedApplier.apply(content, allowExamplePasswords);
        } catch (DataIntegrityViolationException e) {
            throw new SeedException(
                "Seed file " + fileLocation + " conflicts with data already in the database; nothing was seeded. "
                    + "Cause: " + e.getMostSpecificCause().getMessage(),
                e
            );
        }
    }

    private Resource resolve(String location) {
        if (location.startsWith("classpath:")) {
            return resourceLoader.getResource(location);
        }
        return new FileSystemResource(location.replaceFirst("^file:", ""));
    }

    private byte[] read(Resource resource) {
        try (var in = resource.getInputStream()) {
            LOG.info("Reading seed file {}", resource.getDescription());
            return in.readAllBytes();
        } catch (IOException e) {
            throw new SeedException("Cannot read seed file " + fileLocation + ": " + e.getMessage(), e);
        }
    }
}
