package app.alessandrotedesco.asana.github

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessRecyclerViewScrollListener(layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {
    var cursor = ""
    var hasNextPage = true
    // The minimum amount of items to have below your current scroll position before loading more
    private var visibleThreshold = 5
    // The current offset index of data loaded
    private var currentPage = 0
    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0
    // True if we are still waiting for the last set of data to load.
    private var loading = true
    // Sets the starting page index
    private val startingPageIndex = 0
    private var mLayoutManager: RecyclerView.LayoutManager = layoutManager

    /**
     * function called several times during a single user scroll gesture
     */
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = mLayoutManager.itemCount
        val lastVisibleItemPosition: Int =
            (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            currentPage = startingPageIndex
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0)
                loading = true
        }
        // check to see if the dataset count has changed
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }
        // check to see if we have breached the visibleThreshold and need to reload more data.
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            onLoadMore()
            loading = true
        }
    }

    /**
     * method to call whenever performing new searches
     */
    fun resetCursor() {
        cursor = "" // reset cursor
        currentPage = startingPageIndex
        previousTotalItemCount = 0
        loading = true
    }

    /**
     *     Defines the process for actually loading more data based on page
     */
    abstract fun onLoadMore()
}