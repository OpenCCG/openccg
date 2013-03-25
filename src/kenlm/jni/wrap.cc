#include "lm/enumerate_vocab.hh"
#include "lm/model.hh"
#include "util/murmur_hash.hh"

#include <iostream>

#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include <pthread.h>

// Grr.  Everybody's compiler is slightly different and I'm trying to not depend on boost.   
#include <ext/hash_map>

// This is needed to compile on OS X Lion / gcc 4.2.1
namespace __gnu_cxx {
template<>
struct hash<unsigned long long int>
{
	size_t
	operator()(unsigned long long int __x) const
			{
		return __x;
	}
};
}

// Verify that jint and lm::ngram::WordIndex are the same size. If this breaks
// for you, there's a need to revise probString.
namespace {

template<bool> struct StaticCheck {};

template<> struct StaticCheck<true> {
	typedef bool StaticAssertionPassed;
};

typedef StaticCheck<sizeof(jint) == sizeof(lm::WordIndex)>::StaticAssertionPassed FloatSize;

// Vocab ids above what the vocabulary knows about are unknown and should
// be mapped to that.
void MapArray(const std::vector<lm::WordIndex>& map, jint *begin, jint *end) {
	for (jint *i = begin; i < end; ++i) {
		*i = map[*i];
	}
}

char *PieceCopy(const StringPiece &str) {
	char *ret = (char*) malloc(str.size() + 1);
	memcpy(ret, str.data(), str.size());
	ret[str.size()] = 0;
	return ret;
}

// Rather than handle several different instantiations over JNI, we'll just
// do virtual calls C++-side.
class VirtualBase {
public:
	virtual ~VirtualBase() {
	}

	virtual float Prob(jint *begin, jint *end) const = 0;

	virtual float ProbString(jint * const begin, jint * const end,
			jint start) const = 0;

	virtual uint8_t Order() const = 0;

	virtual bool RegisterWord(const StringPiece& word, const int wd_id) = 0;

protected:
	VirtualBase() {
	}

private:
};

template<class Model> class VirtualImpl: public VirtualBase {
public:
	VirtualImpl(const char *name, float fake_oov_cost) :
			m_(name), fake_oov_cost_(fake_oov_cost) {
		// Insert unknown id mapping.
		map_.push_back(0);
	}

	~VirtualImpl() {
	}

	float Prob(jint * const begin, jint * const end) const {
		MapArray(map_, begin, end);

		std::reverse(begin, end - 1);
		lm::ngram::State ignored;
		return *(end - 1) ?
				m_.FullScoreForgotState(
						reinterpret_cast<const lm::WordIndex*>(begin),
						reinterpret_cast<const lm::WordIndex*>(end - 1), *(end - 1),
						ignored).prob :
				fake_oov_cost_;
	}

	float ProbString(jint * const begin, jint * const end, jint start) const {
		MapArray(map_, begin, end);

		float prob;
		lm::ngram::State state;
		if (start == 0) {
			prob = 0;
			state = m_.NullContextState();
		} else {
			std::reverse(begin, begin + start);
			prob = m_.FullScoreForgotState(
					reinterpret_cast<const lm::WordIndex*>(begin),
					reinterpret_cast<const lm::WordIndex*>(begin + start), begin[start],
					state).prob;
			if (begin[start] == 0)
				prob = fake_oov_cost_;
			++start;
		}
		lm::ngram::State state2;
		for (const jint *i = begin + start;;) {
			if (i >= end)
				break;
			float got = m_.Score(state, *i, state2);
			prob += *(i++) ? got : fake_oov_cost_;
			if (i >= end)
				break;
			got = m_.Score(state2, *i, state);
			prob += *(i++) ? got : fake_oov_cost_;
		}
		return prob;
	}

	uint8_t Order() const {
		return m_.Order();
	}

	bool RegisterWord(const StringPiece& word, const int wd_id) {
		if (map_.size() <= wd_id) {
			map_.resize(wd_id + 1, 0);
		}
		bool already_present = false;
		if (map_[wd_id] != 0)
			already_present = true;
		map_[wd_id] = m_.GetVocabulary().Index(word);
		return already_present;
	}

private:
	Model m_;
	float fake_oov_cost_;
	std::vector<lm::WordIndex> map_;
};

VirtualBase *ConstructModel(const char *file_name, float fake_oov_cost) {
	using namespace lm::ngram;
	ModelType model_type;
	if (!RecognizeBinary(file_name, model_type))
		model_type = HASH_PROBING;
	switch (model_type) {
	case HASH_PROBING:
		return new VirtualImpl<ProbingModel>(file_name, fake_oov_cost);
	case TRIE_SORTED:
		return new VirtualImpl<TrieModel>(file_name, fake_oov_cost);
	case ARRAY_TRIE_SORTED:
		return new VirtualImpl<ArrayTrieModel>(file_name, fake_oov_cost);
	case QUANT_TRIE_SORTED:
		return new VirtualImpl<QuantTrieModel>(file_name, fake_oov_cost);
	case QUANT_ARRAY_TRIE_SORTED:
		return new VirtualImpl<QuantArrayTrieModel>(file_name, fake_oov_cost);
	default:
		UTIL_THROW(
				lm::FormatLoadException,
				"Unrecognized file format " << (unsigned) model_type << " in file "
						<< file_name);
	}
}

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_construct(
		JNIEnv *env, jclass, jstring file_name, jfloat fake_oov_cost) {
	const char *str = env->GetStringUTFChars(file_name, 0);
	if (!str)
		return 0;
	jlong ret;
	try {
		ret = reinterpret_cast<jlong>(ConstructModel(str, fake_oov_cost));
	} catch (std::exception &e) {
		std::cerr << e.what() << std::endl;
		abort();
	}
	env->ReleaseStringUTFChars(file_name, str);
	return ret;
}

JNIEXPORT void JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_destroy(
		JNIEnv *env, jclass, jlong pointer) {
	delete reinterpret_cast<VirtualBase*>(pointer);
}

JNIEXPORT jint JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_order(
		JNIEnv *env, jclass, jlong pointer) {
	return reinterpret_cast<VirtualBase*>(pointer)->Order();
}

JNIEXPORT jboolean JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_registerWord(
		JNIEnv *env, jclass, jlong pointer, jstring word, jint id) {
	const char *str = env->GetStringUTFChars(word, 0);
	if (!str)
		return false;
	jint ret;
	try {
		ret = reinterpret_cast<VirtualBase*>(pointer)->RegisterWord(str, id);
	} catch (std::exception &e) {
		std::cerr << e.what() << std::endl;
		abort();
	}
	env->ReleaseStringUTFChars(word, str);
	return ret;
}

JNIEXPORT jfloat JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_prob(
		JNIEnv *env, jclass, jlong pointer, jintArray arr) {
	jint length = env->GetArrayLength(arr);
	if (length <= 0)
		return 0.0;
	// GCC only.
	jint values[length];
	env->GetIntArrayRegion(arr, 0, length, values);

	return reinterpret_cast<const VirtualBase*>(pointer)->Prob(values,
			values + length);
}

JNIEXPORT jfloat JNICALL Java_opennlp_ccg_ngrams_kenlm_jni_KenLM_probString(
		JNIEnv *env, jclass, jlong pointer, jintArray arr, jint start) {
	jint length = env->GetArrayLength(arr);
	if (length <= start)
		return 0.0;
	// GCC only.
	jint values[length];
	env->GetIntArrayRegion(arr, 0, length, values);

	return reinterpret_cast<const VirtualBase*>(pointer)->ProbString(values,
			values + length, start);
}

} // extern
