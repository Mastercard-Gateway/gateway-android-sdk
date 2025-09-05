package com.mastercard.gateway.android.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mastercard.gateway.android.sampleapp.repo.PrefsRepository
import com.mastercard.gateway.android.sampleapp.utils.RegionInfo
import com.mastercard.gateway.android.sdk.Gateway
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val prefsRepo: PrefsRepository) : ViewModel() {

    val merchantId = prefsRepo.getMerchantId()
    val region = prefsRepo.getRegion()
    val merchantServerLink = prefsRepo.getServerUrl()

    val regions: List<RegionInfo> = Gateway.Region.entries.map { RegionInfo(it.name, it.prefix) }


    fun saveSessionData(merchantId: String, region: String, link: String) {
        viewModelScope.launch {
            prefsRepo.saveMerchantId(merchantId)
            prefsRepo.saveRegion(region)
            prefsRepo.saveServerUrl(link)
        }
    }
}