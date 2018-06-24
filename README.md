# MDFA-Toolkit

![alt text](imgs/Selection_067.png =250x400)

The multivariate direct filter approach is a generic real-time signal extraction and forecasting framework endowed with a richly parameterized user-interface allowing for adaptive and fully-regularized data analysis in large multivariate time series. The methodology is based entirely in the frequency domain, where all optimization criteria is defined from regularization, to forecasting, to filter constraints. For an in-depth tutorial on the mathematical formation, the reader is invited to check out any of the many publications or tutorials on the subject from https://blog.zhaw.ch/sef.

This MDFA-Toolkit provides a fast, modularized, and adaptive framework 
in JAVA for doing such real-time signal extraction for a variety of applications. Furthermore, we have developed several components to the package featuring streaming
time series data analysis tools not previously available anywhere else. Such new features include:
1) A fractional differencing optimization tool for transforming nonstationary time-series into stationary time series while preserving memory (inspired by Marcos Lopez de Prado's 
recent book on Advances in Financial Machine Learning, Wiley 2018). 
2) Easy to use interface to four different signal generation outputs:
   Univariate series -> univariate signal
   Univariate series -> multivariate signal
   Multivariate series -> univariate signal
   Multivariate series -> multivariate signal
3) Generalization of optimization criterion for the signal extraction. One can use a periodogram, or a model-based spectral density of the data, or anything in between.
4) Real-time adaptive parameterization control - make slight adjustments to the filter process parameterization effortlessly
5) Build a filtering process from several simpler filters, reducing degrees of freedom

This package also provides an API to three other real-time data analysis frameworks that are now or soon available 
   iMetricaFX - An app written entirely in JavaFX for doing time series data analysis with MDFA
   MDFA-DeepLearning - A new recurrent neural network methodology for learning in large noisy time series
   MDFA-Tradengineer - An automated algorithmic trading platform combining MDFA-Toolkit, MDFA-DeepLearning, and Esper - a 
   library for complex event processing (CEP) and streaming analytics   


## Installation 
The package is built using the Gradle package and dependency management tool along with the 
lombok utility which comes as a jar file in the package. To install lombok for your IDE, please refer
to the installation instructions of lombok (projectlombok.org). 

The easiest way to get the package installed, tested and running is by the use of the Eclipse IDE (although IDEs such as IntelliJ and Netbeans should have the same setup). 

Once the package is cloned in an appropriate folder, import it using Import->Gradle->Gradle Project (here assumes Eclipse being used). All the dependencies will be automatically downloaded, included the ND4J which handles the vector/matrix computations, and all the JUnit tests will be run.  

## Examples
For examples and tutorials on this package's use, please visit the author's blog at imetricablog.com. 
