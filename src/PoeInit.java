import java.io.*;
import java.util.*;
import utility.*;

public class PoeInit {

  public static void model(String testSite) throws Exception {

    Config pc = new Config(testSite);
    Log.reset(pc.logFileName);

    System.out.println();
    System.out.println("Proof-of-Equity: " + pc.siteName + " (" + pc.nodeAddress + ")");

    System.out.println();
    System.out.print("Submitting empty model to blockchain for clearing previous results ... ");
    PrintWriter pw = new PrintWriter(pc.testingResultFileName);
    pw.print("0.0");
    pw.flush();
    pw.close();
    Model em =
        new Model(
            pc.modelMeanFileName,
            pc.modelVarianceFileName,
            pc.testingResultFileName,
            Flag.CLEAR,
            pc.siteName,
            pc.siteName,
            null,
            0);
    if (Util.submitModel(pc, em)) {
      System.out.println("done.");
    } else {
      System.out.println("failed.");
    }

    System.out.print(
        "Waiting for result clearing from all sites, polling every "
            + (pc.pollingTimePeriod / 1000)
            + " seconds ... ");
    boolean cleared = false;
    while (true) {
      cleared = checkResults(pc, Flag.CLEAR);
      if (cleared) {
        break;
      }
      Thread.sleep(pc.pollingTimePeriod);
    }
    System.out.println("done.");

    System.out.print(
        "Waiting "
            + (pc.waitingTimePeriod / 1000)
            + " seconds to confirm clearance of previous results for other sites ... ");
    Thread.sleep(pc.waitingTimePeriod);
    System.out.println("done.");
    System.out.println();

    System.out.print("Submitting empty model again to blockchain to determine order ... ");
    pw = new PrintWriter(pc.trainingResultFileName);
    pw.print("0.0");
    pw.flush();
    pw.close();
    em =
        new Model(
            pc.modelMeanFileName,
            pc.modelVarianceFileName,
            pc.trainingResultFileName,
            Flag.INITIALIZE,
            pc.siteName,
            pc.siteName,
            null,
            0);
    if (Util.submitModel(pc, em)) {
      System.out.println("done.");
    } else {
      System.out.println("failed.");
    }

    System.out.print(
        "Waiting for initializing order of all sites, polling every "
            + (pc.pollingTimePeriod / 1000)
            + " seconds ... ");
    boolean initialized = false;
    while (true) {
      initialized = checkResults(pc, Flag.INITIALIZE);
      if (initialized) {
        break;
      }
      Thread.sleep(pc.pollingTimePeriod);
    }
    System.out.println("done.");

    System.out.print(
        "Waiting "
            + (pc.waitingTimePeriod / 1000)
            + " seconds for other sites to determine the order ... ");
    Thread.sleep(pc.waitingTimePeriod);
    System.out.println("done.");
    System.out.println();

    String[] order = getOrder(pc);
    System.out.println("Order determined based on site names:");
    System.out.print(order[0]);
    for(int i=1; i < order.length; i++) {
        System.out.print(" --> " + order[i]);
    }
    System.out.println();
    System.out.println();

    System.out.print(
        "Waiting "
            + (pc.waitingTimePeriod / 1000)
            + " seconds for other sites to initialze ... ");
    Thread.sleep(pc.waitingTimePeriod);
    System.out.println("done.");
    System.out.println();

    // Iteration
    PoeIter.iterate(pc, order, testSite);
  }

  public static String[] getOrder(Config pc) throws Exception {
    ArrayList<String> order = new ArrayList<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);
    for (Model em : models) {
      if (em.getFlag() == Flag.INITIALIZE) {
        order.add(em.getFromSite());
      }
    }
    Collections.sort(order);
    return order.toArray(new String[order.size()]);
  }

  public static boolean checkResults(Config pc, Flag flag) throws Exception {
    HashSet<String> completedSites = new HashSet<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);
    for (Model em : models) {
      if (em.getFlag() == flag) {
        completedSites.add(em.getFromSite());
      }
    }
    if (completedSites.size() >= pc.totalSiteNumber) {
      return true;
    }
    return false;
  }
}

