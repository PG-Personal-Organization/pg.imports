package pg.plugin.infrastructure.persistence.imports;

public enum ImportStatus {
    NEW,
    ONGOING_PARSING,
    PARSING_FINISHED,
    ONGOING_IMPORTING,
    FAILED,
    COMPLETED,
}
