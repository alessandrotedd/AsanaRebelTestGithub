package app.alessandrotedesco.asana.github.searchActivity

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.alessandrotedesco.asana.github.R
import app.alessandrotedesco.asana.github.api.ApiView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import app.alessandrotedesco.asana.github.searchActivity.JsonSearchResponseModel.DataModel.DataSearchModel.EdgeModel
import kotlinx.android.synthetic.main.activity_search.*
import okhttp3.Call

class SearchActivity : AppCompatActivity(), ApiView {
    override lateinit var loadingScreen: Dialog

    private val presenter = SearchPresenter(this, SearchModel())
    private val linearLayoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // set search bar title
        title = getString(R.string.search_hint)

        // init loading screen
        initLoadingScreen(this)

        // init recycler view
        initRecyclerView()
    }

    override fun initRecyclerView() {
        // set recycler view params
        searchRecyclerView.setHasFixedSize(true)
        searchRecyclerView.layoutManager = linearLayoutManager

        // set infinite scroll
        presenter.initRecyclerViewInfiniteScroll(searchRecyclerView, linearLayoutManager)
    }

    /**
     * show/hide the loading progressBar and block/allow user interaction with the UI, depending on the loading variable value
     * @param loading true if it should start the loading process, false if it should stop it
     * @see blockInput
     * @see unblockInput
     */
    override fun setLoading(loading: Boolean) {
        // start loading
        if (loading) {
            // show loadingScreen
            loadingScreen.show()
            // prevent user from using the UI
            blockInput()
        }
        // stop loading
        else {
            // dismiss diaog
            loadingScreen.dismiss()
            // allow user to interact with the UI
            unblockInput()
        }
    }

    /**
     * Shows repositories in the RecyclerView and shows error message in case of API fail
     */
    override fun showResults(response: Any) {
        runOnUiThread {
            response as JsonSearchResponseModel
            val newResults = ArrayList<EdgeModel>()
            try {
                newResults.addAll(response.data.search.edges)
            } catch (e: Exception) {
                // API fail
                Log.e("SearchActivity", "can't load repositories: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.noConnectionErrorMessage), Toast.LENGTH_LONG).show()
            }

            // check if empty
            if (newResults.isEmpty())
                // show error
                Toast.makeText(this, getString(R.string.no_repositories_found), Toast.LENGTH_LONG).show()

            // not empty, show results
            updateRecyclerView(newResults)
        }
    }

    @UiThread
    fun updateRecyclerView(newResults: ArrayList<EdgeModel>) {
        // Save recycler state (to prevent scroll resetting)
        val recyclerViewState = searchRecyclerView.layoutManager?.onSaveInstanceState()

        // Update recycler
        val newRepositoryAdapter = RepositoryAdapter(presenter.previousResults plus newResults)
        searchRecyclerView.adapter = newRepositoryAdapter

        // Restore recycler state
        searchRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    /**
     * shows connection error snackbar
     */
    override fun showConnectionError(call: Call) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.noConnectionErrorMessage),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.retry)) {
            presenter.makeSearchCall(call)
        }.show()
    }

    /**
     * initialises the search bar, setting a query text listener to the search button
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchButton = menu.findItem(R.id.searchButton).actionView
                as androidx.appcompat.widget.SearchView
        searchButton.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchButton.maxWidth = Int.MAX_VALUE

        // listening to search query text change
        searchButton.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(repositoryName: String): Boolean {
                presenter.searchRepositories(repositoryName)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })

        return true
    }
}

/**
 * merges two ArrayLists
 * @return the merged ArrayList
 */
private infix fun <E> ArrayList<E>.plus(results: ArrayList<E>): ArrayList<E> {
    addAll(results)
    return this
}