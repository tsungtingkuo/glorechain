package glore;

import Jama.Matrix;
import java.io.*;
import java.util.*;
import utility.*;

public class GloreData {

  int m = -1;
  int n = -1;
  Matrix X = null;
  Matrix Y = null;

  public GloreData(String dataFileName) throws Exception {

      // used for reading the data file
      FileInputStream file_stream;
      DataInputStream file_in;
      BufferedReader file_br;
      String file_line;
      String[] line_tokens;

      // data structures used to hold the client data read in from files
      Vector<Vector<Double>> Xv = new Vector<Vector<Double>>();
      Vector<Double> Yv = new Vector<Double>();
      Vector<Double> xrow;

      // data structures used to hold client data for matrix constructors
      double[][] Xa;
      double[] Ya;

      int i, j;

      // access the file
      file_stream = new FileInputStream(dataFileName);
      file_in = new DataInputStream(file_stream);
      file_br = new BufferedReader(new InputStreamReader(file_in));

      // read file and populate X and Y matrices
      n = 0;

      while ((file_line = file_br.readLine()) != null) {
        // update number of rows
        n = n + 1;

        line_tokens = file_line.split("\\s+");

        // System.out.println(Arrays.toString(line_tokens));

        // detect number of columns in data file
        if (m == -1) {
          m = line_tokens.length;
        }
        // line in data file does not match dimensions
        else if (m != line_tokens.length) {
          System.out.println("ERROR: data file dimensions don't " + "match on line " + n + ".");
          System.exit(-1);
        }

        // populate data structures with data
        xrow = new Vector<Double>();
        xrow.add(1.0);
        for (i = 0; i < line_tokens.length - 1; i++) {
          xrow.add(new Double(line_tokens[i]));
        }
        Xv.add(xrow);
        Yv.add(new Double(line_tokens[line_tokens.length - 1]));
      }

      // close input stream
      file_in.close();

      // convert data into arrays to be passed to Matrix's constructor
      Xa = GloreUtil.two_dim_vec_to_arr(Xv);
      Ya = GloreUtil.one_dim_vec_to_arr(Yv);

      // create X and Y matrices
      this.X = new Matrix(Xa);
      this.Y = new Matrix(Ya, Ya.length);
  }

  public Matrix getX() throws Exception {
    return this.X;
  }

  public Matrix getY() throws Exception {
    return this.Y;
  }

  public double[] getYDoubleArray() throws Exception {
    return this.Y.transpose().getArray()[0];
  }

  public int[] getYIntArray() throws Exception {
    double[] yy = getYDoubleArray();
    int[] answers = new int[yy.length];
    for(int yi = 0; yi < yy.length; yi++) {
      answers[yi] = (int)yy[yi];
    }
    return answers;
  }
      
  public double[][] getXArray() throws Exception {
    return this.X.getArray();
  }

  public int getM() throws Exception {
    return this.m;
  }

  public int getN() throws Exception {
    return this.n;
  }

}
