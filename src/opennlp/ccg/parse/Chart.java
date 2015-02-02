package opennlp.ccg.parse;

import java.io.PrintStream;

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
	 * Gets the form ending at a end position and with a given length
	 * 
	 * @param end the end position 
	 * @param length the length of the forms
	 * @return the forms ending at the end position and with a given length
	 */
	Cell getForm(int end, int length);


	/**
	 * Sets a form token ending at a end position and with a given length 
	 * 
	 * @param end the end position 
	 * @param length the length of the forms
	 * @param form the form to set
	 */
	void setForm(int end, int length, Cell form);


	/**
	 * Prints the chart to the output stream
	 * 
	 * @param out the output stream
	 */
	void print(PrintStream out);

	/**
	 * @return the size of the chart
	 */
	int getSize();

}
