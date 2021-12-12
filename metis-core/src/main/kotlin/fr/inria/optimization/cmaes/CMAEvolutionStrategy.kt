@file:Suppress(
    "SpellCheckingInspection", "KDocUnresolvedReference", "RedundantVisibilityModifier", "unused", "SameParameterValue",
    "PropertyName", "GrazieInspection", "PrivatePropertyName"
)

package fr.inria.optimization.cmaes

import java.io.*
import java.util.*
import kotlin.Comparator
import kotlin.math.*

/* 
   Copyright 1996, 2003, 2005, 2007 Nikolaus Hansen 
   e-mail: hansen .AT. lri.fr

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License, version 3,
   as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

log of changes: 
    o updateDistribution(double[][], double[], int) introduced,
      for the time being
      updateDistribution(double[][], double[]) evaluates to
      updateDistribution(double[][], double[], 0), but it might become
      updateDistribution(double[][], double[], popsize)
    o init() cannot be called twice anymore, it's saver like this 
    o warning() and error() print also to display-file
    o checkEigenSystem() call is now an option, gives warnings, not
      errors, and has its precision criteria adapted to Java.
    o 06/08 fix: error for negative eigenvalues did not show up 
    o 09/08: diagonal option included
    o updateDistribution(double[][], double[]) is available, which 
      implements an interface, independent of samplePopulation(). 
    o variable locked is set at the end of supplementRemainders, 
      instead at the beginning (09/03/08)
    o bestever is set anew, if its current fitness is NaN (09/03/08)
    o getBestRecentX() now returns really the recent best (10/03/17) 
      (thanks to Markus Kemmerling for reporting this problem)
    o 2010/12/02: merge of r762 (diagonal option) and r2462 which were 
      subbranches since r752
    o test() uses flgdiag to get internally time linear

WISH LIST:
    o test and consider refinement of 
      updateDistribution(double[][], double[]) that
      implements a "saver" interface, 
      independent of samplePopulation
      for example updateDistribution(ISolutionPoint[] pop)
    o save all input parameters as output-properties file
    o explicit control of data writing behavior in terms of iterations
      to wait until the next writing?
    o clean up sorting of eigenvalues/vectors which is done repeatedly
    o implement a good boundary handling
    o check Java random number generator and/or implement a private one. 
    o implement a general initialize_with_evaluated_points method, which
      estimates a good mean and covariance matrix either from all points
      or only from the lambda best points (actually mu best points then).
      cave about outlier points. 
    o implement a CMA-ES-specific feed points method for initialization. It should
      accept a population of evaluated points iteratively. It 
      just needs to call updateDistribution with a population as input. 
    o save z instead of recomputing it? 
    o improve error management to reasonable standard 
    o provide output writing for given evaluation numbers and/or given fitness values
    o better use the class java.lang.Object.Date to handle elapsed times?  

Last change: $Date: 2011-06-23 $     
*/

/**
 * implements the Covariance Matrix Adaptation Evolution Strategy (CMA-ES)
 * for non-linear, non-convex, non-smooth, global function minimization. The CMA-Evolution Strategy
 * (CMA-ES) is a reliable stochastic optimization method which should be applied,
 * if derivative based methods, e.g. quasi-Newton BFGS or conjugate
 * gradient, fail due to a rugged search landscape (e.g. noise, local
 * optima, outlier, etc.)  of the objective function. Like a
 * quasi-Newton method the CMA-ES learns and applies a variable metric
 * of the underlying search space. Unlike a quasi-Newton method the
 * CMA-ES does neither estimate nor use gradients, making it considerably more
 * reliable in terms of finding a good, or even close to optimal, solution, finally.
 *
 *
 * In general, on smooth objective functions the CMA-ES is roughly ten times
 * slower than BFGS (counting objective function evaluations, no gradients provided).
 * For up to <math>N=10</math> variables also the derivative-free simplex
 * direct search method (Nelder & Mead) can be faster, but it is
 * far less reliable than CMA-ES.
 *
 *
 * The CMA-ES is particularly well suited for non-separable
 * and/or badly conditioned problems.
 * To observe the advantage of CMA compared to a conventional
 * evolution strategy, it will usually take about 30&#215;<math>N</math> function
 * evaluations. On difficult problems the complete
 * optimization (a single run) is expected to take *roughly*  between
 * <math>30&#215;N</math> and <math>300&#215;N<sup>2</sup></math>
 * function evaluations.
 *
 *
 * The main functionality is provided by the methods `double[][] [.samplePopulation]` and
 * `[.updateDistribution]` or `[.updateDistribution]`.
 * Here is an example code snippet, see file
 * <tt>CMAExample1.java</tt> for a similar example, and
 * <tt>CMAExample2.java</tt> for a more extended example with multi-starts implemented.
 * <pre>
 * // new a CMA-ES and set some initial values
 * CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
 * cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
 * cma.setDimension(10); // overwrite some loaded properties
 * cma.setTypicalX(0.5); // in each dimension, setInitialX can be used as well
 * cma.setInitialStandardDeviation(0.2); // also a mandatory setting
 * cma.opts.stopFitness = 1e-9;          // optional setting
 *
 * // initialize cma and get fitness array to fill in later
 * double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];
 *
 * // initial output to files
 * cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files
 *
 * // iteration loop
 * while(cma.stopConditions.getNumber() == 0) {
 *
 * // core iteration step
 * double[][] pop = cma.samplePopulation(); // get a new population of solutions
 * for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
 * fitness[i] = fitfun.valueOf(pop[i]); //    compute fitness value, where fitfun
 * }                                        //    is the function to be minimized
 * cma.updateDistribution(fitness);         // use fitness array to update search distribution
 *
 * // output to files
 * cma.writeToDefaultFiles();
 * ...in case, print output to console, eg. cma.println(),
 * or process best found solution, getBestSolution()...
 * } // while
 *
 * // evaluate mean value as it is the best estimator for the optimum
 * cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution
 * ...retrieve best solution, termination criterion via stopConditions etc...
 *
 * return cma.getBestX(); // best evaluated search point
 *
</pre> *
 * The output generated by the function `writeToDefaultFiles` can be
 * plotted in Matlab or Scilab using <tt>plotcmaesdat.m</tt> or
 * <tt>plotcmaesdat.sci</tt> respectively, see [.writeToDefaultFiles].
 *
 *
 * <P> The implementation follows very closely <a name=HK2004>[3]</a>. It supports small and large
 * population sizes, the latter by using the rank--update [2],
 * together with weighted recombination for the covariance matrix, an
 * improved parameter setting for large populations [3] and an (initially) diagonal covariance matrix [5].
 * The latter is particularly useful for large dimension, e.g. larger 100.
 * The default population size is small [1]. An
 * independent restart procedure with increasing population size [4]
 * is implemented in class `[cmaes.examples.CMAExample2]`.</P>
 *
 * <P><B>Practical hint</B>: In order to solve an optimization problem in reasonable time it needs to be
 * reasonably encoded. In particular the domain width of variables should be
 * similar for all objective variables (decision variables),
 * such that the initial standard deviation can be chosen the same
 * for each variable. For example, an affine-linear transformation could be applied to
 * each variable, such that its typical domain becomes the interval [0,10].
 * For positive variables a log-encoding or a square-encoding
 * should be considered, to avoid the need to set a hard boundary at zero,
 * see <A href="http://www.lri.fr/~hansen/cmaes_inmatlab.html#practical">here for a few more details</A>.
</P> *
 *
 * <P><B>References</B>
</P> * <UL>
 * <LI>[1] Hansen, N. and A. Ostermeier (2001). Completely
 * Derandomized Self-Adaptation in Evolution Strategies. <I>Evolutionary
 * Computation</I>, 9(2), pp. 159-195.
</LI> *
 * <LI>[2] Hansen, N., S.D. Mller and
 * P. Koumoutsakos (2003). Reducing the Time Complexity of the
 * Derandomized Evolution Strategy with Covariance Matrix Adaptation
 * (CMA-ES). <I>Evolutionary Computation</I>, 11(1), pp. 1-18.
 *
</LI> * <LI>[3] Hansen and Kern (2004). Evaluating the CMA Evolution
 * Strategy on Multimodal Test Functions. In <I> Eighth International
 * Conference on Parallel Problem Solving from Nature PPSN VIII,
 * Proceedings</I>, pp. 282-291, Berlin: Springer.
</LI> *
 * <LI>[4]
 * Auger, A, and Hansen, N. (2005). A Restart CMA Evolution Strategy
 * With Increasing Population Size. In <I>Proceedings of the IEEE
 * Congress on Evolutionary Computation, CEC 2005</I>, pp.1769-1776.
</LI> *
 * <LI>[5]
 * Ros, R. and N. Hansen (2008). A Simple
 * Modification in CMA-ES Achieving Linear Time and Space Complexity.
 * In Rudolph et al. (eds.) <I>Parallel Problem Solving from Nature, PPSN X,
 * Proceedings</I>, pp. 296-305, Springer.
</LI> *
</UL> *
 *
 *
 * @author Nikolaus Hansen, 1996, 2003, 2005, 2007
 * @see .samplePopulation
 * @see .updateDistribution
 */
public class CMAEvolutionStrategy : Serializable {

    /**
     * reads properties from fileName and sets strategy parameters and options
     * accordingly
     *
     * @param fileName of properties file
     */

    // Symmetric Householder reduction to tridiagonal form, taken from JAMA package.
    // Symmetric tridiagonal QL algorithm, taken from JAMA package.

    /**
     * update of the search distribution from a population and its
     * function values, an alternative interface for
     * [.updateDistribution]. functionValues is used to establish an
     * ordering of the elements in population. The first nInjected elements do not need to originate
     * from #samplePopulation() or can have been modified (TODO: to be tested).
     *
     * @param population     double[lambda][N], lambda solutions
     * @param functionValues double[lambda], respective objective values of population
     * @param nInjected      int, first nInjected solutions of population were not sampled by
     * samplePopulation() or modified afterwards
     * @see .samplePopulation
     * @see .updateDistribution
     */

