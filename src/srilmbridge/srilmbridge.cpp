/* $Id: srilmbridge.cpp,v 1.13 2007/06/16 22:26:28 coffeeblack Exp $ */
#include <jni.h>
#include <LM.h>
#include <Ngram.h>
#include <NgramCountLM.h>
#include "opennlp_ccg_ngrams_SRILMNgramModel.h"

/*
 * Bridge from Java to the SRILM toolkit library. Loads a language model based
 * on specified parameters, then calculates the probability of a word within a
 * given context.
 *
 * Author: Scott Martin (http://www.ling.osu.edu/~scott/)
 * Version: $Revision: 1.13 $
 */

const static unsigned STANDARD = 0, COUNT = 1;

/*
 * The language model we will use to calculate word probabilities.
 */
LM *lm = NULL;

/*
 * The type of language model in effect, as specified in loadLM.
 */
unsigned nativeLMType = STANDARD;

/*
 * Loads a language model from a specified file with the specified ngram order.
 * The parameter lmType specifies what type (format) of language model to
 * expect.
 *
 * Throws:
 *	java.io.IOException If fileName is null or empty or if a problem is
 * 		encountered reading the language model file.
 *	java.lang.IllegalStateException If an LM has already been loaded.
 *	java.lang.IllegalArgumentException If the specified LM type is not
 *		supported. Currently supports STANDARD (type 0) and COUNT (type 1).
 */
JNIEXPORT void JNICALL Java_opennlp_ccg_ngrams_SRILMNgramModel_loadLM
  	(JNIEnv *env, jobject obj, jint order, jstring fileName, jint lmType) {
  if(lm != NULL) { // already loaded
  	env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
  		"LM already loaded");
  	return;
  }
  
  nativeLMType = lmType;
  
  if(nativeLMType < STANDARD || nativeLMType > COUNT) {
  	// only STANDARD and COUNT are allowed
  	env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
  		"LM type not supported");
  	return;
  }
  if(fileName == 0 || env->GetStringLength(fileName) == 0) {
		env->ThrowNew(env->FindClass("java/io/IOException"),
			"problem reading LM: empty file name");
		return;
  }
	
  Vocab *vocab = new Vocab;
  lm = (nativeLMType == COUNT)
  	? (LM *)new NgramCountLM(*vocab, order)
  	:	(LM *)new Ngram(*vocab, order);
  
  const char* nativeFileName = env->GetStringUTFChars(fileName, NULL);
  
  File file(nativeFileName, "r", 0);
  unsigned lmError = 0;
  
  if(((File *)&file)->error() > 0) {
  	lmError = 1;
  	env->ThrowNew(env->FindClass("java/io/IOException"),
  		"problem with LM file");
  }
  else if(!lm->read(file)) {
  	lmError = 1;
		env->ThrowNew(env->FindClass("java/io/IOException"), "problem reading LM");
  }
  //TODO the following just repeats finalize(), should be in reusable function
  if(lmError > 0) { // destroy lm so this can be called again
  	vocab->~Vocab();
  	
  	if(lm != NULL) {
  		if(nativeLMType == STANDARD) {
	  		((Ngram *)lm)->~Ngram();
	  	}
	  	else if(nativeLMType == COUNT) {
	  		((NgramCountLM *)lm)->~NgramCountLM();
	  	}
	  	else { // as a failsafe, call the abstract destructor
		  	lm->~LM();
		  }
  	
	  	delete lm;
  	}
  	
		lm = NULL; //TODO clean up file somehow?
  }
  
  ((File *)&file)->close();
  env->ReleaseStringUTFChars(fileName, nativeFileName);
}

/*
 * Uses the SRILM toolkit library to calculate the log prob of a word in a
 * specified context. The context is a history of tokens preceeding the
 * specified word specified in reverse order. For example, to find the
 * probability of "rain" in the context "in the rain", this method should be
 * called with "rain" as the parameter `word' and the array {"the", "in"} in
 * the parameter `context'.
 *
 * To calculate the log probability of a single word with no context, call this
 * method with either (1) NULL, or (2) a zero-length array as the value of
 * the parameter `context'.
 *
 * Throws:
 *	java.lang.IllegalStateException If an error happened while loading
 * 		the LM and word probabilities can not be computed.
 *	java.lang.IllegalArgumentException If the specified word is null or
 *		zero-length or if the specified context contains a null or zero-length
 *		string.
 */
JNIEXPORT jfloat JNICALL Java_opennlp_ccg_ngrams_SRILMNgramModel_doLogProb
  	(JNIEnv *env, jobject obj, jstring word, jobjectArray context) {
  // make sure LM is ok to use
  if(lm == NULL) {
  	env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
  		"LM not loaded");
  	return 0;
  }
  
  // sanity checks must throw Java exceptions
  if(word == NULL || env->GetStringLength(word) == 0) {
  	env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
			"word is null or zero-length");
  	return 0;
  }
  
  int contextLength = (context == NULL) ? 0 : env->GetArrayLength(context);

	VocabString nativeWord = (VocabString)env->GetStringUTFChars(word, NULL);
  VocabString nativeContext[contextLength + 1];
  nativeContext[contextLength] = NULL; // context must be terminated by NULL
  
  // build context, converting each Java string to a VocabString
  jstring jstr = NULL;
  for(unsigned i = 0; i < contextLength; i++) {
  	jstr = (jstring)env->GetObjectArrayElement(context, i);
  	
  	if(jstr == NULL || env->GetStringLength(jstr) == 0) {
  		env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
  			"context contains null or zero-length string");
  		
  		env->DeleteLocalRef(jstr);
  		env->ReleaseStringUTFChars(word, nativeWord); // release word
  		// release already translated strings
  		for(unsigned j = (i - 1); j >= 0; j--) {
  			env->ReleaseStringUTFChars(
  				(jstring)env->GetObjectArrayElement(context, j), nativeContext[j]);
  		}
  		
  		return 0;
  	}
  	
  	nativeContext[i] = (VocabString)env->GetStringUTFChars(jstr, NULL);
  }
  
  if(jstr != NULL) {
	  env->DeleteLocalRef(jstr);
	}
  
  LogP prob = lm->wordProb(nativeWord, nativeContext);
  
  // clean up
  env->ReleaseStringUTFChars(word, nativeWord); // release word
  
  // release context strings if any
  for(unsigned k = 0; k < contextLength; k++) {
  	env->ReleaseStringUTFChars((jstring)env->GetObjectArrayElement(context, k),
  		nativeContext[k]);
	}
  
  return prob;
}

/*
 * Should be called by a finalize() method from within Java. Calls the
 * destructor method on the language model object we are using.
 */
JNIEXPORT void JNICALL Java_opennlp_ccg_ngrams_SRILMNgramModel_finalize
  (JNIEnv *env, jobject obj) {
 if(lm != NULL) { // call local destructors if type was specified
  	if(nativeLMType == STANDARD) {
  		((Ngram *)lm)->~Ngram();
  	}
  	else if(nativeLMType == COUNT) {
  		((NgramCountLM *)lm)->~NgramCountLM();
  	}
  	else { // as a failsafe, call the abstract destructor
	  	lm->~LM();
	  }
  	
  	delete lm;
  }
}

