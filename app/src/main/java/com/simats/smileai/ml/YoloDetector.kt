package com.simats.smileai.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import java.nio.ByteBuffer
import java.nio.ByteOrder

class YoloDetector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "tooth_detection.tflite"

    // Labels for YOLOv8 model (FDI Numbering)
    private val labels = listOf(
        "11", "12", "13", "14", "15", "16", "17", "18",
        "21", "22", "23", "24", "25", "26", "27", "28",
        "31", "32", "327", "33", "34", "35", "36", "37", "38",
        "41", "42", "43", "44", "45", "46", "47", "48", "56"
    )

    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (interpreter == null) return emptyList()

        // Input shape for YOLOv8 is [1, 640, 640, 3]
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(640, 640, ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(interpreter!!.getInputTensor(0).dataType())
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Output shape for YOLOv8-s is usually [1, 38, 8400] 
        // (4 box coords + 34 classes = 38 rows)
        val outputShape = interpreter!!.getOutputTensor(0).shape() // [1, 38, 8400]
        val outputBuffer = ByteBuffer.allocateDirect(outputShape[1] * outputShape[2] * 4)
            .order(ByteOrder.nativeOrder())
        
        interpreter!!.run(tensorImage.buffer, outputBuffer)
        outputBuffer.rewind()
        
        val results = mutableListOf<DetectionResult>()
        val data = FloatArray(outputShape[1] * outputShape[2])
        outputBuffer.asFloatBuffer().get(data)

        // Simplified Parsing for FDI Numbering (Threshold 0.4)
        for (i in 0 until 8400) {
            var maxConf = -1f
            var maxIdx = -1
            
            // Classes start from index 4
            for (j in 4 until 38) {
                val conf = data[j * 8400 + i]
                if (conf > maxConf) {
                    maxConf = conf
                    maxIdx = j - 4
                }
            }

            if (maxConf > 0.4f) {
                results.add(DetectionResult(labels[maxIdx], maxConf))
            }
        }

        return results.sortedByDescending { it.confidence }.distinctBy { it.label }
    }

    data class DetectionResult(val label: String, val confidence: Float)

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
