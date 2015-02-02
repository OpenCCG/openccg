package opennlp.ccg.parse;

import java.io.PrintStream;

import opennlp.ccg.synsem.Symbol;
import opennlp.ccg.synsem.SymbolHash;

public class ChartPrinter {

	public final void print(Chart chart, PrintStream out) {

		int[] sizes = new int[chart.getSize()];
		int rows = 0;
		for (int i = 0; i < chart.getSize(); i++) {
			for (int j = i; j < chart.getSize(); j++) {
				if (chart.getForm(i, j) != null) {
					if (chart.getForm(i, j).size() > sizes[i]) {
						sizes[i] = chart.getForm(i, j).size();
					}
				}
			}
			rows += sizes[i];
		}

		String[][] toprint = new String[rows][chart.getSize()];
		String[] words = new String[chart.getSize()];
		int maxwidth = 0;

		for (int i = 0, row = 0; i < chart.getSize(); row += sizes[i++]) {
			for (int j = 0; j < chart.getSize(); j++) {
				for (int s = 0; s < sizes[i]; s++) {
					SymbolHash symbols;
					if (chart.getForm(i, j) == null) {
						symbols = new SymbolHash();
					} else {
						symbols = chart.getForm(i, j).getSymbols();
					}
					if (i == j) {
						words[i] = symbols.asSignSet().iterator().next().getOrthography();
					}
					if (symbols.size() >= s + 1) {
						toprint[row + s][j] = ((Symbol) symbols.toArray()[s]).getCategory().toString();
						if (toprint[row + s][j].length() > maxwidth)
							maxwidth = toprint[row + s][j].length();
					}
				}
			}
		}

		int fullwidth = chart.getSize() * (maxwidth + 3) - 1;
		out.print(" ");
		for (String w : words) {
			out.print(w);
			int pad = (maxwidth + 3) - w.length();
			for (int p = 0; p < pad; p++) {
				out.print(" ");
			}
		}
		out.print("|");
		out.println();
		for (int p = 0; p < fullwidth; p++) {
			out.print("-");
		}
		out.print("| ");
		out.println();

		for (int i = 0, entry = sizes[0], e = 0; i < rows; i++) {
			if (i == entry) {
				out.print("|");
				for (int p = 0; p < fullwidth; p++) {
					out.print("-");
				}
				out.print("|");
				out.println();
				entry += sizes[++e];
			}
			out.print("| ");

			for (int j = 0; j < chart.getSize(); j++) {
				int pad = 1 + maxwidth;
				if (toprint[i][j] != null) {
					out.print(toprint[i][j]);
					pad -= toprint[i][j].length();
				}
				for (int p = 0; p < pad; p++)
					out.print(" ");
				out.print("| ");
			}
			out.println();
		}
		out.print("|");
		for (int p = 0; p < fullwidth; p++)
			out.print("-");
		out.print("| ");
		out.println();
	}

}
