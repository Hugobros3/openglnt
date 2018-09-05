import org.joml.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign

fun Double.toNormalizedInt() = Math.min(Math.max(0, this.toInt()), 255)

fun Vector2ic.sign(): Vector2i = Vector2i(x().sign, y().sign)

operator fun Vector2ic.minus(other: Vector2ic): Vector2i = Vector2i(x() - other.x(), y() - other.y())
operator fun Vector2ic.plus(other: Vector2ic): Vector2i = Vector2i(x() + other.x(), y() + other.y())

operator fun Vector2d.plusAssign(mul: Vector2d?) {
    this.add(mul)
}

fun Vector2d.addScaled(vec: Vector2d, s: Double) {
    this.add(vec.x * s, vec.y * s)
}

fun Vector3d.addScaled(vec: Vector3d, s: Double) {
    this.add(vec.x * s, vec.y * s, vec.z * s)
}

fun Vector4d.addScaled(vec: Vector4d, s: Double) {
    this.add(vec.x * s, vec.y * s, vec.z * s, vec.w * s)
}

fun Vector2dc.toVector2i() = Vector2i(this.x().roundToInt(), this.y().roundToInt())

fun Vector4dc.toArgbInt(): Int {
    val r = (this.x() * 255.0).toNormalizedInt()
    val g = (this.y() * 255.0).toNormalizedInt()
    val b = (this.z() * 255.0).toNormalizedInt()
    val a = (this.w() * 255.0).toNormalizedInt()

    return ((r shl 16)
            or (g shl 8)
            or (b shl 0)
            or (a shl 24))
}

fun argbToVector4d(argb: Int) : Vector4d {
    val r = (argb shr 16) and 0xff
    val g = (argb shr 8) and 0xff
    val b = (argb shr 0) and 0xff
    val a = (argb shr 24) and 0xff
    return Vector4d(r / 255.0, g / 255.0, b / 255.0, a / 255.0)
}

fun barycentric(triangles: Array<Vector2i>, point: Vector2i): Vector3d {
    val u = Vector3d((triangles[2].x - triangles[0].x).toDouble(), (triangles[1].x - triangles[0].x).toDouble(), triangles[0].x - point.x.toDouble()) cross
            Vector3d((triangles[2].y - triangles[0].y).toDouble(), (triangles[1].y - triangles[0].y).toDouble(), triangles[0].y - point.y.toDouble())

    if (u.z.absoluteValue < 1.0) return Vector3d(-1.0, -1.0, -1.0)
    return Vector3d(1.0 - (u.x + u.y) / u.z, u.y / u.z, u.x / u.z)
}

infix fun Vector3d.cross(other: Vector3dc): Vector3d {
    val dest = Vector3d()
    this.cross(other, dest)
    return dest
}