    //    private IntDouble[] computePenalties() {
    //    	int i, j, iNk;
    //    	/* penalize repairment, eg. for boundaries */
    //    	// TODO: figure out whether the change of penalty is too large or fast which can disturb selection
    //    	//       this depence in particular on the length of fit.medianDeltaFit
    //    	if (true || countiter < fit.deltaFitHist.length || countiter % 1*(N+2) == 0) {
    //    		// minimum of differences with distance lambda/2, better the 25%tile?
    //    		// assumes sorted array!! 
    //    		int ii = (sp.getLambda()) / 2;
    //    		double medianDeltaFit = Math.abs(fit.funValues[ii].d - fit.funValues[0].d);
    //    		for (i = 1; i + ii < sp.getLambda(); ++i) 
    //    			// minimum because of outliers 
    //    			medianDeltaFit = Math.min(medianDeltaFit, Math.abs(fit.funValues[ii+i].d - fit.funValues[i].d));
    //    		medianDeltaFit /= sigma * sigma; // should be somehow constant, because dfit depends on sigma (verified on sphere)
    //    		if (medianDeltaFit > 0) {
    ////  			System.out.println("set" + medianDeltaFit + " " + math.median(fit.medianDeltaFit));
    //    			if (fit.idxDeltaFitHist == -1) // first time: write all fields
    //    				for (i = 0; i < fit.deltaFitHist.length; ++i)
    //    					fit.deltaFitHist[i] = medianDeltaFit;
    //    			if (++fit.idxDeltaFitHist == fit.deltaFitHist.length)
    //    				fit.idxDeltaFitHist = 0;
    //    			// save last five values in fit.medianDeltaFit
    //    			fit.deltaFitHist[fit.idxDeltaFitHist] = medianDeltaFit;
    //    		}                
    //    	}
    //    	/* calculate fitness by adding function value and repair penalty */
    //    	double penfactor = 1. * 5. * math.median(fit.deltaFitHist);
    //    	for (iNk = 0; iNk < sp.getLambda(); ++iNk) {
    //    		double sqrnorm = 0;
    //    		double prod = Math.pow(math.prod(diagD), 1.0/(double)N);
    //    		/* calculate C^-1-norm of Delta x: norm(D^(-1) * B^(-1) * (Delta x))^2 */
    //    		for (i = 0; i < N; ++i) {
    //    			double sum = 0.0;
    //    			for (j = 0, sum = 0.; j < N; ++j)
    //    				sum += B[j][i] * ((arxrepaired[fit.funValues[iNk].i][j] - arx[fit.funValues[iNk].i][j]));
    //    			sqrnorm += math.square(sum / (Math.pow(diagD[i], 0.9) * Math.pow(prod, 0.10))); // regularization to I
    //    		}
    //    		// sqrnorm/N equals approximately 1/sigma^2
    //    		fit.fitness[iNk].d = fit.funValues[iNk].d + penfactor * sqrnorm / (N+2); // / (sigma * sigma);
    //    		fit.fitness[iNk].i = fit.funValues[iNk].i;
    //    		// System.out.println(math.median(fit.medianDeltaFit) + " " + sqrnorm / (N+2)); // / (sigma * sigma));
    //    	}
    ////  	if (countiter % 10 == 1)
    ////  	System.out.println(math.median(fit.medianDeltaFit) + " " + sqrnorm);
    //    	return fit.fitness;
    //
    //    }

    //    /** Set lower and upper boundary in all variables
    //     * 
    //     * @param xlow
    //     * @param xup
    //     */
    //    public void setBoundaries(double xlow, double xup) {
    //        int len = 1;
    //        if (N > 0)
    //            len = N;
    //        LBound = new double[len];
    //        UBound = new double[len];
    //        for (int i= 0; i < len; ++i) {
    //            LBound[i] = xlow;
    //            UBound[i] = xup;
    //        }
    //    }
    //    /** sets lower and upper boundaries in all variables. 
    //     * 
    //     * @param xlow lower boundary double[], can be 1-D or of length of the number of variables (dimension). 
    //     * @param xup see xlow
    //     */
    //    public void setBoundaries(double[] xlow, double[] xup) {
    //        if( xlow == null || xup ==  null)
    //            error("boundaries cannot be null");
    //        if (xlow.length == 1 && xup.length == 1) {
    //            setBoundaries(xlow[0], xup[0]);
    //            return;
    //        }
    //        if ((N > 0 && (N != xlow.length || N != xup.length)) 
    //            || (xlow.length != xup.length))
    //            error("dimensions of boundaries do not match");
    //        this.LBound = xlow;
    //        this.UBound = xup;
    //        N = xlow.length; // changes N only if N was 0
    //    }

    /*
      return new String(
      new Long(countiter)
      + " " +						   new Integer(idxRecentOffspring)
      + " " +						   new Long(counteval)
      + " " +						   new Double(recentFunctionValue)
      //				+ " " +  						   new Double(FunctionValue() - recentFunctionValue)
       //				+ " " +  						   new Double(recentMaxFunctionValue - recentFunctionValue)
        + " " +  						   new Double(axisratio)
        + " " +  						   new Integer(math.minidx(math.diag(C)))
        + " " +  						   new Double(sigma *
        Math.sqrt(math.min(math.diag(C))))
        + " " +						   new Integer(math.maxidx(math.diag(C)))
        + " " +						   new Double(sigma *
        Math.sqrt(math.max(math.diag(C))))
        );
        */
    /* formatting template
      String.format(Locale.US, "%1$6.2e %2$+.0e",
      new Object[]{
      new Double(),
      new Double()
      })

      */
    //		   out.print(math.min(diagD));
    //      out.print(" ");
    //      new DecimalFormat("0.00E0").format((3.34)) + " " +
    //      (cma.fit.fitness[(cma.parameters.getLambda()/2)].d
    //      - cma.fit.fitness[0].d) + "," +
    //      cma.fit.fitness[cma.parameters.getLambda()-1].d + ") | " +


    /**
     * calls System.out.println(s) and writes s to the file outcmaesdisp.dat
     * by default, if writeDisplayToFile option is > 0
     *
     * @see .getPrintLine
     */

    /**
     * writes data to files <tt>fileNamePrefix</tt>fit.dat, ...xmean.dat
     * ...xbest.dat, ...std.dat, ...axlen.dat.
     *
     * @param fileNamePrefix prefix String for filenames created to write data
     * @see .writeToDefaultFiles
     */

    /**
     *
     */
    private val versionNumber = "0.99.40"
    val math = MyMath()
    private val SINGLE_MODE = 1 // not in use anymore, keep for later developements?
    val PARALLEL_MODE = 2

    /**
     * options that can be changed (fields can be assigned) at any time to control
     * the running behavior
     */
    var options = CMAOptions()

    /**
     * permits access to whether and which termination conditions were satisfied
     */
    internal var stopConditions = StopCondition()

    /**
     * recent population, no idea whether this is useful to be public
     */
    private var population: Array<DoubleArray?> = arrayOf(null)
    private var fit = FitnessCollector()
    var N = 0
    internal var seed = System.currentTimeMillis()

    /**
     * get used random number generator instance
     */
    private var rand = Random(seed) // Note: it also Serializable

    /**
     * ratio between length of longest and shortest axis
     * of the distribution ellipsoid, which is the square root
     * of the largest divided by the smallest eigenvalue of the covariance matrix
     */
    private var axisRatio = 0.0

    /**
     * number of objective function evaluations counted so far
     */
    var countEval: Long = 0

    /**
     * number of iterations conducted so far
     */
    var countIter: Long = 0

    /* evaluation count when the best solution was found**/
    private var bestEvaluationNumber: Long = 0
    private var bestever_x: DoubleArray = doubleArrayOf()
    var bestever_fit = Double.NaN

    // CMASolution bestever; // used as output variable
    var sigma = 0.0
    private var typicalX: DoubleArray? = null // eventually used to set initialX
    internal var initialX: DoubleArray = doubleArrayOf()// set in the end of init()
    private var LBound: DoubleArray? = null
    private var UBound: DoubleArray? = null// bounds
    var xmean: DoubleArray? = null
    private var xmean_fit = Double.NaN
    var pc: DoubleArray = doubleArrayOf()
    private var ps: DoubleArray = doubleArrayOf()
    var C: Array<DoubleArray> = arrayOf()
    var maxsqrtdiagC = 0.0
    private var minsqrtdiagC = 0.0
    var B: Array<DoubleArray> = arrayOf()
    var diagD: DoubleArray = doubleArrayOf()
    var flgdiag // 0 == full covariance matrix
            = false

    /* init information */
    private var startsigma: DoubleArray? = null
    var maxstartsigma = 0.0
    var minstartsigma = 0.0
    private var iniphase = false

    /**
     * state (postconditions):
     * -1 not yet initialized
     * 0 initialized init()
     * 0.5 reSizePopulation
     * 1 samplePopulation, sampleSingle, reSampleSingle
     * 2.5 updateSingle
     * 3 updateDistribution
     */
    var state = -1.0
    private var citerlastwritten: Long = 0
    private var countwritten: Long = 0
    private var lockDimension = 0
    private var mode = 0
    private var countCupdatesSinceEigenupdate: Long = 0
    private var recentFunctionValue = 0.0

    /**
     * objective function value of the,
     * worst solution of the recent iteration.
     *
     * @return Returns the recentMaxFunctionValue.
     */
    private var worstRecentFunctionValue = 0.0

    /**
     * objective function value of the,
     * best solution in the
     * recent iteration (population)
     *
     * @return Returns the recentFunctionValue.
     * @see .getBestEvaluationNumber
     * @see .getBestFunctionValue
     */
    private var bestRecentFunctionValue = 0.0
    private var idxRecentOffspring = 0
    private var arx: Array<DoubleArray?> = arrayOf()
    private var xold: DoubleArray = doubleArrayOf()
    private var BDz: DoubleArray = doubleArrayOf()
    private var artmp: DoubleArray = doubleArrayOf()
    private var propertiesFileName: String = "CMAEvolutionStrategy.properties"

    /**
     * get properties previously read from a property file.
     *
     * @return java.util.Properties key-value hash table
     * @see .readProperties
     */
    var properties = Properties()
    private var timings: Timing = Timing()
    private var sp = CMAParameters() // alias for inside use

    /**
     * strategy parameters that can be set initially
     */
    internal var parameters = sp // for outside use also
    private var fileswritten = arrayOf<String?>("") // also (re-)initialized in init()

    /**
     * get default parameters in new CMAParameters instance, dimension must
     * have been set before calling getDefaults
     *
     * @see CMAParameters.getDefaults
     */
    val parameterDefaults: CMAParameters
        get() = sp.getDefaults(N)


    /**
     * get best evaluated search point found so far.
     * Remark that the distribution mean was not evaluated
     * but is expected to have an even better function value.
     *
     * @return best search point found so far as double[]
     * @see .getMeanX
     */
    val bestX: DoubleArray?
        get() = if (state < 0) null else bestever_x.clone()

    /**
     * objective function value of best solution found so far.
     *
     * @return objective function value of best solution found so far
     * @see .getBestSolution
     */
    private val bestFunctionValue: Double
        get() = if (state < 0) Double.NaN else bestever_fit

    /**
     * best search point of the recent iteration.
     *
     * @return Returns the recentFunctionValue.
     * @see .getBestRecentFunctionValue
     */
    val bestRecentX: DoubleArray
        get() = genoPhenoTransformation(arx[fit.raw[0]!!.i], null)

    /**
     * Get mean of the current search distribution. The mean should
     * be regarded as the best estimator for the global
     * optimimum at the given iteration. In particular for noisy
     * problems the distribution mean is the solution of choice
     * preferable to the best or recent best. The return value is
     * *not* a copy. Therefore it should not be change it, without
     * deep knowledge of the code (the effect of a mean change depends on
     * the chosen transscription/implementation of the algorithm).
     *
     * @return mean value of the current search distribution
     * @see .getBestX
     * @see .getBestRecentX
     */
    val meanX: DoubleArray
        get() = xmean!!.clone()

    /**
     * search space dimensions must be set before the optimization is started.
     */
    internal var dimension: Int
        get() = N
        set(n) {
            if ((lockDimension > 0 || state >= 0) && N != n) error("dimension cannot be changed anymore or contradicts to initialX")
            N = n
        }


