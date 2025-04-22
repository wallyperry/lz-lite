package run.perry.lz.utils

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.graphics.scale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object BitmapBlurHelper {

    /**
     * 对Bitmap进行高斯模糊处理（兼容HARDWARE配置）
     * @param original 原始位图
     * @param radius 模糊半径 (1-25)
     * @param scale 缩放因子 (0.1f-1f) 建议0.2f-0.5f提高性能
     */
    fun blur(
        original: Bitmap,
        @IntRange(from = 1, to = 25) radius: Int,
        @FloatRange(from = 0.1, to = 1.0) scale: Float = 0.5f
    ): Bitmap? {
        if (radius < 1) return null

        // 1. 处理HARDWARE配置并转换为可编辑格式
        // 处理不同Android版本的配置问题
        val config = when {
            // 检查是否是HARDWARE配置（API 26+）
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && original.config == Bitmap.Config.HARDWARE -> {
                Bitmap.Config.ARGB_8888
            }
            // 处理其他情况
            original.config == null -> Bitmap.Config.ARGB_8888
            else -> original.config
        }

        // 2. 先缩放图片提高性能
        val scaledWidth = (original.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (original.height * scale).toInt().coerceAtLeast(1)

        val workBitmap = try {
            (original.copy(config!!, true) ?: return null).scale(scaledWidth, scaledHeight)
        } catch (e: Exception) {
            return null
        }

        // 3. 执行模糊算法
        return doBlur(workBitmap, radius)?.let {
            // 模糊后恢复到原尺寸（如果需要）
            if (scale != 1f) {
                it.scale(original.width, original.height)
            } else {
                it
            }
        }
    }

    private fun doBlur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (radius < 1) return null

        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)

        try {
            bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        } catch (e: Exception) {
            return null
        }

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)

        var rSum: Int
        var gSum: Int
        var bSum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw = 0

        val vMin = IntArray(max(w, h))

        var divSum = (div + 1) shr 1
        divSum *= divSum

        val dv = IntArray(256 * divSum)
        for (i in dv.indices) {
            dv[i] = i / divSum
        }

        yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackPointer: Int
        var stackStart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routSum: Int
        var goutSum: Int
        var boutSum: Int
        var rinSum: Int
        var ginSum: Int
        var binSum: Int

        for (y in 0 until h) {
            rinSum = 0
            ginSum = 0
            binSum = 0
            routSum = 0
            goutSum = 0
            boutSum = 0
            rSum = 0
            gSum = 0
            bSum = 0

            for (i in -radius..radius) {
                p = pix[yi + min(wm, max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - abs(i)
                rSum += sir[0] * rbs
                gSum += sir[1] * rbs
                bSum += sir[2] * rbs

                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
            }

            stackPointer = radius

            for (x in 0 until w) {
                r[yi] = dv[rSum]
                g[yi] = dv[gSum]
                b[yi] = dv[bSum]

                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum

                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]

                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]

                if (y == 0) {
                    vMin[x] = min(x + radius + 1, wm)
                }
                p = pix[yw + vMin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff

                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]

                rSum += rinSum
                gSum += ginSum
                bSum += binSum

                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer % div]

                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]

                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]

                yi++
            }
            yw += w
        }

        for (x in 0 until w) {
            rinSum = 0
            ginSum = 0
            binSum = 0
            routSum = 0
            goutSum = 0
            boutSum = 0
            rSum = 0
            gSum = 0
            bSum = 0

            yp = -radius * w
            for (i in -radius..radius) {
                yi = max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - abs(i)
                rSum += r[yi] * rbs
                gSum += g[yi] * rbs
                bSum += b[yi] * rbs

                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
            }

            yi = x
            stackPointer = radius

            for (y in 0 until h) {
                pix[yi] = ((0xff000000.toInt() and pix[yi]) or
                          (dv[rSum] shl 16) or
                          (dv[gSum] shl 8) or
                          dv[bSum]).toInt()

                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum

                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]

                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]

                if (x == 0) {
                    vMin[y] = min(y + r1, hm) * w
                }
                p = x + vMin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]

                rSum += rinSum
                gSum += ginSum
                bSum += binSum

                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer]

                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]

                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]

                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}