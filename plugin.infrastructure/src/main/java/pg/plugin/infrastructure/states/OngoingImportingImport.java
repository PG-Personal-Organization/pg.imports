package pg.plugin.infrastructure.states;

public interface OngoingImportingImport extends Import {
    ImportingCompletedImport finishImporting();

    RejectedImport rejectImporting(String reason);

}
