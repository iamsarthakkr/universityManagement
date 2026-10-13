package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.seed.types.SeedTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rollback follows SeedRollback.CASCADED / CLEARED, not the database, so a foreign key added by a migration
 * and missing there would make rollback fail on that constraint (or leave rows behind). This keeps the
 * lists equal to the schema Flyway actually built.
 */
public class SeedRollbackForeignKeyTests extends IntegrationTests {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    void everyForeignKeyToASeededTableShouldBeHandledByRollback() {
        var seededTables = Arrays.stream(SeedTable.values()).map(SeedTable::getTableName).toList();

        var inSchema = new TreeSet<>(jdbc.query(
            "SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE "
                + "WHERE TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME IN (:tables)",
            Map.of("tables", seededTables),
            (rs, rowNum) -> rs.getString("TABLE_NAME") + "." + rs.getString("COLUMN_NAME")
                + " -> " + rs.getString("REFERENCED_TABLE_NAME")
        ));
        var inRollback = new TreeSet<>(Stream.concat(SeedRollback.CASCADED.stream(), SeedRollback.CLEARED.stream())
            .map(SeedRollback.ForeignKey::describe)
            .toList());

        assertEquals(inSchema, inRollback, () ->
            "Foreign keys to seeded tables and SeedRollback.CASCADED / CLEARED differ.\n"
                + "  In the schema but not handled by rollback: " + difference(inSchema, inRollback) + "\n"
                + "  Handled by rollback but not in the schema: " + difference(inRollback, inSchema) + "\n"
                + "Add new keys to CASCADED (child rows are deleted) or CLEARED (reference set to NULL); "
                + "a child table the seed doesn't know yet also needs a SeedTable value, in delete order."
        );
    }

    @Test
    void shouldOnlyClearNullableColumns() {
        for (var key : SeedRollback.CLEARED) {
            var nullable = jdbc.queryForObject(
                "SELECT IS_NULLABLE FROM information_schema.COLUMNS "
                    + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :table AND COLUMN_NAME = :column",
                Map.of("table", key.child().getTableName(), "column", key.column()),
                String.class
            );
            assertEquals("YES", nullable, key.describe() + " is in CLEARED but the column is NOT NULL");
        }
    }

    // Rollback clears these itself, but deleting a user any other way must not fail on them either
    @Test
    void shouldSetClearedColumnsToNullOnDeleteInTheSchemaToo() {
        for (var key : SeedRollback.CLEARED) {
            var deleteRule = jdbc.queryForObject(
                "SELECT rc.DELETE_RULE FROM information_schema.REFERENTIAL_CONSTRAINTS rc "
                    + "JOIN information_schema.KEY_COLUMN_USAGE kcu "
                    + "ON kcu.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA AND kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME "
                    + "WHERE rc.CONSTRAINT_SCHEMA = DATABASE() AND kcu.TABLE_NAME = :table AND kcu.COLUMN_NAME = :column",
                Map.of("table", key.child().getTableName(), "column", key.column()),
                String.class
            );
            assertEquals("SET NULL", deleteRule, key.describe());
        }
    }

    @Test
    void shouldListEachForeignKeyOnlyOnce() {
        var all = Stream.concat(SeedRollback.CASCADED.stream(), SeedRollback.CLEARED.stream()).toList();

        assertEquals(all.size(), new HashSet<>(all).size(), "a foreign key is in CASCADED and CLEARED, or listed twice");
    }

    private static List<String> difference(Set<String> left, Set<String> right) {
        return left.stream().filter(value -> !right.contains(value)).toList();
    }
}
