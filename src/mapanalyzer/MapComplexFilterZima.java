package mapanalyzer;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class MapComplexFilterZima {
    // Symbol metadata: ISSprOM code and whether it is an area symbol
    private static class SymbolInfo {
        final String code;
        final boolean isArea;

        SymbolInfo(String code, boolean isArea){
            this.code = code;
            this.isArea = isArea;
        }
    }

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

        Map<String, SymbolInfo> symbols = getSymbolInfo(doc);

        int totalAreaObjects = 0;
        int simpleAreaObjects = 0;
        int complexAreaObjects = 0;

        NodeList objectNodes = doc.getElementsByTagName("object");
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);

            String symbolId = objectElement.getAttribute("symbol");
            if(symbolId == null || symbolId.isEmpty()){
                continue;
            }

            SymbolInfo info = symbols.get(symbolId);
            if(info == null || !info.isArea){
                continue;
            }

            List<Point2D.Double> points = readObjectGeometry(objectElement);
            if(points.size() < 3){
                continue;
            }

            totalAreaObjects++;

            if(isSimpleShape(points)){
                simpleAreaObjects++;
            }else{
                complexAreaObjects++;
            }
        }

        System.out.println("Map file:                " + file.getName());
        System.out.println("Area objects (analyzed): " + totalAreaObjects);
        System.out.println("Simple area objects:     " + simpleAreaObjects);
        System.out.println("Complex area objects:    " + complexAreaObjects);
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


    /* Returns true if shape is considered "simple".
    Here: simple shape = polygon that can be decomposed into exactly one triangle.
    That means it effectively has 3 unique vertices. */
    private static boolean isSimpleShape(List<Point2D.Double> points) {
        int n = points.size();
        if (n < 3) {
            return false;
        }

        // If last point repeats the first, ignore it
        Point2D.Double first = points.get(0);
        Point2D.Double last = points.get(n - 1);
        if (first.x == last.x && first.y == last.y) {
            n--;
        }

        // Triangulation of a simple polygon uses (n - 2) triangles
        int triangles = Math.max(1, n - 2);
        return triangles == 1; // only triangles are "simple" for now
    }
}
