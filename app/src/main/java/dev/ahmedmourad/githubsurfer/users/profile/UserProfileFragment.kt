package dev.ahmedmourad.githubsurfer.users.profile

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.common.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.githubsurfer.core.users.model.User
import dev.ahmedmourad.githubsurfer.databinding.FragmentUserProfileBinding
import dev.ahmedmourad.githubsurfer.di.injector
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider
import dev.ahmedmourad.githubsurfer.utils.bindToLifecycle

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val args: UserProfileFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<UserProfileViewModel>>

    private val viewModel: UserProfileViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
            this,
            viewModelFactory,
            UserProfileViewModel.defaultArgs(args.user)
        )
    }

    private var binding: FragmentUserProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)
        binding!!.let { b ->
            setupToolbar(b.toolbar)
            b.followers.name.text = getString(R.string.followers)
            b.following.name.text = getString(R.string.following)
            b.repos.name.text = getString(R.string.repos)
            b.gists.name.text = getString(R.string.gists)
            b.email.name.text = getString(R.string.email)
            b.location.name.text = getString(R.string.location)
            b.company.name.text = getString(R.string.company)
            b.blog.name.text = getString(R.string.blog)
            b.username.name.text = getString(R.string.full_name)
            b.bio.name.text = getString(R.string.bio)
        }
        initializeStateObservers()
        initializeNotesInputField()
        initializeSaveButton()
        setHasOptionsMenu(true)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
    }

    private fun populateUi(user: User) {
        binding!!.let { b ->
            Glide.with(requireContext())
                .load(user.avatarUrl)
                .placeholder(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.placeholder)))
                .error(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.error)))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(b.avatar)
            b.toolbarLayout.title = user.login
            b.followers.value.text = user.followersCount.toString()
            b.following.value.text = user.followingCount.toString()
            b.repos.value.text = user.reposCount.toString()
            b.gists.value.text = user.gistsCount.toString()
            b.bio.value.text = user.bio.toString()
            b.location.value.text = user.location.toString()
            b.email.value.text = user.email.toString()
            b.company.value.text = user.company.toString()
            b.blog.value.text = user.blog.toString()
            b.username.value.text = user.name
            b.notes.editText!!.setText(user.notes ?: "")
            b.username.root.visibility = if (user.name == null) GONE else VISIBLE
            b.bio.root.visibility = if (user.bio == null) GONE else VISIBLE
            b.location.root.visibility = if (user.location == null) GONE else VISIBLE
            b.email.root.visibility = if (user.email == null) GONE else VISIBLE
            b.company.root.visibility = if (user.company == null) GONE else VISIBLE
            b.blog.root.visibility = if (user.blog == null) GONE else VISIBLE
        }
    }

    private fun initializeStateObservers() {
        bindToLifecycle {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is UserProfileViewModel.State.Data -> {
                        itemsMode()
                        populateUi(state.item)
                    }
                    is UserProfileViewModel.State.Error -> {
                        if (viewModel.isUserDisplayed.value) {
                            Toast.makeText(
                                context,
                                R.string.something_went_wrong,
                                Toast.LENGTH_LONG
                            ).show()
                            itemsMode()
                        } else {
                            errorMode()
                        }
                    }
                    UserProfileViewModel.State.Loading -> Unit
                    UserProfileViewModel.State.NoConnection -> {
                        if (viewModel.isUserDisplayed.value) {
                            Toast.makeText(
                                context,
                                R.string.no_internet_connection,
                                Toast.LENGTH_LONG
                            ).show()
                            itemsMode()
                        } else {
                            noConnectionMode()
                        }
                    }
                    UserProfileViewModel.State.NoData -> {
                        if (viewModel.isUserDisplayed.value) {
                            Toast.makeText(
                                context,
                                R.string.something_went_wrong,
                                Toast.LENGTH_LONG
                            ).show()
                            itemsMode()
                        } else {
                            noDataMode()
                        }
                    }
                }
            }
        }

        bindToLifecycle {
            viewModel.updateNotesState.collectLatest { state ->
                when (state) {
                    is UserProfileViewModel.UpdateNotesState.Success -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.notes_update_successful,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is UserProfileViewModel.UpdateNotesState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.something_went_wrong,
                            Toast.LENGTH_LONG
                        ).show()
                        setSaveEnabled(true)
                    }
                    null -> Unit
                }
                viewModel.updateNotesState.value = null
            }
        }
    }

    private fun initializeNotesInputField() {
         binding!!.notes.editText!!.doOnTextChanged { text, _, _, _ ->
             viewModel.onNotesChanged(text?.toString())
             setSaveEnabled(viewModel.hasNotesChanged(text?.toString()))
         }
    }

    private fun initializeSaveButton() {
         binding!!.saveButton.setOnClickListener {
             viewModel.onUpdateNotes()
             setSaveEnabled(false)
         }
    }

    private fun setSaveEnabled(enabled: Boolean) {
        binding!!.saveButton.isEnabled = enabled
        binding!!.saveButton.alpha = if (enabled) 1f else 0.7f
    }

    private fun itemsMode() {
        binding!!.content.visibility = VISIBLE
        binding!!.errorView.root.visibility = GONE
    }

    private fun errorMode() {
        binding!!.content.visibility = GONE
        binding!!.errorView.root.visibility = VISIBLE
        binding!!.errorView.errorMessage.setText(R.string.something_went_wrong)
        binding!!.errorView.errorIcon.setImageResource(R.drawable.ic_error)
    }

    private fun noDataMode() {
        binding!!.content.visibility = GONE
        binding!!.errorView.root.visibility = VISIBLE
        binding!!.errorView.errorMessage.setText(R.string.user_not_found)
        binding!!.errorView.errorIcon.setImageResource(R.drawable.ic_error)
    }

    private fun noConnectionMode() {
        binding!!.content.visibility = GONE
        binding!!.errorView.root.visibility = VISIBLE
        binding!!.errorView.errorMessage.setText(R.string.no_internet_connection)
        binding!!.errorView.errorIcon.setImageResource(R.drawable.ic_no_internet)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
