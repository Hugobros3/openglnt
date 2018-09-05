import de.matthiasmann.twl.utils.PNGDecoder
import org.joml.Vector2d
import org.joml.Vector4d
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Texture(val bufferedImage: BufferedImage) {
    fun sample(point: Vector2d) : Vector4d {
        val px = Math.max(0, Math.min(bufferedImage.width - 1, (point.x * bufferedImage.width).toInt()))
        val py = Math.max(0, Math.min(bufferedImage.height - 1, (point.y * bufferedImage.height).toInt()))

        val argb = bufferedImage.getRGB(px, py)
        return argbToVector4d(argb)
    }
}

fun fromFile(file: File) = Texture(ImageIO.read(file))
