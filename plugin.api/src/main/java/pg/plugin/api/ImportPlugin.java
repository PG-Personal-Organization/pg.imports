package pg.plugin.api;

public interface ImportPlugin {
    Long BASIC_CHUNK = 200L;

    String getCode();

    String getVersion();

    String getCodeIdPrefix();

    default Long getChunkSize() {
        return BASIC_CHUNK;
    }
}