    //for (int i = 0; i < sp.getLambda(); ++i) {
    //    s += fit.funValues[i].d + " ";
    //}
    private val dataRowFitness: String
        get() {
            var s = "$countIter $countEval $sigma $axisRatio $bestever_fit "
            if (mode == SINGLE_MODE) s += "$recentFunctionValue " else {
                s += fit.raw[0]!!.value.toString() + " "
                s += fit.raw[sp.getLambda() / 2]!!.value.toString() + " "
                s += fit.raw[sp.getLambda() - 1]!!.value.toString() + " "
                s += (math.min(diagD)
                    .toString() + " " + (math.maxidx(math.diag(C)) + 1) + " " + sigma * maxsqrtdiagC + " " + (math.minidx(
                    math.diag(C)
                ) + 1) + " " + sigma * minsqrtdiagC)
                //for (int i = 0; i < sp.getLambda(); ++i) {
                //    s += fit.funValues[i].d + " ";
                //}
            }
            return s
        }
    private val dataRowXRecentBest: String
        get() {
            var idx = 0
            if (mode == SINGLE_MODE) idx = idxRecentOffspring
            var s: String =
                (countIter.toString() + " " + countEval + " " + sigma + " 0 " + (if (state == 1.0) Double.NaN else fit.raw[idx]!!.value) + " ")
            for (i in 0 until N) {
                s += arx[fit.raw[idx]!!.i]!![i].toString() + " "
            }
            return s
        }
    private val dataRowXMean: String
        get() {
            var s = "$countIter $countEval $sigma 0 0 "
            for (i in 0 until N) {
                s += xmean!![i].toString() + " "
            }
            return s
        }

    /**
     * 6-th to last column are sorted axis lengths axlen
     */
    private val dataRowAxlen: String
        get() {
            var s: String =
                (countIter.toString() + " " + countEval + " " + sigma + " " + axisRatio + " " + maxsqrtdiagC / minsqrtdiagC + " ")
            val tmp = diagD.clone()
            Arrays.sort(tmp)
            for (i in 0 until N) {
                s += tmp[i].toString() + " "
            }
            return s
        }
    private val dataRowStddev: String
        get() {
            var s: String =
                (countIter.toString() + " " + countEval + " " + sigma + " " + (1 + math.maxidx(math.diag(C))) + " " + (1 + math.minidx(
                    math.diag(C)
                )) + " ")
            for (i in 0 until N) {
                s += (sigma * sqrt(C[i][i])).toString() + " "
            }
            return s
        }// ouput correlation in the lower half

    /**
     * correlations and covariances of the search distribution. The
     * first, '%#'-commented row contains itertation number,
     * evaluation number, and sigma. In the remaining rows the upper
     * triangular part contains variances and covariances
     * sigma*sigma*c_ij. The lower part contains correlations c_ij /
     * sqrt(c_ii * c_jj).
     */
    val dataC: String
        get() {
            var j: Int
            var s: String = """%# ${this.countIter} ${this.countEval} $sigma
                |
            """.trimMargin()
            var i = 0
            while (i < N) {
                j = 0
                while (j < i) {
                    // ouput correlation in the lower half
                    s += (C[i][j] / sqrt(C[i][i] * C[j][j])).toString() + " "
                    ++j
                }
                j = i
                while (j < N) {
                    s += (sigma * sigma * C[i][j]).toString() + " "
                    ++j
                }
                s += "\n"
                ++i
            }
            return s
        }

    /**
     * postpones most initialization. For initialization use setInitial...
     * methods or set up a properties file, see file "CMAEvolutionStrategy.properties".
     */
    constructor() {
        state = -1.0
    }

    /**
     * retrieves options and strategy parameters from properties input, see file <tt>CMAEvolutionStrategy.properties</tt>
     * for valid properties
     */
    constructor(properties: Properties) {
        setFromProperties(properties)
        state = -1.0
    }

    /**
     * reads properties (options, strategy parameter settings) from
     * file `propertiesFileName`
     */
    constructor(propertiesFileName: String) {
        this.propertiesFileName = propertiesFileName
        state = -1.0
    }

    /**
     * @param dimension search space dimension, dimension of the
     * objective functions preimage, number of variables
     */
    constructor(dimension: Int) {
        this.dimension = dimension
        state = -1.0
    }

    private fun testAndCorrectNumerics() { // not much left here

        /* Flat Fitness, Test if function values are identical */
        if (countIter > 1 || countIter == 1L && state >= 3) if (fit.fitness[0]!!.value == fit.fitness[min(
                sp.getLambda() - 1, sp.getLambda() / 2 + 1
            ) - 1]!!.value
        ) {
            //warning("flat fitness landscape, consider reformulation of fitness, step-size increased")
            sigma *= exp(0.2 + sp.getCs() / sp.getDamps())
        }

        /* Align (renormalize) scale C (and consequently sigma) */
        /* e.g. for infinite stationary state simulations (noise
         * handling needs to be introduced for that) */
        var fac = 1.0
        if (math.max(diagD) < 1e-6) fac = 1.0 / math.max(diagD) else if (math.min(diagD) > 1e4) fac =
            1.0 / math.min(diagD)
        if (fac != 1.0) {
            sigma /= fac
            for (i in 0 until N) {
                pc[i] *= fac
                diagD[i] *= fac
                for (j in 0..i) C[i][j] *= fac * fac
            }
        }
    } // Test...

    /**
     * initialization providing all mandatory input arguments at once. The following two
     * is equivalent
     * <PRE>
     * cma.init(N, X, SD);
    </PRE> *  and
     * <PRE>
     * cma.setInitalX(X);  //
     * cma.setInitialStandardDeviations(SD);
     * cma.init(N);
    </PRE> *
     *
     *
     * The call to `init` is a point of no return for parameter
     * settings, and demands all mandatory input be set. `init` then forces the
     * setting up of everything and calls
     * `parameters.supplementRemainders()`. If `init` was not called before, it is called once in
     * `samplePopulation()`. The return value is only provided for sake of convenience.
     *
     * @param dimension
     * @param initialX                  double[] can be of size one, where all variables are set to the
     * same value, or of size dimension
     * @param initialStandardDeviations can be of size one, where all standard
     * deviations are set to the same value, or of size dimension
     * @return `double[] fitness` of length population size lambda to assign and pass
     * objective function values to `[.updateDistribution]`
     * @see .init
     * @see .init
     * @see .setInitialX
     * @see .setTypicalX
     * @see .setInitialStandardDeviations
     * @see .samplePopulation
     * @see CMAParameters.supplementRemainders
     */
    fun init(dimension: Int, initialX: DoubleArray?, initialStandardDeviations: DoubleArray?): DoubleArray {
        setInitialX(initialX)
        setInitialStandardDeviations(initialStandardDeviations)
        return init(dimension)
    }

    private fun getArrayOf(x: Double, dim: Int): DoubleArray {
        val res = DoubleArray(dim)
        for (i in 0 until dim) res[i] = x
        return res
    }

    /**
     * @param x   null or x.length==1 or x.length==dim, only for the second case x is expanded
     * @param dim
     * @return `null` or `double[] x` with `x.length==dim`
     */
    private fun expandToDimension(x: DoubleArray?, dim: Int): DoubleArray? {
        if (x == null) return null
        if (x.size == dim) return x
        if (x.size != 1) error("x must have length one or length dimension")
        return getArrayOf(x[0], dim)
    }

    /**
     * @param dimension search space dimension
     * @see .init
     */
    fun init(dimension: Int): DoubleArray {
        this.dimension = dimension
        return init()
    }

    /**
     * @see .init
     */
    fun init(): DoubleArray {
        var i: Int
        if (N <= 0) error("dimension needs to be determined, use eg. setDimension() or setInitialX()")
        if (state >= 0) error("init() cannot be called twice")
        if (state == 0.0) // less save variant
            return DoubleArray(sp.getLambda())
        if (state > 0) error("init() cannot be called after the first population was sampled")
        sp = parameters /* just in case the user assigned parameters */
        if (sp.supplemented == 0) // a bit a hack
            sp.supplementRemainders(N, options)
        sp.locked = 1 // lambda cannot be changed anymore
        diagD = DoubleArray(N)
        i = 0
        while (i < N) {
            diagD[i] = 1.0
            ++i
        }

        /* expand Boundaries */LBound = expandToDimension(LBound, N)
        if (LBound == null) {
            LBound = DoubleArray(N)
            i = 0
            while (i < N) {
                LBound!![i] = Double.NEGATIVE_INFINITY
                ++i
            }
        }
        UBound = expandToDimension(UBound, N)
        if (UBound == null) {
            UBound = DoubleArray(N)
            i = 0
            while (i < N) {
                UBound!![i] = Double.POSITIVE_INFINITY
                ++i
            }
        }

        /* Initialization of sigmas */if (startsigma != null) { //
            when (startsigma!!.size) {
                1 -> {
                    sigma = startsigma!![0]
                }
                N -> {
                    sigma = math.max(startsigma!!)
                    if (sigma <= 0) error("initial standard deviation sigma must be positive")
                    i = 0
                    while (i < N) {
                        diagD[i] = startsigma!![i] / sigma
                        ++i
                    }
                }
                else -> assert(false)
            }
        } else {
            // we might use boundaries here to find startsigma, but I prefer to have stddevs mandatory
            error("no initial standard deviation specified, use setInitialStandardDeviations()")
            sigma = 0.5
        }
        if (sigma <= 0 || math.min(diagD) <= 0) {
            error(
                "initial standard deviations not specified or non-positive, " + "use setInitialStandarddeviations()"
            )
            sigma = 1.0
        }
        /* save initial standard deviation */if (startsigma == null || startsigma!!.size == 1) {
            startsigma = DoubleArray(N)
            i = 0
            while (i < N) {
                startsigma!![i] = sigma * diagD[i]
                ++i
            }
        }
        maxstartsigma = math.max(startsigma!!)
        minstartsigma = math.min(startsigma!!)
        axisRatio = maxstartsigma / minstartsigma // axis parallel distribution

        /* expand typicalX, might still be null afterwards */typicalX = expandToDimension(typicalX, N)

        /* Initialization of xmean */xmean = expandToDimension(xmean, N)
        if (xmean == null) {
            /* set via typicalX */
            if (typicalX != null) {
                xmean = typicalX!!.clone()
                i = 0
                while (i < N) {
                    xmean!![i] += sigma * diagD[i] * rand.nextGaussian()
                    ++i
                }
                /* set via boundaries, is depriciated */
            } else if (math.max(UBound!!) < Double.MAX_VALUE && math.min(LBound!!) > -Double.MAX_VALUE) {
                error("no initial search point (solution) X or typical X specified")
                xmean = DoubleArray(N)
                i = 0
                while (i < N) {
                    /* TODO: reconsider this algorithm to set X0 */
                    var offset = sigma * diagD[i]
                    var range = UBound!![i] - LBound!![i] - 2 * sigma * diagD[i]
                    if (offset > 0.4 * (UBound!![i] - LBound!![i])) {
                        offset = 0.4 * (UBound!![i] - LBound!![i])
                        range = 0.2 * (UBound!![i] - LBound!![i])
                    }
                    xmean!![i] = LBound!![i] + offset + rand.nextDouble() * range
                    ++i
                }
            } else {
                error("no initial search point (solution) X or typical X specified")
                xmean = DoubleArray(N)
                i = 0
                while (i < N) {
                    xmean!![i] = rand.nextDouble()
                    ++i
                }
            }
        }
        assert(xmean != null)
        assert(sigma > 0)

        /* interpret missing option value */if (options.diagonalCovarianceMatrix < 0) // necessary for hello world message
            options.diagonalCovarianceMatrix = (1 * 150 * N / sp.lambda).toLong() // cave: duplication below

        /* non-settable parameters */pc = DoubleArray(N)
        ps = DoubleArray(N)
        B = Array(N) { DoubleArray(N) }
        C = Array(N) { DoubleArray(N) } // essentially only i <= j part is used
        xold = DoubleArray(N)
        BDz = DoubleArray(N)
        bestever_x = xmean!!.clone()
        // bestever = new CMASolution(xmean);
        artmp = DoubleArray(N)
        fit.deltaFitHist = DoubleArray(5)
        fit.idxDeltaFitHist = -1
        i = 0
        while (i < fit.deltaFitHist.size) {
            fit.deltaFitHist[i] = 1.0
            ++i
        }

        // code to be duplicated in reSizeLambda
        fit.fitness = arrayOfNulls(sp.getLambda()) // including penalties, used yet
        fit.raw = arrayOfNulls(sp.getLambda()) // raw function values
        fit.history = DoubleArray(10 + 30 * N / sp.getLambda())
        arx = Array(sp.getLambda()) { DoubleArray(N) }
        population = Array(sp.getLambda()) { DoubleArray(N) }
        i = 0
        while (i < sp.getLambda()) {
            fit.fitness[i] = IntDouble()
            fit.raw[i] = IntDouble()
            ++i
        }

        // initialization
        i = 0
        while (i < N) {
            pc[i] = 0.0
            ps[i] = 0.0
            for (j in 0 until N) {
                B[i][j] = 0.0
            }
            for (j in 0 until i) {
                C[i][j] = 0.0
            }
            B[i][i] = 1.0
            C[i][i] = diagD[i] * diagD[i]
            ++i
        }
        maxsqrtdiagC = sqrt(math.max(math.diag(C)))
        minsqrtdiagC = sqrt(math.min(math.diag(C)))
        countCupdatesSinceEigenupdate = 0
        iniphase = false // obsolete

        /* Some consistency check */i = 0
        while (i < N) {
            if (LBound!![i] > UBound!![i]) error("lower bound is greater than upper bound")
            if (typicalX != null) {
                if (LBound!![i] > typicalX!![i]) error("lower bound '" + LBound!![i] + "'is greater than typicalX" + typicalX!![i])
                if (UBound!![i] < typicalX!![i]) error("upper bound '" + UBound!![i] + "' is smaller than typicalX " + typicalX!![i])
            }
            ++i
        }
        val s = stopConditions.getMessages()
        if (s[0] != "") warning("termination condition satisfied at initialization: ${s[0]}")
        initialX = xmean!!.clone() // keep finally chosen initialX
        timings.start = System.currentTimeMillis()
        timings.starteigen = System.currentTimeMillis()
        state = 0.0
        if (options.verbosity > -1) printlnHelloWorld()
        return DoubleArray(sp.getLambda())
    } // init()

