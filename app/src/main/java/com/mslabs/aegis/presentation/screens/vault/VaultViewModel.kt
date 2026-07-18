package com.mslabs.aegis.presentation.screens.vault

import androidx.lifecycle.ViewModel
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
) : ViewModel() {
    fun loadItems(): Flow<List<DecryptedVaultItem>> = repository.getVaultItems()
}
