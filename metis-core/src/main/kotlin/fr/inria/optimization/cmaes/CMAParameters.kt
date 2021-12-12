@file:Suppress("KDocUnresolvedReference")

package fr.inria.optimization.cmaes

import java.io.Serializable
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sqrt

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
 * Interface to strategy parameters for the CMA Evolution
 * Strategy, most importantly the population size lambda, while the change
 * of other parameters is discouraged.
 * The class CMAParameters processes the
 * strategy parameters, like population size and learning rates, for
 * the class [CMAEvolutionStrategy] where the public field `parameters` of
 * type `CMAParameters` can
 * be used to set the parameter values. The method [.supplementRemainders]
 * supplements those parameters that were not explicitly given,
 * regarding dependencies
 * (eg, the parent number, mu, cannot be larger than the
 * population size lambda) and does a respective consistency checking via method
 * [.check].
 * Parameters cannot be changed after CMAEvolutionStrategy method init()
 * was called.
 * <P> Example code snippet:</P>
 * <PRE>
 * CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
 * cma.parameters.setPopulationSize(33); // set lambda
 * int mu = cma.parameters.getMu(); // will fail as mu was not set and missing
 * // parameters were not supplemented yet
 * cma.readProperties();         // read necessary initial values, might overwrite lambda
 * mu = cma.parameters.getMu();  // might still fail
 * cma.init();                   // finalize initialization, supplement missing parameters
 * mu = cma.parameters.getMu();  // OK now
 * cma.parameters.setMu(4);      // runtime error, parameters cannot be changed after init()
</PRE> *
 *
 * <P>Most commonly, the offspring population size lambda can be changed
 * (increased) from its default value via setPopulationSize to improve the
 * global search capability, see file CMAExample2.java. It is recommended to use the default
 * values first! </P>
 *
 * @see CMAEvolutionStrategy.readProperties
 */
class CMAParameters : Serializable {
    var supplemented // after supplementation, it is undecidable whether a parameter was
            = 0

    // explicitly set from outside, therefore another supplementation is not advisable
    var locked // lock when lambda is used to new data structures
            = 0
    var lambda /* -> mu, <- N */ = 0
    var mu /* -> weights, (lambda) */ = 0
    var mucov: Double/* -> ccov */

    /**
     * Getter for property mueff, the "variance effective selection mass".
     *
     * @return Value of property mueff.
     */
    var mueff /* <- weights */ = 0.0

    /**
     * Getter for property weights.
     *
     * @return Value of property weights.
     */
    var weights /* <- mu, -> mueff, mucov, ccov */: DoubleArray? = null
    var damps /* <- cs, maxeval, lambda */ = 0.0
    var cs /* -> damp, <- N */ = 0.0
    /**
     * Getter for backward time horizon parameter cc for
     * distribution cumulation (for evolution path
     * p<sub>c</sub>).
     *
     * @return Value of cc.
     */
    /**
     * Setter for cc to default value.
     */
    var cc /* <- N */ = 0.0
    /**
     * Getter for property covariance matrix learning rate ccov
     *
     * @return Value of property ccov.
     */// can be set anytime, cave: switching from diagonal to full cov
    /**
     * Setter for covariance matrix learning rate ccov. For ccov=0 no covariance
     * matrix adaptation takes place and only <EM>Cumulation Step-Size
     * Adaptation (CSA)</EM> is conducted, also know as <EM>Path Length Control</EM>.
     *
     * @param ccov New value of property ccov.
     * @see .getCcov
     */
    var ccov /* <- mucov, <- N, <- diagonalcov */: Double
    var ccovsep /* <- ccov */ = 0.0
    var chiN = 0.0
    var recombinationType = RecombinationType.Superlinear // otherwise null

    init {
        mucov = -1.0
        ccov = -1.0
    }