    /**
     * get default parameters in new CMAParameters instance
     *
     * @see CMAParameters.getDefaults
     */
    fun getParameterDefaults(N: Int): CMAParameters {
        return sp.getDefaults(N)
    }

    /**
     * reads properties from Properties class
     * input and sets options and parameters accordingly
     *
     * @param properties java.util.Properties key-value hash table
     * @see .readProperties
     */
    private fun setFromProperties(properties: Properties) {
        var s: String
        options.setOptions(properties)
        if (state >= 0) // only options can be changed afterwards
            return  // defaults are already supplemented

        if (properties.getProperty("typicalX").also { s = it } != null) {
            setTypicalX(options.parseDouble(options.getAllToken(s)))
        }
        if (properties.getProperty("initialX").also { s = it } != null) {
            setInitialX(options.parseDouble(options.getAllToken(s)))
        }
        if (properties.getProperty("initialStandardDeviations").also { s = it } != null) {
            setInitialStandardDeviations(options.parseDouble(options.getAllToken(s)))
        }
        if (properties.getProperty("dimension").also { s = it } != null) { // parseInt does not ignore trailing spaces
            dimension = options.getFirstToken(s).toInt()
        }
        if (properties.getProperty("randomSeed").also { s = it } != null) {
            setSeed(options.getFirstToken(s).toLong())
        }
        if (properties.getProperty("populationSize").also { s = it } != null) {
            sp.populationSize = options.getFirstToken(s).toInt()
        }
        if (properties.getProperty("cCov").also { s = it } != null) {
            sp.ccov = options.getFirstToken(s).toDouble()
        }
    }

    //    private void infoVerbose(String s) {
    //        println(" CMA-ES info: " + s);
    //    }
    private fun warning(s: String) {
        println(" CMA-ES warning: $s")
    }

    private fun error(s: String) { // somehow a relict from the C history of this code
        println(" CMA-ES error: $s")
        throw CMAException(" CMA-ES error: $s")
        //      System.exit(-1);
    }

    /* flgforce == 1 force independent of time measurments,
         * flgforce == 2 force independent of uptodate-status
         */
    private fun eigendecomposition(flgforce: Int) {
        /* Update B and D, calculate eigendecomposition */
        var i: Int
        var j: Int
        if (countCupdatesSinceEigenupdate == 0L && flgforce < 2) return

        //           20% is usually better in terms of running *time* (only on fast to evaluate functions)
        if (!flgdiag && flgforce <= 0 && (timings.eigendecomposition > 1000 + options.maxTimeFractionForEigendecomposition * (System.currentTimeMillis() - timings.starteigen) || countCupdatesSinceEigenupdate < 1.0 / sp.ccov / N / 5.0)) return
        if (flgdiag) {
            i = 0
            while (i < N) {
                diagD[i] = sqrt(C[i][i])
                ++i
            }
            countCupdatesSinceEigenupdate = 0
            timings.starteigen = System.currentTimeMillis() // reset starting time
            timings.eigendecomposition = 0 // not really necessary
        } else {
            // set B <- C
            i = 0
            while (i < N) {
                j = 0
                while (j <= i) {
                    B[j][i] = C[i][j]
                    B[i][j] = B[j][i]
                    ++j
                }
                ++i
            }

            // eigendecomposition
            val offdiag = DoubleArray(N)
            val firsttime = System.currentTimeMillis()
            tred2(N, B, diagD, offdiag)
            tql2(N, diagD, offdiag, B)
            timings.eigendecomposition += System.currentTimeMillis() - firsttime
            if (options.checkEigenSystem > 0) checkEigenSystem(N, C, diagD, B) // for debugging

            // assign diagD to eigenvalue square roots
            i = 0
            while (i < N) {
                if (diagD[i] < 0) // numerical problem?
                    error("an eigenvalue has become negative")
                diagD[i] = sqrt(diagD[i])
                ++i
            }
            countCupdatesSinceEigenupdate = 0
        } // end Update B and D
        axisRatio = if (math.min(diagD) == 0.0) // error management is done elsewhere
            Double.POSITIVE_INFINITY else math.max(diagD) / math.min(diagD)
    } // eigendecomposition

    /* ========================================================= */
    private fun checkEigenSystem(N: Int, C: Array<DoubleArray>, diag: DoubleArray, Q: Array<DoubleArray>): Int /*
       exhaustive test of the output of the eigendecomposition
       needs O(n^3) operations

       produces error
       returns number of detected inaccuracies
    */ {
        /* compute Q diag Q^T and Q Q^T to check */
        var j: Int
        var k: Int
        var res = 0
        var cc: Double
        var dd: Double
        var s: String
        var i = 0
        while (i < N) {
            j = 0
            while (j < N) {
                cc = 0.0
                dd = 0.0
                k = 0
                while (k < N) {
                    cc += diag[k] * Q[i][k] * Q[j][k]
                    dd += Q[i][k] * Q[j][k]
                    ++k
                }
                /* check here, is the normalization the right one? */if (abs(cc - C[if (i > j) i else j][if (i > j) j else i]) / sqrt(
                        C[i][i] * C[j][j]
                    ) > 1e-10 && abs(cc - C[if (i > j) i else j][if (i > j) j else i]) > 1e-9
                ) { /* quite large */
                    s =
                        " " + i + " " + j + " " + cc + " " + C[if (i > j) i else j][if (i > j) j else i] + " " + (cc - C[if (i > j) i else j][if (i > j) j else i])
                    warning("cmaes_t:Eigen(): imprecise result detected $s")
                    ++res
                }
                if (abs(dd - if (i == j) 1 else 0) > 1e-10) {
                    s = "$i $j $dd"
                    warning("cmaes_t:Eigen(): imprecise result detected (Q not orthog.) $s")
                    ++res
                }
                ++j
            }
            ++i
        }
        return res
    }

    private fun tred2(n: Int, V: Array<DoubleArray>, d: DoubleArray, e: DoubleArray) {

        //  This is derived from the Algol procedures tred2 by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.
        for (j in 0 until n) {
            d[j] = V[n - 1][j]
        }

        // Householder reduction to tridiagonal form.
        for (i in n - 1 downTo 1) {

            // Scale to avoid under/overflow.
            var scale = 0.0
            var h = 0.0
            for (k in 0 until i) {
                scale += abs(d[k])
            }
            if (scale == 0.0) {
                e[i] = d[i - 1]
                for (j in 0 until i) {
                    d[j] = V[i - 1][j]
                    V[i][j] = 0.0
                    V[j][i] = 0.0
                }
            } else {

                // Generate Householder vector.
                for (k in 0 until i) {
                    d[k] /= scale
                    h += d[k] * d[k]
                }
                var f = d[i - 1]
                var g = sqrt(h)
                if (f > 0) {
                    g = -g
                }
                e[i] = scale * g
                h -= f * g
                d[i - 1] = f - g
                for (j in 0 until i) {
                    e[j] = 0.0
                }

                // Apply similarity transformation to remaining columns.
                for (j in 0 until i) {
                    f = d[j]
                    V[j][i] = f
                    g = e[j] + V[j][j] * f
                    for (k in j + 1 until i) {
                        g += V[k][j] * d[k]
                        e[k] += V[k][j] * f
                    }
                    e[j] = g
                }
                f = 0.0
                for (j in 0 until i) {
                    e[j] /= h
                    f += e[j] * d[j]
                }
                val hh = f / (h + h)
                for (j in 0 until i) {
                    e[j] -= hh * d[j]
                }
                for (j in 0 until i) {
                    f = d[j]
                    g = e[j]
                    for (k in j until i) {
                        V[k][j] -= f * e[k] + g * d[k]
                    }
                    d[j] = V[i - 1][j]
                    V[i][j] = 0.0
                }
            }
            d[i] = h
        }

        // Accumulate transformations.
        for (i in 0 until n - 1) {
            V[n - 1][i] = V[i][i]
            V[i][i] = 1.0
            val h = d[i + 1]
            if (h != 0.0) {
                for (k in 0..i) {
                    d[k] = V[k][i + 1] / h
                }
                for (j in 0..i) {
                    var g = 0.0
                    for (k in 0..i) {
                        g += V[k][i + 1] * V[k][j]
                    }
                    for (k in 0..i) {
                        V[k][j] -= g * d[k]
                    }
                }
            }
            for (k in 0..i) {
                V[k][i + 1] = 0.0
            }
        }
        for (j in 0 until n) {
            d[j] = V[n - 1][j]
            V[n - 1][j] = 0.0
        }
        V[n - 1][n - 1] = 1.0
        e[0] = 0.0
    }

