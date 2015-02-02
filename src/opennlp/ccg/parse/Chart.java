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

	/**
	 * Gets all forms
	 * 
	 * @return the forms
	 */
	List<Cell> getForms();

	/**
	 * Gets all forms starting at a start position
	 * 
	 * @param start the start position
	 * @return the forms starting at the start position
	 */
	List<Cell> getForms(int start);

	/**
	 * Gets all forms starting at a start position and with a given length
	 * 
	 * @param start the start position 
	 * @param length the length of the forms
	 * @return the forms starting at the start position and with a given length
	 */
	List<Cell> getForms(int start, int length);

	/**
	 * Intantiates a form that realises a grammatical unit
	 *  
	 * @param edge the grammatical unit
	 * @param start the start position of the form
	 * @param length the length of the form 
	 */
	void makeForm(Edge edge, int start, int length);

	/**
	 * Prints to the output stream
	 * 
	 * @param out the output stream
	 */
	void print(PrintStream out);

}
