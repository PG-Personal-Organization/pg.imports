package pg.imports.plugin.infrastructure.states;

public interface ParsingCompletedImport extends Import {
    OngoingImportingImport startImporting();
}
