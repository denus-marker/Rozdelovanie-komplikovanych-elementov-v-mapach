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

    // Raw coordinate from <coords>: x, y and optional flag.
    static class RawCoord {
        final double x;
        final double y;
        final int flags;

        RawCoord(double x, double y, int flags) {
            this.x = x;
            this.y = y;
            this.flags = flags;
        }
    }

    // Result of converting raw .omap geometry into polygonal geometry.
    static class GeometryBuildResult {
        final boolean supported;
        final boolean containsCurves;
        final boolean containsHoles;
        final List<Point2D.Double> points;

        GeometryBuildResult(boolean supported,
                            boolean containsCurves,
                            boolean containsHoles,
                            List<Point2D.Double> points) {
            this.supported = supported;
            this.containsCurves = containsCurves;
            this.containsHoles = containsHoles;
            this.points = points;
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

            List<RawCoord> rawCoords = readRawGeometry(objectElement);
            GeometryBuildResult result = buildPolylineGeometry(rawCoords);
            if (!result.supported) continue;
            List<Point2D.Double> points = result.points;

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
    /*
     Future method:
     parses <coords> into raw coordinates with flags.
     Expected format: x y [flag]; ...
    */
    static List<RawCoord> readRawGeometry(Element objectElement) {
        List<RawCoord> rawCoords = new ArrayList<>();

        if (objectElement == null) {
            return rawCoords;
        }

        NodeList coordsNodes = objectElement.getElementsByTagName("coords");
        if (coordsNodes.getLength() == 0) {
            return rawCoords;
        }

        Element coords = (Element) coordsNodes.item(0);
        String text = coords.getTextContent();
        if (text == null) {
            return rawCoords;
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
                int flags = 0;

                if (tokens.length >= 3) {
                    flags = Integer.parseInt(tokens[2]);
                }

                rawCoords.add(new RawCoord(x, y, flags));
            } catch (NumberFormatException ignored) {
            }
        }

        return rawCoords;
    }

    /*
     Future method:
     converts raw .omap geometry into polygonal geometry.
     Smooth curve segments should be approximated by straight line segments.
     Objects with unsupported geometry (for example holes) may be rejected.
    */
    static GeometryBuildResult buildPolylineGeometry(List<RawCoord> rawCoords) {
        List<Point2D.Double> points = new ArrayList<>();

        if (rawCoords == null || rawCoords.isEmpty()) {
            return new GeometryBuildResult(false, false, false, points);
        }

        boolean containsCurves = false;
        boolean containsHoles = false;

        for (RawCoord coord : rawCoords) {
            if (coord == null) {
                continue;
            }

            if (isHolePoint(coord.flags)) {
                containsHoles = true;
            }

            if (isCurveStart(coord.flags)) {
                containsCurves = true;
            }
        }

        // First supported version: reject objects with holes.
        // Inner contours need separate processing, otherwise triangulation would be incorrect.
        if (containsHoles) {
            return new GeometryBuildResult(false, containsCurves, true, points);
        }

        // Plain polygon: copy points directly.
        // For now, curve flags are only detected and the raw points are preserved.
        // Curve approximation helpers are implemented separately and can be connected later
        // when exact .omap curve flag semantics are finalized.
        for (RawCoord coord : rawCoords) {
            if (coord != null) {
                points.add(new Point2D.Double(coord.x, coord.y));
            }
        }

        boolean supported = points.size() >= 3;
        return new GeometryBuildResult(supported, containsCurves, false, points);
    }

    /*
     Future method:
     should return true if the given flag marks the start of a curve segment.
    */
    static boolean isCurveStart(int flags) {
        // Placeholder interpretation used by the preparation tests and future curve support.
        // Flag 4 marks a coordinate related to a curve segment.
        return (flags & 4) != 0;
    }

    /*
     Future method:
     should return true if the given flag marks a hole point / inner contour.
    */
    static boolean isHolePoint(int flags) {
        // Placeholder interpretation used by the first supported version.
        // Flag 8 marks a point belonging to an inner contour / hole.
        return (flags & 8) != 0;
    }
}