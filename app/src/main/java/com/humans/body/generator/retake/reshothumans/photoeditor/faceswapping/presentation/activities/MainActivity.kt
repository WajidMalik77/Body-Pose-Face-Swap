package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.R
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api.GeminiImageService
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini.ImageGenerationViewModel
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.imageViewToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ImageGenerationViewModel

    // Views
    private lateinit var etPrompt: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnTestApi: Button
    private lateinit var btnClear: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvError: TextView
    private lateinit var tvImagesTitle: TextView
    private lateinit var img: ImageView
    private lateinit var img1: ImageView
    private lateinit var llImagesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ImageGenerationViewModel::class.java]

        // Initialize views
        initViews()

        // Set up observers
        setupObservers()

        // Set up click listeners
        setupClickListeners()

        // Test API connection on startup
        viewModel.testApiConnection()
        GeminiImageService().changeFace(
            apiKey = "AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY",
            targetImage = imageViewToBitmap(img)!!,
            faceImage = imageViewToBitmap(img1)!!,
            prompt = "Replace the face naturally",
        ) { result ->
            result.onSuccess { bitmap ->
                btnGenerate.text="Done"
                img.setImageBitmap(bitmap)
            }.onFailure {
                btnGenerate.text="error ${it.message}"

                it.printStackTrace()
            }
        }

    }

    private fun initViews() {
        img = findViewById(R.id.img)
        img1 = findViewById(R.id.img1)
        etPrompt = findViewById(R.id.etPrompt)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnTestApi = findViewById(R.id.btnTestApi)
        btnClear = findViewById(R.id.btnClear)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        tvError = findViewById(R.id.tvError)
        tvImagesTitle = findViewById(R.id.tvImagesTitle)
        llImagesContainer = findViewById(R.id.llImagesContainer)
    }



    fun generateImagen4Images(
        apiKey: String,
        prompt: String,
        sampleCount: Int = 1,
        onSuccess: (List<Bitmap>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict"

        // Build JSON body
        val jsonBody = JSONObject().apply {
            put("instances", JSONArray().apply {
                put(JSONObject().apply { put("prompt", prompt) })
            })
            put("parameters", JSONObject().apply { put("sampleCount", sampleCount) })
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("key", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                    onError("HTTP ${response.code}: $bodyStr")
                    return
                }

                try {
                    val json = JSONObject(bodyStr)

                    // Check if API returned an error
                    if (json.has("error")) {
                        val errorMsg = json.getJSONObject("error").getString("message")
                        onError("API error: $errorMsg")
                        return
                    }

                    // Parse predictions safely
                    if (!json.has("predictions")) {
                        onError("No predictions found in response")
                        return
                    }

                    val predictions = json.getJSONArray("predictions")
                    val bitmaps = mutableListOf<Bitmap>()

                    for (i in 0 until predictions.length()) {
                        val prediction = predictions.getJSONObject(i)
                        if (!prediction.has("content")) continue

                        val contentArray = prediction.getJSONArray("content")
                        for (j in 0 until contentArray.length()) {
                            val item = contentArray.getJSONObject(j)
                            if (!item.has("image")) continue

                            val base64Image = item.getString("image")
                            val bytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            bitmaps.add(bitmap)
                        }
                    }

                    if (bitmaps.isEmpty()) {
                        onError("No images returned by API")
                        return
                    }

                    onSuccess(bitmaps)

                } catch (e: Exception) {
                    onError("Parse error: ${e.message}")
                }
            }
        })
    }


    private fun setupObservers() {
        generateImagen4Images(
            apiKey = "AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY",
            prompt = "A beautiful mosque at sunset, ultra realistic",
            onSuccess = { bitmap ->
                runOnUiThread {
//                    img.setImageBitmap(bitmap) // display the image
                }
            },
            onError = { error ->
                Log.e("Imagen", error)
            }
        )

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnGenerate.isEnabled = !isLoading
            btnTestApi.isEnabled = !isLoading
        })

        viewModel.generationState.observe(this, Observer { state ->
            when (state) {
                ImageGenerationViewModel.GenerationState.LOADING -> {
                    tvStatus.text = "Generating image..."
                    tvError.visibility = View.GONE
                }
                ImageGenerationViewModel.GenerationState.SUCCESS -> {
                    tvStatus.text = "Image generated successfully!"
                    tvError.visibility = View.GONE
                }
                ImageGenerationViewModel.GenerationState.ERROR -> {
                    tvStatus.text = "Error occurred"
                }
                ImageGenerationViewModel.GenerationState.API_VALID -> {
                    tvStatus.text = "API connection successful!"
                }
                ImageGenerationViewModel.GenerationState.IDLE -> {
                    tvStatus.text = "Ready to generate images"
                }
            }
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            if (error.isNullOrEmpty()) {
                tvError.visibility = View.GONE
            } else {
                tvError.text = error
                tvError.visibility = View.VISIBLE
            }
        })

        viewModel.generatedImages.observe(this, Observer { images ->
            updateImagesUI(images)
        })
    }

    private fun setupClickListeners() {
        btnGenerate.setOnClickListener {
            val prompt = etPrompt.text.toString().trim()
            generateImagen4Images(
                apiKey = "AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY",
                prompt = "A beautiful mosque at sunset, ultra realistic",
                onSuccess = { bitmap ->
                    runOnUiThread {
//                        img.setImageBitmap(bitmap) // display the image
                    }
                },
                onError = { error ->
                    Log.e("Imagen123", error)
                }
            )
            if (prompt.isNotEmpty()) {
                viewModel.generateImageFromText(prompt)
            } else {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
            }
        }

        btnTestApi.setOnClickListener {
            viewModel.testApiConnection()
        }

        btnClear.setOnClickListener {
            viewModel.clearImages()
            llImagesContainer.removeAllViews()
            tvImagesTitle.visibility = View.GONE
            btnClear.visibility = View.GONE
        }
    }

    private fun updateImagesUI(images: List<Bitmap>) {
        llImagesContainer.removeAllViews()

        if (images.isNotEmpty()) {
            tvImagesTitle.visibility = View.VISIBLE
            btnClear.visibility = View.VISIBLE

            images.forEachIndexed { index, bitmap ->
                // Create image view for each bitmap
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        600
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                }

                // Load image using Glide
                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(this@MainActivity)
                        .load(bitmap)
                        .into(imageView)
                }

                llImagesContainer.addView(imageView)

                // Add image number
                val tvImageNumber = TextView(this).apply {
                    text = "Image ${index + 1}"
                    textSize = 12f
                    setTextColor(resources.getColor(android.R.color.darker_gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 0, 0, 8)
                    }
                }
                llImagesContainer.addView(tvImageNumber)
            }
        } else {
            tvImagesTitle.visibility = View.GONE
            btnClear.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear Glide cache to free memory
        Glide.get(this).clearMemory()
    }
}