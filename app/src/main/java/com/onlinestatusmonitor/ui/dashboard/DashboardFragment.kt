package com.onlinestatusmonitor.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker
import com.onlinestatusmonitor.R
import com.onlinestatusmonitor.core.di.ServiceLocator
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import com.onlinestatusmonitor.databinding.DialogAddNumberBinding
import com.onlinestatusmonitor.databinding.FragmentDashboardBinding
import com.onlinestatusmonitor.service.MonitorService
import com.onlinestatusmonitor.ui.dashboard.adapter.LogsAdapter
import com.onlinestatusmonitor.ui.dashboard.adapter.NumbersAdapter
import com.onlinestatusmonitor.util.Formatters

/**
 * Main dashboard:
 * - Start/Stop monitoring (foreground service)
 * - Manage multiple numbers
 * - Real-time (simulated) activity log
 * - Stats: total online/offline + last active
 */
class DashboardFragment : Fragment() {

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val vm: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(ServiceLocator.monitorRepository) as T
            }
        }
    }

    private var selected: MonitoredNumberEntity? = null

    private lateinit var numbersAdapter: NumbersAdapter
    private val logsAdapter = LogsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        numbersAdapter = NumbersAdapter { entity ->
            selected = entity
            renderStats(entity)
        }

        binding.rvNumbers.adapter = numbersAdapter
        binding.rvLogs.adapter = logsAdapter

        binding.btnStart.setOnClickListener {
            MonitorService.start(requireContext())
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
        }

        binding.btnStop.setOnClickListener {
            MonitorService.stop(requireContext())
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
        }

        binding.btnAddNumber.setOnClickListener {
            showAddNumberDialog()
        }

        vm.numbers.observe(viewLifecycleOwner) { list ->
            numbersAdapter.submitList(list)

            // Select a default number for stats (first item).
            if (selected == null && list.isNotEmpty()) {
                selected = list.first()
                renderStats(list.first())
            } else {
                // If the selected number changed in DB, refresh stats.
                selected?.let { sel ->
                    val refreshed = list.firstOrNull { it.id == sel.id }
                    if (refreshed != null) {
                        selected = refreshed
                        renderStats(refreshed)
                    }
                }
            }
        }

        vm.logs.observe(viewLifecycleOwner) { list ->
            logsAdapter.submitList(list)
        }
    }

    private fun showAddNumberDialog() {
        val dialogBinding = DialogAddNumberBinding.inflate(layoutInflater)

        // Optional: keep only phone digits and let CCP handle dialing code.
        dialogBinding.etPhone.requestFocus()

        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
            .setTitle(getString(R.string.dialog_add_number_title))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.btn_add)) { _, _ ->
                val ccp: CountryCodePicker = dialogBinding.ccp
                val countryCode = ccp.selectedCountryCode
                val phone = dialogBinding.etPhone.text?.toString().orEmpty().trim()

                if (phone.isNotBlank()) {
                    vm.addNumber(countryCode, phone)
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun renderStats(entity: MonitoredNumberEntity) {
        binding.tvSelectedNumber.text = "Selected: ${entity.display()}  •  ${if (entity.isOnline) "ONLINE" else "OFFLINE"}"

        val now = System.currentTimeMillis()
        val elapsedSinceChange = (now - entity.lastStatusChangeEpochMs).coerceAtLeast(0)

        val onlineMs = if (entity.isOnline) entity.totalOnlineMs + elapsedSinceChange else entity.totalOnlineMs
        val offlineMs = if (!entity.isOnline) entity.totalOfflineMs + elapsedSinceChange else entity.totalOfflineMs

        binding.tvTotalOnline.text = "${getString(R.string.stat_total_online)}: ${Formatters.formatDuration(onlineMs)}"
        binding.tvTotalOffline.text = "${getString(R.string.stat_total_offline)}: ${Formatters.formatDuration(offlineMs)}"
        binding.tvLastActive.text = "${getString(R.string.stat_last_active)}: " +
            (entity.lastActiveEpochMs?.let { Formatters.formatDateTime(it) } ?: "—")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

