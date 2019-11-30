package app.alessandrotedesco.asana.github.detailsActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.alessandrotedesco.asana.github.R
import app.alessandrotedesco.asana.github.detailsActivity.JsonDetailsResponseModel.DataModel.NodeModel.WatchersModel.LoginModel
import java.util.*

class RepositorySubscriberAdapter(private val mList: ArrayList<LoginModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class RepositorySubscriberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // set repository name
        val repositorySubscriberNameTextView: TextView =
            itemView.findViewById(R.id.repositorySubscriberNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.subscriber_layout, parent, false)
        return RepositorySubscriberViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = mList[position]
        holder as RepositorySubscriberViewHolder

        // set subscriber name
        holder.repositorySubscriberNameTextView.text = currentItem.login
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}