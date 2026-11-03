import com.photosolve.app.domain.image.*
import com.photosolve.app.ui.navigation.AppDestination
import kotlin.math.abs
import kotlin.random.Random

private fun close(actual: Float, expected: Float) = check(abs(actual - expected) < 0.001f) {
    "Expected $expected, got $actual"
}

fun main() {
    val fit = ImagePlacement.fit(4000, 2000, 1000f, 1000f)
    close(fit.x, 0f); close(fit.y, 250f); close(fit.width, 1000f); close(fit.height, 500f)
    val crop = CropRect(0.25f, 0.25f, 0.75f, 0.75f)
    check(crop.pixels(4000, 2000) == PixelCrop(1000, 500, 2000, 1000))
    val moved = crop.drag(CropHandle.MOVE, 100f / fit.width, 50f / fit.height)
    check(moved.pixels(4000, 2000) == PixelCrop(1400, 700, 2000, 1000))
    val edge = crop.drag(CropHandle.LEFT, -0.1f, 0.5f)
    close(edge.left, 0.15f); close(edge.top, crop.top); close(edge.bottom, crop.bottom)
    check(CropRect.FULL.pixels(4033, 3025) == PixelCrop(0, 0, 4033, 3025))
    check(CropRect.FULL.pixels(1, 1) == PixelCrop(0, 0, 1, 1))
    val random = Random(773)
    var area = CropRect.INITIAL
    repeat(10000) {
        area = area.drag(CropHandle.entries.random(random), random.nextFloat() * 4 - 2, random.nextFloat() * 4 - 2)
        check(area.left >= 0f && area.top >= 0f && area.right <= 1f && area.bottom <= 1f)
        check(area.width >= CropRect.MIN_SIZE - 0.00001f && area.height >= CropRect.MIN_SIZE - 0.00001f)
        val pixels = area.pixels(1440, 3200)
        check(pixels.width > 0 && pixels.height > 0 && pixels.x >= 0 && pixels.y >= 0)
        check(pixels.x + pixels.width <= 1440 && pixels.y + pixels.height <= 3200)
    }
    val expected = mapOf(
        AppDestination.SETTINGS to AppDestination.MY,
        AppDestination.MY to AppDestination.HOME,
        AppDestination.SOURCE to AppDestination.HOME,
        AppDestination.PREVIEW to AppDestination.CROP,
        AppDestination.RESULT to AppDestination.PREVIEW,
        AppDestination.HISTORY_DETAIL to AppDestination.HISTORY,
        AppDestination.HISTORY to AppDestination.MY,
    )
    expected.forEach { (page, parent) -> check(page.parent() == parent) }
    check(AppDestination.CROP.parent() == AppDestination.SOURCE)
    check(AppDestination.CROP.parent(false) == AppDestination.HOME)
    println("PASS: coordinate mapping, independent edges, 10000 boundary drags, tiny/full crops, back routes")
}
