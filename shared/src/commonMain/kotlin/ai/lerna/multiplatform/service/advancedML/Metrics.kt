package ai.lerna.multiplatform.service.advancedML
import kotlin.math.round

class Metrics {

    private var labels = mutableListOf<Int>()

    private var predictions = mutableListOf<Double>()

    fun rocCurveScratch(yLabels: List<Int>, yScores: List<Double>): Triple<List<Double>, List<Double>, List<Double>> {
        val thresholds = (0..10).map { it.toDouble() / 10 }
        val fprs = mutableListOf<Double>()
        val tprs = mutableListOf<Double>()

        for (thres in thresholds) {
            val (tp, tn, fp, fn) = confusionMatrix(yLabels, yScores, thres)
            tprs.add(tpr(tp, tn, fp, fn))
            fprs.add(fpr(tp, tn, fp, fn))
        }

        // force first and last fpr, tpr at 0 and 1 thresholds
        fprs[0] = 1.0
        fprs[fprs.size - 1] = 0.0
        tprs[0] = 1.0
        tprs[tprs.size - 1] = 0.0

        return Triple(fprs, tprs, thresholds)
    }

    fun confusionMatrix(yLabels: List<Int>, yScores: List<Double>, thres: Double): List<Int> {
        val yPreds = yScores.map { if (it >= thres) 1 else 0 }
        val tp = (yLabels.zip(yPreds) { label, pred -> if (label == 1 && pred == 1) 1 else 0 }).sum()
        val tn = (yLabels.zip(yPreds) { label, pred -> if (label == 0 && pred == 0) 1 else 0 }).sum()
        val fp = (yLabels.zip(yPreds) { label, pred -> if (label == 0 && pred == 1) 1 else 0 }).sum()
        val fn = (yLabels.zip(yPreds) { label, pred -> if (label == 1 && pred == 0) 1 else 0 }).sum()

        return listOf(tp, tn, fp, fn)
    }

    fun tpr(tp: Int, tn: Int, fp: Int, fn: Int): Double {
        return tp.toDouble() / (tp + fn).toDouble()
    }

    fun fpr(tp: Int, tn: Int, fp: Int, fn: Int): Double {
        return fp.toDouble() / (fp + tn).toDouble()
    }

    fun aucScore(): Double {

        val (fprs, tprs, thresholds) = rocCurveScratch(labels, predictions)
        var totalAuc = 0.0

        for (i in 0 until 10) {
            totalAuc += (fprs[i] - fprs[i + 1]) * ((tprs[i + 1] + tprs[i]) / 2.0)
        }

        return round(totalAuc * 1000) / 1000 // rounding to 3 decimal places
    }

    fun append(yTrue: Int, yPred: Double) {
        labels.add(yTrue)
        predictions.add(yPred)
    }

    fun reset() {
        labels.clear()
        predictions.clear()
    }
}