package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.PrefUtil
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.filters.FilterType
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.AdsConstants.LifeTimePref

fun Context.loadFontFromAssets(fontName: String): Typeface {
    return Typeface.createFromAsset(assets, "fonts/$fontName")
}

val colorList = listOf(
    "#CD320D", "#0C3F8A", "#20AB8C", "#20C71D", "#347B29", "#DE7F7F", "#601010", "#FF7F7E",

    "#AFC32E", "#FFE923", "#1A9890", "#0EF4E7", "#9519E0", "#566DE7", "#CF2BC9",
    "#F912C6", "#D9D9D9", "#F44336", "#E53935", "#C62828",

    "#FCE4EC", "#F8BBD0", "#F48FB1", "#F06292", "#EC407A", "#E91E63", "#C2185B", "#880E4F",

    "#F3E5F5", "#795548", "#E1BEE7", "#CE93D8", "#BA68C8", "#AB47BC", "#9C27B0", "#6A1B9A",

    "#E8EAF6", "#C5CAE9", "#9FA8DA", "#7986CB", "#5C6BC0", "#3F51B5", "#283593",

    "#E3F2FD", "#BBDEFB", "#90CAF9", "#64B5F6", "#42A5F5", "#2196F3", "#1565C0",

    "#E0F7FA", "#B2EBF2", "#80DEEA", "#4DD0E1", "#26C6DA", "#00BCD4", "#00838F",

    "#E8F5E9", "#C8E6C9", "#A5D6A7", "#81C784", "#66BB6A", "#4CAF50", "#2E7D32",

    "#FFFDE7", "#FFF9C4", "#FFF59D", "#FFF176", "#FFEE58", "#FFEB3B", "#F9A825",

    "#FFF3E0", "#FFE0B2", "#FFCC80", "#FFB74D", "#FFA726", "#FF9800", "#EF6C00",

    "#FBE9E7", "#FFCCBC", "#FFAB91", "#FF8A65", "#FF7043", "#FF5722", "#D84315"
)

data class FontItem(

    val name: String,
    val assetFileName: String
)

