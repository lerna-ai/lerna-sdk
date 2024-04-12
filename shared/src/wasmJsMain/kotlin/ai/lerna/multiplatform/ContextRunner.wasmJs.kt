package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2

actual class ContextRunner actual constructor() {

    actual fun run(
        context: KMMContext,
        runPeriodic: KSuspendFunction0<Unit>
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            runPeriodic()
        }
    }


    actual fun run(
        context: KMMContext,
        strVal1: String,
        strVal2: String,
        runInContext: KSuspendFunction2<String, String, Unit>
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            runInContext(strVal1, strVal2)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    actual fun runBlocking(
        context: KMMContext,
        modelName: String,
        runPeriodic: KSuspendFunction1<String, Unit>
    ) {
        GlobalScope.promise {
            runPeriodic(modelName)
        }

    }


    @OptIn(DelicateCoroutinesApi::class)
    actual fun runBlocking(
        context: KMMContext,
        runPeriodic: KSuspendFunction0<Unit>
    ) {
        GlobalScope.promise {
            runPeriodic()
        }
    }

}