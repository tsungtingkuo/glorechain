import java.io.*;
import java.util.*;
import utility.*;

public class GloreChain {

  public static void main(String[] args) throws Exception {

    String datasetName = args[0];
    String testSite = args[1];

    Config pc = new Config(testSite, true);
    pc.change(datasetName, testSite, "true");
    pc = new Config(testSite);

    PoeInit.model(testSite);

    System.out.print(
        "Waiting for results from all sites, polling every "
            + (pc.pollingTimePeriod / 1000)
            + " seconds ... ");
    double averageResult = -1;
    while (true) {
      averageResult = computeAverageConsensusResult(pc);
      if (averageResult != -1) {
        break;
      }
      Thread.sleep(pc.pollingTimePeriod);
    }
    String iteration = Utility.loadStringArray(pc.iterationFileName)[0];
    System.out.println("Done, average test result among sites = " + averageResult + ", iteration = " + iteration);
    System.out.println();

    pc.clear();
  }

  public static double computeAverageConsensusResult(Config pc) throws Exception {
    HashSet<String> completedSites = new HashSet<String>();
    ArrayList<Model> models = Util.getRecentModels(pc);

    for (Model em : models) {
      if (em.getFlag() == Flag.TEST) {
        completedSites.add(em.getFromSite());
      }
    }

    double sumResult = 0.0d;

    if (completedSites.size() >= pc.totalSiteNumber) {
      System.out.println();
      for (Model em : models) {
        if (em.getFlag() == Flag.TEST) {
          sumResult += em.getResult();
        }
      }
      double averageResult = sumResult / (double) pc.totalSiteNumber;
      return averageResult;
    }
    return -1;
  }
}