    private fun tql2(n: Int, d: DoubleArray, e: DoubleArray, V: Array<DoubleArray>) {

        //  This is derived from the Algol procedures tql2, by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.
        for (i in 1 until n) {
            e[i - 1] = e[i]
        }
        e[n - 1] = 0.0
        var f = 0.0
        var tst1 = 0.0
        val eps = 2.0.pow(-52.0)
        for (l in 0 until n) {

            // Find small subdiagonal element
            tst1 = tst1.coerceAtLeast(abs(d[l]) + abs(e[l]))
            var m = l
            while (m < n) {
                if (abs(e[m]) <= eps * tst1) {
                    break
                }
                m++
            }

            // If m == l, d[l] is an eigenvalue,
            // otherwise, iterate.
            if (m > l) {
                var iter = 0
                do {
                    iter += 1 // (Could check iteration count here.)

                    // Compute implicit shift
                    var g = d[l]
                    var p = (d[l + 1] - g) / (2.0 * e[l])
                    var r = math.hypot(p, 1.0)
                    if (p < 0) {
                        r = -r
                    }
                    d[l] = e[l] / (p + r)
                    d[l + 1] = e[l] * (p + r)
                    val dl1 = d[l + 1]
                    var h = g - d[l]
                    for (i in l + 2 until n) {
                        d[i] -= h
                    }
                    f += h

                    // Implicit QL transformation.
                    p = d[m]
                    var c = 1.0
                    var c2 = c
                    var c3 = c
                    val el1 = e[l + 1]
                    var s = 0.0
                    var s2 = 0.0
                    for (i in m - 1 downTo l) {
                        c3 = c2
                        c2 = c
                        s2 = s
                        g = c * e[i]
                        h = c * p
                        r = math.hypot(p, e[i])
                        e[i + 1] = s * r
                        s = e[i] / r
                        c = p / r
                        p = c * d[i] - s * g
                        d[i + 1] = h + s * (c * g + s * d[i])

                        // Accumulate transformation.
                        for (k in 0 until n) {
                            h = V[k][i + 1]
                            V[k][i + 1] = s * V[k][i] + c * h
                            V[k][i] = c * V[k][i] - s * h
                        }
                    }
                    p = -s * s2 * c3 * el1 * e[l] / dl1
                    e[l] = s * p
                    d[l] = c * p

                    // Check for convergence.
                } while (abs(e[l]) > eps * tst1)
            }
            d[l] = d[l] + f
            e[l] = 0.0
        }

        // Sort eigenvalues and corresponding vectors.
        for (i in 0 until n - 1) {
            var k = i
            var p = d[i]
            for (j in i + 1 until n) {
                if (d[j] < p) { // NH find smallest k>i
                    k = j
                    p = d[j]
                }
            }
            if (k != i) {
                d[k] = d[i] // swap k and i
                d[i] = p
                for (j in 0 until n) {
                    p = V[j][i]
                    V[j][i] = V[j][k]
                    V[j][k] = p
                }
            }
        }
    } // tql2

    /**
     * not really in use so far, just clones and copies
     *
     * @param popx genotype
     * @param popy phenotype, repaired
     * @return popy
     */
    private fun genoPhenoTransformation(popx: Array<DoubleArray?>, popy: Array<DoubleArray?>?): Array<DoubleArray?> {
        var popyv = popy
        if (popyv == null || popyv.contentEquals(popx) || popyv.size != popx.size) popyv = arrayOfNulls(popx.size)
        for (i in popyv.indices) popyv[i] = genoPhenoTransformation(popx[i], popyv[i])
        return popyv
    }

    /**
     * not really in use so far, just clones and copies
     *
     * @param popx genotype
     * @param popy phenotype, repaired
     * @return popy
     */
    private fun phenoGenoTransformation(
        popx: Array<DoubleArray>, @Suppress("SameParameterValue") popy: Array<DoubleArray?>?
    ): Array<DoubleArray?> {
        var popyv = popy
        if (popyv == null || popyv.contentEquals(popx) || popyv.size != popx.size) popyv = arrayOfNulls(popx.size)
        for (i in popyv.indices) popyv[i] = phenoGenoTransformation(popx[i], popyv[i])
        return popyv
    }

    /**
     * not really in use so far, just clones and copies
     *
     * @param x genotype
     * @param y phenotype
     * @return y
     */
    private fun genoPhenoTransformation(x: DoubleArray?, y: DoubleArray?): DoubleArray {
        var yv = y
        if (yv == null || yv.contentEquals(x) || yv.size != x!!.size) {
            yv = x!!.clone()
            return yv // for now return an identical copy
        }
        for (i in 0 until N) yv[i] = x[i]
        return yv
    }

    /**
     * not really in use so far, just clones and copies
     *
     * @param x genotype
     * @param y phenotype
     * @return y
     */
    private fun phenoGenoTransformation(x: DoubleArray, y: DoubleArray?): DoubleArray {
        var yv = y
        if (yv == null || yv.contentEquals(x) || yv.size != x.size) {
            yv = x.clone()
            return yv // for now return an identical copy
        }
        for (i in 0 until N) yv[i] = x[i]
        return yv
    }

    /**
     * Samples the recent search distribution lambda times
     *
     * @return double[][] population, lambda times dimension array of sampled solutions,
     * where `lambda == parameters.getPopulationSize()`
     * @see .resampleSingle
     * @see .updateDistribution
     * @see CMAParameters.getPopulationSize
     */
    fun samplePopulation(): Array<DoubleArray?> {
        var i: Int
        var j: Int
        var sum: Double
        if (state < 0) init() else if (state < 3 && state > 2) error("mixing of calls to updateSingle() and samplePopulation() is not possible") else eigendecomposition(
            0
        ) // latest possibility to generate B and diagD
        if (state != 1.0) ++countIter
        state = 1.0 // can be repeatedly called without problem
        idxRecentOffspring = sp.getLambda() - 1 // not really necessary at the moment


        // ensure maximal and minimal standard deviations
        if (options.lowerStandardDeviations != null && options.lowerStandardDeviations!!.isNotEmpty()) {
            i = 0
            while (i < N) {
                val d = options.lowerStandardDeviations!![min(i, options.lowerStandardDeviations!!.size - 1)]
                if (d > sigma * minsqrtdiagC) sigma = d / minsqrtdiagC
                ++i
            }
        }
        if (options.upperStandardDeviations != null && options.upperStandardDeviations!!.isNotEmpty()) {
            i = 0
            while (i < N) {
                val d = options.upperStandardDeviations!![min(i, options.upperStandardDeviations!!.size - 1)]
                if (d < sigma * maxsqrtdiagC) sigma = d / maxsqrtdiagC
                ++i
            }
        }
        testAndCorrectNumerics()

        /* sample the distribution */
        var iNk = 0
        while (iNk < sp.getLambda()) {
            /*
         * generate scaled
         * random vector (D * z)
         */

            // code duplication from resampleSingle because of possible future resampling before GenoPheno
            /* generate scaled random vector (D * z) */if (flgdiag) {
                i = 0
                while (i < N) {
                    arx[iNk]!![i] = xmean!![i] + sigma * diagD[i] * rand.nextGaussian()
                    ++i
                }
            } else {
                i = 0
                while (i < N) {
                    artmp[i] = diagD[i] * rand.nextGaussian()
                    ++i
                }

                /* add mutation (sigma * B * (D*z)) */i = 0
                while (i < N) {
                    j = 0
                    sum = 0.0
                    while (j < N) {
                        sum += B[i][j] * artmp[j]
                        ++j
                    }
                    arx[iNk]!![i] = xmean!![i] + sigma * sum
                    ++i
                }
            }
            ++iNk
        }

        // I am desperately missing a const/readonly/visible qualifier.
        return genoPhenoTransformation(arx, population).also { population = it }
    } // end samplePopulation()

    /**
     * re-generate the `index`-th solution. After getting lambda
     * solution points with samplePopulation() the i-th point,
     * i=0...lambda-1, can be sampled anew by resampleSingle(i).
     *
     * <PRE>
     * double[][] pop = cma.samplePopulation();
     * // check some stuff, i-th solution went wrong, therefore
     * pop[i] = cma.resampleSingle(i); // assignment to keep the population consistent
     * for (i = 0,...)
     * fitness[i] = fitfun.valueof(pop[i]);
    </PRE> *
     *
     * @see .samplePopulation
     */
    fun resampleSingle(index: Int): DoubleArray {
        var i: Int
        var j: Int
        var sum: Double
        if (state != 1.0) error("call samplePopulation before calling resampleSingle(int index)")

        /* sample the distribution */
        /* generate scaled random vector (D * z) */if (flgdiag) {
            i = 0
            while (i < N) {
                arx[index]!![i] = xmean!![i] + sigma * diagD[i] * rand.nextGaussian()
                ++i
            }
        } else {
            i = 0
            while (i < N) {
                artmp[i] = diagD[i] * rand.nextGaussian()
                ++i
            }

            /* add mutation (sigma * B * (D*z)) */i = 0
            while (i < N) {
                j = 0
                sum = 0.0
                while (j < N) {
                    sum += B[i][j] * artmp[j]
                    ++j
                }
                arx[index]!![i] = xmean!![i] + sigma * sum
                ++i
            }
        }
        return genoPhenoTransformation(arx[index], population[index]).also { population[index] = it }
    } // resampleSingle

