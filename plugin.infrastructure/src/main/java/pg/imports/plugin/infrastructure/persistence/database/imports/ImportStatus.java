package pg.imports.plugin.infrastructure.persistence.database.imports;

public enum ImportStatus {
    NEW,
    ONGOING_PARSING,
    PARSING_FINISHED,
    ONGOING_IMPORTING,
    PARSING_FAILED,
    IMPORTING_FAILED,
    IMPORTING_COMPLETED,
}
