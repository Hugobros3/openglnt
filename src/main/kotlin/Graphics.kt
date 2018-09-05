import DemoShaders.basicShader
import org.joml.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign

class Graphics(val bufferedImage: BufferedImage) {
    val bufferSize = Vector2i(bufferedImage.width, bufferedImage.height)

    fun clear(clearColor: Vector4dc) {
        for (y in 0 until bufferSize.y)
            for (x in 0 until bufferSize.x)
                bufferedImage.setRGB(x, y, clearColor.toArgbInt())
    }

    fun point(position: Vector2ic, color: Vector4dc) {
        if (position.x() >= 0 && position.y() >= 0 && position.x() < bufferSize.x && position.y() < bufferSize.y)
            bufferedImage.setRGB(position.x(), position.y(), color.toArgbInt())
    }

    fun line(start: Vector2dc, end: Vector2dc, color: Vector4dc) = line(start.inViewport().toVector2i(), end.inViewport().toVector2i(), color)

    fun line(starti: Vector2ic, endi: Vector2ic, color: Vector4dc) {
        var pos = Vector2i(starti)
        val diff = endi - starti
        val step = diff.sign()
        when {
            diff.x.absoluteValue >= diff.y.absoluteValue -> {
                var err = 0.0
                val epp = diff.y.absoluteValue.toDouble() / diff.x.absoluteValue.toDouble()
                while (endi != starti) {
                    if (pos.x == endi.x())
                        break

                    err += epp
                    if (err >= 1.0) {
                        pos.y += step.y
                        err -= 1.0
                    }

                    pos.x += step.x
                    point(pos, color)

                }
            }
            diff.y.absoluteValue > diff.x.absoluteValue -> {
                var err = 0.0
                val epp = diff.x.absoluteValue.toDouble() / diff.y.absoluteValue.toDouble()
                while (endi != starti) {
                    if (pos.y == endi.y())
                        break

                    err += epp
                    if (err >= 1.0) {
                        pos.x += step.x
                        err -= 1.0
                    }

                    pos.y += step.y
                    point(pos, color)

                }
            }
        }
    }

    var shader = basicShader
    val checker = Texture(ImageIO.read(javaClass.getResource("/pepe.png")))

    fun triangle(a: Vertex, b: Vertex, c: Vertex, color: Vector4d) {
        val vertices = arrayOf(a, b, c)

        val ai = a.position.inViewport().toVector2i()
        val bi = b.position.inViewport().toVector2i()
        val ci = c.position.inViewport().toVector2i()

        val dbgColor = Vector4d(1.0, 0.0, 1.0, 1.0)

        val points = arrayOf(ai, bi, ci)
        val sorted = points.sortedBy { it.y() }

        val top = sorted[0]
        val mid = sorted[1]
        val bot = sorted[2]

        //println("top $top mid $mid bot $bot")

        val cut = Vector2i(bot.x + ((top.x - bot.x) / (top.y - bot.y).toDouble() * (mid.y - bot.y)).roundToInt(), mid.y)

        val midleft = if (cut.x <= mid.x) cut else mid
        val midright = if (cut.x <= mid.x) mid else cut

        //Top part
        for (y in top.y..mid.y) {
            val scale: Double = (mid.y - y).toDouble() / (top.y - mid.y).toDouble()
            val sx: Int = midleft.x + ((midleft.x - top.x) * scale).toInt()
            val ex: Int = midright.x + ((midright.x - top.x) * scale).toInt()

            for(x in sx..ex) {
                val p = Vector2i(x, y)
                val bary = barycentric(points, p)

                val texcoord = extractAndInterpolate(vertices, bary) { this.textureCoordinate }

                //val color = Vector4d(bary.x, bary.y, bary.z, 1.0)
                val color = checker.sample(texcoord)

                point(p, color)
            }
            //line(Vector2i(sx, y), Vector2i(ex, y), color)
        }
        //Bottom part
        for (y in mid.y..bot.y) {
            val scale: Double = (y - mid.y).toDouble() / (mid.y - bot.y).toDouble()
            val sx: Int = midleft.x + ((midleft.x - bot.x) * scale).toInt()
            val ex: Int = midright.x + ((midright.x - bot.x) * scale).toInt()

            for(x in sx..ex) {
                val p = Vector2i(x, y)
                val bary = barycentric(points, p)

                val texcoord = extractAndInterpolate(vertices, bary) { this.textureCoordinate }

                //val color = Vector4d(bary.x, bary.y, bary.z, 1.0)
                val color = checker.sample(texcoord)

                point(p, color)
            }
            //line(Vector2i(sx, y), Vector2i(ex, y), color)
        }

        /*line(ai, bi, dbgColor)
        line(ai, ci, dbgColor)
        line(ci, bi, dbgColor)
        line(cut, mid, dbgColor)*/
    }

    fun Vector2dc.inViewport() = Vector2d(x() * bufferSize.x, y() * bufferSize.y)

    fun Vector4dc.inViewport() = Vector2d(x() * bufferSize.x, y() * bufferSize.y)

    fun extractAndInterpolate(points: Array<Vertex>, bary: Vector3d, extractor: Vertex.() -> Vector2dc) : Vector2d{
        val acc = Vector2d()

        acc += Vector2d(extractor.invoke(points[0])).mul(bary.x)
        acc += Vector2d(extractor.invoke(points[1])).mul(bary.y)
        acc += Vector2d(extractor.invoke(points[2])).mul(bary.z)

        return acc
    }
}

operator fun Vector2d.plusAssign(mul: Vector2d?) {
    this.add(mul)
}
