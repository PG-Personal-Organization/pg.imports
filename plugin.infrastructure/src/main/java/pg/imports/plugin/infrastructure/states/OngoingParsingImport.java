package pg.imports.plugin.infrastructure.states;

public interface OngoingParsingImport extends Import {
    ParsingCompletedImport finishParsing();

    RejectedImport rejectParsing(String reason);

}
