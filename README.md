# DynaBench - Testing and Evading Dynamic Analysis in Android Apps

## Bachelor Thesis

DynaBench is the result of my bachelor thesis for the [Bachelor of Science Technology, Liberal Arts and Sciences (better known as ATLAS)](https://www.utwente.nl/en/education/bachelor/programmes/university-college-twente/) at the University of Twente. Read the abstract (or full paper) below.

### Abstract of the paper

<p> 
Today’s smart phones are a treasure trove of
private information. Access to this data is often
abused by apps. In response, the research
community has proposed tools to analyse apps
for privacy leaks. To evaluate these tools microbenchmarks
exist for static analysis tools but
not for dynamic analysis tools. These existing
micro-benchmarks are insufficient to test
dynamic analysis since dynamic analysis has
unique problems. In particular, apps can recognise
analysis is happening and stop malicious
behaviour. Techniques to apps use to do this
recognition we call ‘evasion techniques’.
We present DynaBench, an open source
micro-benchmark for dynamic analysis tools.
DynaBench functions by testing if the tool is robust
against these evasion techniques. This is
done by actively trying to evade analysis using
different techniques. We tested DynaBench
against the VirusTotal platform and found that
seven out of thirteen techniques were succesful
in evading dynamic analysis. We conclude that
is remains relatively easy to evade dynamic
analysis.
<p/>

[Link to full paper](CapstoneTimmeBethe.pdf)

## Repository Overview

Just a quick overview of the repository for those who are interested.

* The source code of the app in DynaBench can be found in [`benchmark_src`](benchmark_src/)
* The folder [`dynamic_code_loading`](dynamic-code-loading/) houses an Android project `Dex` that is used to build the code that is fetched by the DynaBench app [`DynamicCodeLoading`](benchmark_src/DynamicCodeLoading/). The other folder `dex_files`, has two compiled dex files that could be fetched by the DynamicCodeLoading app. However, I do not think the `logging` one still works.
* `pipeline.sh` is a short shell script that builds all projects, extracts their debug APKs and submits them to a dynamic analysis tool using `submit_samples.py`.

