package opennlp.ccg.parse;

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
	 * Gets the form ending at a end position and with a given hops
	 * 
	 * @param end the end position 
	 * @param hops the hops between characters/words in the forms
	 * @return the forms ending at the end position and with a given hops
	 */
	Cell getForm(int end, int hops);


	/**
	 * Sets a form token ending at a end position and with a given hops 
	 * 
	 * @param end the end position 
	 * @param hops the hops between characters/words in the forms
	 * @param form the form to set
	 */
	void setForm(int end, int hops, Cell form);

	/**
	 * @return the size of the chart
	 */
	int getSize();

}
