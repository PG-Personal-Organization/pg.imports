package pg.imports.plugin.infrastructure.persistence.database.imports;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pg.imports.plugin.infrastructure.processing.errors.ImportNotExistsException;

import java.util.List;
import java.util.Optional;

public interface ImportRepository extends JpaRepository<ImportEntity, String> {
    @Query("select i from imports i where i.id = :id and i.status = :status")
    Optional<ImportEntity> findByIdAndStatus(@Param("id") String id,
                                             @Param("status") ImportStatus status);

    @Query("select i from imports i where i.id = :id and i.status in :statuses")
    Optional<ImportEntity> findByIdAndStatusIn(@Param("id") String id,
                                               @Param("statuses") List<ImportStatus> statuses);


    default ImportEntity getNewImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.NEW)
                .orElseThrow(() -> new ImportNotExistsException("Scheduled import with id " + id + " not found."));
    }

    default ImportEntity getRejectedParsingImport(final String id) {
        return findByIdAndStatusIn(id, List.of(ImportStatus.ONGOING_PARSING, ImportStatus.PARSING_FINISHED))
                .orElseThrow(() -> new ImportNotExistsException("Import in parsing/completed parsing with id " + id + " not found."));
    }

    default ImportEntity getParsingImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.ONGOING_PARSING)
                .orElseThrow(() -> new ImportNotExistsException("Import in parsing with id " + id + " not found."));
    }

    default ImportEntity getParsedImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.PARSING_FINISHED)
                .orElseThrow(() -> new ImportNotExistsException("Import in completed parsing with id " + id + " not found."));
    }

    default ImportEntity getImportingImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.ONGOING_IMPORTING)
                .orElseThrow(() -> new ImportNotExistsException("Import in importing with id " + id + " not found."));
    }

    default ImportEntity getCompletedImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.IMPORTING_COMPLETED)
                .orElseThrow(() -> new ImportNotExistsException("Import in completed importing with id " + id + " not found."));
    }
}
