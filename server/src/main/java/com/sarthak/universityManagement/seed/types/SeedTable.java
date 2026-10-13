package com.sarthak.universityManagement.seed.types;

// Tables a seed run writes to, in the order a rollback deletes from them (children before parents).
public enum SeedTable {
    ENROLLMENT("enrollment"),
    COURSE_OFFERING("course_offering"),
    SEMESTER("semester"),
    COURSE("course"),
    STUDENT_REGISTRATION("student_registration"),
    INSTRUCTOR_REGISTRATION("instructor_registration"),
    STUDENT("student"),
    INSTRUCTOR("instructor"),
    USERS("users"),
    DEPARTMENT("department");

    private final String tableName;

    SeedTable(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public static SeedTable fromTableName(String tableName) {
        for (var table : values()) {
            if (table.tableName.equals(tableName)) {
                return table;
            }
        }
        throw new IllegalStateException("Unknown seeded table '" + tableName + "' in seed_rows");
    }
}
