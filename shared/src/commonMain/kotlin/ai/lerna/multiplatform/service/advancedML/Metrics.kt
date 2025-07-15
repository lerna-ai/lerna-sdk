package ai.lerna.multiplatform.service.advancedML
import kotlin.math.round

class Metrics {

    private var labels = mutableListOf<Int>()

    private var predictions = mutableListOf<Float>()

    fun rocCurveScratch(yLabels: List<Int>, yScores: List<Float>): Triple<List<Float>, List<Float>, List<Float>> {
        val thresholds = (0..10).map { it.toFloat() / 10 }
        val fprs = mutableListOf<Float>()
        val tprs = mutableListOf<Float>()

        for (thres in thresholds) {
            val (tp, tn, fp, fn) = confusionMatrix(yLabels, yScores, thres)
            tprs.add(tpr(tp, tn, fp, fn))
            fprs.add(fpr(tp, tn, fp, fn))
        }

        // force first and last fpr, tpr at 0 and 1 thresholds
        fprs[0] = 1.0f
        fprs[fprs.size - 1] = 0.0f
        tprs[0] = 1.0f
        tprs[tprs.size - 1] = 0.0f

        return Triple(fprs, tprs, thresholds)
    }

    fun confusionMatrix(yLabels: List<Int>, yScores: List<Float>, thres: Float): List<Int> {
        val yPreds = yScores.map { if (it >= thres) 1 else 0 }
        val tp = (yLabels.zip(yPreds) { label, pred -> if (label == 1 && pred == 1) 1 else 0 }).sum()
        val tn = (yLabels.zip(yPreds) { label, pred -> if (label == 0 && pred == 0) 1 else 0 }).sum()
        val fp = (yLabels.zip(yPreds) { label, pred -> if (label == 0 && pred == 1) 1 else 0 }).sum()
        val fn = (yLabels.zip(yPreds) { label, pred -> if (label == 1 && pred == 0) 1 else 0 }).sum()

        return listOf(tp, tn, fp, fn)
    }

    fun tpr(tp: Int, tn: Int, fp: Int, fn: Int): Float {
        return tp.toFloat() / (tp + fn).toFloat()
    }

    fun fpr(tp: Int, tn: Int, fp: Int, fn: Int): Float {
        return fp.toFloat() / (fp + tn).toFloat()
    }

    fun aucScore(): Float {

        val (fprs, tprs, thresholds) = rocCurveScratch(labels, predictions)
        var totalAuc = 0.0f

        for (i in 0 until 10) {
            totalAuc += (fprs[i] - fprs[i + 1]) * ((tprs[i + 1] + tprs[i]) / 2.0f)
        }

        return round(totalAuc * 1000) / 1000 // rounding to 3 decimal places
    }

    fun append(yTrue: Int, yPred: Float) {
        labels.add(yTrue)
        predictions.add(yPred)
    }

    fun reset() {
        labels.clear()
        predictions.clear()
    }
}