package com.wing.tree.bruni.translator.view.compose.composable

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.wing.tree.bruni.core.constant.ONE
import com.wing.tree.bruni.core.constant.TWO
import com.wing.tree.bruni.core.extension.float
import com.wing.tree.bruni.core.extension.half
import com.wing.tree.bruni.translator.R
import com.wing.tree.bruni.translator.extension.*
import com.wing.tree.bruni.translator.model.History
import com.wing.tree.bruni.translator.view.compose.ui.theme.Typography
import com.wing.tree.bruni.translator.viewModel.HistoryViewModel
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
private fun Background(
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(colorScheme.error)
            .padding(24.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.round_delete_24px),
            contentDescription = null,
            tint = colorScheme.onError
        )
    }
}

@Composable
private fun DismissContent(
    item: History.Item,
    onIconClick: (rowid: Int, isFavorite: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.background(colorScheme.background)) {
        val tint = if (item.isStarred) {
            animateColorAsState(targetValue = colorScheme.primary)
        } else {
            animateColorAsState(targetValue = colorScheme.onBackground)
        }

        Column(
            modifier = Modifier
                .weight(ONE.float)
                .paddingStart(24.dp)
                .paddingVertical(12.dp)
        ) {
            Text(
                text = item.sourceText,
                color = colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis,
                maxLines = TWO,
                style = Typography.titleMedium.copyAsDp()
            )

            Text(
                text = item.translatedText,
                color = colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                maxLines = TWO,
                style = Typography.titleMedium.copyAsDp()
            )
        }

        IconButton(
            onClick = {
                item.isStarred = item.isStarred.not()

                onIconClick(item.rowid, item.isFavorite.not())
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.round_fill_star_24px),
                contentDescription = null,
                tint = tint.value
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Item(
    item: History.Item,
    onItemClick: (History.Item) -> Unit,
    onIconClick: (rowid: Int, isFavorite: Boolean) -> Unit,
    onDismissed: (History.Item) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            onDismissed(item)
            true
        },
        positionalThreshold = { it.half }
    )

    val horizontalArrangement = when (dismissState.dismissDirection) {
        DismissDirection.StartToEnd -> Arrangement.Start
        DismissDirection.EndToStart -> Arrangement.End
        else ->Arrangement.Start
    }

    SwipeToDismiss(
        state = dismissState,
        background = {
            Background(
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxSize()
            )
        },
        dismissContent = {
            AnimatedVisibility(
                visible = dismissState.isNotDismissed(),
                exit = shrinkVertically().plus(fadeOut())
            ) {
                DismissContent(
                    item = item,
                    onIconClick = onIconClick,
                    modifier = modifier
                )
            }
        },
        modifier = Modifier.clickable {
            onItemClick(item)
        }
    )
}

@Composable
private fun TranslatedOn(
    item: History.TranslatedOn, 
    modifier: Modifier = Modifier
) {
    val pattern = stringResource(id = R.string.pattern)
    val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
    val text = dateTimeFormatter.format(item.localDate)

    Text(
        text = text,
        modifier = modifier,
        color = colorScheme.secondary,
        style = Typography.labelLarge.copyAsDp()
    )
}

@Composable
internal fun HistoryScreen(
    lazyPagingItems: LazyPagingItems<History>,
    onItemClick: (History.Item) -> Unit,
    onIconClick: (rowid: Int, isFavorite: Boolean) -> Unit,
    onDismissed: (History.Item) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = lazyPagingItems,
            key = { it.key }
        ) { item ->
            when(item) {
                is History.Item -> Item(
                    item = item,
                    onItemClick = onItemClick,
                    onIconClick = onIconClick,
                    onDismissed = onDismissed
                )
                is History.TranslatedOn -> TranslatedOn(
                    item = item,
                    modifier = Modifier
                        .paddingHorizontal(24.dp)
                        .paddingVertical(12.dp)
                )
                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBar(
    viewModel: HistoryViewModel,
    navigationOnClick: () -> Unit,
    actionOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            @StringRes
            val id = if (viewModel.loadFavorites) {
                R.string.favorites
            } else {
                R.string.history
            }

            Text(text = stringResource(id = id))
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigationOnClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            if (viewModel.loadFavorites.not()) {
                IconButton(onClick = actionOnClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_fill_star_24px),
                        null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun DismissState.isDismissed() = isDismissed(DismissDirection.StartToEnd)
    .or(isDismissed(DismissDirection.EndToStart))

@OptIn(ExperimentalMaterial3Api::class)
private fun DismissState.isNotDismissed() = isDismissed().not()