fun Context.loadFontsFromAssets(): List<FontItem> {
    val fontItems = mutableListOf<FontItem>()
    val assetManager = assets
    try {
        val fontFiles = assetManager.list("fonts") ?: emptyArray()
        for (fileName in fontFiles) {
            if (fileName.endsWith(".ttf", ignoreCase = true) || fileName.endsWith(
                    ".otf",
                    ignoreCase = true
                )
            ) {
                val displayName = fileName
                    .substringBeforeLast('.')
                    .replace('_', ' ')
                    .replaceFirstChar { it.uppercaseChar() }

                fontItems.add(FontItem(displayName, fileName))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fontItems
}

enum class ImageTarget {
    FIRST,
    SECOND
}

/*@RequiresApi(Build.VERSION_CODES.P)
fun Context.uriToBitmap(uri: Uri): Bitmap {
    val source = ImageDecoder.createSource(contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.isMutableRequired = true
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}*/
fun Context.uriToBitmap(uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun cropToAspectRatio(
    bitmap: Bitmap,
    aspectX: Int,
    aspectY: Int
): Bitmap {

    val width = bitmap.width
    val height = bitmap.height
    val targetRatio = aspectX.toFloat() / aspectY

    val currentRatio = width.toFloat() / height

    val cropWidth: Int
    val cropHeight: Int

    if (currentRatio > targetRatio) {
        cropHeight = height
        cropWidth = (height * targetRatio).toInt()
    } else {
        cropWidth = width
        cropHeight = (width / targetRatio).toInt()
    }

    val x = (width - cropWidth) / 2
    val y = (height - cropHeight) / 2

    return Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)
}

fun Bitmap.toCacheUri(context: Context): Uri {
    val file = File(context.cacheDir, "ucrop_${System.currentTimeMillis()}.jpg")
    val out = FileOutputStream(file)
    this.compress(Bitmap.CompressFormat.JPEG, 100, out)
    out.flush()
    out.close()
    return Uri.fromFile(file)
}

fun Context.openPrivacyPolicy(url: String) {
    if (url.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(this, "Privacy policy link not available", Toast.LENGTH_SHORT).show()
    }
}
data class PhotoEditListModel(var name: String, var img: Int,var isFree: Boolean=false)
data class Filters(var name: String, var img: Int,var type: FilterType)

fun Context.getCarouselList() = arrayListOf(
    PhotoEditListModel(getString(R.string.replicate_body_pose), R.drawable.premium_carousel__1_),
    PhotoEditListModel(getString(R.string.face_swap), R.drawable.premium_carousel__2_),
    PhotoEditListModel(getString(R.string.ghibli_photo), R.drawable.premium_carousel__3_),
    PhotoEditListModel(getString(R.string.ai_photo_editor), R.drawable.premium_carousel__4_),

)

fun Context.getEditorList() = arrayListOf(
    PhotoEditListModel(getString(R.string.crop), R.drawable.photo_editor_crop,true),
    PhotoEditListModel(getString(R.string.adjust), R.drawable.adjust_editor,true),
    PhotoEditListModel(getString(R.string.filters), R.drawable.filter_editor,true),
    PhotoEditListModel(getString(R.string.re_styler), R.drawable.restyle_editor),
    PhotoEditListModel(getString(R.string.text), R.drawable.font_editor,true),
    PhotoEditListModel(getString(R.string.bg_remover), R.drawable.bg_editor),
)

fun Context.getImageToImageList() = arrayListOf(
    PhotoEditListModel(getString(R.string.pose), R.drawable.select_pose),
    PhotoEditListModel(getString(R.string.bg_remover), R.drawable.bg_remover),
    PhotoEditListModel(getString(R.string.re_styler), R.drawable.restyler),
    PhotoEditListModel(getString(R.string.upscale_image), R.drawable.upscale),
)
fun Context.getVariationNumber() = arrayListOf(
    1,2,3,4,6,8,10
)

fun Context.getCameraList() = arrayListOf(
    PhotoEditListModel(getString(R.string.pose), R.drawable.select_pose),
    PhotoEditListModel(getString(R.string.edit), R.drawable.photo_editor,true),
    PhotoEditListModel(getString(R.string.image_variation), R.drawable.image_variation),
    PhotoEditListModel(getString(R.string.bg_remover), R.drawable.bg_remover),
    PhotoEditListModel(getString(R.string.ai_resizer), R.drawable.ai_resizer),
    PhotoEditListModel(getString(R.string.re_styler), R.drawable.restyler),
    PhotoEditListModel(getString(R.string.crop), R.drawable.crop,true),
    PhotoEditListModel(getString(R.string.upscale_image), R.drawable.upscale),
)

fun Context.getRotateList() = arrayListOf(
    PhotoEditListModel("Flip H", R.drawable.flip_h),
    PhotoEditListModel(getString(R.string.rotate), R.drawable.rotate),
    PhotoEditListModel("Flip V", R.drawable.flip_h),
)
fun Context.getFiltersList() = arrayListOf(
 Filters(FilterType.NONE.toString(), R.drawable.filter__3_,FilterType.NONE),
 Filters(
     FilterType.SEPIA.toString(), R.drawable.filter__1_,FilterType.SEPIA),
 Filters(
     FilterType.BLACK_WHITE.toString(), R.drawable.filter__2_,FilterType.BLACK_WHITE),
 Filters(
     FilterType.COOL.toString(), R.drawable.filter__4_,FilterType.COOL),
 Filters(
     FilterType.WARM.toString(), R.drawable.filter__5_,FilterType.WARM),
 Filters(
     FilterType.VINTAGE.toString(), R.drawable.filter__6_,FilterType.VINTAGE),
 Filters(
     FilterType.BRIGHT.toString(), R.drawable.filter__7_,FilterType.BRIGHT),
 Filters(
     FilterType.CONTRAST.toString(), R.drawable.filter__8_,FilterType.CONTRAST),
 Filters(
     FilterType.FADE.toString(), R.drawable.filter__9_,FilterType.FADE),
 Filters(
     FilterType.FILM.toString(), R.drawable.filter__10_,FilterType.FILM),
 Filters(
     FilterType.SHARP.toString(), R.drawable.filter__11_,FilterType.SHARP),
 Filters(
     FilterType.SOFT.toString(), R.drawable.filter__12_,FilterType.SOFT),
 Filters(
     FilterType.NIGHT.toString(), R.drawable.filter__13_,FilterType.NIGHT),
 Filters(
     FilterType.SUNSET.toString(), R.drawable.filter__14_,FilterType.SUNSET),
 Filters(
     FilterType.PINK.toString(), R.drawable.filter__5_,FilterType.PINK),
 Filters(
     FilterType.GREEN.toString(), R.drawable.filter__6_,FilterType.GREEN),
 Filters(
     FilterType.BLUE.toString(), R.drawable.filter__7_,FilterType.BLUE),
 Filters(
     FilterType.CYAN.toString(), R.drawable.filter__8_,FilterType.CYAN),
)


fun Context.getRatioList() = arrayListOf(
    PhotoEditListModel(getString(R.string.original), R.drawable.original_pic),
    PhotoEditListModel(getString(R.string.square), R.drawable.square_ratio),
    PhotoEditListModel(getString(R.string._2_3), R.drawable.ratio_23),
    PhotoEditListModel(getString(R.string._3_4), R.drawable.ratio_34),
    PhotoEditListModel(getString(R.string._5_7), R.drawable.ratio_57),
    PhotoEditListModel(getString(R.string._9_16), R.drawable.ratio_916),
)

fun Context.getReStyleList() = arrayListOf(
    PhotoEditListModel("Ghibli", R.drawable.ghibili),
)

fun imageViewToBitmap(imageView: ImageView): Bitmap? {
    val drawable = imageView.drawable ?: return null

    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "IMG_${System.currentTimeMillis()}.jpg"
): Boolean {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // ✅ Android 10+
        saveForAndroid10Plus(context, bitmap, fileName)
    } else {
        // ✅ Android 9 & below
        saveForLegacy(context, bitmap, fileName)
    }
}

@Suppress("DEPRECATION")
private fun saveForLegacy(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Boolean {
    return try {
        // 📁 Get Pictures directory
        val picturesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        // 📂 Create app folder
        val appDir = File(picturesDir, "Human Art")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        // 🖼️ Image file
        val imageFile = File(appDir, fileName)

        // ✍️ Write bitmap
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }

        // 🔄 Refresh gallery
        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun saveForAndroid10Plus(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Boolean {
    val resolver = context.contentResolver

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Human Art")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = resolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ) ?: return false

    return try {
        resolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        true
    } catch (e: Exception) {
        false
    }
}


fun Context.saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "IMG_${System.currentTimeMillis()}.jpg"
): Boolean {
    val resolver = context.contentResolver

    val imageCollection = MediaStore.Images.Media.getContentUri(
        MediaStore.VOLUME_EXTERNAL_PRIMARY
    )

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppName")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = resolver.insert(imageCollection, values) ?: return false

    return try {
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


data class MixedItem(
    val type: Int,
    val title: String = "",
    val imageRes: Int = 0,
    val bg: Int = 0,
    val isFree: Boolean = false,
)

const val TYPE_FIRST= 4
const val TYPE_FIRST_TWO = 0
const val TYPE_FOUR_VERTICAL = 1
const val TYPE_TITLE = 2
const val TYPE_LAST_TWO = 3

fun Context.getHomeData() = arrayListOf<MixedItem>(
    // First 2 items (horizontal)
    MixedItem(
        TYPE_FIRST,
        getString(R.string.select_pose),
        R.drawable.select_pose,
        R.drawable.select_pose_bg
    ),
    MixedItem(
        TYPE_FIRST_TWO,
        getString(R.string.select_pose),
        R.drawable.select_pose,
        R.drawable.select_pose_bg
    ),
    MixedItem(
        TYPE_FIRST_TWO,
        getString(R.string.face_swap),
        R.drawable.face_swap,
        R.drawable.photo_editor_bg
    ),


// Next 4 items (vertical)
    MixedItem(
        TYPE_FOUR_VERTICAL,
        getString(R.string.face_style),
        R.drawable.face_style,
        R.drawable.face_style_bg
    ),
    MixedItem(
        TYPE_FOUR_VERTICAL,
        getString(R.string.photo_editor),
        R.drawable.photo_editor,
        R.drawable.text_to_img_bg,true
    ),
    MixedItem(
        TYPE_FOUR_VERTICAL,
        getString(R.string.text_to_image),
        R.drawable.text_to_img,
        R.drawable.img_img_bg,
        true
    ),
    MixedItem(
        TYPE_FOUR_VERTICAL,
        getString(R.string.image_to_image), R.drawable.image_to_image, R.drawable.camera_bg
    ),
    MixedItem(
        TYPE_FOUR_VERTICAL,
        getString(R.string.camera), R.drawable.camera
    ),
// Next 6 items
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.re_styler), R.drawable.restyler),
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.image_variation), R.drawable.image_variation),
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.ai_resizer), R.drawable.ai_resizer),
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.crop_un_crop), R.drawable.crop, isFree = true),
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.bg_remover), R.drawable.bg_remover),
    MixedItem(TYPE_FOUR_VERTICAL, getString(R.string.upscale_image), R.drawable.upscale),

// Title (full width)
    MixedItem(TYPE_TITLE, getString(R.string.templates)),

// Last 2 items (image only)
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__1_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__2_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__3_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__4_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__5_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__6_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__7_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__8_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__9_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__10_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__11_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__12_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__13_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__14_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__15_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__16_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__17_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__18_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__19_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__20_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__21_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__22_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__23_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__24_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__25_),
    MixedItem(TYPE_LAST_TWO, imageRes = R.drawable.template__26_),
)
fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "temp_image.png")
    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
fun NavController.safeNavigate(@IdRes resId: Int, args: Bundle? = null) {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)
    if (action != null) {
        navigate(resId, args)
    }
}

fun androidx.fragment.app.Fragment.navigateWithBitmap(bitmap: Bitmap,resId: Int) {
    val uri = saveBitmapToCache(requireContext(), bitmap)

    val bundle = Bundle().apply {
        putString("BITMAP_BYTES", uri.toString())
    }
    findNavController().safeNavigate(resId, bundle)
}
fun isPremium(context: Context): Boolean {
    return    (PrefUtil(context).getBool(
        "is_premium",
        false
    ) || context.getSharedPreferences(
        LifeTimePref,
        0
    ).getBoolean("premium", false))
}


