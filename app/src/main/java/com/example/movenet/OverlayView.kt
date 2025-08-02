package com.example.movenet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.movenet.data.BodyPart
import com.example.movenet.data.KeyPoint
import kotlin.math.max

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var keypoints: List<KeyPoint> = listOf()
    private val pointPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        strokeWidth = 8f
    }
    private val linePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val bodyJoints = listOf(
        Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (keypoints.isEmpty()) return

        val width = this.width.toFloat()
        val height = this.height.toFloat()

        val keypointsMap = keypoints.associateBy { it.bodyPart }

        for (keypoint in keypoints) {
            if (keypoint.score > 0.3f) {
                canvas.drawCircle(
                    keypoint.coordinate.x * width,
                    keypoint.coordinate.y * height,
                    8f,
                    pointPaint
                )
            }
        }

        for (joint in bodyJoints) {
            val p1 = keypointsMap[joint.first]
            val p2 = keypointsMap[joint.second]
            if (p1 != null && p2 != null && p1.score > 0.3f && p2.score > 0.3f) {
                canvas.drawLine(
                    p1.coordinate.x * width,
                    p1.coordinate.y * height,
                    p2.coordinate.x * width,
                    p2.coordinate.y * height,
                    linePaint
                )
            }
        }
    }

    fun setKeypoints(keypoints: List<KeyPoint>) {
        this.keypoints = keypoints
        invalidate() // Redraw the view
    }
}
