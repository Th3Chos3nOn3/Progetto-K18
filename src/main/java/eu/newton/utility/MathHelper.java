package eu.newton.utility;

public final class MathHelper {

    public static long factorial(long n) {

        long tmp = 1;

        for (int i = 1; i < n; i++) {
            tmp *= i;
        }

        return tmp;
    }

    public static double binomialCoefficient(int n, int k) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
}