    /**
     * compute Mahalanobis norm of x - mean w.r.t. the current distribution
     * (using covariance matrix times squared step-size for the inner product).
     * TODO: to be tested.
     *
     * @param x
     * @param mean
     * @return Malanobis norm of x - mean: sqrt((x-mean)' C^-1 (x-mean)) / sigma
     */
    private fun mahalanobisNorm(x: DoubleArray?, mean: DoubleArray?): Double {
        var yi: Double
        var snorm = 0.0
        var j: Int
        // snorm = (x-mean)' Cinverse (x-mean) = (x-mean)' (BD^2B')^-1 (x-mean)
        //       = (x-mean)' B'^-1 D^-2 B^-1 (x-mean)
        //       = (x-mean)' B D^-1 D^-1 B' (x-mean)
        //       = (D^-1 B' (x-mean))' * (D^-1 B' (x-mean))
        /* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
        var i = 0
        while (i < N) {
            j = 0
            yi = 0.0
            while (j < N) {
                yi += B[j][i] * (x!![j] - mean!![j])
                ++j
            }
            // yi = i-th component of B' (x-mean)
            snorm += yi * yi / diagD[i] / diagD[i]
            ++i
        }
        return sqrt(snorm) / sigma
    }

    /**
     * update of the search distribution from a population and its
     * function values, see [.updateDistribution].
     * This might become updateDistribution(double[][], double[], popsize)
     * in future.
     *
     * @param population     double[lambda][N], lambda solutions
     * @param functionValues double[lambda], respective objective values of population
     * @see .samplePopulation
     * @see .updateDistribution
     * @see .updateDistribution
     */
    @JvmOverloads
    fun updateDistribution(population: Array<DoubleArray>, functionValues: DoubleArray?, nInjected: Int = 0) {
        // TODO: Needs to be tested yet for nInjected > 0
        // pass first input argument
        arx = phenoGenoTransformation(population, null) // TODO should still be tested
        for (i in 0 until nInjected) {
            warning("TODO: checking of injected solution has not yet been tested")
            // if (mahalanobisNorm(arx[0], xmean) > Math.sqrt(N) + 2) // testing: seems fine
            //     System.out.println(mahalanobisNorm(arx[i], xmean)/Math.sqrt(N));
            val upperLength = sqrt(N.toDouble()) + 2.0 * N / (N + 2.0) // should become an interfaced parameter?
            val fac = upperLength / mahalanobisNorm(arx[i], xmean)
            if (fac < 1) for (j in 0 until N) arx[i]!![j] = xmean!![j] + fac * (arx[i]!![j] - xmean!![j])
        }
        updateDistribution(functionValues)
    }

    /**
     * update of the search distribution after samplePopulation(). functionValues
     * determines the selection order (ranking) for the solutions in the previously sampled
     * population. This is just a different interface for updateDistribution(double[][], double[]).
     *
     * @see .samplePopulation
     * @see .updateDistribution
     */
    internal fun updateDistribution(functionValues: DoubleArray?) {
        if (state == 3.0) {
            error("updateDistribution() was already called")
        }
        if (functionValues!!.size != sp.getLambda()) error(
            "argument double[] funcionValues.length=" + functionValues.size + "!=" + "lambda=" + sp.getLambda()
        )

        /* pass input argument */for (i in 0 until sp.getLambda()) {
            fit.raw[i]!!.value = functionValues[i]
            fit.raw[i]!!.i = i
        }
        countEval += sp.getLambda().toLong()
        recentFunctionValue = math.min(fit.raw)!!.value
        worstRecentFunctionValue = math.max(fit.raw)!!.value
        bestRecentFunctionValue = math.min(fit.raw)!!.value
        updateDistribution()
    }

    private fun updateDistribution() {
        var i: Int
        var j: Int
        var k: Int
        var iNk: Int
        var hsig: Int
        var sum: Double
        if (state == 3.0) {
            error("updateDistribution() was already called")
        }

        /* sort function values */Arrays.sort(fit.raw, fit.raw[0])
        iNk = 0
        while (iNk < sp.getLambda()) {
            fit.fitness[iNk]!!.value = fit.raw[iNk]!!.value // superfluous at time
            fit.fitness[iNk]!!.i = fit.raw[iNk]!!.i
            ++iNk
        }

        /* update fitness history */i = fit.history.size - 1
        while (i > 0) {
            fit.history[i] = fit.history[i - 1]
            --i
        }
        fit.history[0] = fit.raw[0]!!.value

        /* save/update bestever-value */updateBestEver(
            arx[fit.raw[0]!!.i], fit.raw[0]!!.value, countEval - sp.getLambda() + fit.raw[0]!!.i + 1
        )

        /* re-calculate diagonal flag */flgdiag =
            options.diagonalCovarianceMatrix == 1L || options.diagonalCovarianceMatrix >= countIter
        if (options.diagonalCovarianceMatrix == -1L) // options might have been re-read
            flgdiag = countIter <= 1 * 150 * N / sp.lambda // CAVE: duplication of "default"

        /* calculate xmean and BDz~N(0,C) */i = 0
        while (i < N) {
            xold[i] = xmean!![i]
            xmean!![i] = 0.0
            iNk = 0
            while (iNk < sp.getMu()) {
                xmean!![i] += sp.weights!![iNk] * arx[fit.fitness[iNk]!!.i]!![i]
                ++iNk
            }
            BDz[i] = sqrt(sp.mueff) * (xmean!![i] - xold[i]) / sigma
            ++i
        }

        /* cumulation for sigma (ps) using B*z */if (flgdiag) {
            /* given B=I we have B*z = z = D^-1 BDz  */
            i = 0
            while (i < N) {
                ps[i] = ((1.0 - sp.getCs()) * ps[i] + sqrt(sp.getCs() * (2.0 - sp.getCs())) * BDz[i] / diagD[i])
                ++i
            }
        } else {
            /* calculate z := D^(-1) * B^(-1) * BDz into artmp, we could have stored z instead */
            i = 0
            while (i < N) {
                j = 0
                sum = 0.0
                while (j < N) {
                    sum += B[j][i] * BDz[j]
                    ++j
                }
                artmp[i] = sum / diagD[i]
                ++i
            }
            /* cumulation for sigma (ps) using B*z */i = 0
            while (i < N) {
                j = 0
                sum = 0.0
                while (j < N) {
                    sum += B[i][j] * artmp[j]
                    ++j
                }
                ps[i] = ((1.0 - sp.getCs()) * ps[i] + sqrt(sp.getCs() * (2.0 - sp.getCs())) * sum)
                ++i
            }
        }

        /* calculate norm(ps)^2 */
        var psxps = 0.0
        i = 0
        while (i < N) {
            psxps += ps[i] * ps[i]
            ++i
        }

        /* cumulation for covariance matrix (pc) using B*D*z~N(0,C) */hsig = 0
        if ((sqrt(psxps) / sqrt(
                1.0 - (1.0 - sp.getCs()).pow(2.0 * countIter)
            ) / sp.chiN) < 1.4 + 2.0 / (N + 1.0)
        ) {
            hsig = 1
        }
        i = 0
        while (i < N) {
            pc[i] = (1.0 - sp.cc) * pc[i] + (hsig * sqrt(sp.cc * (2.0 - sp.cc)) * BDz[i])
            ++i
        }

        /* stop initial phase, not in use anymore as hsig does the job */if (iniphase && countIter > min(
                1 / sp.getCs(),
                1 + N / sp.getMucov()
            )
        ) if ((psxps / sp.getDamps() / (1.0 - (1.0 - sp.getCs()).pow(countIter.toDouble()))) < N * 1.05) iniphase =
            false

        /* update of C */
        if (sp.ccov > 0 && !iniphase) {
            ++countCupdatesSinceEigenupdate

            /* update covariance matrix */i = 0
            while (i < N) {
                j = if (flgdiag) i else 0
                while (j <= i) {
                    C[i][j] =
                        ((1 - sp.getCcov(flgdiag)) * C[i][j] + (sp.ccov * (1.0 / sp.getMucov()) * (pc[i] * pc[j] + ((1 - hsig) * sp.cc * (2.0 - sp.cc) * C[i][j]))))
                    k = 0
                    while (k < sp.getMu()) {
                        /*
                     * additional rank mu
                     * update
                     */C[i][j] += ((sp.ccov * (1 - 1.0 / sp.getMucov()) * sp.weights!![k] * (arx[fit.fitness[k]!!.i]!![i] - xold[i]) * (arx[fit.fitness[k]!!.i]!![j] - xold[j])) / sigma / sigma)
                        ++k
                    }
                    ++j
                }
                ++i
            }
            maxsqrtdiagC = sqrt(math.max(math.diag(C)))
            minsqrtdiagC = sqrt(math.min(math.diag(C)))
        } // update of C

        /* update of sigma */sigma *= exp(
            (sqrt(psxps) / sp.chiN - 1) * sp.getCs() / sp.getDamps()
        )
        state = 3.0
    } // updateDistribution()

    /**
     * assigns lhs to a different instance with the same values,
     * sort of smart clone, but it may be that clone is as smart already
     *
     * @param rhs
     * @param lhs
     * @return
     */
    private fun assignNew(rhs: DoubleArray?, lhs: DoubleArray?): DoubleArray {
        var lhsv = lhs
        assert(
            rhs != null // will produce an error anyway
        )
        if (lhsv != null && !lhsv.contentEquals(rhs) && lhsv.size == rhs!!.size) for (i in lhsv.indices) lhsv[i] =
            rhs[i] else lhsv = rhs!!.clone()
        return lhsv
    }

    private fun updateBestEver(x: DoubleArray?, fitness: Double, eval: Long) {
        if (fitness < bestever_fit || java.lang.Double.isNaN(bestever_fit)) {  // countiter == 1 not needed anymore
            bestever_fit = fitness
            bestEvaluationNumber = eval
            bestever_x = assignNew(x, bestever_x) // save (hopefully) efficient assignment
        }
    }

    /**
     * the final setting of initial `x` can
     * be retrieved only after `init()` was called
     *
     * @return `double[] initialX` start point chosen for
     * distribution mean value `xmean`
     */
    @JvmName("getInitialX1")
    fun getInitialX(): DoubleArray {
        if (state < 0) error("initiaX not yet available, init() must be called first")
        return initialX.clone()
    }

    /**
     * sets `initialX` to the same value in each coordinate
     *
     * @param x value
     * @see .setInitialX
     */
    internal fun setInitialX(x: Double) {
        if (state >= 0) error("initial x cannot be set anymore")
        xmean = doubleArrayOf(x) // allows "late binding" of dimension N
    }

    /**
     * set initial search point to input value `x`. `x.length==1` is possible, otherwise
     * the search space dimension is set to `x.length` irrevocably
     *
     * @param x double[] initial point
     * @see .setInitialX
     * @see .setInitialX
     */
    @JvmName("setInitialX1")
    fun setInitialX(x: DoubleArray?) {
        if (state >= 0) error("initial x cannot be set anymore")
        if (x!!.size == 1) { // to make properties work
            setInitialX(x[0])
            return
        }
        if (N > 0 && N != x.size) error("dimensions do not match")
        if (N == 0) dimension = x.size
        assert(N == x.size)
        xmean = DoubleArray(N)
        for (i in 0 until N) xmean!![i] = x[i]
        lockDimension = 1 // because xmean is set up
    }

    /**
     * @see .setSeed
     */
    @JvmName("getSeed1")
    fun getSeed(): Long {
        return seed
    }

    /**
     * Setter for the seed for the random number generator
     * java.util.Random(seed). Changing the seed will only take
     * effect before [.init] was called.
     *
     * @param seed a long value to initialize java.util.Random(seed)
     */
    @JvmName("setSeed1")
    fun setSeed(seed: Long) {
        var seedv = seed
        if (state >= 0) warning("setting seed has no effect at this point") else {
            if (seedv <= 0) seedv = System.currentTimeMillis()
            this.seed = seedv
            rand.setSeed(seedv)
        }
    }

    /**
     * number of objective function evaluations counted so far
     */
    fun setCountEval(c: Long): Long {
        return c.also { countEval = it }
    }

    /**
     * sets typicalX value, the same value in each coordinate
     *
     * @see .setTypicalX
     */
    private fun setTypicalX(x: Double) {
        if (state >= 0) error("typical x cannot be set anymore")
        typicalX = doubleArrayOf(x) // allows "late binding" of dimension
    }

    /**
     * sets typicalX value, which will be overwritten by initialX setting from properties
     * or [.setInitialX] function call.
     * Otherwise the initialX is sampled normally distributed from typicalX with initialStandardDeviations
     *
     * @see .setTypicalX
     * @see .setInitialX
     * @see .setInitialStandardDeviations
     */
    @JvmName("setTypicalX1")
    fun setTypicalX(x: DoubleArray?) {
        if (state >= 0) error("typical x cannot be set anymore")
        if (x!!.size == 1) { // to make properties work
            setTypicalX(x[0])
            return
        }
        if (N < 1) dimension = x.size
        if (N != x.size) error("dimensions N=" + N + " and input x.length=" + x.size + "do not agree")
        typicalX = DoubleArray(N)
        for (i in 0 until N) typicalX!![i] = x[i]
        lockDimension = 1
    }

    internal fun setInitialStandardDeviation(startsigma: Double) {
        if (state >= 0) error("standard deviations cannot be set anymore")
        this.startsigma = doubleArrayOf(startsigma)
    }

    private fun setInitialStandardDeviations(startsigma: DoubleArray?) {
        // assert startsigma != null; // assert should not be used for public arg check
        if (state >= 0) error("standard deviations cannot be set anymore")
        if (startsigma!!.size == 1) { // to make properties work
            setInitialStandardDeviation(startsigma[0])
            return
        }
        if (N > 0 && N != startsigma.size) error(
            "dimensions N=" + N + " and input startsigma.length=" + startsigma.size + "do not agree"
        )
        if (N == 0) dimension = startsigma.size
        assert(N == startsigma.size)
        this.startsigma = startsigma.clone()
        lockDimension = 1
    }

    /**
     * set initial seach point `xmean` coordinate-wise uniform
     * between `l` and `u`,
     * dimension needs to have been set before
     *
     * @param l double lower value
     * @param u double upper value
     * @see .setInitialX
     * @see .setInitialX
     */
    fun setInitialX(l: Double, u: Double) {
        if (state >= 0) error("initial x cannot be set anymore")
        if (N < 1) error("dimension must have been specified before")
        xmean = DoubleArray(N)
        for (i in xmean!!.indices) xmean!![i] = l + (u - l) * rand.nextDouble()
        lockDimension = 1
    }

    /**
     * set initial seach point `x` coordinate-wise uniform
     * between `l` and `u`,
     * dimension needs to have been set before
     *
     * @param l double lower value
     * @param u double upper value
     */
    fun setInitialX(l: DoubleArray, u: DoubleArray) {
        if (state >= 0) error("initial x cannot be set anymore")
        if (l.size != u.size) error("length of lower and upper values disagree")
        dimension = l.size
        xmean = DoubleArray(N)
        for (i in xmean!!.indices) xmean!![i] = l[i] + (u[i] - l[i]) * rand.nextDouble()
        lockDimension = 1
    }

    /**
     * returns an informative initial message of the CMA-ES optimizer
     */
    fun helloWorld(): String {
        return "(" + sp.getMu() + "," + sp.getLambda() + ")-CMA-ES(mu_eff=" + (10.0 * sp.mueff).roundToLong() / 10.0 + "), Ver=\"" + versionNumber + "\", dimension=" + N + ", " + options.diagonalCovarianceMatrix + " diagonal iter." + ", randomSeed=" + seed + " (" + Date().toString() + ")"

    }

    /**
     * calls println(helloWorld())
     *
     * @see .helloWorld
     * @see .println
     */
    private fun printlnHelloWorld() {
        println(helloWorld())
    }

    /**
     * writes a string to a file, overwrites first, appends afterwards.
     *
     * Example: cma.writeToFile("cmaescorr.dat", cma.writeC());
     *
     * @param filename  is a String giving the name of the file to be written
     * @param data      is a String of text/data to be written
     * @param flgAppend for flgAppend>0 old data are not overwritten
     */
    private fun writeToFile(filename: String, data: String?, flgAppend: Int) {
        var appendflag = flgAppend > 0
        var i = 0
        while (!appendflag && i < fileswritten.size) {
            if (filename == fileswritten[i]) {
                appendflag = true
            }
            ++i
        }
        var out: PrintWriter? = null
        try {
            out = PrintWriter(FileWriter(filename, appendflag))
            out.println(data)
            out.flush() // no idea whether this makes sense
            out.close()
        } catch (e: FileNotFoundException) {
            warning("Could not find file '$filename'(FileNotFoundException)")
        } catch (e: IOException) {
            warning("Could not open/write to file $filename")
            //e.printStackTrace();            // output goes to System.err
            //e.printStackTrace(System.out);  // send trace to stdout
        } finally {
            out?.close()
        }
        // if first time written
        // append filename to fileswritten
        if (!appendflag) {
            val s = fileswritten
            fileswritten = arrayOfNulls(fileswritten.size + 1)
            for (iv in s.indices) fileswritten[iv] = s[iv]
            fileswritten[fileswritten.size - 1] = filename
        }
    }

    /**
     * writes data output to default files. Maximum time spent
     * for writing can be controlled in the properties file. For negative values
     * no writing takes place, overruling the `flgForce` input parameter below.
     *
     * @param flgForce 0==write depending on time spent with writing,
     * 1==write if the iteration count has changed,
     * 2==write always, overruled by negative values of maxTimeFractionForWriteToDefaultFiles property
     * @see .writeToDefaultFiles
     */
    fun writeToDefaultFiles(flgForce: Int) {
        if (flgForce > 0 && countIter != citerlastwritten) citerlastwritten =
            -1 // force writing if something new is there
        if (flgForce >= 2) citerlastwritten = -1 // force writing
        writeToDefaultFiles(options.outputFileNamesPrefix)
    }

    /**
     * writes data output to default files. Uses opts.outputFileNamesPrefix to create filenames.
     * Columns 1-2 are iteration number and function evaluation count,
     * columns 6- are the data according to the filename. Maximum time spent
     * for writing can be controlled in the properties file.
     *
     *
     * The output is written to files that can be printed in Matlab or Scilab (a free
     * and easy to install Matlab "clone").
     *
     *
     * Matlab:
     * <pre>
     * cd 'directory_where_outfiles_and_plotcmaesdat.m_file_are'
     * plotcmaesdat;
    </pre> *
     * Scilab:
     * <pre>
     * cd 'directory_where_outfiles_and_plotcmaesdat.sci_file_are'
     * getf('plotcmaesdat.sci');
     * plotcmaesdat;
    </pre> *
     *
     *
     * @see .writeToDefaultFiles
     * @see .writeToDefaultFiles
     */
    @JvmOverloads
    fun writeToDefaultFiles(fileNamePrefix: String? = options.outputFileNamesPrefix) {
        if (options.maxTimeFractionForWriteToDefaultFiles < 0) // overwrites force flag
            return
        if (citerlastwritten >= 0) { // negative value forces writing
            if (state < 1) return
            if (countIter == citerlastwritten) return
            if (options.maxTimeFractionForWriteToDefaultFiles <= 0) return
            if (countIter > 4 && stopConditions.index == 0 // has no effect if stopCondition.test() was not called
                // iteration gap is less than two times of the average gap, to not have large data holes
                // spoils the effect of reducing the timeFraction late in the run
                && countIter - citerlastwritten - 1 < 2.0 * (countIter - countwritten + 1.0) / (countwritten + 1.0) // allowed time is exhausted
                && (timings.writedefaultfiles > options.maxTimeFractionForWriteToDefaultFiles * (System.currentTimeMillis() - timings.start))
            ) return
        }
        val firsttime = System.currentTimeMillis()
        writeToFile(fileNamePrefix + "fit.dat", dataRowFitness, 1)
        writeToFile(fileNamePrefix + "xmean.dat", dataRowXMean, 1)
        writeToFile(fileNamePrefix + "xrecentbest.dat", dataRowXRecentBest, 1)
        writeToFile(fileNamePrefix + "stddev.dat", dataRowStddev, 1) // sigma*sqrt(diag(C))
        writeToFile(fileNamePrefix + "axlen.dat", dataRowAxlen, 1)
        timings.writedefaultfiles += System.currentTimeMillis() - firsttime
        //        System.out.println(timings.writedefaultfiles + " "
//                + (System.currentTimeMillis()-timings.start)  + " " + opts.maxTimeFractionForWriteToDefaultFiles);
        if (countIter < 3) timings.writedefaultfiles = 0
        ++countwritten
        citerlastwritten = countIter
    }

    /**
     * writes header lines to the default files. Could become XML if needed.
     *
     * @param flgAppend == 0 means overwrite files,  == 1 means append to files
     */
    fun writeToDefaultFilesHeaders(flgAppend: Int) {
        writeToDefaultFilesHeaders(options.outputFileNamesPrefix, flgAppend)
    }

    /**
     * Writes headers (column annotations) to files <prefix>fit.dat, ...xmean.dat
     * ...xbest.dat, ...std.dat, ...axlen.dat, and in case the first data
     * line, usually with the initial values.
     *
     * @param fileNamePrefix String for filenames created to write data
    </prefix> */
    private fun writeToDefaultFilesHeaders(fileNamePrefix: String?, flgAppend: Int) {
        if (options.maxTimeFractionForWriteToDefaultFiles < 0) // overwrites force flag
            return
        val s = """
             (randomSeed=$seed, ${Date()})
             
             """.trimIndent()
        writeToFile(
            fileNamePrefix + "fit.dat",
            "%# iteration evaluations sigma axisratio fitness_of(bestever best median worst) mindii idxmaxSD maxSD idxminSD minSD $s",
            flgAppend
        )
        writeToFile(
            fileNamePrefix + "xmean.dat", "%# iteration evaluations sigma void void mean(1...dimension) $s", flgAppend
        )
        if (state == 0.0) writeToFile(fileNamePrefix + "xmean.dat", dataRowXMean, 1)
        writeToFile(
            fileNamePrefix + "xrecentbest.dat",
            "%# iteration evaluations sigma void fitness_of_recent_best x_of_recent_best(1...dimension) $s",
            flgAppend
        )
        writeToFile(
            fileNamePrefix + "stddev.dat",
            "%# iteration evaluations sigma idxmaxSD idxminSD SDs=sigma*sqrt(diag(C)) $s",
            flgAppend
        )
        if (state == 0.0) writeToFile(fileNamePrefix + "stddev.dat", dataRowStddev, 1)
        writeToFile(
            fileNamePrefix + "axlen.dat",
            "%# iteration evaluations sigma axisratio stddevratio sort(diag(D)) (square roots of eigenvalues of C) $s",
            flgAppend
        )
        if (state == 0.0) writeToFile(fileNamePrefix + "axlen.dat", dataRowAxlen, 1)
    }

    /**
     * Interface to whether and which termination criteria are satisfied
     */
    inner class StopCondition {
        var index = 0 // number of messages collected == index where to write next message
        private var messages = arrayOf<String?>("") // Initialisation with empty string
        private var lastcounteval = 0.0

        /**
         * true whenever a termination criterion was met. clear()
         * re-sets this value to false.
         *
         * @see .clear
         */
        private val isTrue: Boolean
            get() = test() > 0

        /**
         * evaluates to NOT isTrue().
         *
         * @see .isTrue
         */
        val isFalse: Boolean
            get() = !isTrue

        /**
         * greater than zero whenever a termination criterion was satisfied, zero otherwise.
         * clear() re-sets this value to zero.
         *
         * @return number of generated termination condition messages
         */
        val number: Int
            get() = test()

        /**
         * get description messages of satisfied termination criteria.
         * The messages start with one of "Fitness:", "TolFun:", "TolFunHist:",
         * "TolX:", "TolUpX:", "MaxFunEvals:", "MaxIter:", "ConditionNumber:",
         * "NoEffectAxis:", "NoEffectCoordinate:".
         *
         * @return String[] s with messages of termination conditions.
         * s[0].equals("") is true if no termination condition is satisfied yet
         */
        @JvmName("getMessages1")
        fun getMessages(): Array<String?> {
            test()
            return messages /* first string might be empty */
        }

        /**
         * remove all earlier termination condition messages
         */
        fun clear() {
            messages = arrayOf("")
            index = 0
        }

        private fun appendMessage(s: String) {
            // could be replaced by ArrayList<String> or Vector<String>
            // but also String[] can be iterated easily since version 1.5
            val mold = messages
            messages = arrayOfNulls(index + 1)

            /* copy old messages */if (index >= 0) System.arraycopy(mold, 0, messages, 0, index)
            messages[index++] = "$s (iter=$countIter,eval=$countEval)"
        }

        /**
         * Tests termination criteria and evaluates to  greater than zero when a
         * termination criterion is satisfied. Repeated tests append the met criteria repeatedly,
         * only if the evaluation count has changed.
         *
         * @return number of termination criteria satisfied
         */
        private fun test(): Int {
            if (state < 0) return 0 // not yet initialized
            if (index > 0 && (countEval.toDouble() == lastcounteval || countEval.toDouble() == lastcounteval + 1)) // one evaluation for xmean is ignored
                return index // termination criterion already met
            lastcounteval = countEval.toDouble()

            /* FUNCTION VALUE */
            if ((countIter > 1 || state >= 3) && bestever_fit <= options.stopFitness) appendMessage(
                "Fitness: Objective function value dropped below the target function value " + options.stopFitness
            )

            /* #Fevals */
            if (countEval >= options.stopMaxFunEvals) appendMessage("MaxFunEvals: maximum number of function evaluations " + options.stopMaxFunEvals + " reached")

            /* #iterations */
            if (countIter >= options.stopMaxIter) appendMessage("MaxIter: maximum number of iterations reached")

            /* TOLFUN */
            if ((countIter > 1 || state >= 3) && math.max(fit.history)
                    .coerceAtLeast(fit.fitness[fit.fitness.size - 1]!!.value) - math.min(fit.history)
                    .coerceAtMost(fit.fitness[0]!!.value) <= options.stopTolFun
            ) appendMessage("TolFun: function value changes below stopTolFun=" + options.stopTolFun)

            /* TOLFUNHIST */
            if (options.stopTolFunHist >= 0 && countIter > fit.history.size) {
                if (math.max(fit.history) - math.min(fit.history) <= options.stopTolFunHist) appendMessage("TolFunHist: history of function value changes below stopTolFunHist=" + options.stopTolFunHist)
            }

            /* TOLX */
            val tolx = options.stopTolX.coerceAtLeast(options.stopTolXFactor * minstartsigma)
            if (sigma * maxsqrtdiagC < tolx && sigma * math.max(math.abs(pc)) < tolx) appendMessage("TolX or TolXFactor: standard deviation below $tolx")

            /* TOLXUP */
            if (sigma * maxsqrtdiagC > options.stopTolUpXFactor * maxstartsigma) appendMessage(
                "TolUpX: standard deviation increased by more than stopTolUpXFactor=" + options.stopTolUpXFactor + ", larger initial standard deviation recommended"
            )

            /* STOPNOW */
            if (options.stopnow) appendMessage("Manual: flag Options.stopnow set or stop now in .properties file")

            /* Internal (numerical) stopping termination criteria */

            /* Test each principal axis i, whether x == x + 0.1 * sigma * rgD[i] * B[i] */
            for (iAchse in 0 until N) {
                var iKoo: Int
                val l = if (flgdiag) iAchse else 0
                val u = if (flgdiag) iAchse + 1 else N
                val fac = 0.1 * sigma * diagD[iAchse]
                iKoo = l
                while (iKoo < u) {
                    if (xmean!![iKoo] != xmean!![iKoo] + fac * B[iKoo][iAchse]) break // is OK for this iAchse
                    ++iKoo
                }
                if (iKoo == u) // no break, therefore no change for axis iAchse
                    appendMessage(
                        "NoEffectAxis: Mutation " + 0.1 * sigma * diagD[iAchse] + " in a principal axis " + iAchse + " has no effect"
                    )
            } /* for iAchse */

            /* Test whether one component of xmean is stuck */
            for (iKoo in 0 until N) {
                if (xmean!![iKoo] == xmean!![iKoo] + 0.2 * sigma * sqrt(C[iKoo][iKoo]))
                    appendMessage(
                        "NoEffectCoordinate: Mutation of size " + 0.2 * sigma * sqrt(C[iKoo][iKoo]) + " in coordinate " + iKoo + " has no effect"
                    )
            } /* for iKoo */

            /* Condition number */
            if (math.min(diagD) <= 0)
                appendMessage("ConditionNumber: smallest eigenvalue smaller or equal zero")
            else if (math.max(diagD) / math.min(diagD) > 1e7)
                appendMessage("ConditionNumber: condition number of the covariance matrix exceeds 1e14")
            return index // call to appendMessage increments index
        }
    } // StopCondtion

    /* fitness information */
    inner class FitnessCollector {
        var history: DoubleArray = doubleArrayOf()
        internal var fitness: Array<IntDouble?> = arrayOf()
        internal var raw: Array<IntDouble?> = arrayOf()

        /**
         * history of delta fitness / sigma^2. Here delta fitness is the minimum of
         * fitness value differences with distance lambda/2 in the ranking.
         */
        var deltaFitHist = DoubleArray(5)
        var idxDeltaFitHist = 0
    }

    /**
     * some simple math utilities
     */
    inner class MyMath {
        // implements java.io.Serializable {
        var itest = 0
        fun square(d: Double): Double {
            return d * d
        }

        fun prod(ar: DoubleArray): Double {
            var res = 1.0
            for (i in ar.indices) res *= ar[i]
            return res
        }

        fun median(ar: DoubleArray): Double {
            // need a copy of ar
            val ar2 = DoubleArray(ar.size)
            for (i in ar.indices) ar2[i] = ar[i]
            Arrays.sort(ar2)
            return if (ar2.size % 2 == 0) (ar2[ar.size / 2] + ar2[ar.size / 2 - 1]) / 2.0 else ar2[ar.size / 2]
        }

        /**
         * @return Maximum value of 1-D double array
         */
        fun max(ar: DoubleArray): Double {
            var m: Double
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (m < ar[i]) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * sqrt(a^2 + b^2) without under/overflow.
         */
        fun hypot(a: Double, b: Double): Double {
            var r = 0.0
            if (abs(a) > abs(b)) {
                r = b / a
                r = abs(a) * sqrt(1 + r * r)
            } else if (b != 0.0) {
                r = a / b
                r = abs(b) * sqrt(1 + r * r)
            }
            return r
        }
        /**
         * @param ar     double[]
         * @param maxidx last index to be considered
         * @return index of minium value of 1-D double
         * array between index 0 and maxidx
         */
        /**
         * @return index of minium value of 1-D double array
         */
        @JvmOverloads
        fun minidx(ar: DoubleArray, maxidx: Int = ar.size - 1): Int {
            var idx: Int
            idx = 0
            var i = 1
            while (i < maxidx) {
                if (ar[idx] > ar[i]) idx = i
                ++i
            }
            return idx
        }

        /**
         * @param ar     double[]
         * @param maxidx last index to be considered
         * @return index of minium value of 1-D double
         * array between index 0 and maxidx
         */
        internal fun minidx(ar: Array<IntDouble>, maxidx: Int): Int {
            var idx: Int
            idx = 0
            var i = 1
            while (i < maxidx) {
                if (ar[idx].value > ar[i].value) idx = i
                ++i
            }
            return idx
        }

        /**
         * @return index of maximum value of 1-D double array
         */
        fun maxidx(ar: DoubleArray): Int {
            var idx: Int
            idx = 0
            var i = 1
            while (i < ar.size) {
                if (ar[idx] < ar[i]) idx = i
                ++i
            }
            return idx
        }

        /**
         * @return Minimum value of 1-D double array
         */
        fun min(ar: DoubleArray): Double {
            var m: Double
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (m > ar[i]) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * @return Maximum value of 1-D Object array where the object implements Comparator
         * Example: max(Double arx, arx[0])
         */
        fun max(ar: Array<Double?>, c: Comparator<Double?>): Double? {
            var m: Double?
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (c.compare(m, ar[i]) > 0) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * @return Maximum value of 1-D IntDouble array
         */
        internal fun max(ar: Array<IntDouble?>): IntDouble? {
            var m: IntDouble?
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (m!!.compare(m, ar[i]) < 0) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * @return Minimum value of 1-D IntDouble array
         */
        internal fun min(ar: Array<IntDouble?>): IntDouble? {
            var m: IntDouble?
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (m!!.compare(m, ar[i]) > 0) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * @return Minimum value of 1-D Object array defining a Comparator
         */
        fun min(ar: Array<Double?>, c: Comparator<Double?>): Double? {
            var m: Double?
            m = ar[0]
            var i = 1
            while (i < ar.size) {
                if (c.compare(m, ar[i]) < 0) m = ar[i]
                ++i
            }
            return m
        }

        /**
         * @return Diagonal of an 2-D double array
         */
        fun diag(ar: Array<DoubleArray>): DoubleArray {
            val diag = DoubleArray(ar.size)
            var i = 0
            while (i < ar.size && i < ar[i].size) {
                diag[i] = ar[i][i]
                ++i
            }
            return diag
        }

        /**
         * @return 1-D double array of absolute values of an 1-D double array
         */
        fun abs(v: DoubleArray): DoubleArray {
            val res = DoubleArray(v.size)
            for (i in v.indices) res[i] = abs(v[i])
            return res
        }
    } // MyMath

    inner class Timing {
        private var birth: Long = System.currentTimeMillis()// time at construction, not really in use
        var start = birth // time at end of init()
        var starteigen: Long = 0 // time after flgdiag was turned off, ie when calls to eigen() start
        var eigendecomposition: Long = 0 // spent time in eigendecomposition
        var writedefaultfiles: Long = 0 // spent time in writeToDefaultFiles

    }

    /**
     * very provisional error handling. Methods of the class
     * CMAEvolutionStrategy might throw the CMAException, that
     * need not be catched, because it extends the "unchecked"
     * RuntimeException class
     */
    inner class CMAException internal constructor(s: String?) : RuntimeException(s)

}

internal class IntDouble : Comparator<IntDouble?> {
    var i = 0// unique integer value, useful after sorting
    var value = 0.0 //double value

    override fun compare(o1: IntDouble?, o2: IntDouble?): Int {
        if (o1!!.value < o2!!.value) return -1
        if (o1.value > o2.value) return 1
        if (o1.i < o2.i) return -1
        return if (o1.i > o2.i) 1 else 0
    }

    fun equals(o1: IntDouble, o2: IntDouble?): Boolean {
        return o1.compare(o1, o2) == 0
    }
} // IntDouble
