package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.utils.GeminiApiTester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
//    private val apiKey = "YOUR_API_KEY_HERE" // Get from secure storage
    private val apiKey = "AIzaSyCAVVsPMMyCUhU8xIPFoJjIP-RglKMBpMY"

    private val apiTester = GeminiApiTester(apiKey)

    fun testApiConnection() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Testing API connection...")

            apiTester.testApiKeyAndModels { result ->
                result.onSuccess { apiResult ->
                    if (apiResult.isValid) {
                        // API key is valid
                        val message = buildString {
                            append("✅ API Key is valid!\n\n")
                            append("Available models:\n")
                            apiResult.availableModels.forEach { model ->
                                append("• $model\n")
                            }
                            if (apiResult.hasGeminiPro) {
                                append("\n✅ Gemini Pro models available")
                            }
                        }
                        _uiState.value = UiState.Success(message)
                    } else {
                        // API key is invalid
                        val errorMsg = if (apiResult.isAuthenticationError) {
                            "❌ Invalid API Key. Please check your API key."
                        } else {
                            "❌ API Error: ${apiResult.errorMessage}"
                        }
                        _uiState.value = UiState.Error(errorMsg)
                    }
                }.onFailure { exception ->
                    _uiState.value = UiState.Error("Network error: ${exception.message}")
                }
            }
        }
    }

    fun testSimplePrompt() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Testing content generation...")

            apiTester.testContentGeneration("Say 'Hello World' in a creative way") { result ->
                result.onSuccess { contentResult ->
                    if (contentResult.isSuccessful) {
                        val message = """
                            ✅ Content generation successful!
                            
                            Model used: ${contentResult.modelUsed}
                            
                            Response: ${contentResult.responseText}
                        """.trimIndent()
                        _uiState.value = UiState.Success(message)
                    } else {
                        val errorMsg = "Content generation failed: ${contentResult.errorMessage}"
                        _uiState.value = UiState.Error(errorMsg)
                    }
                }.onFailure { exception ->
                    _uiState.value = UiState.Error("Error: ${exception.message}")
                }
            }
        }
    }

    // UI State
    sealed class UiState {
        data object Idle : UiState()
        data class Loading(val message: String) : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
}