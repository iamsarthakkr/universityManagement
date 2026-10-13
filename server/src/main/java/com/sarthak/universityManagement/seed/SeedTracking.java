package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.seed.types.SeedTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Bookkeeping for seed data for rollback plan
@Repository
public class SeedTracking {
    private final JdbcTemplate jdbcTemplate;

    public SeedTracking(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record SeedRun(int id, int formatVersion, String checksum, LocalDate anchorDate, Instant appliedAt) {}

    public Optional<SeedRun> findActiveRun() {
        var runs = jdbcTemplate.query(
            "SELECT id, formatVersion, checksum, anchorDate, appliedAt FROM seed_runs "
                + "WHERE rolledBackAt IS NULL ORDER BY id DESC",
            (rs, rowNum) -> new SeedRun(
                rs.getInt("id"),
                rs.getInt("formatVersion"),
                rs.getString("checksum"),
                rs.getDate("anchorDate").toLocalDate(),
                rs.getTimestamp("appliedAt").toInstant()
            )
        );
        if (runs.size() > 1) {
            throw new SeedException("Found " + runs.size() + " active seed runs; expected at most one");
        }
        return runs.stream().findFirst();
    }

    public int startRun(int formatVersion, String checksum, LocalDate anchorDate, Instant appliedAt) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO seed_runs (formatVersion, checksum, anchorDate, appliedAt) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, formatVersion);
            statement.setString(2, checksum);
            statement.setDate(3, Date.valueOf(anchorDate));
            statement.setTimestamp(4, Timestamp.from(appliedAt));
            return statement;
        }, keyHolder);
        return keyHolder.getKeyAs(Number.class).intValue();
    }

    public void recordRows(int runId, SeedTable table, Collection<Integer> rowIds) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO seed_rows (seedRunId, tableName, rowId) VALUES (?, ?, ?)",
            rowIds,
            rowIds.size(),
            (statement, rowId) -> {
                statement.setInt(1, runId);
                statement.setString(2, table.getTableName());
                statement.setInt(3, rowId);
            }
        );
    }

    public Map<SeedTable, Set<Integer>> findRows(int runId) {
        Map<SeedTable, Set<Integer>> rows = new EnumMap<>(SeedTable.class);
        jdbcTemplate.query(
            "SELECT tableName, rowId FROM seed_rows WHERE seedRunId = ? ORDER BY tableName, rowId",
            rs -> {
                var table = SeedTable.fromTableName(rs.getString("tableName"));
                rows.computeIfAbsent(table, t -> new LinkedHashSet<>()).add(rs.getInt("rowId"));
            },
            runId
        );
        return rows;
    }

    public void markRolledBack(int runId, Instant rolledBackAt) {
        jdbcTemplate.update(
            "UPDATE seed_runs SET rolledBackAt = ? WHERE id = ?",
            Timestamp.from(rolledBackAt),
            runId
        );
    }
}
