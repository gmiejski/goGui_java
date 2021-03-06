package lab3;

import gogui.*;
import lab3.structures.LinePair;
import lab3.structures.Q;
import lab3.structures.T;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static gogui.GoGui.*;

public class Lab3Main {

    public static final String LAB3_SRC_MAIN_RESOURCES = Paths.get("lab3", "src", "main", "resources").toString();
    public static final String INPUT_FILE_EXTENSION = ".json";

    public static void main(String[] args) {
        Set<Point> intersectionPoints = fireAlgorithm("input");
        GoGui.clear();
        Set<Point> intersectionPoints2 = fireAlgorithm("input2");
        GoGui.clear();
        Set<Point> intersectionPoints3 = fireAlgorithm("input3");
        GoGui.clear();
        Set<Point> intersectionPoints4 = fireAlgorithm("input4");
        GoGui.clear();
    }

    private static Set<Point> fireAlgorithm(String fileName) {
        GeoList<Line> lines = loadLinesFromJson(Paths.get(LAB3_SRC_MAIN_RESOURCES,fileName + INPUT_FILE_EXTENSION).toString());
        GeoList<Point> points = new GeoList<>();
        GeoList<Line> helper = new GeoList<>();

        Map<Point, Line> pointToLine = new HashMap<>();

        for (Line line : lines) {
            points.push_back(line.getPoint1());
            points.push_back(line.getPoint2());
            pointToLine.put(line.getPoint1(), line);
            pointToLine.put(line.getPoint2(), line);
        }

        Q q = new Q(points);
        T t = new T();
        snapshot();

        Line broomstick = null;
        while (q.hasNext()) {
            Point p = q.next();
            broomstick = getNextBroomstick(helper, broomstick, p.x);
            snapshot();

            if (q.isIntersectionPoint(p)) {

                t.swapIntersectionLines(p);
                LinePair intersectionLines = t.getIntersectionLines(p);

                Line l1 = intersectionLines.l1;
                Line l2 = intersectionLines.l2;

                findIntersectionsWithNeighbouringLines(l1, q, t);
                findIntersectionsWithNeighbouringLines(l2, q, t);

            } else {
                Line currentLine = pointToLine.get(p);
                currentLine.activate();

                Point lineSecondPoint = getAnotherEnd(p, currentLine);

                if (p.x < lineSecondPoint.x) {
                    t.add(currentLine, p.x);

                    findIntersectionsWithNeighbouringLines(currentLine, q, t);

                } else {
                    t.remove(currentLine);
                    Optional<Line> leftNeighbor = t.getRightNeighbor(currentLine);
                    Optional<Line> rightNeighbor = t.getLeftNeighbor(currentLine);

                    if (leftNeighbor.isPresent() && rightNeighbor.isPresent()) {
                        Line left = leftNeighbor.get();
                        Line right = rightNeighbor.get();

                        processNeighboringLine(q, left, right, t);
                    }
                    currentLine.processed();
                }
            }
            helper.clear();
        }

        lines.setStatus(GeoObject.Status.PROCESSED);
        helper.clear();
        snapshot();

        saveJSON("C:\\home\\aaaaStudia\\Semestr_VII\\Geometria\\gogui\\visualization-grunt\\public\\data\\sweep." + fileName + ".data.json");
        GoGui.saveJSON("lab3\\src\\main\\resources\\sweep." + fileName + ".data.json");
        saveJSON("results\\sweep." + fileName + ".data.json");

        System.out.println("Number of intersections: " + q.getIntersectionPoints().size());
        q.getIntersectionPoints().forEach(intersection -> {
            System.out.println(intersection);
            System.out.println(t.getIntersectionLines(intersection));
        });

        return q.getIntersectionPoints();
    }

    private static void findIntersectionsWithNeighbouringLines(Line currentLine, Q q, T t) {
        Optional<Line> rightNeighbor = t.getRightNeighbor(currentLine);

        if (rightNeighbor.isPresent()) {
            Line line = rightNeighbor.get();

            processNeighboringLine(q, currentLine, line, t);
        }

        Optional<Line> leftNeighbor = t.getLeftNeighbor(currentLine);

        if (leftNeighbor.isPresent()) {
            Line line = leftNeighbor.get();

            processNeighboringLine(q, currentLine, line, t);
        }
    }

    private static void processNeighboringLine(Q q, Line currentLine, Line line, T t) {
        line.activate();
        snapshot();
        findIntersection(currentLine, line, q, t);
    }

    private static Point getAnotherEnd(Point knownPoint, Line line) {
        Point other;
        if (knownPoint == line.getPoint1()) {
            other = line.getPoint2();
        } else {
            other = line.getPoint1();
        }
        return other;
    }

    private static Line getNextBroomstick(GeoList<Line> helper, Line broomstick, double x) {
        if (broomstick == null) {
            broomstick = new Line(new Point(x, 0.0), new Point(x, 1000.0));
        } else {
            helper.remove(broomstick);
            broomstick = new Line(new Point(x, 0.0), new Point(x, 1000.0));
        }

        helper.push_back(broomstick);
        return broomstick;
    }

    private static void findIntersection(Line l1, Line l2, Q q, T t) {
        Point intersection = l1.intersectionPoint(l2);

        if (l1.containsPoint(intersection) && l2.containsPoint(intersection)) {
//            System.out.println("Lines : " + l1 + " and " + l2 + " intersects at: " + intersection);
            if (!q.isIntersectionPoint(intersection)) {
                q.addIntersectionPoint(intersection);
                t.addIntersectionLines(intersection, l1, l2);
            }
            snapshot();
        }
    }
}
