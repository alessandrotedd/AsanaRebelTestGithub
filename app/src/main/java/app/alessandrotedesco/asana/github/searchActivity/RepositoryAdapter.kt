package app.alessandrotedesco.asana.github.searchActivity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import app.alessandrotedesco.asana.github.R
import app.alessandrotedesco.asana.github.detailsActivity.DetailsActivity
import app.alessandrotedesco.asana.github.searchActivity.JsonSearchResponseModel.DataModel.DataSearchModel.EdgeModel
import kotlinx.coroutines.*
import java.net.URL
import java.util.*

class RepositoryAdapter(private val mList: ArrayList<EdgeModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // set owner image
        val repositoryOwnerImageView: View = itemView.findViewById(R.id.repositoryOwnerImageView)
        // set repository name
        val repositoryNameTextView: TextView = itemView.findViewById(R.id.repositoryNameTextView)
        // set repository description
        val repositoryDescriptionTextView: TextView =
            itemView.findViewById(R.id.repositoryDescriptionTextView)
        // set number of forks
        val repositoryNumberOfForksTextView: TextView =
            itemView.findViewById(R.id.numberOfForksTextView)

        /**
         * pass data (repository name and ID) to the details activity
         */
        fun bind(repositoryName: String, repositoryId: String, context: Context) {
            itemView.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("repositoryName", repositoryName)
                intent.putExtra("repositoryId", repositoryId)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_layout, parent, false)
        return RepositoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = mList[position].node
        holder as RepositoryViewHolder

        holder.bind(currentItem.name, currentItem.id, holder.itemView.context)

        // load owner image asynchronously, in order to display the items as soon as possible
        val url = URL(currentItem.owner.avatarUrl)
        loadImageAsynchronously(url, holder)

        // set repository name
        holder.repositoryNameTextView.text = currentItem.name

        // set repository description
        holder.repositoryDescriptionTextView.text = currentItem.description

        // set number of forks
        holder.repositoryNumberOfForksTextView.text = currentItem.forkCount.toString()
    }

    /**
     * loads owner image asynchronously
     * @param url the avatar image URL
     * @param holder the recyclerView viewHolder
     */
    private fun loadImageAsynchronously(url: URL, holder: RepositoryViewHolder) {
        // get bitmap image from the given URL
        val bmp = GlobalScope.async(Dispatchers.IO) {
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
                .toDrawable(holder.itemView.context.resources)
        }
        // set owner image
        GlobalScope.launch(Dispatchers.Main) {
            holder.repositoryOwnerImageView.background = bmp.await()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}