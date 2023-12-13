package rt.sae32.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Remove duplicate keys from a JSON file
 * <p>
 *     This class is used to remove duplicate keys from a JSON file.
 *     It is used in the Main class.
 *     It takes a JSON file as input and returns a JSONArray.
 *
 */
public class RemoveDuplicateKeys {
    public static JSONArray main(String InputFile) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Load json file
            File jsonFile = new File(InputFile);
            JsonNode rootNode = mapper.readTree(jsonFile);

            // Delete duplicate keys
            JsonNode cleanedNode = removeDuplicateKeys(rootNode);

            // Return cleaned json
            return new JSONArray(cleanedNode.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JsonNode removeDuplicateKeys(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Set<String> fieldNames = new HashSet<>();
            Iterator<String> fieldIterator = objectNode.fieldNames();

            while (fieldIterator.hasNext()) {
                String fieldName = fieldIterator.next();
                if (!fieldNames.add(fieldName)) {
                    // Duplicate key found, remove it
                    fieldIterator.remove();
                } else {
                    // Clean field value
                    JsonNode fieldValue = objectNode.get(fieldName);
                    objectNode.set(fieldName, removeDuplicateKeys(fieldValue));
                }
            }
        } else if (node.isArray()) {
            // Clean array elements
            for (JsonNode arrayElement : node) {
                removeDuplicateKeys(arrayElement);
            }
        }

        return node;
    }
}
