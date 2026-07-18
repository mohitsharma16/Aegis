package com.mslabs.aegis.presentation.screens.detail

import androidx.lifecycle.ViewModel
import com.mslabs.aegis.domain.model.DecryptedVaultItem
import com.mslabs.aegis.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: VaultRepository,
) : ViewModel() {
    suspend fun save(item: DecryptedVaultItem) {
        repository.saveVaultItem(item)
    }
}
