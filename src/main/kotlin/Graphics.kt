import org.joml.*
import javax.imageio.ImageIO
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

class Graphics(val context: DSGContext) {
    val bufferedImage = context.image
    val bufferSize = Vector2i(bufferedImage.width, bufferedImage.height)

    lateinit var shader: Shader<Vertex, IntermediaryData>
        private set

    lateinit private var vsCtx: ActualVertexShaderCtx<Vertex>
    lateinit private var fsCtx: ActualFragmentShaderCtx<IntermediaryData>

    var drawMode = DrawMode.SHADER

    val checker = Texture(ImageIO.read(javaClass.getResource("/checker.png")))

    enum class DrawMode {
        FLAT_COLOR,
        TEXTURED,
        SHADER,
    }

    fun usingShader(shader: Shader<*, *>) {
        this.shader = shader as Shader<Vertex, IntermediaryData>
        vsCtx = shader.createVertexCtx()
        fsCtx = shader.createFragmentCtx()
    }

    fun clear(clearColor: Vector4dc) {
        for (y in 0 until bufferSize.y)
            for (x in 0 until bufferSize.x) {
                bufferedImage.setRGB(x, y, clearColor.toArgbInt())
                context.zbuffer[x * bufferSize.x + y] = Float.MAX_VALUE
            }
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

    fun triangle(a: Vertex, b: Vertex, c: Vertex, color: Vector4d) {
        var color = color
        val vertices = arrayOf(a, b, c)

        vsCtx.vertex = a
        val oa = shader.vertexShader.invoke(vsCtx)
        oa.position.normHomogeneous()

        vsCtx.vertex = b
        val ob = shader.vertexShader.invoke(vsCtx)
        ob.position.normHomogeneous()

        vsCtx.vertex = c
        val oc = shader.vertexShader.invoke(vsCtx)
        oc.position.normHomogeneous()

        val transformedVertices = arrayOf(oa, ob, oc)

        val ai = oa.position.inViewport().toVector2i()
        val bi = ob.position.inViewport().toVector2i()
        val ci = oc.position.inViewport().toVector2i()

        val dbgColor = Vector4d(1.0, 0.0, 1.0, 1.0)

        val points = arrayOf(ai, bi, ci)
        val sorted = points.sortedBy { it.y() }

        val top = sorted[0]
        val mid = sorted[1]
        val bot = sorted[2]

        //println("top $top mid $mid bot $bot")
        if (top.y == bot.y)
            return //degerate

        val cut = Vector2i(bot.x + ((top.x - bot.x) / (top.y - bot.y).toDouble() * (mid.y - bot.y)).roundToInt(), mid.y)

        val midleft = if (cut.x <= mid.x) cut else mid
        val midright = if (cut.x <= mid.x) mid else cut

        //Shading init
        fsCtx.setup(oa, ob, oc)

        //Top part (top is y = 0 remember !)
        for (y in Math.max(top.y, 0)..Math.min(bufferSize.y - 1, mid.y)) {
            val scale: Double = (mid.y - y).toDouble() / (top.y - mid.y).toDouble()
            val sx: Int = midleft.x + ((midleft.x - top.x) * scale).toInt()
            val ex: Int = midright.x + ((midright.x - top.x) * scale).toInt()

            for (x in Math.max(0, sx)..Math.min(bufferSize.x - 1, ex)) {
                val p = Vector2i(x, y)
                val bary = barycentric(points, p)

                if (drawMode == DrawMode.SHADER) {
                    fsCtx.interpolateInputs(bary)

                    val depth = fsCtx.passedData.position.z
                    if (depth <= context.zbuffer[x * bufferSize.x + y])
                        context.zbuffer[x * bufferSize.x + y] = depth.toFloat()
                    else
                        continue

                    //if(fsCtx.passedData.position.z > 1.0 || fsCtx.passedData.position.z < 0.0)
                    //    println(fsCtx.passedData.position.z)
                    shader.fragmentShader.invoke(fsCtx)
                    color = fsCtx.output
                } else if (drawMode == DrawMode.TEXTURED) {
                    val texcoord = extractAndInterpolate(transformedVertices, bary) {
                        (this as? CubeApplication.IntermediaryDataWithTexcoord)?.texcoord ?: Vector2d(0.0)
                    }
                    color = checker.sample(texcoord)
                }

                point(p, color)
            }
        }
        //Bottom part
        for (y in Math.max(0, mid.y)..Math.min(bufferSize.y - 1, bot.y)) {
            val scale: Double = (y - mid.y).toDouble() / (mid.y - bot.y).toDouble()
            val sx: Int = midleft.x + ((midleft.x - bot.x) * scale).toInt()
            val ex: Int = midright.x + ((midright.x - bot.x) * scale).toInt()

            for (x in Math.max(0, sx)..Math.min(bufferSize.x - 1, ex)) {
                val p = Vector2i(x, y)
                val bary = barycentric(points, p)

                if (drawMode == DrawMode.SHADER) {
                    fsCtx.interpolateInputs(bary)

                    val depth = fsCtx.passedData.position.z
                    if (depth <= context.zbuffer[x * bufferSize.x + y])
                        context.zbuffer[x * bufferSize.x + y] = depth.toFloat()
                    else
                        continue

                    //if(fsCtx.passedData.position.z > 1.0 || fsCtx.passedData.position.z < 0.0)
                    //    println(fsCtx.passedData.position.z)
                    shader.fragmentShader.invoke(fsCtx)
                    color = fsCtx.output
                } else if (drawMode == DrawMode.TEXTURED) {
                    val texcoord = extractAndInterpolate(transformedVertices, bary) {
                        (this as? CubeApplication.IntermediaryDataWithTexcoord)?.texcoord ?: Vector2d(0.0)
                    }
                    color = checker.sample(texcoord)
                }

                point(p, color)
            }
        }

        /*line(ai, bi, dbgColor)
        line(ai, ci, dbgColor)
        line(ci, bi, dbgColor)*/
        //line(cut, mid, dbgColor)
    }

    fun Vector2dc.inViewport() = Vector2d(x() * bufferSize.x, y() * bufferSize.y)

    fun Vector4dc.inViewport() = Vector2d(x() * bufferSize.x, y() * bufferSize.y)

    var usePerspectiveCorectInterpolation = true

    /** 'fixed function' interp; unused */
    fun <I : IntermediaryData> extractAndInterpolate(points: Array<I>, bary: Vector3d, extractor: I.() -> Vector2dc): Vector2d {
        val acc = Vector2d()

        var weights = bary
        if (usePerspectiveCorectInterpolation) {
            val z0 = points[0].position.z
            val z1 = points[1].position.z
            val z2 = points[2].position.z

            acc += (Vector2d(extractor.invoke(points[0])).mul(weights.x / z0))
            acc += (Vector2d(extractor.invoke(points[1])).mul(weights.y / z1))
            acc += (Vector2d(extractor.invoke(points[2])).mul(weights.z / z2))

            val divisor = weights.x / z0 + weights.y / z1 + weights.z / z2
            acc.mul(1.0 / divisor)
        } else {
            acc += Vector2d(extractor.invoke(points[0])).mul(weights.x)
            acc += Vector2d(extractor.invoke(points[1])).mul(weights.y)
            acc += Vector2d(extractor.invoke(points[2])).mul(weights.z)
        }

        return acc
    }

    inner class ActualVertexShaderCtx<VF : Vertex> : Shader.VertexShaderCtx<VF> {
        lateinit override var vertex: VF
    }

    fun <VF : Vertex, IR : IntermediaryData> Shader<VF, IR>.createVertexCtx(): ActualVertexShaderCtx<VF> {
        return ActualVertexShaderCtx<VF>()
    }

    inner class ActualFragmentShaderCtx<IR : IntermediaryData> : Shader.FragmentShaderCtx<IR> {
        override val frameNumber: Int
            get() = context.frame

        lateinit override var passedData: IR
        override var output: Vector4d = Vector4d(0.0)

        lateinit private var a: IR
        lateinit private var b: IR
        lateinit private var c: IR
        lateinit private var weights: Vector3d

        private lateinit var interpolators: MutableList<() -> Unit>

        fun setup(a: IR, b: IR, c: IR) {
            this.a = a
            this.b = b
            this.c = c
        }

        fun interpolateInputs(weights: Vector3d) {
            this.weights = weights

            passedData = a.clone() as IR

            if (!::interpolators.isInitialized)
                createInterpolators()

            interpolators.forEach { it.invoke() }
        }

        private fun createInterpolators() {
            interpolators = mutableListOf<() -> Unit>()

            for (member in passedData.javaClass.kotlin.memberProperties.filterIsInstance<KMutableProperty<*>>()) {
                //println("Analysing member $member ${member.returnType.javaClass}")
                if (member.returnType.javaType == Vector2d::class.java) {
                    //println("Creating interpolator for $member")
                    interpolators.add {
                        val z0 = a.position.z
                        val z1 = b.position.z
                        val z2 = c.position.z

                        val interpolated = Vector2d(0.0)
                        val aValue: Vector2d = member.getter.call(a) as Vector2d
                        val bValue: Vector2d = member.getter.call(b) as Vector2d
                        val cValue: Vector2d = member.getter.call(c) as Vector2d
                        interpolated.addScaled(aValue, weights.x / z0)
                        interpolated.addScaled(bValue, weights.y / z1)
                        interpolated.addScaled(cValue, weights.z / z2)

                        val divisor = weights.x / z0 + weights.y / z1 + weights.z / z2
                        interpolated.mul(1.0 / divisor)
                        member.setter.call(passedData, interpolated)
                    }
                }

                if (member.returnType.javaType == Vector3d::class.java) {
                    //println("Creating interpolator for $member")
                    interpolators.add {
                        val z0 = a.position.z
                        val z1 = b.position.z
                        val z2 = c.position.z

                        val interpolated = Vector3d(0.0)
                        val aValue: Vector3d = member.getter.call(a) as Vector3d
                        val bValue: Vector3d = member.getter.call(b) as Vector3d
                        val cValue: Vector3d = member.getter.call(c) as Vector3d

                        interpolated.addScaled(aValue, weights.x / z0)
                        interpolated.addScaled(bValue, weights.y / z1)
                        interpolated.addScaled(cValue, weights.z / z2)

                        val divisor = weights.x / z0 + weights.y / z1 + weights.z / z2
                        interpolated.mul(1.0 / divisor)
                        member.setter.call(passedData, interpolated)
                    }
                }

                if (member.returnType.javaType == Vector4d::class.java) {
                    //println("Creating interpolator for $member")
                    interpolators.add {
                        val z0 = a.position.z
                        val z1 = b.position.z
                        val z2 = c.position.z

                        val interpolated = Vector4d(0.0)
                        val aValue: Vector4d = member.getter.call(a) as Vector4d
                        val bValue: Vector4d = member.getter.call(b) as Vector4d
                        val cValue: Vector4d = member.getter.call(c) as Vector4d

                        interpolated.addScaled(aValue, weights.x / z0)
                        interpolated.addScaled(bValue, weights.y / z1)
                        interpolated.addScaled(cValue, weights.z / z2)

                        val divisor = weights.x / z0 + weights.y / z1 + weights.z / z2
                        interpolated.mul(1.0 / divisor)
                        member.setter.call(passedData, interpolated)
                    }
                }
            }
        }
    }

    fun <VF : Vertex, IR : IntermediaryData> Shader<VF, IR>.createFragmentCtx(): ActualFragmentShaderCtx<IR> {
        return ActualFragmentShaderCtx<IR>()
    }


}

private fun Vector4d.normHomogeneous() {
    this.x /= this.w
    this.y /= this.w
    //this.z /= this.w
    //println(this.z)
    //this.w = 1.0
}
