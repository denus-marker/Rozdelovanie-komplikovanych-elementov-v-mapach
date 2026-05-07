package mapanalyzer.summer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {
    /*
     Normalizes polygon representation.
     Planned purpose:
        - remove duplicated closing point if the last point equals the first
        - return a consistent polygon representation for later processing
    */
    public static List<Point2D.Double> normalizePolygon(List<Point2D.Double> points){
        List<Point2D.Double> normalized = new ArrayList<>();

        if (points==null || points.isEmpty()) return normalized;

        // Copy all non-null points so the original list is not modified
        for (Point2D.Double p : points){
            if (p!=null){
                normalized.add(new Point2D.Double(p.x, p.y));
            }
        }

        if (normalized.isEmpty()) return normalized;

        // Remove duplicated closing point if the last point equals the first
        if (normalized.size() >= 2){
            Point2D.Double first = normalized.getFirst();
            Point2D.Double last = normalized.getLast();

            if (first.x == last.x && first.y == last.y){
                normalized.removeLast();
            }
        }

        // Remove consecutive duplicate points
        List<Point2D.Double> cleaned = new ArrayList<>();
        Point2D.Double prev = null;
        for (Point2D.Double curr : normalized){
            if (prev == null || prev.x != curr.x || prev.y != curr.y){
                cleaned.add(curr);
                prev = curr;
            }
        }

        // After removing consecutive duplicates, check again
        // whether the polygon ends with the same point it starts with
        if (cleaned.size() >= 2){
            Point2D.Double first = cleaned.getFirst();
            Point2D.Double last = cleaned.getLast();
            if (first.x == last.x && first.y == last.y) {
                cleaned.removeLast();
            }
        }

        return cleaned;
    }

    /*
     Helper geometric method.
     It should compute signed polygon area.
     This is useful for determining polygon orientation.
    */
    public static double polygonArea(List<Point2D.Double> points) {
        if (points == null || points.size() < 3) return 0.0;

        double area = 0.0;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            Point2D.Double curr = points.get(i);
            Point2D.Double next = points.get((i + 1) % n);

            area += curr.x * next.y - next.x * curr.y;
        }

        return area / 2.0;
    }

    // Should return true if polygon vertices are ordered clockwise.
    // Degenerate polygons with zero area return false.
    public static boolean isClockwise(List<Point2D.Double> points){
        return polygonArea(points) < 0;
    }

    /*
     Helper method for orientation of three points.
     Returns the signed 2D cross product of vectors AB and AC.
     Positive value means left turn, negative means right turn,
     zero means the points are collinear.
    */
    public static double cross(Point2D.Double a, Point2D.Double b, Point2D.Double c){
        double abx = b.x - a.x;
        double aby = b.y - a.y;
        double acx = c.x - a.x;
        double acy = c.y - a.y;

        return abx * acy - aby * acx;
    }

    /*
     Should determine whether the angle (prev, curr, next)
     is convex for the given polygon orientation.
     This will be important for triangulation.
    */
    public static boolean isConvex(Point2D.Double prev, Point2D.Double curr, Point2D.Double next, boolean clockwise){
        double turn = cross(prev, curr, next);
        double eps = 1e-9;

        // Collinear points do not form a proper convex corner
        if (Math.abs(turn) < eps){
            return false;
        }

        // For clockwise polygons, a convex corner is a right turn (negative cross)
        // For counterclockwise polygons, a convex corner is a left turn (positive cross)
        return clockwise ? turn < 0 : turn > 0;
    }

    /*
     Should determine whether point p lies inside triangle abc
     or on its boundary.
     This will be used during polygon splitting.
    */
    public static boolean pointInTriangle(Point2D.Double p, Point2D.Double a, Point2D.Double b, Point2D.Double c){
        if (p == null || a == null || b == null || c == null){
            return false;
        }

        double eps = 1e-9;

        // Degenerate triangle has no valid interior
        double triangleTurn = cross(a,b,c);
        if (Math.abs(triangleTurn) < eps){
            return false;
        }

        // Compute orientation of point p with respect to each triangle edge
        double c1 = cross(a, b, p);
        double c2 = cross(b, c, p);
        double c3 = cross(c, a, p);

        // Point is inside or on boundary if all signs are consistent
        // allowing a small epsilon tolerance for floating-point errors
        boolean hasNegative = (c1 < -eps) || (c2 < -eps) || (c3 < -eps);
        boolean hasPositive = (c1 > eps) || (c2 > eps) || (c3 > eps);

        return !(hasNegative && hasPositive);
    }

    /*
     Should determine whether triangle abc contains any other
     polygon vertex inside it.
     This is needed when validating a candidate triangle
     during triangulation.
    */
    public static boolean containsAnyPointInside(
            List<Point2D.Double> polygon,
            Point2D.Double a,
            Point2D.Double b,
            Point2D.Double c
    ){
        if (polygon == null || polygon.isEmpty() || a == null || b == null || c == null) {
            return false;
        }

        double eps = 1e-9;
        for (Point2D.Double p : polygon){
            if (p == null){
                continue;
            }

            // Skip the triangle's own vertices
            boolean isA = Math.abs(p.x - a.x) < eps && Math.abs(p.y - a.y) < eps;
            boolean isB = Math.abs(p.x - b.x) < eps && Math.abs(p.y - b.y) < eps;
            boolean isC = Math.abs(p.x - c.x) < eps && Math.abs(p.y - c.y) < eps;

            if (isA || isB || isC){
                continue;
            }

            // If any other polygon vertex lies inside the triangle
            // or on its boundary, this is not a valid ear
            if (pointInTriangle(p, a, b, c)) {
                return true;
            }
        }

        return false;
    }

    /*
     Future method:
     returns one point on a cubic Bezier curve for parameter t in <0,1>.
     Control points: p0, p1, p2, p3.
    */
    public static Point2D.Double cubicBezierPoint(
            Point2D.Double p0,
            Point2D.Double p1,
            Point2D.Double p2,
            Point2D.Double p3,
            double t
    ) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /*
     Future method:
     approximates one cubic Bezier segment by a polyline with the given number of steps.
     The result should contain points lying on the approximated curve.
    */
    public static List<Point2D.Double> approximateCurveSegment(
            Point2D.Double p0,
            Point2D.Double p1,
            Point2D.Double p2,
            Point2D.Double p3,
            int steps
    ) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}