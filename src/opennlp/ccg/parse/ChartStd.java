package opennlp.ccg.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;

import opennlp.ccg.synsem.Sign;
import opennlp.ccg.synsem.SignHash;

/**
 * The standard implementation of a chart as a (n x n) table.
 * 
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @author Daniel Couto-Vale
 */
public class ChartStd implements Chart, Serializable {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = -55772431506718482L;

	/**
	 * The data structure.
	 */
	private final Cell[][] table;

	/**
	 * Constructor
	 * 
	 * @param size the size of the table
	 */
	public ChartStd(int size) {
		table = new Cell[size][size];
	}

	/**
	 * Constructor
	 * 
	 * @param chartFile the chart file
	 * @throws IOException when there is a problem with reading the file
	 * @throws FileNotFoundException when the file is not found
	 */
	public ChartStd(File chartFile) throws FileNotFoundException , IOException {
		FileInputStream fis = new FileInputStream(chartFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		try {
			table = (Cell[][]) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		} finally {
			ois.close();
			bis.close();
			fis.close();
		}
	}

	@Override
	public final Cell getForm(int end, int length) {
		return table[end][length];
	}

	@Override
	public final void setForm(int end, int length, Cell form) {
		table[end][length] = form;
	}

	@Override
	public void print(PrintStream out) {
		int[] sizes = new int[getSize()];
		int rows = 0;
		for (int i = 0; i < getSize(); i++) {
			for (int j = i; j < getSize(); j++) {
				if (getForm(i, j) != null) {
					if (getForm(i, j).size() > sizes[i]) {
						sizes[i] = getForm(i, j).size();
					}
				}
			}
			rows += sizes[i];
		}

		String[][] toprint = new String[rows][getSize()];
		String[] words = new String[getSize()];
		int maxwidth = 0;

		for (int i = 0, row = 0; i < getSize(); row += sizes[i++]) {
			for (int j = 0; j < getSize(); j++) {
				for (int s = 0; s < sizes[i]; s++) {
					SignHash symbols;
					if (getForm(i, j) == null) {
						symbols = new SignHash();
					} else {
						symbols = getForm(i, j).getSigns();
					}
					if (i == j) {
						words[i] = symbols.asSignSet().iterator().next().getOrthography();
					}
					if (symbols.size() >= s + 1) {
						toprint[row + s][j] = ((Sign) symbols.toArray()[s]).getCategory().toString();
						if (toprint[row + s][j].length() > maxwidth)
							maxwidth = toprint[row + s][j].length();
					}
				}
			}
		}

		int fullwidth = getSize() * (maxwidth + 3) - 1;
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

			for (int j = 0; j < getSize(); j++) {
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

	@Override
	public final int getSize() {
		return table.length;
	}

}
