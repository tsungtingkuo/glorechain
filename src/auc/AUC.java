package auc;

import java.io.*;
import java.util.*;
import utility.*;

public class AUC {

	public static double compute(double[] scores, int[] answers) throws Exception {
		
		long time = Calendar.getInstance().getTimeInMillis();
                Random r = new Random();
		String unique = time + "." + r.nextLong();
		String dir = "temp";
		String aucName = dir + "/auc" + unique + ".txt";
		String printName = dir + "/print" + unique + ".txt";
		File f = new File(dir);
		f.mkdir();
		PrintWriter pw = new PrintWriter(aucName);
		for(int i=0; i<scores.length; i++) {
			pw.println(scores[i] + " " + answers[i]);
		}
		pw.flush();
		pw.close();
		String[] args = new String[2];
		args[0] = aucName;
		args[1] = "list";
		
		PrintStream so = System.out;
		FileOutputStream fos = new FileOutputStream(printName);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		
		auc.AUCCalculator.main(args);
		System.out.flush();
		
		System.setOut(so);
		ps.flush();
		ps.close();
		fos.flush();
		fos.close();

		FileReader fr = new FileReader(printName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;
		String ls = null;
		String lls = null;
		while ((s=lnr.readLine()) != null) {
			lls = ls;
			ls = s;
		}
		lnr.close();
		fr.close();

		String[] t = ls.split(" ");
		double auc = Double.parseDouble(t[t.length-1]);

		File f1 = new File(aucName);
		f1.delete();
		File f2 = new File(aucName + ".pr");
		f2.delete();
		File f3 = new File(aucName + ".roc");
		f3.delete();
		File f4 = new File(aucName + ".spr");
		f4.delete();
		File f5 = new File(printName);
		f5.delete();

		return auc;
	}
}
