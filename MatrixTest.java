public class MatrixTest {
  public static void main(String[] args) {
    int t = 3;
    int numberOfStates = 3;
    double[][] A = new double[numberOfStates][numberOfStates];
    for (int i = 0; i < numberOfStates; i++) {
      for (int j = 0; j < numberOfStates; j++) {
        A[i][j] = i + j;
      }
    }

    printMatrix(A);

    double[][] stateDist = new double[numberOfStates][numberOfStates];
      /*for (int tc = 0; tc < t; tc++) {
        for (int i = 0; i < numberOfStates; i++) {
          for (int j = 0; j < numberOfStates; j++) {
            for (int k = 0; k < numberOfStates; k++) {
              stateDist[i][j] += A[i][k] * A[k][j];
              //System.out.printf("Multiplying A[%d][%d] (%.2f) with A[%d][%d] (%.2f) to get stateDist[%d][%d]: %.2f\n", 
              //  i, k, A[i][k], k, j, A[k][j], i, k, stateDist[i][k]);
            }
          }
        }
      }*/

      stateDist = powerMatrix(A, 3);
      printMatrix(stateDist);
  }

  static double[][] multiplyMatrices(double[][] a, double[][] b) {
    double[][] res = new double[a.length][b.length];
    int numberOfStates = 3;
    for (int i = 0; i < numberOfStates; i++) {
      for (int j = 0; j < numberOfStates; j++) {
        for (int k = 0; k < numberOfStates; k++) {
          res[i][j] += a[i][k] * b[k][j];
          //System.out.printf("Multiplying A[%d][%d] (%.2f) with A[%d][%d] (%.2f) to get stateDist[%d][%d]: %.2f\n", 
          //  i, k, A[i][k], k, j, A[k][j], i, k, stateDist[i][k]);
        }
      }
    }

    return res;
  }

  static double[][] powerMatrix (double[][] a, int p) {
    double[][] result = a;
    for (int n = 1; n < p; ++ n)
        result = multiplyMatrices(result, a);
    return result;
  }

  public static void printMatrix(double[][] m) {
    System.out.printf("[");
    for (int i = 0; i < m.length; i++) {
      for (int j = 0; j < m[i].length; j++) {
        System.out.printf("%.2f", m[i][j]);
        if (j < m[i].length - 1) System.out.printf(" ");
      }
      if (i < m[i].length - 1) System.out.printf("\n");
    }
    System.out.printf("]\n");
  }
}