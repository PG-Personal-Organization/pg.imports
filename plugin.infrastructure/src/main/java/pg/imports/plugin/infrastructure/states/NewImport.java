package pg.imports.plugin.infrastructure.states;

public interface NewImport extends Import {
    OngoingParsingImport startParsing();
}
