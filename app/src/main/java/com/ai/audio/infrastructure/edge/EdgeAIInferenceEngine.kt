package com.ai.audio.infrastructure.edge

import android.content.Context
import com.ai.audio.infrastructure.model.AudioBuffer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.util.concurrent.Executors

class EdgeAIInferenceEngine private constructor(private val context: Context) {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null
    private val inferenceExecutor = Executors.newSingleThreadExecutor()

    companion object {
        @Volatile private var instance: EdgeAIInferenceEngine? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: EdgeAIInferenceEngine(context.applicationContext).also { instance = it }
        }
    }

    // ONNX মডেল লোড করা (Edge Optimization)
    fun initializeModel(modelPath: String, onComplete: (Boolean) -> Unit) {
        inferenceExecutor.execute {
            try {
                val modelBytes = context.assets.open(modelPath).readBytes()
                val options = OrtSession.SessionOptions().apply {
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                }
                ortSession = env.createSession(modelBytes, options)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    // সাব-১০০ms লেটেন্সি নিশ্চিত করতে অন-ডিভাইস ইনফারেন্স রান করা
    fun generateAudioEdge(latentVector: FloatArray, shape: LongArray, onResult: (AudioBuffer?) -> Unit) {
        inferenceExecutor.execute {
            if (ortSession == null) {
                onResult(null)
                return@execute
            }
            try {
                val startTime = System.currentTimeMillis()
                
                val floatBuffer = FloatBuffer.wrap(latentVector)
                val inputTensor = OnnxTensor.createTensor(env, floatBuffer, shape)
                val inputs = mapOf("input_latent" to inputTensor)

                ortSession?.use { session ->
                    val results = session.run(inputs)
                    val outputTensor = results[0].value as Array<FloatArray>
                    
                    val latency = System.currentTimeMillis() - startTime
                    // লেটেন্সি মনিটরিং এবং লগিং (কারেন্ট স্ট্যাটাস চেক)
                    android.util.Log.d("EdgeAI", "Inference Latency: ${latency}ms")
                    
                    val flatData = outputTensor.flatMap { it.asIterable() }.toFloatArray()
                    onResult(AudioBuffer(flatData, sampleRate = 48000))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}
