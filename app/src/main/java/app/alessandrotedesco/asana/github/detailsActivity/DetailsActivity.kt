package app.alessandrotedesco.asana.github.detailsActivity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import app.alessandrotedesco.asana.github.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_details.*
import okhttp3.Call
import app.alessandrotedesco.asana.github.detailsActivity.JsonDetailsResponseModel.DataModel.NodeModel.WatchersModel.LoginModel
import java.util.*

class DetailsActivity : AppCompatActivity(), DetailsView {

    private lateinit var repositoryId: String
    private val presenter = DetailsPresenter(this, DetailsModel())
    private val linearLayoutManager = LinearLayoutManager(this)
    override lateinit var loadingScreen: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // init repositoryID
        initRepositoryID()

        // init recycler view
        initRecyclerView()

        // init loading screen
        initLoadingScreen(this)

        // retrieve repository info
        presenter.getSubscribersList(repositoryId)

        // set repository name
        repositoryDetailsNameTextView.text = intent.getStringExtra("repositoryName")
    }

    override fun initRecyclerView() {
        // set recycler view params
        subscribersRecyclerView.setHasFixedSize(true)
        subscribersRecyclerView.layoutManager = linearLayoutManager

        // set infinite scroll
        presenter.initRecyclerViewInfiniteScroll(subscribersRecyclerView, linearLayoutManager)
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
     * Shows subscribers in the RecyclerView and shows error message in case of API fail
     */
    override fun showResults(response: Any) {
        response as JsonDetailsResponseModel
        runOnUiThread {
            // set repository subscribers count text
            repositoryDetailsSubscribersCountTextView.text = String.format(Locale.getDefault(), getString(R.string.number_of_subscribers_format), response.data.node.watchers.totalCount)

            // handle JSON data
            val newResults = ArrayList<LoginModel>()
            try {
                newResults.addAll(response.data.node.watchers.nodes)
            } catch (e: Exception) {
                // API fail
                Log.e("DetailsActivity", "can't load subscribers: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.noConnectionErrorMessage), Toast.LENGTH_LONG).show()
            }
            updateRecyclerView(newResults)
        }
    }

    /**
     * Updates the recycler view with new results, to add to the previous ones.
     * Must run in the UI thread.
     *
     * @param newResults the new results to add to the recycler view
     */
    @UiThread
    private fun updateRecyclerView(newResults: ArrayList<LoginModel>) {
        // Save recycler state (to prevent scroll resetting)
        val recyclerViewState = subscribersRecyclerView.layoutManager?.onSaveInstanceState()

        // Update recycler
        val newRepositoryAdapter = RepositorySubscriberAdapter(presenter.previousResults plus newResults)
        subscribersRecyclerView.adapter = newRepositoryAdapter

        // Restore recycler state
        subscribersRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
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
            presenter.makeSubscribersCall(call)
        }.show()
    }

    override fun initRepositoryID() {
        // get repository ID
        val repositoryIdNullable = intent.getStringExtra("repositoryId")
        if (repositoryIdNullable != null)
            repositoryId = repositoryIdNullable
        else {
            // log error
            Log.e("DetailsActivity", "repositoryId is null")
            // show error as toast
            Toast.makeText(this, getString(R.string.null_repository_id), Toast.LENGTH_LONG).show()
            // end activity
            finish()
        }
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