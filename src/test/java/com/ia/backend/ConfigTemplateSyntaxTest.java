package com.ia.backend;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Guards the committed YAML templates: following the README's install steps copies
 * {@code application-dev.yaml.example}, so a broken template must fail CI instead of
 * shipping a fresh checkout that cannot start.
 */
class ConfigTemplateSyntaxTest {

    private static final List<String> YAML_FILES = List.of(
            "src/main/resources/application.yaml",
            "src/main/resources/application-dev.yaml.example",
            "src/main/resources/application-prod.yaml",
            "src/test/resources/application-test.yaml"
    );

    @Test
    void committedYamlTemplatesMustParse() throws Exception {
        for (String file : YAML_FILES) {
            String content = Files.readString(Path.of(file), StandardCharsets.UTF_8);
            assertNotNull(new Yaml().load(content), () -> file + " must parse as valid YAML");
        }
    }
}