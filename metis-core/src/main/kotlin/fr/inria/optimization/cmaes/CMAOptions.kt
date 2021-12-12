package fr.inria.optimization.cmaes

import java.io.Serializable
import java.util.*

/*
    Copyright 2003, 2005, 2007 Nikolaus Hansen 
    e-mail: hansen .AT. bionik.tu-berlin.de
            hansen .AT. lri.fr

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

  Last change: $Date: 2010-12-02 23:57:21 +0100 (Thu, 02 Dec 2010) $
 */
/**
 * Simple container of (mostly generic) options for the
 * optimization, like the maximum number of objective
 * function evaluations, see class fields.  No explicit setting of
 * options is needed to
 * initialize the CMA-ES ([CMAEvolutionStrategy.init])
 * and options of the CMA-ES can be set
 * and changed any time, either via a property file and the method
 * [CMAEvolutionStrategy.readProperties], or new values can simply be
 * assigned to the fields of the public `opts` field of
 * the class `CMAEvolutionStrategy` (yeah, I know, not exactly Java style).
 */
class CMAOptions : Serializable {
    /**
     * number of initial iterations with diagonal covariance matrix, where
     * 1 means always. Default is
     * diagonalCovarianceMatrix=0, but this will presumably change in future.
     * As long as iterations<=diagonalCovarianceMatrix
     * the internal time complexity is linear in the search space dimensionality
     * (memory requirements remain quadratic).
     */
    var diagonalCovarianceMatrix: Long = 0 // -1; 

    /**
     * lower bound for standard deviations (step sizes). The
     * Array can be of any length. The i-th entry corresponds to
     * the i-th variable. If length&#60;dim the last entry is recycled for
     * all remaining variables. Zero entries mean, naturally, no
     * lower bound. <P>CAVE: there is an interference with stopTolX (and stopTolXFactor):
     * if lowerStdDev is larger than stopTolX, the termination criterion
     * can never be satisfied.</P>
     *
     * Example:
     * <pre> CMAEvolutionStrategy es = new CMAEvolutionStrategy();
     * es.options.lowerStandardDeviations = new double[]{1e-4,1e-8}; // 1e-8 for all but first variable
    </pre> *
     *
     * @see .stopTolX
     *
     * @see .stopTolXFactor
     */
    var lowerStandardDeviations: DoubleArray? = null

    /**
     * upper bound for standard deviations (step lengths).
     * Zero entries mean no upper
     * bound. Be aware of the interference with option stopTolUpXFactor.
     *
     * @see .lowerStandardDeviations
     *
     * @see .stopTolUpXFactor
     */
    var upperStandardDeviations: DoubleArray? = null

    /**
     * stop if function value drops below the target
     * function value stopFitness. Default = `Double.MIN_VALUE`
     */
    var stopFitness = -Double.MAX_VALUE

    /**
     * stop if the
     * maximum function value difference of all iteration-best
     * solutions of the last 10 +
     * 30*N/lambda iterations
     * and all solutions of the recent iteration
     * become <= stopTolFun. Default = 1e-12.
     */
    var stopTolFun = 1e-12

    /**
     * stop if the maximum function value difference of all iteration-best
     * solutions of the last 10 +
     * 30*N/lambda iterations become smaller than
     * stopTolFunHist. Default = 1e-13. The measured objective
     * function value differences do not include repair
     * penalties.
     */
    var stopTolFunHist = 1e-13 // used if non-null

    /**
     * stop if search steps become smaller than stopTolX. Default = 0
     */
    var stopTolX = 0.0

    /**
     * stop if search steps become smaller than stopTolXFactor * initial step size.
     * Default = 1e-11.
     */
    var stopTolXFactor = 1e-11 // used if TolX is null

    /**
     * stop if search steps become larger than stopTolUpXFactor
     * * initial step size. Default = 1e3. When this termination
     * criterion applies on a static objective function, the initial
     * step-size was chosen far too
     * small (or divergent behavior is observed).
     */
    var stopTolUpXFactor = 1e3 // multiplier for initial sigma

    /**
     * stop if the number of objective function evaluations exceed stopMaxFunEvals
     */
    var stopMaxFunEvals = Long.MAX_VALUE // it is not straight forward to set a dimension dependent
    // default as the user can first set stopMaxFunEvals
    // and afterwards the dimension
    /**
     * stop if the number of iterations (generations) exceed stopMaxIter
     */
    var stopMaxIter = Long.MAX_VALUE

    /**
     * if true stopping message "Manual:..." is generated
     */
    var stopnow = false
    /**
     * flag used by methods iterate(), whether to write output to files.
     * Methods write an output file if flgWriteFile&#62;0.
     */
    /**
     * determines whether CMA says hello after initialization.
     *
     * @see CMAEvolutionStrategy.helloWorld
     */
    var verbosity = 1

    /**
     * Output files written will have the names outputFileNamesPrefix*.dat
     */
    var outputFileNamesPrefix = "outcmaes"

    /**
     * if chosen > 0 the console output from functions `print...` is saved
     * additionally into a file, by default <tt>outcmaesdisp.dat</tt>
     */
    var writeDisplayToFile = 1

    /**
     * only for >= 1 results are always exactly reproducible, as otherwise the update of the
     * eigensystem is conducted depending on time measurements, defaut is 0.2
     */
    var maxTimeFractionForEigendecomposition = 0.2

    /**
     * default is 0.1
     */
    var maxTimeFractionForWriteToDefaultFiles = 0.1

