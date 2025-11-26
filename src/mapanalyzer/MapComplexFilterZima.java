package mapanalyzer;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

// Reads .omap, finds used ISSprOM codes and counts complex objects by code
public class MapComplexFilterZima {

    // Default ISSprOM codes treated as complex
    public static final Set<String> DEFAULT_COMPLEX_CODES = new HashSet<>(Arrays.asList("201", "203", "206", "207", "208", "210",
            "301", "307", "310",
            "410", "411",
            "107", "113",

            "515", "518", "520", "529", "533",
            "512.1", "512.2", "512.3",

            "708", "709",

            "104", "202", "308", "406", "408", "521"
            "201.0", "203.0", "206.0", "207.0", "208.0", "210.0",
            "301.0", "307.0", "310.0",
            "410.0", "411.0",
            "107.0", "113.0",

            "515.0", "518.0", "520.0", "529.0", "533.0",
            "512.1", "512.2", "512.3",

            "708.0", "709.0",

            "104.0", "202.0", "308.0", "406.0", "408.0", "521.0"
    ));

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter .omap file path: ");
        String path = scanner.nextLine().trim();

        File file = new File(path);
        if (!file.isFile()) {
            System.err.println("File not found: " + file.getAbsolutePath());
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        Map<String, String> symbolDefs = getSymbolDefinitions(doc);

        int totalObjects = countAllObjectsWithCode(doc, symbolDefs);
        int complexObjects = countComplexObjects(doc, symbolDefs, DEFAULT_COMPLEX_CODES);

        Set<String> usedCodes = getUsedSymbolCodes(doc, symbolDefs);
        Set<String> complexCodesOnMap = new LinkedHashSet<>(usedCodes);
        complexCodesOnMap.retainAll(DEFAULT_COMPLEX_CODES);

        System.out.println("Used symbol codes on map:");
        int i = 0;
        for (String code : usedCodes) {
            i++;
            if (i == 10) {
                System.out.println();
                i = 0;
            }
            System.out.print(code + ", ");
        }

        System.out.println();
        System.out.println("Map file:              " + file.getName());
        System.out.println("Total objects:         " + totalObjects);
        System.out.println("Complex objects:       " + complexObjects);
        System.out.println("Complex codes (default): " + DEFAULT_COMPLEX_CODES);
        System.out.println("Complex codes on map:  " + complexCodesOnMap);
    }

    // Returns map: symbol id -> ISSprOM code
    private static Map<String, String> getSymbolDefinitions(Document doc) {
        Map<String, String> map = new HashMap<>();
        NodeList symbolNodes = doc.getElementsByTagName("symbol");
        for (int i = 0; i < symbolNodes.getLength(); i++) {
            Element symbolElement = (Element) symbolNodes.item(i);
            String id = symbolElement.getAttribute("id");
            String code = symbolElement.getAttribute("code");
            if (!id.isEmpty() && !code.isEmpty()) {
                map.put(id, code);
            }
        }
        return map;
    }

    // Returns set of ISSprOM codes used by objects
    private static Set<String> getUsedSymbolCodes(Document doc, Map<String, String> symbolDefs) {
        Set<String> used = new LinkedHashSet<>();

        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            String symbolId = objectElement.getAttribute("symbol");
            if (symbolId == null || symbolId.isEmpty()) continue;

            String code = symbolDefs.getOrDefault(symbolId, symbolId).trim();
            if (!code.isEmpty()) {
                used.add(code);
            }
        }
        return used;
    }

    // Returns number of objects that have any symbol code
    private static int countAllObjectsWithCode(Document doc,
                                               Map<String, String> symbolDefs) {
        int count = 0;
        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            String symbolId = objectElement.getAttribute("symbol");
            if (symbolId == null || symbolId.isEmpty()) continue;

            String code = symbolDefs.getOrDefault(symbolId, symbolId).trim();
            if (!code.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // Returns number of objects whose ISSprOM code is in complexCodes
    private static int countComplexObjects(Document doc,
                                           Map<String, String> symbolDefs,
                                           Set<String> complexCodes) {
        int count = 0;
        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            String symbolId = objectElement.getAttribute("symbol");
            if (symbolId == null || symbolId.isEmpty()) continue;

            String code = symbolDefs.getOrDefault(symbolId, symbolId).trim();
            if (!code.isEmpty() && complexCodes.contains(code)) {
                count++;
            }
        }
        return count;
    }
}
