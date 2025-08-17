package pg.imports.tests.data.common.importing;

import java.util.ArrayList;
import java.util.List;

public class InMemoryImportedPaymentsRepository {
    private final List<ImportedPayment> importedPayments = new ArrayList<>();

    void saveAll(final List<ImportedPayment> payments) {
        importedPayments.addAll(payments);
    }

    List<ImportedPayment> findAll() {
        return importedPayments;
    }
}