    /**
     * checks eigendecomposition mainly for debugging purpose, default is 0==no-check;
     * the function checkEigenSystem requires O(N^3) operations.
     */
    var checkEigenSystem = 0

    /**
     * This is the only place where the reading of a new option needs to be declared
     *
     * @param properties
     */
    fun setOptions(properties: Properties) {
        var s: String
        diagonalCovarianceMatrix =
            getFirstToken(properties.getProperty("diagonalCovarianceMatrix"), diagonalCovarianceMatrix)
        if (properties.getProperty("stopFitness").also { s = it } != null) stopFitness =
            java.lang.Double.valueOf(getFirstToken(s))
        stopTolFun = getFirstToken(properties.getProperty("stopTolFun"), stopTolFun)
        stopTolFunHist = getFirstToken(properties.getProperty("stopTolFunHist"), stopTolFunHist)
        stopTolX = getFirstToken(properties.getProperty("stopTolX"), stopTolX)
        stopTolXFactor = getFirstToken(properties.getProperty("stopTolXFactor"), stopTolXFactor)
        stopTolUpXFactor = getFirstToken(properties.getProperty("stopTolUpXFactor"), stopTolUpXFactor)
        stopMaxFunEvals = getFirstToken(properties.getProperty("stopMaxFunEvals"), stopMaxFunEvals)
        stopMaxIter = getFirstToken(properties.getProperty("stopMaxIter"), stopMaxIter)
        if (properties.getProperty("upperStandardDeviations")
                .also { s = it } != null && s != ""
        ) upperStandardDeviations = parseDouble(getAllToken(s))
        if (properties.getProperty("lowerStandardDeviations")
                .also { s = it } != null && s != ""
        ) lowerStandardDeviations = parseDouble(getAllToken(s))
        outputFileNamesPrefix =
            properties.getProperty("outputFileNamesPrefix", outputFileNamesPrefix).split("\\s").toTypedArray()[0]
        maxTimeFractionForEigendecomposition = getFirstToken(
            properties.getProperty("maxTimeFractionForEigendecomposition"),
            maxTimeFractionForEigendecomposition
        )
        maxTimeFractionForWriteToDefaultFiles = getFirstToken(
            properties.getProperty("maxTimeFractionForWriteToDefaultFiles"),
            maxTimeFractionForWriteToDefaultFiles
        )
        stopnow = "now" == getFirstToken(properties.getProperty("stop"))
        writeDisplayToFile = getFirstToken(properties.getProperty("writeDisplayToFile"), writeDisplayToFile)
        checkEigenSystem = getFirstToken(properties.getProperty("checkEigenSystem"), checkEigenSystem)
    }

    /**
     * Returns the double value of the first token of a string s or the default,
     * if the string is null or empty. This method should become generic with respect to the
     * type of second argument.
     *
     * @param s   string where the first token is read from
     * @param def double default value, in case the string is empty
     */
    private fun getFirstToken(s: String?, def: Double): Double {
        if (s == null) return def
        val ar = s.split("\\s+").toTypedArray()
        return if (ar[0] == "") def else java.lang.Double.valueOf(ar[0])
    }

    /**
     * should become generic with type argument?
     */
    fun getFirstToken(s: String?): String {
        if (s == null) return ""
        val ar: Array<String> = s.split("\\s+").toTypedArray()
        return ar[0]
    }

    /**
     * Returns the Integer value of the first token of a string s or the default,
     * if the string is null or empty. This method should become generic with respect to the
     * type of second argument.
     *
     * @param s   string where the first token is read from
     * @param def Integer default value, in case the string is empty
     */
    private fun getFirstToken(s: String?, def: Int): Int {
        if (s == null) return def
        val ar = s.split("\\s+").toTypedArray()
        return if (ar[0] == "") def else Integer.valueOf(ar[0])
    }

    //    public <T> T getFirstToken(String s, T def) {
    //        if (s == null)
    //            return def;
    //        String[] ar = s.split("\\s+");
    //        if (ar[0].equals(""))
    //            return def;
    //        return (T)(ar[0]); /* this fails */
    //    }
    private fun removeComments(s: String): String {
        var sv = s
        // remove trailing comments
        var i: Int = sv.indexOf("#")
        if (i >= 0) sv = sv.substring(0, i)
        i = sv.indexOf("!")
        if (i >= 0) sv = sv.substring(0, i)
        i = sv.indexOf("%")
        if (i >= 0) sv = sv.substring(0, i)
        i = sv.indexOf("//")
        if (i >= 0) sv = sv.substring(0, i)
        return sv
    }

    /**
     * Returns def if s==null or empty, code dublicate, should become generic
     */
    private fun getFirstToken(s: String?, def: Long): Long {
        if (s == null) return def
        val ar = removeComments(s).split("\\s+").toTypedArray()
        return if (ar[0] == "") def else java.lang.Long.valueOf(ar[0])
    }

    fun getAllToken(s: String): Array<String?> {
        // split w.r.t. white spaces regexp \s+
        return removeComments(s).split("\\s+").toTypedArray()
    }

    fun parseDouble(ars: Array<String?>?): DoubleArray {
        val ard = DoubleArray(ars!!.size)
        for (i in ars.indices) {
            ard[i] = ars[i]!!.toDouble()
        }
        return ard
    }

    companion object {
        // needs to be public to make sure that a using class can excess Options.
        // Therefore, if not nested, needs to move into a separate file
        private const val serialVersionUID = 2255162105325585121L
    }
}