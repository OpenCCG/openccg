
compile:
	javah -d . -classpath ${OPENCCG_HOME}/classes \
		opennlp.ccg.ngrams.SRILMNgramModel
	g++ -o ${OPENCCG_HOME}/lib/libsrilmbridge.so \
		-Wl,-soname,srilmbridge.so \
		-I${JAVA_HOME}/include \
		-I${JAVA_HOME}/include/linux \
		-I${SRILM}/include \
		-L${SRILM}/lib/i686 \
		srilmbridge.cpp ${SRILM}/lm/src/LM.cc \
		-shared -lc -ldstruct -lflm -llattice -lmisc -loolm \
		-Wno-deprecated		

clean:
	rm ${OPENCCG_HOME}/lib/libsrilmbridge.so
	rm opennlp_ccg_ngrams_SRILMNgramModel.h
