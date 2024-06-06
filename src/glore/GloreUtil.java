package glore;

import Jama.Matrix;
import java.util.*;

public class GloreUtil {

  public static final double EPSILON = Math.pow(10.0, -6.0);

  public static Matrix arrayToMatrix(double[] array) throws Exception {
    Matrix matrix = new Matrix(array.length, 1, 0.0);
    for (int i = 0; i < array.length; i++) {
      matrix.set(i, 0, array[i]);
    }
    return matrix;
  }

  public static boolean isConverged(double[] beta0, double[] beta1, int iter, int maxIteration) throws Exception {
    return isConverged(arrayToMatrix(beta0), arrayToMatrix(beta1), iter, maxIteration);
  }

  public static double getDifference(double[] beta0, double[] beta1) throws Exception {
    return getDifference(arrayToMatrix(beta0), arrayToMatrix(beta1));
  }

  public static boolean isConverged(Matrix beta0, Matrix beta1, int iter, int maxIteration) throws Exception {
    return (getDifference(beta0, beta1) <= EPSILON || iter > maxIteration);
  }

  public static double getDifference(Matrix beta0, Matrix beta1) throws Exception {
    return max_abs((beta1.minus(beta0)).getArray());
  }

  /* Returns the absolute maximum of the elements in the two dimensional
  array matrix. */
  public static double max_abs(double[][] matrix) {
    int i, j;
    boolean set = false;
    double max = 0;

    // iterate through matrix
    for (i = 0; i < matrix.length; i++) {
      for (j = 0; j < matrix[i].length; j++) {

        // maintain absolute max number found
        if (!set) {
          max = Math.abs(matrix[i][j]);
          set = true;
        } else if (Math.abs(matrix[i][j]) > max) {
          max = Math.abs(matrix[i][j]);
        }
      }
    }

    return max;
  }

  /* Convert a 2D vector of Doubles into a 2D array of doubles. */
  public static double[][] two_dim_vec_to_arr(Vector<Vector<Double>> V) {
    // allocate part of the array
    double[][] A = new double[V.size()][];
    int i;

    // allocate and convert rows of the vector
    for (i = 0; i < V.size(); i++) {
      A[i] = one_dim_vec_to_arr(V.get(i));
    }

    // return 2D array
    return A;
  }

  /* Convert a Vector of Doubles into an array of doubles. */
  public static double[] one_dim_vec_to_arr(Vector<Double> V) {
    int size = V.size();
    int i;
    double[] A = new double[size];

    for (i = 0; i < size; i++) {
      A[i] = (V.get(i)).doubleValue();
    }

    return A;
  }

  /* Set each element of the 2D double array to e^a where a is the value of
  an element. */
  public static void exp(double[][] A) {
    int i, j;
    for (i = 0; i < A.length; i++) {
      for (j = 0; j < A[i].length; j++) {
        A[i][j] = Math.exp(A[i][j]);
      }
    }
  }

  /* Set each element of the 2D double array to 1 + a where a is the value of
  an element. */
  public static void add_one(double[][] A) {
    int i, j;
    for (i = 0; i < A.length; i++) {
      for (j = 0; j < A[i].length; j++) {
        A[i][j] = 1 + A[i][j];
      }
    }
  }

  /* Set each element of the 2D double array to 1/a where a is the value of
  an element. */
  public static void div_one(double[][] A) {
    int i, j;
    for (i = 0; i < A.length; i++) {
      for (j = 0; j < A[i].length; j++) {
        A[i][j] = 1.0 / A[i][j];
      }
    }
  }

  /* Given an array of length n, returns an n by n matrix M where
  M[i][j] = A[i] if i = j and 0 otherwise. */
  public static Matrix diag(double[] A) {
    int n = A.length;
    int i;

    Matrix M = new Matrix(n, n, 0.0);
    for (i = 0; i < n; i++) {
      M.set(i, i, A[i]);
    }
    return M;
  }

  /* Returns an n by n matrix where the diagonal entries are v and the
  other entries are 0 */
  public static Matrix diag(double v, int n) {
    int i;
    double[][] A = new double[n][n];
    for (i = 0; i < n; i++) {
      A[i][i] = v;
    }
    return new Matrix(A);
  }

  public static Matrix diag(int n) {
    return diag(0.0000001, n);
  }

  /* Return a one dimensional array that is the sum of the E.length
  one dimensional vectors. */
  public static double[][] row_sums(double[][] E, int m, int s) {

    int i, j;
    double[][] sums;
    sums = new double[m][1];

    // init sums
    for (i = 0; i < m; i++) {
      sums[i][0] = 0.0;
    }
    // for each client, add its contribution to sums
    for (i = 0; i < s; i++) {
      for (j = 0; j < m; j++) {
        sums[j][0] = sums[j][0] + E[i][j];
      }
    }
    return sums;
  }

  public static double[][] row_sums(double[][][] D, int m, int s) {

    int i, j, k;
    double[][] sums;
    sums = new double[m][m];
    // init sums
    for (i = 0; i < m; i++) {
      for (j = 0; j < m; j++) {
        sums[i][j] = 0;
      }
    }
    // for each client, add its contribution to sums
    for (i = 0; i < s; i++) {
      for (j = 0; j < m; j++) {
        for (k = 0; k < m; k++) {
          sums[j][k] = sums[j][k] + D[i][j][k];
        }
      }
    }
    return sums;
  }

}
