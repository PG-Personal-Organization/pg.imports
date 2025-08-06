package pg.imports.plugin.infrastructure.persistence.imports;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.imports.plugin.infrastructure.processing.errors.ImportNotExistsException;

import java.util.Optional;

public interface ImportRepository extends JpaRepository<ImportEntity, String> {
    Optional<ImportEntity> findByIdAndStatus(String id, ImportStatus status);

    default ImportEntity getNewImport(final String id) {
        return findByIdAndStatus(id, ImportStatus.NEW)
                .orElseThrow(() -> new ImportNotExistsException("Scheduled import with id " + id + " not found."));
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
