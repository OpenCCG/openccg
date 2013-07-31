package opennlp.ccg.ngrams.kenlm.jni;

// TODO(Joshua devs): include my state object with your LM state then
// update this API to pass state instead of int[].

public class KenLM {
  /**
   * Load the binary, platform-dependent library containing the KenLM JNI
   * bridge code. 
   * @throws UnsatisfiedLinkError If Java can't find the 'ken' library.
   */
  static {
      System.loadLibrary("ken");
  }

  private final long pointer;
  // this is read from the config file, used to set maximum order
  private final int ngramOrder;
  // inferred from model file (may be larger than ngramOrder)
  private final int N;


  private final static native long construct(String file_name, float fake_oov);

  private final static native void destroy(long ptr);

  private final static native int order(long ptr);

  private final static native boolean registerWord(long ptr, String word, int id);

  private final static native float prob(long ptr, int words[]);

  private final static native float probString(long ptr, int words[], int start);

  public KenLM(int order, String file_name) {
    float lm_ceiling_cost = 99.0f;
    ngramOrder = order;
    pointer = construct(file_name, -lm_ceiling_cost);
    N = order(pointer);
  }

  public void destroy() {
    destroy(pointer);
  }

  public int getOrder() {
    return N;
  }

  public boolean registerWord(String word, int id) {
    return registerWord(pointer, word, id);
  }

  public float prob(int words[]) {
    return prob(pointer, words);
  }

}
