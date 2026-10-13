package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.seed.types.SeedTable;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Removes the rows a seed run created, plus every row that depends on them (for example an enrollment a
 * demo visitor requested as a seeded student). Rows that don't reach seeded data are never touched.
 * The dry run and the real rollback build the same {@link Plan}, so what the dry run reports is what goes.
 */
@Service
public class SeedRollback {
    private static final Logger LOG = LogManager.getLogger(SeedRollback.class);

    record ForeignKey(SeedTable child, String column, SeedTable parent) {
        String describe() {
            return child.getTableName() + "." + column + " -> " + parent.getTableName();
        }
    }

    /*
     * Every foreign key that points at a seeded table is in exactly one of these two lists, and
     * SeedRollbackForeignKeyTests compares them with the live schema. A migration that adds a foreign key
     * to a seeded table fails that test until the key is listed here.
     */

    // Deleting the parent row deletes the child rows that reference it
    static final List<ForeignKey> CASCADED = List.of(
        new ForeignKey(SeedTable.COURSE, "departmentId", SeedTable.DEPARTMENT),
        new ForeignKey(SeedTable.STUDENT, "departmentId", SeedTable.DEPARTMENT),
        new ForeignKey(SeedTable.INSTRUCTOR, "departmentId", SeedTable.DEPARTMENT),
        new ForeignKey(SeedTable.STUDENT_REGISTRATION, "departmentId", SeedTable.DEPARTMENT),
        new ForeignKey(SeedTable.INSTRUCTOR_REGISTRATION, "departmentId", SeedTable.DEPARTMENT),
        new ForeignKey(SeedTable.STUDENT, "userId", SeedTable.USERS),
        new ForeignKey(SeedTable.INSTRUCTOR, "userId", SeedTable.USERS),
        new ForeignKey(SeedTable.COURSE_OFFERING, "courseId", SeedTable.COURSE),
        new ForeignKey(SeedTable.COURSE_OFFERING, "semesterId", SeedTable.SEMESTER),
        new ForeignKey(SeedTable.COURSE_OFFERING, "instructorId", SeedTable.INSTRUCTOR),
        new ForeignKey(SeedTable.ENROLLMENT, "studentId", SeedTable.STUDENT),
        new ForeignKey(SeedTable.ENROLLMENT, "courseOfferingId", SeedTable.COURSE_OFFERING)
    );

    // The child row stays and the reference is cleared: a registration outlives the user who reviewed it
    static final List<ForeignKey> CLEARED = List.of(
        new ForeignKey(SeedTable.STUDENT_REGISTRATION, "reviewedBy", SeedTable.USERS),
        new ForeignKey(SeedTable.INSTRUCTOR_REGISTRATION, "reviewedBy", SeedTable.USERS)
    );

    // Followed in reverse: a profile and its login live and die together, so a deleted profile takes its user with it
    private static final List<ForeignKey> PROFILE_USERS = List.of(
        new ForeignKey(SeedTable.STUDENT, "userId", SeedTable.USERS),
        new ForeignKey(SeedTable.INSTRUCTOR, "userId", SeedTable.USERS)
    );

    private final SeedTracking seedTracking;
    private final NamedParameterJdbcTemplate jdbc;
    private final EntityManager entityManager;
    private final Clock clock;

    public SeedRollback(SeedTracking seedTracking, NamedParameterJdbcTemplate jdbc, EntityManager entityManager, Clock clock) {
        this.seedTracking = seedTracking;
        this.jdbc = jdbc;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    public record Plan(int runId, Map<SeedTable, Set<Integer>> seeded, Map<SeedTable, Set<Integer>> toDelete) {
        public int dependentCount(SeedTable table) {
            return toDelete.getOrDefault(table, Set.of()).size() - seeded.getOrDefault(table, Set.of()).size();
        }

        String summary() {
            return toDelete.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> entry.getKey().getTableName() + "=" + entry.getValue().size()
                    + " (" + dependentCount(entry.getKey()) + " dependent)")
                .collect(Collectors.joining(", "));
        }
    }

