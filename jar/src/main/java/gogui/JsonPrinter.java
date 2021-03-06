package gogui;

import gogui.history.State;

import javax.json.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class JsonPrinter {

    private TreeSet<Point> points = new TreeSet<>();
    private TreeSet<InternalLine> lines = new TreeSet<>();
    private List<State> states;

    private static Version version = Version.ZMUDA;

    public JsonPrinter(List<State> states) {
        this.states = states;
    }

    public String getJSON() {
        getPoints();
        getLines();

        JsonObjectBuilder obj = Json.createObjectBuilder();
        obj.add("history", getJSONAllStates());
        obj.add("lines", getJSONLinesDefinition());
        obj.add("points", getJSONPointsDefinition());

        return obj.build().toString();
    }


    private void getPoints() {
        points.clear();

        for (State state : states) {
            points.addAll(state.getPoints().stream().collect(Collectors.toList()));

            for (Line line : state.getLines()) {
                Point p1 = line.point1;
                Point p2 = line.point2;
                points.add(p1);
                points.add(p2);
            }
        }
    }

    void getLines() {
        lines.clear();

        for (State state : states) {
            for (Line line : state.getLines()) {
                Point p1 = line.point1;
                Point p2 = line.point2;

                InternalLine iline = new InternalLine();
                iline.point1id = getPointID(p1);
                iline.point2id = getPointID(p2);
                iline.normalize();
                lines.add(iline);
            }
        }
    }

    int getPointID(Point p) {
        int i = 0;
        for (Point point : points) {
            if (p.equals(point)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    JsonStructure getJSONPointsDefinition() {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (Point point : points) {
            JsonObject jsonPoint = Json.createObjectBuilder().add("x", point.x).add("y", point.y).build();
            jsonArray.add(jsonPoint);
        }

        return jsonArray.build();
    }

    JsonStructure getJSONLinesDefinition() {

        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (InternalLine internalLine : lines) {
            JsonObject jsonPoint = Json.createObjectBuilder().add("p1", internalLine.point1id).add("p2", internalLine.point2id).build();
            jsonArray.add(jsonPoint);
        }

        return jsonArray.build();
    }

    JsonStructure getJSONAllStates() {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (State state : states) {
            jsonArray.add(getJsonState(state));
        }
        return jsonArray.build();
    }

    private JsonObject getJsonState(State state) {

        Map<Point, String> pointsColors = new HashMap<>();
        for (Point point : state.getPoints()) {
            if (pointsColors.containsKey(point))
                pointsColors.put(point, getFinalColor(point, pointsColors.get(point)));
            else
                pointsColors.put(point, point.getColor());
        }

        JsonArrayBuilder pointsArray = Json.createArrayBuilder();

        for (Map.Entry<Point, String> pointStatusEntry : pointsColors.entrySet()) {
            Point point = pointStatusEntry.getKey();
            int pointId = getPointID(point);
            JsonObjectBuilder jsonPointBuilder = Json.createObjectBuilder().add("pointID", pointId);
            if ( Version.SLONKA.equals(version)) {
                jsonPointBuilder.add("style", pointStatusEntry.toString());
            } else if ( Version.ZMUDA.equals(version)) {
                jsonPointBuilder.add("color", pointStatusEntry.getKey().getColor());
            }
            pointsArray.add(jsonPointBuilder.build());
        }

        JsonArrayBuilder linesArray = Json.createArrayBuilder();

        for (Line line : state.getLines()) {
            int lineID = getLineID(line);
            JsonObjectBuilder jsonLineBuilder = Json.createObjectBuilder().add("lineID", lineID);
            if ( Version.SLONKA.equals(version)) {
                jsonLineBuilder.add("style", line.getStatus().toString());
            } else if ( Version.ZMUDA.equals(version)) {
                jsonLineBuilder.add("color", line.getColor());
            }
            linesArray.add(jsonLineBuilder.build());
        }

        return Json.createObjectBuilder().add("lines", linesArray).add("points", pointsArray).build();
    }

    private String getFinalColor(Point p1, String p2) {
        if ( p1.hasCustomColor()) {
            return p1.getColor();
        }

        return p2;
    }

    int getLineID( InternalLine iline) {
        int i = 0;
        for (InternalLine line : lines) {
            if (line.equals(iline)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    int getLineID(Line line) {
        InternalLine iline = new InternalLine();
        iline.point1id = getPointID(line.point1);
        iline.point2id = getPointID(line.point2);
        iline.normalize();
        return getLineID(iline);
    }


    private class InternalLine implements Comparable {
        int point1id, point2id;

        boolean isSmaller(InternalLine that) {
            return point1id == that.point1id ? point2id < that.point2id : point1id < that.point1id;
        }

        void normalize() {
            if (point1id > point2id) {
                int temp = point1id;
                point1id = point2id;
                point2id = temp;
            }
        }

        @Override
        public int compareTo(Object o) {

            InternalLine that = (InternalLine) o;
            if (point1id == that.point1id) {
                if (point2id < that.point2id) {
                    return -1;
                } else if (point2id > that.point2id) {
                    return 1;
                } else return 0;
            } else if (point1id < that.point1id) {
                return -1;
            } else if (point1id > that.point1id) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InternalLine that = (InternalLine) o;

            if (point1id != that.point1id) return false;
            if (point2id != that.point2id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = point1id;
            result = 31 * result + point2id;
            return result;
        }
    }

}
