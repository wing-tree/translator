package com.wing.tree.bruni.translator.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wing.tree.bruni.core.constant.*
import com.wing.tree.bruni.core.extension.hundreds
import com.wing.tree.bruni.core.extension.string
import com.wing.tree.bruni.core.useCase.*
import com.wing.tree.bruni.translator.BuildConfig
import com.wing.tree.bruni.translator.domain.model.Translation
import com.wing.tree.bruni.translator.domain.useCase.CharactersUseCase
import com.wing.tree.bruni.translator.domain.useCase.SourceUseCase
import com.wing.tree.bruni.translator.domain.useCase.TargetUseCase
import com.wing.tree.bruni.translator.domain.useCase.TranslateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class TranslatorViewModel(
    private val charactersUseCase: CharactersUseCase,
    private val translateUseCase: TranslateUseCase,
    private val sourceUseCase: SourceUseCase,
    private val targetUseCase: TargetUseCase
) : ViewModel() {
    private val stopTimeout = FIVE.seconds
    private val source = sourceUseCase.get().map { result ->
        result.getOrNull() ?: BuildConfig.SOURCE.also {
            sourceUseCase.put(it)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout),
        initialValue = BuildConfig.SOURCE
    )

    private val target = targetUseCase.get().map { result ->
        result.getOrNull() ?: BuildConfig.TARGET.also {
            targetUseCase.put(it)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout),
        initialValue = BuildConfig.TARGET
    )

    private val translations = MutableStateFlow<Result<List<Translation>>>(Result.Loading)

    protected val ioDispatcher = Dispatchers.IO

    val isListening = MutableStateFlow(false)
    val result: StateFlow<Result<*>> = translations

    val sourceLanguage: String
        get() = source.value

    val sourceLocale: Locale
        get() = Locale(sourceLanguage)

    val displaySourceLanguage = source.map {
        Locale(it).displayLanguage
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout),
        initialValue = Locale(sourceLanguage).displayLanguage
    )

    @Suppress("MemberVisibilityCanBePrivate")
    val targetLanguage: String get() = target.value

    val targetLocale: Locale
        get() = Locale(targetLanguage)

    val displayTargetLanguage = target.map {
        Locale(it).displayLanguage
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout),
        initialValue = Locale(targetLanguage).displayLanguage
    )

    val characters = charactersUseCase.get().map { it.getOrDefault(ZERO) }
    val sourceText = MutableStateFlow<String?>(null)
    val translatedText: StateFlow<String?> = translations.map {
        it.firstOrNull()?.translatedText
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout),
        initialValue = null
    )

    init {
        val timeout = FIVE.hundreds.milliseconds

        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            combine(source, target, sourceText) { source, target, sourceText ->
                Triple(source, target, sourceText)
            }
                .debounce(timeout)
                .collectLatest { (source, target, sourceText) ->
                    sourceText?.let {
                        translate(source, target, it)
                    }
                }
        }
    }

    private fun translate(source: String, target: String, sourceText: CharSequence) = viewModelScope.launch {
        val result = withContext(ioDispatcher) {
            val parameter = TranslateUseCase.Parameter(
                source = source,
                sourceText = sourceText.string,
                target = target
            )

            translateUseCase(parameter)
        }

        translations.update { result }
    }

    fun clearCharacters() = viewModelScope.launch(ioDispatcher) {
        charactersUseCase.clear()
    }

    fun swapLanguages() {
        viewModelScope.launch {
            val source = source.value
            val target = target.value

            sourceUseCase.put(target)
            targetUseCase.put(source)
        }
    }
}
