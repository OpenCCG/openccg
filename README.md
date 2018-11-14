# OpenCCG

OpenCCG is a system for parsing and generating text using [combinatory categorial grammar](https://en.wikipedia.org/wiki/Combinatory_categorial_grammar) for syntax and [hybrid logic dependency semantics](https://www.aclweb.org/anthology/P02-1041) for, well, the semantic representation.

If that seems like a mouthful, don't worry too much about the details right now.
You can get started [installing OpenCCG](https://davehowcroft.com/post/installing-openccg/) and [working with OpenCCG using the `tccg` utility](https://davehowcroft.com/post/getting-started-with-openccg/) right now.

If, on the other hand, you want to start understanding what that mouthful means, Johanna Moore at the University of Edinburgh has some [helpful course notes on NLG in general and OpenCCG in particular](https://www.inf.ed.ac.uk/teaching/courses/nlg/).

# Project information

See CHANGES for a description of the project status. Also see the OpenCCG web site and wiki at UT Austin: 

* http://openccg.sf.net
* http://www.utcompling.com/wiki/openccg

This `README.md` file contains the configuration and build instructions. Next you'll probably want to look at the tutorial on writing grammars in the human-friendly 'dot ccg' syntax on [the UT Austin OpenCCG wiki](http://www.utcompling.com/wiki/openccg/visccg-tutorial).

After that it may be helpful to look at the "native" grammar specification in "Specifying Grammars for OpenCCG: A Rough Guide" in `docs/grammars-rough-guide.pdf`, as well as the `SAMPLE_GRAMMARS` file for descriptions of the sample grammars that come with the distribution, including ones using the DotCCG syntax.  A (somewhat dated) programmer's guide to using the OpenCCG realizer appears in `docs/realizer-manual.pdf`.

This release also includes a broad English coverage grammar from the CCGBank and associated statistical models; see `docs/ccgbank-README` for details.


# Requirements

* Version 1.6 or later of the Java 2 SDK (http://java.sun.com)
* For ccg2xml and other tools, Python version 2.4 to 2.7 (http://www.python.org)


# Libraries

If you're working with the latest source version from GitHub, you'll need to download the external libraries from the latest release, as GitHub discourages including binaries in their repos:

* Download the [latest release of OpenCCG from sourceforge](https://sourceforge.net/projects/openccg/)
* Unpack the archive and copy over the files from `openccg/lib/`, as well as `openccg/ccgbank/bin/ner/NERApp.jar`
* Build the latest source as described further below


# Configuring your environment variables

The easiest thing to do is to set the environment variables `JAVA_HOME` and `OPENCCG_HOME` to the relevant locations on your system. Set `JAVA_HOME` to match the top level directory containing the Java installation you want to use.

For example, on Windows:

```
C:\> set JAVA_HOME=C:\Program Files\jdk1.6.0_04
```

or on Unix:

```
% setenv JAVA_HOME /usr/local/java
  (csh)
> export JAVA_HOME=/usr/java
  (ksh, bash)
```

On Windows, to get these settings to persist, it's actually easiest to set your environment variables through the System Properties from the Control Panel. For example, under WinXP, go to Control Panel, click on System Properties, choose the Advanced tab, click on Environment Variables, and add your settings in the User variables area.

Next, likewise set `OPENCCG_HOME` to be the top level directory where you unzipped the download. In Unix, type `pwd` in the directory where this file is and use the path given to you by the shell as `OPENCCG_HOME`.  You can set this in the same manner as for `JAVA_HOME` above.

Next, add the directory `OPENCCG_HOME/bin` to your path. For example, you can set the path in your `.bashrc` file as follows:

```
> export PATH="$PATH:$OPENCCG_HOME/bin"
```

On Windows, you should also add the python main directory to your path.

Finally, if you are going to use [KenLM](https://kheafield.com/code/kenlm/) with very large language models for realization with CCGbank-extracted grammars on linux, you'll also need to set the library load path:

```
> export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$OPENCCG_HOME/lib
```

Once you have taken care of these things, you should be able to build and use the OpenCCG Library.

**Note**: Spaces are allowed in `JAVA_HOME` but not in `OPENCCG_HOME`.  To set an environment variable with spaces in it, you need to put quotes around the value when on Unix, but you must *NOT* do this when under Windows.


# Increasing Java memory limit

If you're working with a broad coverage grammar and statistical parsing or realization models, you'll probably need to increase the default memory limit for running OpenCCG's tools.  You can do so by editing `bin/ccg-env[.bat]`, increasing the JAVA_MEM environment variable at the end of this script.  For training perceptron models in memory, you may need 16g; for realization with the very large gigaword 5-gram model, you may need 8g; otherwise, for parsing and realization with CCGbank-derived models, 4g or possibly even 2g should suffice; finally, for small grammars 512m or 256m should be ok.

# Trying it out

If you've managed to configure the system, you should be able to change to the directory for the "tiny" sample grammar and run `tccg` (for text ccg), the command-line tool for interactively testing grammars:

```
> cd grammars
> cd tiny
> tccg (Windows/Unix)
```

Provided tccg starts properly, it loads the grammar files, parses them, and shows the command-line interface (at which point you can type `:h` for help or `:q` to quit).

If you trouble starting up tccg, make sure you have set the environment variables properly, and that the tccg script (located in `openccg/bin`) calls the right shell environment (top-line of the script; to solve the problem, either comment out this line or correct the path).


# Visualizing semantic graphs

Semantic dependency graphs in testbed files can be visualized with the help of Graphviz's dot tool.  First, download and install [Graphviz](http://www.graphviz.org/).  Then, use tccg to create a testbed files with logical forms in it.  For example, you can try some examples in the worldcup sample grammar and save them to a file using the command ':2tb tb.xml'.  Then make a directory to store the visualized graphs. Finally, run the ccg-draw-graph tool as shown below:

```
> cd grammars/worldcup
> tccg (parse examples, save using ':2tb tb.xml')
> mkdir graphs
> ccg-draw-graph -i tb.xml -v graphs/g
```

You can also show the semantic classes or word indices using the `-c` or `-w` options, respectively.  The graphs can be displayed with any PDF display tool.

Note that the graph visualization requires the logical forms to be stored in an xml node-rel format for graphs, as in the worldcup or routes sample grammars.  See `SAMPLE_GRAMMARS` for more information.


# Creating disjunctive logical forms

This release includes a new disjunctivizer package, for creating a disjunctive LF XML structure based on an LF graph difference.  An LF graph difference is a characterization of the difference between two Hybrid Logic Dependency Semantics graphs and an alignment between them in terms of the edits needed to make one into the other: inserts, deletes, and substitutions.  See the build file for junit tests that illustrate how to use the package.

# Generating grammar documentation

OpenCCG includes a tool for generating HTML documentation of the XML files that specify a grammar. It can be run either from the `ccg-grammardoc` script in the `bin/` directory, or as an Ant task. An example of how to incorporate GrammarDoc into an Ant build file is given in the "tiny" grammar (`grammars/tiny/build.xml`), in a build target called `document`.


# Building the system from source

The OpenCCG build system is based on Apache Ant.  Ant is a little but very handy tool that uses a build file written in XML (`build.xml`) as building instructions.  Building the Java portion of OpenCCG is accomplished using the script `ccg-build`; this works under Windows and Unix, but requires that you run it from the top-level directory (where the `build.xml` file is located).  If everything is right and all the required packages are visible, this action will generate a file called openccg.jar in the `./lib` directory.

Note that you should *not* build from source by invoking 'ant' directly.  Instead, you should use `ccg-build` as shown below (Unix), after ensuring that you've set `OPENCCG_HOME`, `JAVA_HOME` and updated your `PATH` (the `ccg-build` script invokes ant with various parameters that aren't set properly if ant is invoked from the command line):

```
> cd $OPENCCG_HOME
> ccg-build
```

# Working with the Eclipse IDE

The Eclipse IDE can be used for editing the Java source code, though setup can be a bit tricky.  The most reliable method seems to be as follows.  First, follow the instructions above for building the source from the command line.  Then, in Eclipse, choose File|New|Java Project to create a new Java Project, and give it a name, such as 'openccg'. Leave the default settings as they are, and click Next.  Then choose Link Additional Source and browse to the folder `src/` in the directory where you installed OpenCCG (i.e. `$OPENCCG_HOME/src`).  You'll need to give this location a new name, such as 'src2' ('src' is already taken by default).  The final step is to Add External JARs under the Libraries tab.  From OpenCCG's lib directory (i.e. `$OPENCCG_HOME/lib`), choose all of the `.jar` files.  At this point, you should be able to hit Finish and the code should compile in Eclipse.

Note that with Eclipse's default settings, the code will compile in your Eclipse workspace, which is separate from your OpenCCG installation (this is a good thing, as Eclipse uses a `bin/` directory for compiled Java classes, whereas OpenCCG uses `bin/` for command-line scripts).  Thus, once you have made a round of changes in Eclipse and are ready to try them out in OpenCCG, go back to the command line in `$OPENCCG_HOME` and invoke `ccg-build` to re-build the `openccg.jar` file. This will make your changes available in OpenCCG's programs, such as `tccg`.

# Bug Reports

Please report bugs at by creating [an issue with a description of the problem](https://github.com/OpenCCG/openccg/issues). 

