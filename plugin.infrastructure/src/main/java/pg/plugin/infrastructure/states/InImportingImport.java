package pg.plugin.infrastructure.states;

public interface InImportingImport extends Import {
    ImportingCompletedImport finishImporting();

    RejectedImport rejectImporting(String reason);

}
