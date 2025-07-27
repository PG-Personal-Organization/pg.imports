package pg.imports.tests.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pg.plugin.api.strategies.db.RecordData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRecordData implements RecordData {
    private String name;
    private String value;
    private Integer orderId;
}
