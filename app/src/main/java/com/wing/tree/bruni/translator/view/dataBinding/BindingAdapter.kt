package com.wing.tree.bruni.translator.view.dataBinding

import androidx.databinding.BindingAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.wing.tree.bruni.core.useCase.Result
import com.wing.tree.bruni.translator.widget.SpeechRecognitionButton

@BindingAdapter("isListening")
fun setListening(speechRecognitionButton: SpeechRecognitionButton, isListening: Boolean) {
    speechRecognitionButton.isListening = isListening
}

@BindingAdapter("result")
fun setResult(circularProgressIndicator: CircularProgressIndicator, result: Result<*>) {
    when(result) {
        Result.Loading -> circularProgressIndicator.show()
        else -> circularProgressIndicator.hide()
    }
}
