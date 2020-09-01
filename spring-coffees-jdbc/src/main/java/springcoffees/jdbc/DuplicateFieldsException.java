package springcoffees.jdbc;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.stream.Collectors;

/*
 * Exception, thrown when an attempt to insert or update data results in violation of a primary key
 * or unique constraints. Note: it slightly differs from the library's DuplicateKeyException in that
 * it provides access to unmodifiable map of names and values for all rejected duplicate fields.
 */
public class DuplicateFieldsException extends DataIntegrityViolationException {
    private final Map<String, String> duplicateFields;

    private DuplicateFieldsException(String message, Map<String, String> duplicateFields) {
        super(message);
        this.duplicateFields = duplicateFields;
    }

    public static DuplicateFieldsException constructFrom(Map<String, String> duplicateFields) {
        Map<String, String> duplicates = Map.copyOf(duplicateFields);
        String message = duplicates.entrySet().stream()
                .map(entry -> String.format("field '%s' with value '%s' already exists",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; ", "Attempt to update or insert new records " +
                        "with repeated values that are supposed to be unique: ", "."));
        return new DuplicateFieldsException(message, duplicates);
    }

    public Map<String, String> getDuplicateFields() {
        return duplicateFields;
    }

}
