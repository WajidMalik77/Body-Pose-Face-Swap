package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


/*
class AnalyzeImageUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        prompt: String = "Describe this image in detail"
    ): Flow<Result<String>> = flow {
        emit(Result.Loading())
        try {
            val result = repository.analyzeImageWithPrompt(bitmap, prompt)
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}*/
