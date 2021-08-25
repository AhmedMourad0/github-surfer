package dev.ahmedmourad.githubsurfer.users.search

import android.app.SearchManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.common.SearchHandler
import dev.ahmedmourad.githubsurfer.common.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.githubsurfer.databinding.FragmentSearchResultsBinding
import dev.ahmedmourad.githubsurfer.di.injector
import dev.ahmedmourad.githubsurfer.users.profile.parcel
import dev.ahmedmourad.githubsurfer.users.ui.*
import dev.ahmedmourad.githubsurfer.utils.hideIme
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider
import dev.ahmedmourad.githubsurfer.utils.bindToLifecycle

class SearchResultsFragment : Fragment(), SearchHandler {

    private var shimmer: View? = null

    private val args: SearchResultsFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<SearchResultsViewModel>>

    private val viewModel: SearchResultsViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
            this,
            viewModelFactory,
            SearchResultsViewModel.defaultArgs(args.query)
        )
    }

    private var binding: FragmentSearchResultsBinding? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().injector.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = View.inflate(requireContext(), R.layout.fragment_search_results, null)
        shimmer = createCombinedShimmerLayout(requireContext())
        root.findViewById<FrameLayout>(R.id.content).addView(shimmer)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchResultsBinding.bind(view)
        initializePostsList()
        initializeStateObservers()
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding!!.appBar.toolbar)
        setHasOptionsMenu(true)
        activity.supportActionBar!!.setHomeButtonEnabled(true)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_results, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Removing the icon from the search view
        val searchManager = getSystemService(requireContext(), SearchManager::class.java)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager?.getSearchableInfo(requireActivity().componentName))
        searchView.isIconifiedByDefault = true
        searchView.isIconified = false
        searchView.setQuery(viewModel.query.value, false)
        searchView.clearFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializePostsList() {
        adapter = SearchResultsAdapter(requireContext()) { user ->
            viewModel.lastVisitedUserId.value = user.id
            val action = SearchResultsFragmentDirections
                .actionSearchResultsFragmentToUserProfileFragment(user.parcel())
            findNavController().navigate(action)
        }
        binding!!.recycler.adapter = adapter
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding!!.recycler.layoutManager = layoutManager
        binding!!.recycler.isVerticalScrollBarEnabled = true
    }

    private fun initializeStateObservers() {

        bindToLifecycle {
            viewModel.state.collectLatest { state ->
                hideIme()
                when (state) {

                    is SearchResultsViewModel.State.Data -> {
                        itemsMode()
                        binding!!.recycler.post {
                            adapter.submitData(state.data)
                        }
                    }

                    SearchResultsViewModel.State.Loading -> {
                        loadingMode()
                    }

                    is SearchResultsViewModel.State.Error -> {
                        if (adapter.itemCount > 0) {
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
                }
            }
        }

        bindToLifecycle {
            viewModel.lastVisitedUser.collectLatest { user ->
                user ?: return@collectLatest
                layoutManager.scrollToPositionWithOffset(adapter.refreshItem(user), 0)
            }
        }
    }

    private fun itemsMode() {
        binding!!.recycler.visibility = View.VISIBLE
        binding!!.errorView.root.visibility = View.GONE
        shimmer!!.visibility = View.GONE
    }

    private fun errorMode() {
        binding!!.recycler.visibility = View.GONE
        shimmer!!.visibility = View.GONE
        binding!!.errorView.root.visibility = View.VISIBLE
        binding!!.errorView.errorMessage.setText(R.string.something_went_wrong)
        binding!!.errorView.errorIcon.setImageResource(R.drawable.ic_error)
    }

    private fun loadingMode() {
        binding!!.recycler.visibility = View.GONE
        binding!!.errorView.root.visibility = View.GONE
        shimmer!!.visibility = View.VISIBLE
        adapter.submitData(emptyList())
    }

    override fun onSearch(query: String?) {
        hideIme()
        if (query.isNullOrBlank()) {
            findNavController().popBackStack()
            return
        }
        viewModel.onQueryChanged(query)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onRefreshLastVisitedUser()
    }

    override fun onDestroyView() {
        binding = null
        shimmer = null
        super.onDestroyView()
    }
}
