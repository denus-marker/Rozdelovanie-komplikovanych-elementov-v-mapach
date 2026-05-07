package mapanalyzer.summer;

import org.junit.jupiter.api.Test;
import java.awt.geom.Point2D;
import java.util.List;

import static mapanalyzer.summer.EarClippingTriangulator.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 Unit tests for the implemented polygon splitting logic.
 These tests verify:
    - basic triangulation of simple and concave polygons,
    - behavior on invalid and degenerate input,
    - robustness for different vertex orders and normalized forms,
    - handling of duplicate and collinear vertices,
    - correct classification of simple shapes,
    - preservation of polygon area after triangulation.

 Together, the tests define the expected behavior of splitComplexShape(...)
 and the current interpretation of isSimpleShape(...).
 */
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

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(1, parts.size(),
                "Triangle should produce exactly one simple part.");

        assertEquals(3, parts.get(0).size(),
                "The produced simple part should have exactly 3 vertices.");
    }

    @Test
    void quadrilateralProducesTwoTriangles() {
        // Simple quadrilateral expected to split into 2 triangles.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

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

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(3, parts.size(),
                "Concave polygon with 5 vertices should produce exactly 3 triangles.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void invalidPolygonProducesNoParts() {
        // Less than 3 points -> not a valid polygon.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertTrue(parts.isEmpty(),
                "Polygon with less than 3 vertices should produce no parts.");
    }

    @Test
    void clockwiseQuadrilateralProducesTwoTriangles() {
        // Same rectangle as before, but vertices are ordered clockwise.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(2, parts.size(),
                "Clockwise quadrilateral should still be split into 2 triangles.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void openTriangleProducesOneSimplePart() {
        // Triangle without repeated closing point.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(0.0, 1.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(1, parts.size(),
                "Open triangle should still produce exactly one simple part.");
        assertEquals(3, parts.get(0).size(),
                "The produced simple part should have exactly 3 vertices.");
    }

    @Test
    void polygonWithConsecutiveDuplicatePointsStillTriangulates() {
        // Rectangle with one duplicated consecutive vertex.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(2, parts.size(),
                "Polygon with consecutive duplicate points should still triangulate correctly.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void polygonWithCollinearVertexStillTriangulates() {
        // Bottom edge contains an extra collinear point.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(2.0, 0.0),
                new Point2D.Double(2.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(2, parts.size(),
                "Polygon with a collinear vertex should still triangulate into 2 triangles.");

        for (List<Point2D.Double> triangle : parts) {
            assertEquals(3, triangle.size(),
                    "Each produced simple part should be a triangle.");
        }
    }

    @Test
    void degenerateCollinearPolygonProducesNoParts() {
        // All points lie on one line -> zero area polygon.
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(2.0, 0.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertTrue(parts.isEmpty(),
                "Degenerate collinear polygon should produce no parts.");
    }

    @Test
    void triangulationPreservesAreaForQuadrilateral() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(2.0, 0.0),
                new Point2D.Double(2.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(2, parts.size(),
                "Quadrilateral should be split into 2 triangles.");

        double originalArea = 2.0;
        double trianglesArea = 0.0;

        for (List<Point2D.Double> triangle : parts) {
            trianglesArea += triangleArea(triangle.get(0), triangle.get(1), triangle.get(2));
        }

        assertEquals(originalArea, trianglesArea, 1e-9,
                "Sum of triangle areas should equal original polygon area.");
    }

    @Test
    void triangulationPreservesAreaForConcavePolygon() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(2.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(2.0, 2.0),
                new Point2D.Double(0.0, 2.0),
                new Point2D.Double(0.0, 0.0)
        );

        List<List<Point2D.Double>> parts = splitComplexShape(points);

        assertEquals(3, parts.size(),
                "Concave polygon should be split into 3 triangles.");

        double originalArea = 3.0;
        double trianglesArea = 0.0;

        for (List<Point2D.Double> triangle : parts) {
            trianglesArea += triangleArea(triangle.get(0), triangle.get(1), triangle.get(2));
        }

        assertEquals(originalArea, trianglesArea, 1e-9,
                "Sum of triangle areas should equal original polygon area.");
    }

    @Test
    void isSimpleShapeReturnsTrueForTriangle() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        assertTrue(isSimpleShape(points),
                "Triangle should be classified as a simple shape.");
    }

    @Test
    void isSimpleShapeReturnsFalseForQuadrilateral() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        assertFalse(isSimpleShape(points),
                "Quadrilateral should not be classified as a simple shape.");
    }

    @Test
    void isSimpleShapeReturnsFalseForInvalidPolygon() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0)
        );

        assertFalse(isSimpleShape(points),
                "Polygon with less than 3 vertices should not be simple.");
    }

    private static double triangleArea(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
        return Math.abs(a.x * (b.y - c.y) +
                        b.x * (c.y - a.y) +
                        c.x * (a.y - b.y)) / 2.0;
    }
}
