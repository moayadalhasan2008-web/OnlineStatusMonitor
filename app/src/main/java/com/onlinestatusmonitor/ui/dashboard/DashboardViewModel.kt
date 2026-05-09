package com.onlinestatusmonitor.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.onlinestatusmonitor.data.local.entity.ActivityLogEntity
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import com.onlinestatusmonitor.data.repository.MonitorRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: MonitorRepository
) : ViewModel() {

    val numbers: LiveData<List<MonitoredNumberEntity>> = repository.observeNumbers().asLiveData()
    val logs: LiveData<List<ActivityLogEntity>> = repository.observeLatestLogs().asLiveData()

    fun addNumber(countryCode: String, phoneNumber: String) {
        viewModelScope.launch {
            repository.addNumber(countryCode, phoneNumber)
        }
    }
}

