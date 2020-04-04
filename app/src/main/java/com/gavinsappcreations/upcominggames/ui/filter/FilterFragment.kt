package com.gavinsappcreations.upcominggames.ui.filter

import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gavinsappcreations.upcominggames.databinding.FragmentFilterBinding
import com.gavinsappcreations.upcominggames.utilities.hideKeyboard

class FilterFragment : Fragment() {

    private val viewModel: FilterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFilterBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.executePendingBindings()

        binding.platformRecyclerView.adapter =
            PlatformAdapter(
                viewModel.unsavedFilterOptions,
                PlatformAdapter.OnCheckedChangeListener { platformIndex, isChecked ->
                    viewModel.onPlatformCheckedChange(platformIndex, isChecked)
                })

        DateInputTextWatcher(binding.startDateTextInputEditText).listen()
        DateInputTextWatcher(binding.endDateTextInputEditText).listen()

        binding.applyButton.setOnClickListener {
            viewModel.onUpdateFilterOptions(
                binding.startDateTextInputEditText.error?.toString(),
                binding.startDateTextInputEditText.text?.toString(),
                binding.endDateTextInputEditText.error?.toString(),
                binding.endDateTextInputEditText.text?.toString()
            )
        }

        binding.nestedScrollView.setOnScrollChangeListener { scrollView, _, _, _, _ ->
            binding.topHorizontalLineView.visibility =
                if (scrollView.canScrollVertically(-1)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        viewModel.popBackStack.observe(viewLifecycleOwner, Observer {
            hideKeyboard(binding.startDateTextInputEditText)
            findNavController().popBackStack()
        })

        viewModel.updateFilterOptions.observe(viewLifecycleOwner, Observer {

            it.getContentIfNotHandled()?.let { updateFilterOptions ->
                if (updateFilterOptions) {
                    viewModel.saveNewFilterOptions()
                    hideKeyboard(binding.startDateTextInputEditText)
                    findNavController().popBackStack()
                } else {
                    displayInvalidDateToast()
                }
            }
        })

        return binding.root
    }


    private fun displayInvalidDateToast() {
        Toast.makeText(
            requireContext(),
            "Before proceeding, you must enter valid dates in the \"Release date\" section.",
            Toast.LENGTH_LONG
        ).show()

        val vibrator =
            requireActivity().getSystemService(VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator?.vibrate(200)
        }
    }


}
