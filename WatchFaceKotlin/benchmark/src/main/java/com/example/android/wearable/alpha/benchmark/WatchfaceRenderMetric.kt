@file:OptIn(ExperimentalMetricApi::class, ExperimentalPerfettoTraceProcessorApi::class)

package com.example.android.wearable.alpha.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.TraceMetric
import androidx.benchmark.perfetto.ExperimentalPerfettoTraceProcessorApi
import androidx.benchmark.perfetto.PerfettoTraceProcessor

object WatchfaceRenderMetric : TraceMetric() {
    override fun getResult(
        captureInfo: CaptureInfo,
        traceSession: PerfettoTraceProcessor.Session
    ): List<Measurement> {
        val rows = traceSession.query(
            """
            SELECT
              slice.name as name,
              slice.ts as ts,
              slice.dur as dur
            FROM slice
              INNER JOIN thread_track on slice.track_id = thread_track.id
              INNER JOIN thread USING(utid)
              INNER JOIN process USING(upid)
            WHERE (
              ( slice.name LIKE "Choreographer#doFrame%" AND process.pid LIKE thread.tid AND slice.parent_stack_id = 0) OR
              ( slice.name LIKE "DrawFrame%" AND thread.name like "RenderThread" )
            ) AND (process.name = "${captureInfo.targetPackageName}")
            ORDER BY ts ASC
        """.trimIndent()
        )

        val (draw, render) = rows.partition { it.string("name").startsWith("DrawFrame") }

        val drawSamples = draw.map { it.long("dur").toDouble() / 1000000 }
        val renderSamples = render.map { it.long("dur").toDouble() / 1000000 }
        return listOf(
            Measurement("Draw", drawSamples.ifEmpty { listOf(0.0) }),
            Measurement("Render", renderSamples.ifEmpty { listOf(0.0) })
        )
    }
}
