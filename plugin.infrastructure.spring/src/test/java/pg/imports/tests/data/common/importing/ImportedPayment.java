package pg.imports.tests.data.common.importing;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ToString
@EqualsAndHashCode
@Data
@Builder
@AllArgsConstructor
public class ImportedPayment {
    private UUID id;
    private String name;
    private BigDecimal value;
    private Integer orderId;
    private String importId;
    private LocalDateTime importedOn;
}
