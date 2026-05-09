package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini


//class GenerateContentUseCase @Inject constructor(
//    private val repository: GeminiRepository
//) {
//    suspend operator fun invoke(prompt: String): Flow<Result<String>> = flow {
//        emit(Result.loading())
//        try {
//            val result = repository.generateTextFromPrompt(prompt)
//            emit(result)
//        } catch (e: Exception) {
//            emit(Result.failure(e))
//        }
//    }
//}