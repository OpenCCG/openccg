from ubuntu:16.04

# install dependencies
RUN apt-get update && \
    apt-get install -y default-jdk ant maven python2.7 graphviz \
        jam libboost-all-dev zlib1g-dev gfortran g++ make \
        git wget

# set up all the environment variables
ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64" \
    OPENCCG_HOME="/openccg" \
    ANT_HOME="/usr/share/ant"
ENV PATH="$PATH:$OPENCCG_HOME/bin"
ENV PATH="$PATH:$ANT_HOME/bin"
ENV PATH="$PATH:/maxent/src/opt"

COPY . ./openccg

WORKDIR /openccg

# optionally build models if they are present
RUN ./models.sh && \
#    # set up all the jar dependencies
    mvn dependency:copy-dependencies -DoutputDirectory='./lib' && \
    # set up stanford core nlp dependencies
    mv lib/stanford-corenlp-3.9.2.jar ccgbank/stanford-nlp/stanford-core-nlp.jar && \
    jar xf lib/stanford-corenlp-3.9.2-models.jar && \
    cp edu/stanford/nlp/models/ner/* ccgbank/stanford-nlp/classifiers/ && \
    rm -rf edu && \
    # set up the project
    ccg-build

#    # set up the maxent toolkit
#WORKDIR /usr/share
#
#RUN git clone https://github.com/lzhang10/maxent
#
#WORKDIR /usr/share/maxent
#
#RUN ./configure && make && make install

#    # set up the srilm tools
#WORKDIR /usr/share
#
#RUN wget https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/moses-suite/srilm-1.6.0.simple.tar.gz && \
#    tar xzf srilm-1.6.0.simple.tar.gz && \
#    cd srilm-1.6.0 && \
#    # set the path to SRILM
#    sed -i "s/# SRILM =.*/SRILM = \/usr\/share\/srilm-1.6.0/" Makefile && \
#    # for 64 bit systems
#    make MACHINE_TYPE=i686-m64
#    # for 32 bit systems (not tested, but might work)
#    #make MACHINE_TYPE=i686

#WORKDIR /openccg

CMD ["bash"]
