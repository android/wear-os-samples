@file:OptIn(ExperimentalMetricApi::class, ExperimentalPerfettoTraceProcessorApi::class)
@file:Suppress("SEALED_INHERITOR_IN_DIFFERENT_MODULE", "SEALED_INHERITOR_IN_DIFFERENT_PACKAGE", "SEALED_SUPERTYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER", "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER")

package androidx.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.Metric
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi

import androidx.benchmark.perfetto.PerfettoTraceProcessor

class DummyMetric: Metric() {
    override fun getResult(
        captureInfo: CaptureInfo,
        traceSession: PerfettoTraceProcessor.Session
    ): List<Measurement> {
        return listOf(Measurement("XYZ", 10.0))
    }

    override fun configure(packageName: String) {
    }

    override fun start() {
    }

    override fun stop() {
    }
}
