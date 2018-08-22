# LSTM Hypertagger

Code in this folder was adapted from the [taggerflow repository](https://github.com/uwnlp/taggerflow) used to train the supertagging model from [LSTM CCG Parsing](http://homes.cs.washington.edu/~kentonl/pub/llz-naacl.2016.pdf) at NAACL ([Lewis et al., 2016](http://homes.cs.washington.edu/~kentonl/pub/llz-naacl.2016.bib)).

Modifications include added files and added code in existing files. Added files and directories are listed in the summary of modifications below. Added or modified code in existing files is marked with "### ADDED FOR HYPERTAGGER ###" or "### MODIFIED FOR HYPERTAGGER ###" at beginning and end of changed code segment.

## Summary of Modifications
* \* is used below as wildcard for file names
* Added allrel\_\* sub-directories to data folder
* Added categories\_hypertagger, dev\_hypertagger, final\_vocab.p, test\_hypertagger, train\_hypertagger, unknown\_\* files to data folder
* Added data\_stats, db\_csv, eval, feat\_processing, hypertagger\_logs, logs-allrel-engparen, new\_eval, output, pickle, and realize\_eval directories and all contained files to project
* Added AnnotSeqReader and WordFeats classes in ccgbank.py file
* Added client.py, definitions.py, restored\_model.py, and server.py files to project
* Added HypertaggerData class to data.py file
* Added parameters for SupertaggerEvaluator constructor in evaluation.py; also had SupertaggerEvaluator output write predicted supertags to file
* Added several classes to features.py
* Changed taggerflow.py to use components for LSTM hypertagger instead of for Lewis et al.'s supertagger
* Added code to train.py to use statistical analysis components in eval and new\_eval directories

## Dependencies
* Tensorflow (r0.11)
 * https://www.tensorflow.org/versions/r0.11/get_started/os_setup.html#pip-installation
* Python 2.7

## External Downloads
There are some large files needed to run the LSTM hypertagger. These are in the lstm\_files.zip ZIP folder. After unzipping the folder, copy the subdirectories and their contents to this folder (\<OPENCCG\_HOME\>/src/hypertagger_lstm).

## Training and Evaluation
* `python taggerflow.py grid.json`
  * Trains a supertagging model.
  * Logs evaluation results.
  * Writes checkpoints to the log directory.
