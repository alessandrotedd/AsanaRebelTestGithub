package app.alessandrotedesco.asana.github.detailsActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.alessandrotedesco.asana.github.EndlessRecyclerViewScrollListener
import app.alessandrotedesco.asana.github.detailsActivity.JsonDetailsResponseModel.DataModel.NodeModel.WatchersModel.LoginModel
import com.google.gson.Gson
import okhttp3.Call

class DetailsPresenter(var detailsView: DetailsView, private val detailsModel: DetailsModel) :
    DetailsModel.OnDetailsFinishedListener {

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    var lastRepositoryId = ""
    var previousResults: ArrayList<LoginModel> =
        ArrayList()

    /**
     * load the first page of subscribers
     */
    fun getSubscribersList(repositoryId: String) {
        resetSearch(repositoryId) // new search
        // start the loading animation
        detailsView.setLoading(true)
        // start api call
        detailsModel.searchSubscribers(repositoryId, "", this)
    }

    /**
     * resets the search by resetting the cursor, emptying the loaded result list and the last searched repository ID
     */
    private fun resetSearch(repositoryId: String) {
        scrollListener.resetCursor() // reset scroll listener cursor state
        previousResults = ArrayList() // empty previous results
        lastRepositoryId = repositoryId // set last search status
    }

    /**
     * makes a call in order to get a list of subscribers
     * @param call the call to make / retry
     */
    fun makeSubscribersCall(call: Call) {
        detailsModel.makeDetailsCall(call, this)
    }

    /**
     * parses JSON response, stops loading animation, updates the page and shows the results via the details view
     * @param jsonResponse the JSON response string
     */
    override fun onSuccess(jsonResponse: String?) {
        // parse JSON response
        val response = Gson().fromJson(
            jsonResponse,
            JsonDetailsResponseModel::class.java
        )

        // stop the loading animation
        detailsView.setLoading(false)

        // update page cursor
        updatePage(response.data.node.watchers.pageInfo.endCursor, response.data.node.watchers.pageInfo.hasNextPage)

        // show repository info in the details view
        detailsView.showResults(response)
    }


    /**
     * handles an error on an API call by showing the connection error via the details view and stopping the loading screen
     * @param call a copy of the failed call, to be used later on with a retry method
     */
    override fun onError(call: Call) {
        // show connection error
        detailsView.showConnectionError(call)

        // stop the loading animation
        detailsView.setLoading(false)
    }

    /**
     * loads next page and starts the loading screen
     * @param the cursor to provide to the GraphQL query
     */
    fun loadNextPage(cursor: String) {
        if (scrollListener.hasNextPage) {
            // start the loading animation
            detailsView.setLoading(true)
            // send request
            detailsModel.searchSubscribers(lastRepositoryId, cursor, this)
        }
    }

    /**
     * set infinite scroll listener to the search recycler view
     */
    fun initRecyclerViewInfiniteScroll(
        subscriberRecyclerView: RecyclerView,
        linearLayoutManager: LinearLayoutManager
    ) {
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore() {
                loadNextPage(cursor)
            }
        }
        subscriberRecyclerView.addOnScrollListener(scrollListener)
    }

    /**
     * updates the page's info
     */
    private fun updatePage(cursor: String, hasNextPage: Boolean) {
        scrollListener.cursor = cursor
        scrollListener.hasNextPage = hasNextPage
    }
}