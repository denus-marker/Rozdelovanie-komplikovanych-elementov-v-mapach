package mapanalyzer;

import org.junit.jupiter.api.Test;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapComplexFilterZimaTest {

    @Test
    void triangleIsSimple() {
        // Closed triangle: last point equals first
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0)
        );

        assertTrue(MapComplexFilterZima.isSimpleShape(points),
                "Triangle polygon should be considered simple.");
    }

    @Test
    void quadrilateralIsComplex() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0),
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(0.0, 1.0),
                new Point2D.Double(0.0, 0.0) // closed polygon
        );

        assertFalse(MapComplexFilterZima.isSimpleShape(points),
                "Quadrilateral polygon should be considered complex.");
    }

    @Test
    void lessThanThreePointsIsNotSimple() {
        List<Point2D.Double> points = List.of(
                new Point2D.Double(0.0, 0.0),
                new Point2D.Double(1.0, 0.0)
        );

        assertFalse(MapComplexFilterZima.isSimpleShape(points),
                "Polygon with less than 3 vertices must not be simple.");
    }
}
