package dev.ahmedmourad.githubsurfer.users.users

import android.app.SearchManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dev.ahmedmourad.githubsurfer.R
import dev.ahmedmourad.githubsurfer.common.AssistedViewModelFactory
import dev.ahmedmourad.githubsurfer.common.SearchHandler
import dev.ahmedmourad.githubsurfer.common.SimpleSavedStateViewModelFactory
import dev.ahmedmourad.githubsurfer.di.injector
import dev.ahmedmourad.githubsurfer.users.profile.parcel
import dev.ahmedmourad.githubsurfer.users.users.FindUsersViewModel.Companion.INITIAL_SINCE
import dev.ahmedmourad.githubsurfer.users.ui.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import javax.inject.Inject
import javax.inject.Provider
import dev.ahmedmourad.githubsurfer.utils.bindToLifecycle

class FindUsersFragment : Fragment(), SearchHandler {

    private var recycler: RecyclerView? = null
    private var errorView: View? = null
    private var errorMessage: TextView? = null
    private var errorIcon: ImageView? = null
    private var toolbar: MaterialToolbar? = null
    private var newDataFab: ExtendedFloatingActionButton? = null
    private var swipeToRefresh: SwipeRefreshLayout? = null
    private var shimmer: View? = null

    @Inject
    lateinit var viewModelFactory: Provider<AssistedViewModelFactory<FindUsersViewModel>>

    private val viewModel: FindUsersViewModel by viewModels {
        SimpleSavedStateViewModelFactory(
            this,
            viewModelFactory,
            FindUsersViewModel.defaultArgs()
        )
    }

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: FindUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().injector.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        newDataFab = createNewDataFab(requireContext())
        recycler = createUsersRecyclerView(requireContext())
        errorView = createErrorView(requireContext())
        errorMessage = errorView!!.findViewById(R.id.error_message)
        errorIcon = errorView!!.findViewById(R.id.error_icon)
        shimmer = createCombinedShimmerLayout(requireContext())
        val content = createContentFrameLayout(requireContext()).apply {
            addView(recycler)
            addView(errorView)
            addView(shimmer)
        }
        swipeToRefresh = createSwipeToRefresh(requireContext()).apply {
            addView(content)
        }
        val appBar = createAppBar(requireContext())
        toolbar = appBar.findViewById(R.id.toolbar)
        return createRootConstraintLayout(requireContext()).apply {
            addView(appBar)
            addView(newDataFab)
            addView(swipeToRefresh)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePostsList()
        initializeNewDataFab()
        initializeSwipeToRefresh()
        initializeStateObservers()
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_find_users, menu)
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
    }

    private fun initializePostsList() {
        adapter = FindUsersAdapter(
            requireContext(),
            viewModel.items,
            { viewModel.pagingState.value = viewModel.pagingState.value.copy(since = it) }
        ) { user ->
            viewModel.lastVisitedUserId.value = user.id
            val action = FindUsersFragmentDirections
                .actionFindUsersFragmentToUserProfileFragment(user.parcel())
            findNavController().navigate(action)
        }
        recycler!!.adapter = adapter
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recycler!!.layoutManager = layoutManager
        recycler!!.isVerticalScrollBarEnabled = true
    }

    private fun initializeNewDataFab() {
        newDataFab!!.setOnClickListener {
            swipeToRefresh!!.isRefreshing = true
            newDataFab!!.hide()
            loadingMode()
            viewModel.enforceUpToDate()
        }
        bindToLifecycle {
            viewModel.hasUpToDate.collectLatest { hasUpToDate ->
                if (hasUpToDate) newDataFab!!.show() else newDataFab!!.hide()
            }
        }
    }

    private fun initializeSwipeToRefresh() {
        swipeToRefresh!!.setOnRefreshListener {
            viewModel.onRefresh()
        }
        bindToLifecycle {
            viewModel.endRefresh.collectLatest {
                swipeToRefresh!!.isRefreshing = false
            }
        }
    }

    private fun initializeStateObservers() {

        if (viewModel.items.value.isNotEmpty()) {
            adapter.submitData(viewModel.items.value, null)
            itemsMode()
        }

        bindToLifecycle {
            viewModel.state
                .drop(if (viewModel.items.value.isNotEmpty()) 1 else 0)
                .collectLatest { state ->
                    when (state) {

                        is FindUsersViewModel.State.Cached -> {
                            itemsMode()
                            recycler!!.post {
                                adapter.submitData(state.data, state.since)
                            }
                        }

                        is FindUsersViewModel.State.UpToDate -> {
                            itemsMode()
                            recycler!!.post {
                                adapter.submitData(state.data, state.since)
                            }
                        }

                        FindUsersViewModel.State.NoConnection -> {
                            if (adapter.itemCount > 0) {
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

                        is FindUsersViewModel.State.Error -> {
                            if (adapter.itemCount > 0) {
                                Toast.makeText(
                                    context,
                                    R.string.something_went_wrong,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                itemsMode()
                            } else {
                                errorMode()
                            }
                        }

                        FindUsersViewModel.State.Loading -> {
                            loadingMode()
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
        recycler!!.visibility = View.VISIBLE
        errorView!!.visibility = View.GONE
        shimmer!!.visibility = View.GONE
        swipeToRefresh!!.isRefreshing = false
    }

    private fun errorMode() {
        newDataFab!!.hide()
        recycler!!.visibility = View.GONE
        shimmer!!.visibility = View.GONE
        swipeToRefresh!!.isRefreshing = false
        errorView!!.visibility = View.VISIBLE
        errorMessage!!.setText(R.string.something_went_wrong)
        errorIcon!!.setImageResource(R.drawable.ic_error)
    }

    private fun noConnectionMode() {
        newDataFab!!.hide()
        recycler!!.visibility = View.GONE
        shimmer!!.visibility = View.GONE
        swipeToRefresh!!.isRefreshing = false
        errorView!!.visibility = View.VISIBLE
        errorMessage!!.setText(R.string.no_internet_connection)
        errorIcon!!.setImageResource(R.drawable.ic_no_internet)
    }

    private fun loadingMode() {
        newDataFab!!.hide()
        recycler!!.visibility = View.GONE
        errorView!!.visibility = View.GONE
        shimmer!!.visibility = View.VISIBLE
    }

    override fun onSearch(query: String?) {
        query ?: return
        val action = FindUsersFragmentDirections
            .actionFindUsersFragmentToSearchResultsFragment(query)
        findNavController().navigate(action)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onRefreshLastVisitedUser()
    }

    override fun onDestroyView() {
        recycler = null
        errorView = null
        errorMessage = null
        errorIcon = null
        toolbar = null
        newDataFab = null
        swipeToRefresh = null
        shimmer = null
        super.onDestroyView()
    }
}
