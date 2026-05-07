package mapanalyzer.summer;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OmapAreaReader {

    private static class SymbolInfo {
        final String code;
        final boolean isArea;

        SymbolInfo(String code, boolean isArea) {
            this.code = code;
            this.isArea = isArea;
        }
    }

    public static List<List<Point2D.Double>> readAreaGeometries(File inputFile) throws Exception {
        List<List<Point2D.Double>> areaGeometries = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputFile);
        doc.getDocumentElement().normalize();

        Map<String, SymbolInfo> symbolInfoMap = getSymbolInfo(doc);

        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);

            String symbolId = objectElement.getAttribute("symbol");
            if (symbolId == null || symbolId.isEmpty()) {
                continue;
            }

            SymbolInfo info = symbolInfoMap.get(symbolId);
            if (info == null || !info.isArea) {
                continue;
            }

            List<Point2D.Double> points = readObjectGeometry(objectElement);
            areaGeometries.add(points);
        }

        return areaGeometries;
    }

    // Builds symbolId -> (code, isArea) map
    private static Map<String, SymbolInfo> getSymbolInfo(Document doc) {
        Map<String, SymbolInfo> map = new HashMap<>();

        NodeList symbolNodes = doc.getElementsByTagName("symbol");
        for (int i = 0; i < symbolNodes.getLength(); i++) {
            Element symbolElement = (Element) symbolNodes.item(i);

            String id = symbolElement.getAttribute("id");
            String code = symbolElement.getAttribute("code");
            if (id == null || id.isEmpty() || code == null || code.isEmpty()) {
                continue;
            }

            // If this symbol has an <area_symbol> child, we treat it as area
            boolean isArea = symbolElement.getElementsByTagName("area_symbol").getLength() > 0;

            map.put(id, new SymbolInfo(code.trim(), isArea));
        }

        return map;
    }

    // Parses <coords> text into list of points (x y [flag]; ...)
    private static List<Point2D.Double> readObjectGeometry(Element objectElement) {
        List<Point2D.Double> points = new ArrayList<>();

        NodeList coordsNodes = objectElement.getElementsByTagName("coords");
        if (coordsNodes.getLength() == 0) {
            return points;
        }

        Element coords = (Element) coordsNodes.item(0);
        String text = coords.getTextContent();
        if (text == null) {
            return points;
        }

        String[] parts = text.trim().split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }

            String[] tokens = part.split("\\s+");
            if (tokens.length < 2) {
                continue;
            }

            try {
                double x = Double.parseDouble(tokens[0]);
                double y = Double.parseDouble(tokens[1]);
                points.add(new Point2D.Double(x, y));
            } catch (NumberFormatException ignored) {
            }
        }

        return points;
    }
}