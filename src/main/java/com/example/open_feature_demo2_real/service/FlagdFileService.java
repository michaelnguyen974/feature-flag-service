package com.example.open_feature_demo2_real.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FlagdFileService {
    private static final Logger log = LoggerFactory.getLogger(FlagdFileService.class);
    private final String flagsFilePath;
    private final ObjectMapper objectMapper;
    private static final String ON = "on";

    // TODO: filepath for flagd.json correct?
    public FlagdFileService(@Value("${flagd.file.path:./flags.flagd.json}") String flagsFilePath) {
        this.flagsFilePath = flagsFilePath;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getAllFlags() throws IOException {
        File file = new File(flagsFilePath);
        if (!file.exists()) {
            createDefaultFlagsFile();
        }

        JsonNode root = objectMapper.readTree(file);
        JsonNode flags = root.get("flags");

        Map<String, Object> flagsMap = new HashMap<>();
        if (flags != null) {
            flags.fields().forEachRemaining(entry -> {
                String flagName = entry.getKey();
                JsonNode flagConfig = entry.getValue();

                Map<String, Object> flagInfo = new HashMap<>();
                flagInfo.put("state", flagConfig.get("state").asText());
                flagInfo.put("defaultVariant", flagConfig.get("defaultVariant"));

                JsonNode variants = flagConfig.get("variants");
                Map<String, Object> variantsMap = new HashMap<>();

                if (variants != null) {
                    variants.fields().forEachRemaining(variant ->
                            variantsMap.put(variant.getKey(), variant.getValue().asText())
                    );
                }
                flagInfo.put("variants", variantsMap);

                boolean isEnabled = "ENABLED".equals(flagConfig.get("state").asText()) &&
                        "on".equals(flagConfig.get("defaultVariant").asText());
                flagInfo.put("enabled", isEnabled);

                flagsMap.put(flagName, flagInfo);
            });
        }
        return flagsMap;
    }

    public void toggleFlag(String flagName) throws IOException {
        File file = new File(flagsFilePath);
        JsonNode root = objectMapper.readTree(file);

        ObjectNode flags = (ObjectNode) root.get("flags");
        if (flags != null && flags.has(flagName)) {
            ObjectNode flag = (ObjectNode) flags.get(flagName);
            String currentVariant = flag.get("defaultVariant").asText();
            String newVariant = ON.equals(currentVariant) ? "off" : "on";
            flag.put("defaultVariant", newVariant);
            flag.put("state", "ENABLED");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        }
    }

    public void enableAllFlags() throws IOException {
        File file = new File(flagsFilePath);
        JsonNode root = objectMapper.readTree(file);

        ObjectNode flags = (ObjectNode) root.get("flags");
        if (flags != null) {
            flags.fields().forEachRemaining(entry -> {
                ObjectNode flag = (ObjectNode) entry.getValue();
                flag.put("defaultVariant", "on");
                flag.put("state", "ENABLED");
            });

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
            log.info("Enabling all flags");
        }
    }

    public void disableAllFlags() throws IOException {
        File file = new File(flagsFilePath);
        JsonNode root = objectMapper.readTree(file);

        ObjectNode flags = (ObjectNode) root.get("flags");
        if (flags != null) {
            flags.fields().forEachRemaining(entry -> {
                ObjectNode flag = (ObjectNode) entry.getValue();
                flag.put("defaultVariant", "off");
                flag.put("state", "DISABLED"); // changed to DISABLED

                log.info("Disabled all feature flags with flag: {}", flag.get(0));

            });

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        }
    }

    private void createDefaultFlagsFile() throws IOException {
        String defaultContent = """
                {
                  "flags": {
                    "welcome-message": {
                      "variants": {
                        "on": true,
                        "off": false
                      },
                      "state": "ENABLED",
                      "defaultVariant": "off"
                    }
                  }
                }
                """;

        File file = new File(flagsFilePath);
        file.getParentFile().mkdirs();
        objectMapper.writeValue(file, objectMapper.readTree(defaultContent));
    }
}