    /**
     * Checks strategy parameter setting with respect to principle
     * consistency. Returns a string with description of the first
     * error found, otherwise an empty string "".
     */
    private fun check(): String {
        if (lambda <= 1) return "offspring population size lambda must be greater than onem is $lambda"
        if (mu < 1) return "parent number mu must be greater or equal to one, is $mu"
        if (mu > lambda) return "parent number mu $mu must be smaller or equal to offspring population size lambda $lambda"
        if (weights!!.size != mu) return "number of recombination weights " + weights!!.size + " disagrees with parent number mu " + mu
        if (cs <= 0 || cs > 1) return "0 < cs <= 1 must hold for step-size cumulation parameter cs, is $cs"
        if (damps <= 0) return "step-size damping parameter damps must be greater than zero, is $damps"
        if (cc <= 0 || cc > 1) return "0 < cc <= 1 must hold for cumulation parameter cc, is $cc"
        if (mucov < 0) return "mucov >= 0 must hold, is $mucov"
        return if (ccov < 0) "learning parameter ccov >= 0 must hold, is $ccov" else ""
    }

    /**
     * get default parameter setting depending on given dimension N
     *
     * @param N dimension
     * @return default parameter setting
     * @see .getDefaults
     */
    fun getDefaults(N: Int): CMAParameters {
        if (N == 0) error("default parameters needs dimension been set")
        val p = CMAParameters()
        p.supplementRemainders(N, CMAOptions())
        return p
    }

    /**
     * get default parameter setting depending on dimension N and
     * population size lambda. Code snippet to get, for example, the default parent
     * number value mu (weighted recombination is default):
     *
     * <PRE>
     * int default_mu_for_dimension_42 = new CMAParameters().getDefaults(42).getMu();
     *
     *
     * CMAEvolutionStrategy cma = new CMAEvolutionStrategy(42);
     * int the_same_most_convenient = cma.getParameterDefaults().getMu();
     * int also_the_same = cma.getParameterDefaults(42).getMu();
    </PRE> *
     *
     * @param N
     * @param lambda
     * @return default parameter setting
     * @see .getDefaults
     */
    fun getDefaults(N: Int, lambda: Int): CMAParameters {
        val p = CMAParameters()
        p.setLambda(lambda)
        p.supplementRemainders(N, CMAOptions())
        return p
    }

    /**
     * Supplements all default parameter values that were not explicitly set already.
     * Also checks whether the values that were already explicitly set are fine.
     *
     * @param N    search space dimension
     * @param opts [CMAOptions] where stopMaxFunEvals and
     * stopMaxIter are used to set step-size damping parameter damps. This is of minor relevance.
     */
    fun supplementRemainders(N: Int, opts: CMAOptions) {
        // parameters that can be zero were initialized to -1
        if (supplemented > 0) error("defaults cannot be supplemented twice")
        if (N == 0) error("dimension must be greater than zero")
        supplemented = 1
        locked = 1
        chiN = (sqrt(N.toDouble())
                * (1.0 - 1.0 / (4.0 * N) + 1.0 / (21.0 * N * N)))

        // set parameters to their default if they were not set before
        if (lambda <= 0) lambda = (4.0 + 3.0 * ln(N.toDouble())).toInt()
        if (mu <= 0) mu = floor(lambda / 2.0).toInt()
        if (weights == null) setWeights(mu, recombinationType) else if (weights!!.isEmpty()) setWeights(
            mu,
            recombinationType
        )
        if (cs <= 0) cs = (mueff + 2) / (N + mueff + 3)
        if (damps <= 0) damps = ((1 + 2 * 0.0.coerceAtLeast(sqrt((mueff - 1.0) / (N + 1.0)) - 1))
                * 0.3.coerceAtLeast(1 - N / (1e-6 + opts.stopMaxIter.coerceAtMost(opts.stopMaxFunEvals / lambda)))
                + cs)
        if (cc <= 0) cc = 4.0 / (N + 4.0)
        if (mucov < 0) mucov = mueff
        if (ccov < 0) { // TODO: setting should depend on gendiagonalcov
            ccov = (2.0 / (N + 1.41) / (N + 1.41) / mucov
                    + (1 - 1.0 / mucov)
                    * 1.0.coerceAtMost((2 * mueff - 1) / (mueff + (N + 2) * (N + 2))))
            ccovsep = 1.0.coerceAtMost(ccov * (N + 1.5) / 3.0)
        }

        // check everything
        val s = check()
        if (s != "") error(s) // if any prior setting does not work
    } // supplementRemainders