    @Transactional(readOnly = true)
    public Optional<Plan> dryRun() {
        var plan = plan();
        if (plan.isEmpty()) {
            LOG.info("Seed rollback dry run: no active seed run, nothing would be deleted");
        } else {
            LOG.info("Seed rollback dry run for run {} would delete: {}", plan.get().runId(), plan.get().summary());
        }
        return plan;
    }

    @Transactional
    public Optional<Plan> rollback() {
        var plan = plan();
        if (plan.isEmpty()) {
            LOG.info("Seed rollback: no active seed run, nothing to do");
            return plan;
        }
        execute(plan.get());
        seedTracking.markRolledBack(plan.get().runId(), Instant.now(clock));
        // The JDBC deletes bypassed the persistence context; drop anything it still holds for those rows
        entityManager.clear();
        LOG.info("Seed run {} rolled back, deleted: {}", plan.get().runId(), plan.get().summary());
        return plan;
    }

    private Optional<Plan> plan() {
        // Pending JPA writes must reach the database before the JDBC queries below can see them
        entityManager.flush();
        return seedTracking.findActiveRun().map(run -> {
            var seeded = seedTracking.findRows(run.id());
            return new Plan(run.id(), seeded, withDependents(seeded));
        });
    }

    // Follows the keys until nothing new turns up; the profile -> user rule loops back, so one pass isn't enough
    private Map<SeedTable, Set<Integer>> withDependents(Map<SeedTable, Set<Integer>> seeded) {
        Map<SeedTable, Set<Integer>> rows = new EnumMap<>(SeedTable.class);
        seeded.forEach((table, ids) -> rows.put(table, new LinkedHashSet<>(ids)));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (var key : CASCADED) {
                var childIds = select("id", key.child(), key.column(), rows.getOrDefault(key.parent(), Set.of()));
                changed |= rows.computeIfAbsent(key.child(), t -> new LinkedHashSet<>()).addAll(childIds);
            }
            for (var key : PROFILE_USERS) {
                var userIds = select(key.column(), key.child(), "id", rows.getOrDefault(key.child(), Set.of()));
                changed |= rows.computeIfAbsent(key.parent(), t -> new LinkedHashSet<>()).addAll(userIds);
            }
        }
        return rows;
    }

    // Column and table names only ever come from the constants above, never from input
    private List<Integer> select(String column, SeedTable table, String whereColumn, Set<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        var sql = "SELECT " + column + " FROM " + table.getTableName() + " WHERE " + whereColumn + " IN (:ids)";
        return jdbc.queryForList(sql, Map.of("ids", ids), Integer.class);
    }

    private void execute(Plan plan) {
        var rows = plan.toDelete();
        var enrollments = rows.getOrDefault(SeedTable.ENROLLMENT, Set.of());
        var offerings = rows.getOrDefault(SeedTable.COURSE_OFFERING, Set.of());

        // Offerings that survive but lose enrollments need their seat count recomputed afterwards
        Set<Integer> survivingOfferings = new LinkedHashSet<>();
        if (!enrollments.isEmpty()) {
            survivingOfferings.addAll(jdbc.queryForList(
                "SELECT DISTINCT courseOfferingId FROM enrollment WHERE id IN (:ids)",
                Map.of("ids", enrollments),
                Integer.class
            ));
            survivingOfferings.removeAll(offerings);
        }

        for (var key : CLEARED) {
            var parentIds = rows.getOrDefault(key.parent(), Set.of());
            if (!parentIds.isEmpty()) {
                jdbc.update(
                    "UPDATE " + key.child().getTableName() + " SET " + key.column() + " = NULL WHERE " + key.column() + " IN (:ids)",
                    Map.of("ids", parentIds)
                );
            }
        }

        for (var table : SeedTable.values()) {
            var ids = rows.getOrDefault(table, Set.of());
            if (!ids.isEmpty()) {
                jdbc.update("DELETE FROM " + table.getTableName() + " WHERE id IN (:ids)", Map.of("ids", ids));
            }
        }

        if (!survivingOfferings.isEmpty()) {
            jdbc.update(
                "UPDATE course_offering o SET enrolled = "
                    + "(SELECT COUNT(*) FROM enrollment e WHERE e.courseOfferingId = o.id AND e.status = 'ENROLLED') "
                    + "WHERE o.id IN (:ids)",
                Map.of("ids", survivingOfferings)
            );
        }
    }
}
