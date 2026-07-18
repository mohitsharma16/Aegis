package com.mslabs.aegis.presentation.screens.detail

import com.mslabs.aegis.domain.model.DecryptedVaultItem

data class ItemDetailScreen(
    val item: DecryptedVaultItem?,
    val isEditing: Boolean,
)
