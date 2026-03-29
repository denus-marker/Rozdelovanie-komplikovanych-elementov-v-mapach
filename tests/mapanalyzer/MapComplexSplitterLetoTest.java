package mapanalyzer;

import org.junit.jupiter.api.Test;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapComplexSplitterLetoTest {

    @Test
    void triangleProducesOneSimplePart() {
        // Closed triangle: last point equals first
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = MapComplexSplitterLeto.splitComplexShape(points);

        assertEquals(1, parts.size(),
                "Triangle should produce exactly one simple part.");

        assertEquals(3, parts.get(0).size(),
                "The produced simple part should have exactly 3 vertices.");
    }

    @Test
    void quadrilateralProducesTwoTriangles() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0) // closed polygon
        );

        List<List<Point2D.Double>> parts = MapComplexSplitterLeto.splitComplexShape(points);

        assertEquals(2, parts.size(),
                "Quadrilateral should be split into exactly 2 triangles.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void concavePolygonProducesNTriangles() {
        // Concave polygon with 5 unique vertices -> should produce n - 2 = 3 triangles
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(2.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(2.0, 2.0),
                new Point2D.Double(0.0, 2.0),
                new Point2D.Double(0.0, 0.0) // closed polygon
        );

        List<List<Point2D.Double>> parts = MapComplexSplitterLeto.splitComplexShape(points);

        assertEquals(3, parts.size(),
                "Concave polygon with 5 vertices should produce exactly 3 triangles.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void invalidPolygonProducesNoParts() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0)
        );

        List<List<Point2D.Double>> parts = MapComplexSplitterLeto.splitComplexShape(points);

        assertTrue(parts.isEmpty(),
                "Polygon with less than 3 vertices should produce no parts.");
    }
}