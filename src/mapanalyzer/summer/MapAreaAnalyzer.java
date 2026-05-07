package mapanalyzer.summer;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.List;

public class MapAreaAnalyzer {

    public static class SplitStats {
        public int totalAreaObjects;
        public int simpleAreaObjects;
        public int complexAreaObjects;
        public int producedSimpleParts;

        public SplitStats(int totalAreaObjects, int complexAreaObjects, int simpleAreaObjects, int producedSimpleParts) {
            this.totalAreaObjects = totalAreaObjects;
            this.complexAreaObjects = complexAreaObjects;
            this.simpleAreaObjects = simpleAreaObjects;
            this.producedSimpleParts = producedSimpleParts;
        }
    }

    public static SplitStats analyze(File inputFile) throws Exception {
        SplitStats stats = new SplitStats(0, 0, 0, 0);

        List<List<Point2D.Double>> areaGeometries = OmapAreaReader.readAreaGeometries(inputFile);

        for (List<Point2D.Double> points : areaGeometries) {
            List<Point2D.Double> normalized = GeometryUtils.normalizePolygon(points);

            if (normalized.size() < 3) {
                continue;
            }

            stats.totalAreaObjects++;

            if (EarClippingTriangulator.isSimpleShape(normalized)) {
                stats.simpleAreaObjects++;
            } else {
                stats.complexAreaObjects++;
            }

            List<List<Point2D.Double>> parts = EarClippingTriangulator.splitComplexShape(normalized);
            stats.producedSimpleParts += parts.size();
        }

        return stats;
    }
}