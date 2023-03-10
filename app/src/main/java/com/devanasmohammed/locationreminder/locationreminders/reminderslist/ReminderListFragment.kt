package com.devanasmohammed.locationreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.devanasmohammed.locationreminder.R
import com.devanasmohammed.locationreminder.authentication.AuthenticationActivity
import com.devanasmohammed.locationreminder.base.BaseFragment
import com.devanasmohammed.locationreminder.base.NavigationCommand
import com.devanasmohammed.locationreminder.databinding.FragmentRemindersBinding
import com.devanasmohammed.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.devanasmohammed.locationreminder.utils.setTitle
import com.devanasmohammed.locationreminder.utils.setup
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("DEPRECATION")
class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    private fun signOut(){
        AuthUI.getInstance().signOut(requireContext())
            .addOnSuccessListener {
                val intent = Intent(activity, AuthenticationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

}
