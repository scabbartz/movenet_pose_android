package com.example.movenet

import android.graphics.PointF
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.movenet.data.BodyPart
import com.example.movenet.data.KeyPoint
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MoveNetAnalyzer(
    private val interpreter: Interpreter,
    private val overlayView: OverlayView,
    private val jsonTextView: TextView
) : ImageAnalysis.Analyzer {

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()
        if (bitmap == null) {
            image.close()
            return
        }

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        val outputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 1, 17, 3), DataType.FLOAT32)

        interpreter.run(inputFeature0.buffer, outputFeature0.buffer)

        val keypoints = parseOutput(outputFeature0.floatArray)

        overlayView.post {
            overlayView.setKeypoints(keypoints)
        }

        jsonTextView.post {
            jsonTextView.text = keypointsToJson(keypoints)
        }

        image.close()
    }

    private fun parseOutput(array: FloatArray): List<KeyPoint> {
        val keypoints = mutableListOf<KeyPoint>()
        for (i in 0 until 17) {
            val y = array[i * 3 + 0]
            val x = array[i * 3 + 1]
            val score = array[i * 3 + 2]
            keypoints.add(KeyPoint(BodyPart.fromInt(i), PointF(x, y), score))
        }
        return keypoints
    }

    private fun keypointsToJson(keypoints: List<KeyPoint>): String {
        val json = JSONObject()
        val keypointsArray = JSONArray()
        for (keypoint in keypoints) {
            if (keypoint.score > 0.3) { // Only include keypoints with a decent score
                val obj = JSONObject()
                obj.put("name", keypoint.bodyPart.name)
                obj.put("x", keypoint.coordinate.x)
                obj.put("y", keypoint.coordinate.y)
                obj.put("score", keypoint.score)
                keypointsArray.put(obj)
            }
        }
        json.put("keypoints", keypointsArray)
        return json.toString(4) // Indent for readability
    }
}
