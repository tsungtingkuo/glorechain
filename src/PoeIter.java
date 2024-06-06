import Jama.Matrix;
import java.io.*;
import java.util.*;
import utility.*;
import glore.*;

class PoeIter {

  public static void iterate(Config pc, String[] order, String testSite) throws Exception {

      // count the number of iterations
      int iter = 0;

      // Time: Glore
      GloreClient gc = new GloreClient(pc.trainFileName, pc.testFileName);

      // Flags
      Flag[] flags = {Flag.TRANSFER, Flag.CONSENSUS};

      // Variables
      PrintWriter pw;
      Model em;

      // iteratively update beta1
      while (!gc.isConverged(iter, pc.maxIteration)) {

        String nextSiteName = order[(iter % pc.totalSiteNumber)];

        System.out.println("Next site name: " + nextSiteName);
        System.out.println("Difference: " + gc.getDifference());
        System.out.println("Iteration " + iter);

        // Tim: update model
        gc.updateModel();

        // Tim: get D and E
        Utility.saveDouble2DArray(pc.modelVarianceFileName, gc.getD(), "\t");
        Utility.saveDoubleArray(pc.modelMeanFileName, gc.getE());

        // Tim: wait between reading and writing
        System.out.print(
            "Waiting "
                + (pc.waitingTimePeriod / 1000)
                + " seconds between iteration for reading/writing ... ");
        Thread.sleep(pc.waitingTimePeriod);
        System.out.println("done.");

        // Tim: send D and E to blockchain
        System.out.print("Submitting D and E to blockchain ..." );
        pw = new PrintWriter(pc.trainingResultFileName);
        pw.print(gc.getDifference());
        pw.flush();
        pw.close();
        em =
            new Model(
                pc.modelMeanFileName,
                pc.modelVarianceFileName,
                pc.trainingResultFileName,
                Flag.UPDATE,
                pc.siteName,
                nextSiteName,
                null,
                iter);
        Util.submitModel(pc, em);
        System.out.println("done.");

        // Tim: check if this site is next 'server' site
        if(pc.siteName.equalsIgnoreCase(nextSiteName)) {

          System.out.println("Serving as 'server' for this iteration.");
          int m = gc.getE().length;
          int s = pc.totalSiteNumber;
  
          // allocate memory for the client's data
          double[][][] D = new double[s][m][m];
          double[][] E = new double[s][m];
          double[] beta0 = new double[m];
          double[] beta1 = new double[m];
          int i;

          // Tim: read D and E from blockchain
          System.out.print(
              "Waiting for collecting D and E from all sites, polling every "
                  + (pc.pollingTimePeriod / 1000)
                  + " seconds ... ");
          boolean collected = false;
          while (true) {
            collected = isModelCollected(pc, Flag.UPDATE);
            if (collected) {
              break;
            }
            Thread.sleep(pc.pollingTimePeriod);
          }
          System.out.println("done.");
          for(int sn = 0; sn < order.length; sn++) {
            em = findModelFromSite(pc, Flag.UPDATE, order[sn]);
            D[sn] = em.modelVariance;
            E[sn] = em.modelMean;
          }

          beta1 = gc.getBeta1();
          beta0 = Arrays.copyOf(beta1, beta1.length);
          beta1 = GloreServer.combineModels(D, E, beta1);
          gc.setBeta1(beta1);

          // Tim: send beta1 to blockchain
          System.out.print("Submitting beta1 to blockchain ..." );
          Utility.saveDoubleArray(pc.modelMeanFileName, beta1);
          pw = new PrintWriter(pc.trainingResultFileName);
          pw.print(GloreUtil.getDifference(beta0, beta1));
          pw.flush();
          pw.close();
          if(GloreUtil.isConverged(beta0, beta1, iter, pc.maxIteration)) {
            em =
                new Model(
                    pc.modelMeanFileName,
                    pc.modelVarianceFileName,
                    pc.trainingResultFileName,
                    Flag.CONSENSUS,
                    pc.siteName,
                    pc.siteName,
                    null,
                    iter);
          }
          else {
            em =
                new Model(
                    pc.modelMeanFileName,
                    pc.modelVarianceFileName,
                    pc.trainingResultFileName,
                    Flag.TRANSFER,
                    pc.siteName,
                    pc.siteName,
                    null,
                    iter);
          }
          Util.submitModel(pc, em);
          System.out.println("done.");

        }
        else {

          // Tim: receive beta1 from blockchain
          System.out.print(
              "Waiting for collecting beta1 from 'server', polling every "
                  + (pc.pollingTimePeriod / 1000)
                  + " seconds ... ");
          boolean collected = false;
          while (true) {
            collected = isModelCollectedFromSite(pc, flags, iter, nextSiteName);
            if (collected) {
              break;
            }
            Thread.sleep(pc.pollingTimePeriod);
          }
          System.out.println("done.");
          em = findModelFromSiteForIter(pc, flags, nextSiteName, iter);
    
          // Tim: set beta1
          gc.setBeta1(em.modelMean);
        }

        System.out.println(Arrays.toString(gc.getBeta1()));
        System.out.println();
    
        iter = iter + 1;
      }

      System.out.println("Difference on convergence: " + gc.getDifference());
      System.out.println("Finished iteration.");

      // Tim: compute AUC and send (1 - auc) to blockchain
      double trainAUC = gc.computeTrainAUC();
      System.out.println("Train AUC = " + trainAUC);
      double testAUC = gc.computeTestAUC();
      System.out.println("Test AUC = " + testAUC);

      // Wait for AUCs
      int errorWaitingTime = pc.waitingTimePeriod;
      System.out.print(
          "Waiting "
              + (errorWaitingTime / 1000)
              + " seconds to evaluate result for other sites ... ");
      Thread.sleep(errorWaitingTime);
      System.out.println("done.");

      System.out.print("Submitting Test AUC to blockchain for result collection ..." );
      pw = new PrintWriter(pc.testingResultFileName);
      pw.print(testAUC);
      pw.flush();
      pw.close();
      em =
          new Model(
              pc.modelMeanFileName,
              pc.modelVarianceFileName,
              pc.testingResultFileName,
              Flag.TEST,
              pc.siteName,
              pc.siteName,
              null,
              iter - 1);
      if (Util.submitModel(pc, em)) {
        System.out.println("done.");
      } else {
        System.out.println("failed.");
      }
      System.out.println();

      Util.saveIteration(pc, testSite, iter - 1);
  }

