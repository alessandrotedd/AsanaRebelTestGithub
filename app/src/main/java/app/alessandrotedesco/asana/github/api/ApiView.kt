package app.alessandrotedesco.asana.github.api

import okhttp3.Call

interface ApiView : LoadingView {
    fun showResults(response: Any)
    fun showConnectionError(call: Call)
    fun initRecyclerView()
}
