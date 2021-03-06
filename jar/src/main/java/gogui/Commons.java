package gogui;

public class Commons {

    public static final double EPSILON = 1.0e-12;
    public static final double PI = 3.141592653;
    public static final double M_PI = 3.14159265358979323846;

    public static boolean compareDouble(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean compareDouble(double a, double b) {
        return compareDouble(a, b, EPSILON);
    }

    // Converts degrees to radians.
    public static double degreesToRadians(double angleDegrees) {
        return angleDegrees * Commons.M_PI / 180.0;
    }

    // Converts radians to degrees.
    public static double radiansToDegrees(double angleRadians) {
        return angleRadians * 180.0 / Commons.M_PI;
    }

}
