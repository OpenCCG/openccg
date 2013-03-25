package opennlp.ccg.realize.hypertagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/* this is a copy of TagExtract, modified to extract SRILM-format data using  the full-words
 * element from XML-format LFs.
 * The input is a file in which each line is a "pred-info"-style line, one per LF.
 * The input file is given as the sole commandline argument. 
 * The factors are written to stdout.
 */
public class LMFactorExtractor {
	public static void main(String args[]) {
		BufferedReader rd = null;
		int lineNum = 0;
		int bNum = 0;
		try {
			rd = new BufferedReader(new FileReader(new File(args[0])));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		String line = null;
		StringBuilder out;
		while(true) {
			try { 
				line = rd.readLine();
			}
			catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			lineNum++;
			if(line == null) {
				break;
			}
			if(line.matches("^\\s*$")) {
				continue;
			}
			out = new StringBuilder();
			out.append("<s> ");
			String[] fields = line.split("\\s+");
			bNum = 0;
			for(String f : fields) {
				bNum++;
				String[] info = f.split(":");
				if(info.length != 4) {
					System.err.println("Wrong number of fields encountered in input line " + lineNum + ", bundle " + bNum);
					System.exit(1);
				}
				out.append(info[3]);
				out.append(":S-");
				out.append(info[3]);
				out.append(":P-");
				out.append(info[2]);
				out.append(":T-");
				out.append(info[1]);
				out.append(" ");
			}
			out.append("</s>");
			System.out.println(out.toString());
		}
	}	
}
