package mapanalyzer;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MapComplexFilterZimaIntegrationTest {

    @Test
    void analyzesSmallOmapFile() throws Exception {
        // Minimal .omap-like XML with one simple and one complex area object
        String omapXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <map>
                  <symbol id="s1" code="101">
                    <area_symbol/>
                  </symbol>
                  <symbol id="s2" code="102">
                    <area_symbol/>
                  </symbol>

                  <!-- Simple triangle -->
                  <object id="1" symbol="s1">
                    <coords>
                      0 0 0; 1 0 0; 0 1 0; 0 0 0;
                    </coords>
                  </object>

                  <!-- Complex quadrilateral -->
                  <object id="2" symbol="s2">
                    <coords>
                      0 0 0; 1 0 0; 1 1 0; 0 1 0; 0 0 0;
                    </coords>
                  </object>
                </map>
                """;

        Path tempFile = Files.createTempFile("test-map", ".omap");
        Files.writeString(tempFile, omapXml, StandardCharsets.UTF_8);

        // Prepare fake stdin and capture stdout
        String input = tempFile.toAbsolutePath() + System.lineSeparator();
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));

            MapComplexFilterZima.main(new String[0]);

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            Files.deleteIfExists(tempFile);
        }

        String output = outContent.toString(StandardCharsets.UTF_8);
        String normalized = output.replaceAll("\\s+", " ");

        // Basic checks on reported statistics
        assertTrue(normalized.contains("Area objects (analyzed): 2"),
                "Program should report 2 analyzed area objects.");
        assertTrue(normalized.contains("Simple area objects: 1"),
                "Program should report 1 simple area object.");
        assertTrue(normalized.contains("Complex area objects: 1"),
                "Program should report 1 complex area object.");
    }
}
