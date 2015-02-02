package opennlp.ccg.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of a sparse chart as a hash map of a hash map. This implementation has cost
 * O(1) for set and get operations.
 * 
 * This chart is more efficient in space and less efficient in time than a dense chart. It is to be
 * preferred in cases where the chart has many atoms.
 * 
 * @author Daniel Couto-Vale
 */
public class SparseChart implements Chart, Serializable {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = -55772431506718482L;

	/**
	 * The data structure.
	 */
	private final Map<Integer, Map<Integer, Form>> formMapMap;

	/**
	 * The size of the chart.
	 */
	private final int size;

	/**
	 * Constructor
	 * 
	 * @param size the size of the table
	 */
	public SparseChart(int size) {
		this.size = size;
		this.formMapMap = new HashMap<Integer, Map<Integer, Form>>();
	}

	/**
	 * Constructor
	 * 
	 * @param chartFile the chart file
	 * @throws IOException when there is a problem with reading the file
	 * @throws FileNotFoundException when the file is not found
	 */
	@SuppressWarnings("unchecked")
	public SparseChart(File chartFile) throws FileNotFoundException , IOException {
		FileInputStream fis = new FileInputStream(chartFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		try {
			this.formMapMap = (HashMap<Integer, Map<Integer, Form>>) ois.readObject();
			int maxIndex = 0;
			for (Integer index : formMapMap.keySet()) {
				if (index > maxIndex) {
					maxIndex = index;
				}
			}
			this.size = maxIndex + 1;
		} catch (ClassNotFoundException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		} finally {
			ois.close();
			bis.close();
			fis.close();
		}
	}

	@Override
	public final Form getForm(int first, int last) {
		if (first < 0 || last >= size || first > last) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Map<Integer, Form> formMap = formMapMap.get(first);
		if (formMap == null) {
			return null;
		}
		return formMap.get(last);
	}

	@Override
	public final void setForm(int first, int last, Form form) {
		if (first < 0 || last >= size || first > last) {
			throw new ArrayIndexOutOfBoundsException();
		}
		Map<Integer, Form> formMap = formMapMap.get(first);
		if (formMap == null) {
			formMap = new HashMap<Integer, Form>();
			formMapMap.put(first, formMap);
		}
		formMap.put(last, form);
	}

	@Override
	public final int size() {
		return size;
	}

}
