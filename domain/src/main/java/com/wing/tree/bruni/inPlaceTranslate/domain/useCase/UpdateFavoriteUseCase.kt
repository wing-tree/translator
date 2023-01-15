package com.wing.tree.bruni.inPlaceTranslate.domain.useCase

import com.wing.tree.bruni.core.useCase.CoroutineUseCase
import com.wing.tree.bruni.core.useCase.IOCoroutineDispatcher
import com.wing.tree.bruni.inPlaceTranslate.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdateFavoriteUseCase @Inject constructor(
    @IOCoroutineDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val historyRepository: HistoryRepository
) : CoroutineUseCase<UpdateFavoriteUseCase.Parameter, Unit>(coroutineDispatcher) {
    override suspend fun execute(parameter: Parameter) {
        historyRepository.updateFavorite(parameter.rowid, parameter.isFavorite)
    }

    data class Parameter(val rowid: Int, val isFavorite: Boolean)
}