  public static boolean isModelCollected(Config pc, Flag flag) throws Exception {
    HashSet<String> completedSites = new HashSet<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);
    for (Model em : models) {
      if (em.getFlag() == flag && pc.siteName.equalsIgnoreCase(em.getToSite())) {
        completedSites.add(em.getFromSite());
      }
    }
    if (completedSites.size() >= pc.totalSiteNumber) {
      return true;
    }
    return false;
  }

  public static boolean isModelCollectedFromSite(Config pc, Flag flag, int iter, String fromSite)
      throws Exception {
    HashSet<String> completedSites = new HashSet<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);
    for (Model em : models) {
      if ((em.getFlag() == flag && fromSite.equalsIgnoreCase(em.getFromSite()))
          && em.getIteration() == iter) {
        return true;
      }
    }
    return false;
  }

  public static boolean isModelCollectedFromSite(Config pc, Flag[] flags, int iter, String fromSite)
      throws Exception {
    HashSet<String> completedSites = new HashSet<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);
    for (Model em : models) {
      boolean isFlagFound = false;
      for(Flag flag : flags) {
        if (em.getFlag() == flag) {
          isFlagFound = true;
          break;
        }
      }
      if ((isFlagFound && fromSite.equalsIgnoreCase(em.getFromSite()))
          && em.getIteration() == iter) {
        return true;
      }
    }
    return false;
  }

  public static Model findModelFromSite(Config pc, Flag flag, String fromSite)
      throws Exception {
    ArrayList<Model> models = Util.getRecentModels(pc);
    Model model = null;
    for (Model em : models) {
      if ((em.getFlag() == flag
          && pc.siteName.equalsIgnoreCase(em.getToSite())
          && fromSite.equalsIgnoreCase(em.getFromSite()))) {
        model = em;
      }
    }
    return model;
  }

  public static Model findModelFromSiteForIter(Config pc, Flag flag, String fromSite, int iter) throws Exception {
    ArrayList<Model> models = Util.getRecentModels(pc);
    Model model = null;
    for (Model em : models) {
      if ((em.getFlag() == flag && fromSite.equalsIgnoreCase(em.getFromSite()))
          && em.getIteration() == iter) {
        model = em;
      }
    }
    return model;
  }

  public static Model findModelFromSiteForIter(Config pc, Flag[] flags, String fromSite, int iter) throws Exception {
    ArrayList<Model> models = Util.getRecentModels(pc);
    Model model = null;
    for (Model em : models) {
      boolean isFlagFound = false;
      for(Flag flag : flags) {
        if (em.getFlag() == flag) {
          isFlagFound = true;
          break;
        }
      }
      if ((isFlagFound && fromSite.equalsIgnoreCase(em.getFromSite()))
          && em.getIteration() == iter) {
        model = em;
      }
    }
    return model;
  }
}
