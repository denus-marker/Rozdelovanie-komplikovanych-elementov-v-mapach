package mapanalyzer.summer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static mapanalyzer.summer.GeometryUtils.*;

public class EarClippingTriangulator {

    /* Returns true if shape is considered "simple".
    Here: simple shape = polygon that can be decomposed into exactly one triangle.
    That means it effectively has 3 unique vertices. */
    public static boolean isSimpleShape(List<Point2D.Double> points) {
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

    /*
     Planned main method of the summer part.
     It should split a complex polygon into simple parts.
     The currently intended simple part is a triangle.

     Result: a list of simple polygons, each represented by a list of points.
    */
    public static List<List<Point2D.Double>> splitComplexShape(List<Point2D.Double> points){
        List<List<Point2D.Double>> parts = new ArrayList<>();
        double eps = 1e-9;

        // Normalize input first so the method also works
        // when called directly from tests with a closed polygon.
        List<Point2D.Double> polygon = GeometryUtils.normalizePolygon(points);

        if (polygon.size() < 3) {
            return parts;
        }

        // Remove collinear vertices to make triangulation more stable.
        // This helps avoid cases where a "corner" is not a real corner.
        boolean changed = true;
        while (changed && polygon.size() > 3) {
            changed = false;
            List<Point2D.Double> cleaned = new ArrayList<>();

            int n = polygon.size();
            for (int i = 0; i < n; i++) {
                Point2D.Double prev = polygon.get((i - 1 + n) % n);
                Point2D.Double curr = polygon.get(i);
                Point2D.Double next = polygon.get((i + 1) % n);

                if (Math.abs(cross(prev, curr, next)) < eps) {
                    changed = true;
                    continue;
                }

                cleaned.add(curr);
            }

            if (cleaned.size() < 3) {
                return new ArrayList<>();
            }

            polygon = cleaned;
        }

        // Degenerate polygon cannot be triangulated.
        if (polygon.size() < 3 || Math.abs(polygonArea(polygon)) < eps) {
            return parts;
        }

        // A triangle is already a simple part.
        if (polygon.size() == 3) {
            return getLists(parts, polygon);
        }

        // Work on a modifiable copy.
        List<Point2D.Double> working = new ArrayList<>();
        for (Point2D.Double p : polygon) {
            working.add(new Point2D.Double(p.x, p.y));
        }

        boolean clockwise = isClockwise(working);

        // Safety guard to avoid infinite loops on invalid input.
        int guard = working.size() * working.size();

        while (working.size() > 3 && guard-- > 0) {
            boolean earFound = false;
            int n = working.size();

            for (int i = 0; i < n; i++) {
                Point2D.Double prev = working.get((i - 1 + n) % n);
                Point2D.Double curr = working.get(i);
                Point2D.Double next = working.get((i + 1) % n);

                // Ear tip must be convex.
                if (!isConvex(prev, curr, next, clockwise)) {
                    continue;
                }

                // Candidate ear triangle must not contain another polygon vertex.
                if (containsAnyPointInside(working, prev, curr, next)) {
                    continue;
                }

                // Valid ear found -> store triangle.
                List<Point2D.Double> triangle = new ArrayList<>();
                triangle.add(new Point2D.Double(prev.x, prev.y));
                triangle.add(new Point2D.Double(curr.x, curr.y));
                triangle.add(new Point2D.Double(next.x, next.y));
                parts.add(triangle);

                // Remove ear tip and continue.
                working.remove(i);
                earFound = true;
                break;
            }

            if (!earFound) {
                // Recovery step: try removing one collinear vertex if present.
                boolean removedCollinear = false;
                int m = working.size();

                for (int i = 0; i < m; i++) {
                    Point2D.Double prev = working.get((i - 1 + m) % m);
                    Point2D.Double curr = working.get(i);
                    Point2D.Double next = working.get((i + 1) % m);

                    if (Math.abs(cross(prev, curr, next)) < eps) {
                        working.remove(i);
                        removedCollinear = true;
                        break;
                    }
                }

                if (!removedCollinear) {
                    // Likely invalid polygon (for example self-intersection).
                    return new ArrayList<>();
                }

                if (working.size() < 3 || Math.abs(polygonArea(working)) < eps) {
                    return new ArrayList<>();
                }

                clockwise = isClockwise(working);
            }
        }

        // Final remaining triangle
        if (working.size() == 3) {
            if (Math.abs(cross(working.get(0), working.get(1), working.get(2))) < eps) {
                return new ArrayList<>();
            }

            return getLists(parts, working);
        }

        // If guard was exhausted or polygon ended in an invalid state,
        // treat it as non-triangulable.
        return new ArrayList<>();
    }

    private static List<List<Point2D.Double>> getLists(List<List<Point2D.Double>> parts, List<Point2D.Double> polygon) {
        List<Point2D.Double> triangle = new ArrayList<>();
        triangle.add(new Point2D.Double(polygon.get(0).x, polygon.get(0).y));
        triangle.add(new Point2D.Double(polygon.get(1).x, polygon.get(1).y));
        triangle.add(new Point2D.Double(polygon.get(2).x, polygon.get(2).y));
        parts.add(triangle);
        return parts;
    }
}