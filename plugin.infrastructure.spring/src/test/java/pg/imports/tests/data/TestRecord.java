package pg.imports.tests.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beanio.annotation.Field;
import pg.plugin.api.strategies.db.RecordData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.beanio.annotation.Record
public class TestRecord implements RecordData {
    @Field(at = 0)
    private String name;
    @Field(at = 1)
    private String value;
    @Field(at = 2)
    private Integer orderId;
}