    /**
     * Getter for property mu.
     *
     * @return Value of property mu.
     */
    @JvmName("getMu1")
    fun getMu(): Int {
        return mu
    }

    /**
     * Getter for offspring population size lambda, no check, whether lambda was already set properly
     *
     * @return Value of lambda
     */
    @JvmName("getLambda1")
    fun getLambda(): Int {
        return lambda
    }

    /**
     * Setter for offspring population size alias sample size
     * alias lambda, use setPopulationSize() for outside use.
     *
     * @param lambda set population size
     * @see .setPopulationSize
     */
    @JvmName("setLambda1")
    fun setLambda(lambda: Int) {
        if (locked != 0) error("parameters cannot be set anymore")
        this.lambda = lambda
    }
    /**
     * @see .getLambda
     */
    /**
     * Setter for offspring population size (lambda). If (only) lambda is
     * set, other parameters, eg. mu and recombination weights and
     * subsequently learning rates for the covariance matrix etc. are
     * chosen accordingly
     *
     * @param lambda is the offspring population size
     */
    var populationSize: Int
        get() = getLambda()
        set(lambda) {
            setLambda(lambda)
        }

    /**
     * normalizes recombination weights vector and sets mueff
     */
    @JvmName("setWeights1")
    private fun setWeights(weights: DoubleArray) {
        assert(locked == 0)
        var sum = 0.0
        for (i in weights.indices) sum += weights[i]
        for (i in weights.indices) weights[i] /= sum
        this.weights = weights
        // setMu(weights.length);
        var sum1 = 0.0
        var sum2 = 0.0
        for (i in 0 until mu) {
            sum1 += weights[i]
            sum2 += weights[i] * weights[i]
        }
        mueff = sum1 * sum1 / sum2
    }

    /**
     * Setter for recombination weights
     *
     * @param mu is the number of parents, number of weights > 0
     */
    private fun setWeights(mu: Int, recombinationType: RecombinationType) {
        val w = DoubleArray(mu)
        when (recombinationType) {
            RecombinationType.Equal -> for (i in 0 until mu) w[i] = 1.0
            RecombinationType.Linear -> for (i in 0 until mu) w[i] = (mu - i).toDouble()
            else -> for (i in 0 until mu) w[i] =
                ln((mu + 1).toDouble()) - ln((i + 1).toDouble())// default, seems as enums can be null
        }
        setWeights(w)
    }

    /**
     * Getter for property mucov. mucov determines the
     * mixing between rank-one and rank-mu update. For
     * mucov = 1, no rank-mu updated takes place.
     *
     * @return Value of property mucov.
     */
    @JvmName("getMucov1")
    fun getMucov(): Double {
        return mucov
    }

    /**
     * Getter for property covariance matrix learning rate ccov
     *
     * @param flgdiag boolean, true for getting the learning rate when
     * only the diagonal of the covariance matrix is updated
     * @return Value of property ccov.
     */
    fun getCcov(flgdiag: Boolean): Double {
        return if (flgdiag) ccovsep else ccov
    }

    /**
     * Getter for step-size damping damps.  The damping damps
     * determines the amount of step size change.
     *
     * @return Value of damps.
     */
    @JvmName("getDamps1")
    fun getDamps(): Double {
        return damps
    }

    /**
     * Getter for cs, parameter for the backward time horizon for the cumulation for sigma.
     *
     * @return Value of property cs.
     */
    @JvmName("getCs1")
    fun getCs(): Double {
        return cs
    }

    private fun error(s: String) { // somehow a relict from the C history of this code
        println(" CMA-ES error: $s")
        throw CMAEvolutionStrategy().CMAException(" CMA-ES error: $s") // TODO this looks like a real hack
        //      System.exit(-1);
    }

    enum class RecombinationType {
        Superlinear, Linear, Equal
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = -1305062342816588003L
    }
}