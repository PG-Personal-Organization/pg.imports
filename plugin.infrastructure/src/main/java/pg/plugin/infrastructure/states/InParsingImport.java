package pg.plugin.infrastructure.states;

public interface InParsingImport extends Import {
    AfterParsingImport finishParsing();
}
