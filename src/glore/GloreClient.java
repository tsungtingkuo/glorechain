package glore;

import Jama.Matrix;
import java.io.*;
import java.util.*;
import utility.*;
import auc.*;

public class GloreClient {

  GloreData train = null;
  GloreData test = null;

  Matrix D = null;
  Matrix E = null;
  Matrix beta0 = null;
  Matrix beta1 = null;

  public GloreClient(String trainDataFileName, String testDataFileName) throws Exception {

      this.train = new GloreData(trainDataFileName);
      this.test = new GloreData(testDataFileName);

      this.beta0 = new Matrix(train.getM(), 1, -1.0);
      this.beta1 = new Matrix(train.getM(), 1, 0.0); 
  }

  public void updateModel() throws Exception {

        beta0 = beta1.copy();

        Matrix P = (train.getX().times(-1)).times(beta0);
        GloreUtil.exp(P.getArray());
        GloreUtil.add_one(P.getArray());
        GloreUtil.div_one(P.getArray());

        Matrix W = P.copy();
        W.timesEquals(-1.0);
        GloreUtil.add_one(W.getArray());
        W.arrayTimesEquals(P);
        W = W.transpose();
        W = GloreUtil.diag(W.getArray()[0]);

        D = ((train.getX().transpose()).times(W)).times(train.getX());
        E = (train.getX().transpose()).times(train.getY().plus(P.uminus()));
  }

  public GloreData getTrain() {
      return this.train;
  }

  public GloreData getTest() {
      return this.test;
  }

  public double computeTrainAUC() throws Exception {
      return computeAUC(train);
  }

  public double computeTestAUC() throws Exception {
      return computeAUC(test);
  }

  public double computeAUC(GloreData gd) throws Exception {
      double[] scores = predict(gd);
      int[] answers = gd.getYIntArray();
      return AUC.compute(scores, answers);
  }

  public double[] predict(GloreData gd) throws Exception {
      Matrix P = (gd.getX().times(-1)).times(beta1);
      GloreUtil.exp(P.getArray());
      GloreUtil.add_one(P.getArray());
      GloreUtil.div_one(P.getArray());
      return P.transpose().getArray()[0];
  }

  public boolean isConverged(int iter, int maxIteration) throws Exception {
    return GloreUtil.isConverged(beta0, beta1, iter, maxIteration);
  }

  public double getDifference() throws Exception {
    return GloreUtil.getDifference(beta0, beta1);
  }

  public double[][] getD() throws Exception {
    return D.getArray();
  }

  public double[] getE() throws Exception {
    return E.transpose().getArray()[0];
  }

  public double[] getBeta1() throws Exception {
    return beta1.transpose().getArray()[0];
  }

  public void setBeta1(double[] modelMean) throws Exception {
    this.beta1 = GloreUtil.arrayToMatrix(modelMean);
  }

  public void setBeta1(Matrix beta1) throws Exception {
    this.beta1 = beta1;
  }

}
