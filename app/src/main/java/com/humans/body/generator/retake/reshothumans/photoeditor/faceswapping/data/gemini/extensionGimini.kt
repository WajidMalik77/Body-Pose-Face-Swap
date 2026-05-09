package com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping.gemini

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

fun <T> Result<T>.exceptionOrNull(): Throwable? = when (this) {
    is Result.Error -> exception
    else -> null
}

// Extension functions for Flow
fun <T> Result<T>.toFlow(): Flow<Result<T>> = flow {
    emit(this@toFlow)
}

fun <T> Result.Companion.loading(): Result<T> = Result.Loading
fun <T> Result.Companion.success(data: T): Result<T> = Result.Success(data)
fun <T> Result.Companion.failure(exception: Throwable): Result<T> = Result.Error(exception)