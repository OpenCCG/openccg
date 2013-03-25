#!/bin/bash
# based on compile_query_only.sh

echo Compiling source files

rm {lm,util}/*.o 2>/dev/null
set -e

CXXFLAGS="-I. -O3 -DNDEBUG -DKENLM_MAX_ORDER=6 -fPIC $CXXFLAGS"

#Grab all cc files in these directories except those ending in test.cc or main.cc
objects=""
for i in util/double-conversion/*.cc util/*.cc lm/*.cc; do
  if [ "${i%test.cc}" == "$i" ] && [ "${i%main.cc}" == "$i" ]; then
    g++ $CXXFLAGS -c $i -o ${i%.cc}.o
    objects="$objects ${i%.cc}.o"
  fi
done



echo Compiling binaries into bin

mkdir -p bin
g++ $CXXFLAGS lm/build_binary_main.cc $objects -o bin/build_binary
g++ $CXXFLAGS lm/query_main.cc $objects -o bin/query
g++ $CXXFLAGS lm/kenlm_max_order_main.cc -o bin/kenlm_max_order



echo Compiling JNI library and moving it to openccg/lib

g++ $CXXFLAGS jni/wrap.cc -I $JAVA_HOME/include -I $JAVA_HOME/include/linux $objects -shared -Wl,-soname,libken.so -o libken.so -lz -Wno-deprecated -pthread
mv libken.so ../../lib/.
