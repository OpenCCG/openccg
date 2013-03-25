package opennlp.ccg.realize.hypertagger;

import opennlp.ccg.synsem.LF;

/**
 * @author espinosa
 *	This class encapsulates a LF and its corresponding gold standard predicate info, if available.
 */
public class LFInfo {
	LF lf;
	String fullWords;
	String lfNum;
	public LFInfo(LF lf, String fullWords, String lfNum) {
		this.lf = lf;
		this.fullWords = fullWords;
		this.lfNum = lfNum;
	}
	public LF getLF() {
		return this.lf;
	}
	public String getFullWords() {
		return this.fullWords;
	}
	public String getLFNum() {
		return this.lfNum;
	}
}
