package glore;

import Jama.Matrix;
import java.io.*;
import java.util.*;
import utility.*;

public class GloreServer {

  // Tim: s = total site number
  public static double[] combineModels(double[][][] D, double[][] E, double[] previousBeta1) throws Exception {

      Matrix temp_a, temp_b, temp_c;

      Matrix beta1 = GloreUtil.arrayToMatrix(previousBeta1);
      Matrix beta0 = beta1.copy();

      /* beta1<-beta0+solve(rowSums(d,dims=2)+
      diag(0.0000001,m))%*%(rowSums(e,dims=2)) */
      temp_a = new Matrix(GloreUtil.row_sums(E, previousBeta1.length, D.length));
      temp_b = new Matrix(GloreUtil.row_sums(D, previousBeta1.length, D.length));
      temp_c = GloreUtil.diag(previousBeta1.length);

      temp_b = temp_b.plus(temp_c);
      temp_b = temp_b.inverse();
      temp_b = temp_b.times(temp_a);
      beta1 = beta0.plus(temp_b);

      return beta1.transpose().getArray()[0];
  }

}
