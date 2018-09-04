package com.jforexcn.inbox.math;

/**
 * Created by simple on 11/10/2017.
 */


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class RegressionLine
{
    /** sum of x */
    private double sumX;

    /** sum of y */
    private double sumY;

    /** sum of x*x */
    private double sumXX;

    /** sum of x*y */
    private double sumXY;

    /** sum of y*y */
    private double sumYY;

    /** sum of yi-y */
    private double sumDeltaY;

    /** sum of sumDeltaY^2 */
    private double sumDeltaY2;

    /** 误差 */
    private double sse;

    private double sst;

    private double E;

    private ArrayList<Double> listX;

    private ArrayList<Double> listY;

    private double XMin, XMax, YMin, YMax;

    /** line coefficient a0 */
    private double a0;

    /** line coefficient a1 */
    private double a1;

    /** number of data points */
    private int pn;

    /** true if coefficients valid */
    private boolean coefsValid;

    private boolean confidenceIntervalValid;

    private double confidenceLevel = 0.95;

    private double lower = 0.0;

    private double upper = 0.0;

    /**
     * Constructor.
     */
    public RegressionLine() {
        XMax = 0;
        YMax = 0;
        pn = 0;
        listX = new ArrayList<>();
        listY = new ArrayList<>();
    }

    /**
     * Return the current number of data points.
     *
     * @return the count
     */
    public int getDataPointCount() {
        return pn;
    }

    /**
     * Return the coefficient a0.
     *
     * @return the value of a0
     */
    public double getA0() {
        validateCoefficients();
        return a0;
    }

    /**
     * Return the coefficient a1.
     *
     * @return the value of a1
     */
    public double getA1() {
        validateCoefficients();
        return a1;
    }

    /**
     * Return the sum of the x values.
     *
     * @return the sum
     */
    public double getSumX() {
        return sumX;
    }

    /**
     * Return the sum of the y values.
     *
     * @return the sum
     */
    public double getSumY() {
        return sumY;
    }

    /**
     * Return the sum of the x*x values.
     *
     * @return the sum
     */
    public double getSumXX() {
        return sumXX;
    }

    /**
     * Return the sum of the x*y values.
     *
     * @return the sum
     */
    public double getSumXY() {
        return sumXY;
    }

    public double getSumYY() {
        return sumYY;
    }

    public double getXMin() {
        return XMin;
    }

    public double getXMax() {
        return XMax;
    }

    public double getYMin() {
        return YMin;
    }

    public double getYMax() {
        return YMax;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
        this.confidenceIntervalValid = false;
    }

    public double getA0Lower() {
        validateConfidenceInterval();
        return lower;
    }

    public double getA0Upper() {
        validateConfidenceInterval();
        return upper;
    }

    /**
     * Add a new data point: Update the sums.
     *
     * @param  x,y
     *            the new data point
     */
    public void addDataPoint(double x, double y) {
        sumX += x;
        sumY += y;
        sumXX += x * x;
        sumXY += x * y;
        sumYY += y * y;

        if (x > XMax) {
            XMax = x;
        }
        if (y > YMax) {
            YMax = y;
        }

        // 把每个点的具体坐标存入ArrayList中，备用
        if (x != 0 && y != 0) {
            try {
                listX.add(pn, x);
                listY.add(pn, y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ++pn;
        coefsValid = false;
    }

    /**
     * Return the value of the regression line function at x. (Implementation of
     * Evaluatable.)
     *
     * @param x
     *            the value of x
     * @return the value of the function at x
     */
    public double at(double x) {
        if (pn < 2)
            return Float.NaN;

        validateCoefficients();
        return a0 + a1 * x;
    }

    /**
     * Reset.
     */
    public void reset() {
        pn = 0;
        sumX = sumY = sumXX = sumXY = 0;
        coefsValid = false;
    }

    /**
     * Validate the coefficients. 计算方程系数 y=ax+b 中的a
     */
    private void validateCoefficients() {
        if (coefsValid)
            return;

        if (pn >= 2) {
            double xBar = sumX / pn;
            double yBar = sumY / pn;

            a1 = (pn * sumXY - sumX * sumY) / (pn * sumXX - sumX
                    * sumX);
            a0 = yBar - a1 * xBar;
        } else {
            a0 = a1 = Double.NaN;
        }

        coefsValid = true;
    }

    private void validateConfidenceInterval() {
        if (confidenceIntervalValid) {
            return;
        }
        ArrayList<Double> a0List = new ArrayList<>();
        int count = (int) (listX.size() * (1 - confidenceLevel));
        for (int i = 0; i < listX.size(); i++) {
            a0List.add(i, listY.get(i) - getA1() * listX.get(i));
        }
        Collections.sort(a0List);
        lower = a0List.get(count);
        upper = a0List.get(listX.size() - count);
        System.out.println("lower: " + lower);
        System.out.println("upper: " + upper);

        confidenceIntervalValid = true;
    }


    /**
     * 返回误差
     */
    public double getR() {
        // 遍历这个list并计算分母
        for (int i = 0; i < pn - 1; i++) {
            double Yi = listY.get(i);
            double Y = at(listX.get(i));
            double deltaY = Yi - Y;
            double deltaY2 = deltaY * deltaY;

            sumDeltaY2 += deltaY2;

        }

        sst = sumYY - (sumY * sumY) / pn;
        // System.out.println("sst:" + sst);
        E = 1 - sumDeltaY2 / sst;

        return round(E, 4);
    }

    // 用于实现精确的四舍五入
    private double round(double v, int scale) {

        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }

        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
