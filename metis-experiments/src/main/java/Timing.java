/*
 * Copyright (C) Zijie Cong 2021
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Timing {

    private final StringBuilder output;
    private long previousDimension;
    private long cumulativeEvaluations;
    private long startTime;
    private final long overallStartTime;

    /**
     * Constructor
     */
    public Timing() {

        super();

        this.output = new StringBuilder();
        this.previousDimension = 0;
        this.cumulativeEvaluations = 0;
        this.startTime = System.nanoTime();
        this.overallStartTime = this.startTime;
    }

    /**
     * Keeps track of the total number of evaluations and elapsed time. Produces an output string when the
     * current problem is of a different dimension than the previous one or when null.
     */
    public void timeProblem(CocoProblem cocoProblem) {

        if ((cocoProblem == null) || (this.previousDimension != CocoJNI.cocoProblemGetDimension(cocoProblem.getPointer()))) {

            /* Output existing timing information */
            if (this.cumulativeEvaluations > 0) {
                long elapsedTime = System.nanoTime() - this.startTime;
                String elapsed = String.format(Locale.ENGLISH, "%.2e", elapsedTime / (1e+9) / (1.0 * this.cumulativeEvaluations));
                this.output
                        .append("d=")
                        .append(this.previousDimension)
                        .append(" done in ")
                        .append(elapsed)
                        .append(" seconds/evaluation\n");
            }

            if (cocoProblem != null) {
                /* Re-initialize the timing_data */
                this.previousDimension = CocoJNI.cocoProblemGetDimension(cocoProblem.getPointer());
                this.cumulativeEvaluations = CocoJNI.cocoProblemGetEvaluations(cocoProblem.getPointer());
                this.startTime = System.nanoTime();
            }

        } else {
            this.cumulativeEvaluations += CocoJNI.cocoProblemGetEvaluations(cocoProblem.getPointer());
        }
    }

    /**
     * Outputs the collected timing data.
     */
    public void output() {

        /* Record the last problem */
        timeProblem(null);

        long elapsedTime = System.nanoTime() - this.overallStartTime;
        long hours = TimeUnit.HOURS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        long minutes = TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS) - hours * 60;
        long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) - hours * 3600 - minutes * 60;
        String elapsed = String.format("Total elapsed time: %dh%02dm%02ds\n", hours, minutes, seconds);

        this.output.insert(0, "\n");
        this.output.append(elapsed);

        System.out.append(this.output.toString());

    }
}
