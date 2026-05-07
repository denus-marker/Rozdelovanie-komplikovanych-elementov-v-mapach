package mapanalyzer.summer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CurveApproximationPreparationTest {

    @Test
    void cubicBezierPointReturnsStartPointForT0() {
        Point2D.Double p0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p1 = new Point2D.Double(1.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(1.0, 1.0);
        Point2D.Double p3 = new Point2D.Double(2.0, 1.0);

        Point2D.Double result = GeometryUtils.cubicBezierPoint(p0, p1, p2, p3, 0.0);

        assertEquals(0.0, result.x, 1e-9);
        assertEquals(0.0, result.y, 1e-9);
    }

    @Test
    void cubicBezierPointReturnsEndPointForT1() {
        Point2D.Double p0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p1 = new Point2D.Double(1.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(1.0, 1.0);
        Point2D.Double p3 = new Point2D.Double(2.0, 1.0);

        Point2D.Double result = GeometryUtils.cubicBezierPoint(p0, p1, p2, p3, 1.0);

        assertEquals(2.0, result.x, 1e-9);
        assertEquals(1.0, result.y, 1e-9);
    }

    @Test
    void approximateCurveSegmentProducesMoreThanTwoPoints() {
        Point2D.Double p0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p1 = new Point2D.Double(1.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(1.0, 1.0);
        Point2D.Double p3 = new Point2D.Double(2.0, 1.0);

        List<Point2D.Double> points =
                GeometryUtils.approximateCurveSegment(p0, p1, p2, p3, 8);

        assertTrue(points.size() > 2,
                "Approximated curve should contain more than 2 points.");
    }

    @Test
    void readRawGeometryParsesFlags() throws Exception {
        Element objectElement = createObjectElement("""
                <object symbol="s1">
                    <coords>
                        0 0 0; 1 0 4; 2 1 0;
                    </coords>
                </object>
                """);

        List<OmapAreaReader.RawCoord> raw = OmapAreaReader.readRawGeometry(objectElement);

        assertEquals(3, raw.size(),
                "Raw geometry should contain 3 coordinates.");
        assertEquals(4, raw.get(1).flags,
                "Second coordinate should preserve parsed flags.");
    }

    @Test
    void buildPolylineGeometryKeepsPlainPolygonSupported() {
        List<OmapAreaReader.RawCoord> raw = List.of(
                new OmapAreaReader.RawCoord(0.0, 0.0, 0),
                new OmapAreaReader.RawCoord(1.0, 0.0, 0),
                new OmapAreaReader.RawCoord(1.0, 1.0, 0),
                new OmapAreaReader.RawCoord(0.0, 1.0, 0),
                new OmapAreaReader.RawCoord(0.0, 0.0, 0)
        );

        OmapAreaReader.GeometryBuildResult result = OmapAreaReader.buildPolylineGeometry(raw);

        assertTrue(result.supported,
                "Plain polygon without curves should be supported.");
        assertFalse(result.containsCurves,
                "Plain polygon should not be marked as containing curves.");
        assertFalse(result.containsHoles,
                "Plain polygon should not be marked as containing holes.");
        assertEquals(5, result.points.size(),
                "Plain polygon should preserve its point count.");
    }

    @Test
    void buildPolylineGeometryRejectsHoleGeometry() {
        List<OmapAreaReader.RawCoord> raw = List.of(
                new OmapAreaReader.RawCoord(0.0, 0.0, 0),
                new OmapAreaReader.RawCoord(1.0, 0.0, 0),
                new OmapAreaReader.RawCoord(1.0, 1.0, 8) // example placeholder hole flag
        );

        OmapAreaReader.GeometryBuildResult result = OmapAreaReader.buildPolylineGeometry(raw);

        assertFalse(result.supported,
                "Geometry with hole flag should be unsupported in the first version.");
        assertTrue(result.containsHoles,
                "Geometry should be marked as containing holes.");
    }

    private static Element createObjectElement(String xml) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        return document.getDocumentElement();
    }
}