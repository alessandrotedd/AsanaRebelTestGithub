package app.alessandrotedesco.asana.github.searchActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.alessandrotedesco.asana.github.EndlessRecyclerViewScrollListener
import app.alessandrotedesco.asana.github.api.ApiView
import com.google.gson.Gson
import okhttp3.Call
import app.alessandrotedesco.asana.github.searchActivity.JsonSearchResponseModel.DataModel.DataSearchModel.EdgeModel

class SearchPresenter(var searchView: ApiView, private val searchModel: SearchModel) :
    SearchModel.OnSearchFinishedListener {

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    var lastRepositoryName = ""
    var previousResults: ArrayList<EdgeModel> = ArrayList()

    /**
     * make a new search
     */
    fun searchRepositories(repositoryName: String) {
        resetSearch(repositoryName) // new search
        // start the loading animation
        searchView.setLoading(true)
        // start api call
        searchModel.searchRepositories(repositoryName, "", this)
    }


    /**
     * resets the search by resetting the cursor, emptying the loaded result list and the last searched repository name
     */
    private fun resetSearch(repositoryName: String) {
        scrollListener.resetCursor() // reset scroll listener cursor state
        previousResults = ArrayList() // empty previous results
        lastRepositoryName = repositoryName // set last search status
    }

    /**
     * makes a call in order to get a list of repositories
     * @param call the call to make / retry
     */
    fun makeSearchCall(call: Call) {
        searchModel.makeSearchCall(call, this)
    }

    /**
     * parses JSON response, stops loading animation, updates the page and shows the results via the search view
     * @param jsonResponse the JSON response string
     */
    override fun onSuccess(jsonResponse: String?) {
        // parse JSON response
        val response = Gson().fromJson(
            jsonResponse,
            JsonSearchResponseModel::class.java
        )

        // stop the loading animation
        searchView.setLoading(false)

        // update page cursor
        updatePage(response.data.search.pageInfo.endCursor, response.data.search.pageInfo.hasNextPage)

        // show repositories in the search view
        searchView.showResults(response)
    }

    /**
     * handles an error on an API call by showing the connection error via the search view and stopping the loading screen
     * @param call a copy of the failed call, to be used later on with a retry method
     */
    override fun onError(call: Call) {
        // show connection error
        searchView.showConnectionError(call)

        // stop the loading animation
        searchView.setLoading(false)
    }

    /**
     * loads next page and starts the loading screen
     * @param the cursor to provide to the GraphQL query
     */
    fun loadNextPage(cursor: String) {
        if (scrollListener.hasNextPage) {
            // start the loading animation
            searchView.setLoading(true)
            // start api call
            searchModel.searchRepositories(lastRepositoryName, cursor, this)
        }
    }

    /**
     * set infinite scroll listener to the search recycler view
     */
    fun initRecyclerViewInfiniteScroll(
        searchRecyclerView: RecyclerView,
        linearLayoutManager: LinearLayoutManager
    ) {
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore() {
                loadNextPage(cursor)
            }
        }
        searchRecyclerView.addOnScrollListener(scrollListener)
    }

    /**
     * updates the page's info
     */
    private fun updatePage(cursor: String, hasNextPage: Boolean) {
        scrollListener.cursor = cursor
        scrollListener.hasNextPage = hasNextPage
    }
}