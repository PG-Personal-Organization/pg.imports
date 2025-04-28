package pg.plugin.api.parsing;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderOutputItem<TYPE> implements Serializable {

    private String id;

    private int itemNumber;

    private TYPE rawItem;

    private String partitionId;

    private int chunkNumber;

}
