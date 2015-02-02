package opennlp.ccg.parse;

import java.io.PrintStream;
import java.util.List;

/**
 * A chart.
 * 
 * WARNING: This is an abstraction of a static data structure. It is not supposed to include methods
 * for applying grammatical rules nor any other kind of ruled construction. 
 * 
 * @author Daniel Couto-Vale
 */
public interface Chart {

	List<Cell> getForms();

	List<Cell> getForms(int start);

	List<Cell> getForms(int start, int length);

	List<Cell> makeForm(Edge edge, int start, int length);

	void print(PrintStream out);

}
