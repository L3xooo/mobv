package eu.mcomputing.mobv.zadanie.ui.adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(private val onClick: (User) -> Unit) :

    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private val users = mutableListOf<User>()

    fun setUsers(list: List<User>) {
        users.clear()
        users.addAll(list.filter { it.uid != SharedPreferencesUtil.userId }) // skip yourself
        notifyDataSetChanged()
    }

    // Reference for UI components each row
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.userName)
        private val avatar = itemView.findViewById<ShapeableImageView>(R.id.avatar)
        private val radius = itemView.findViewById<TextView>(R.id.radius)
        private val updated = itemView.findViewById<TextView>(R.id.updated)

        // Fill with data
        fun bind(user: User) {
            name.text = user.name
            radius.text = user.radius.toString()
            avatar.setImageResource(R.drawable.profile_avatar_placeholder)

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val parsedDate = dateFormat.parse(user.updated)
                Log.d("UserAdapter", "Parsed date: $parsedDate")
                val timeInMillis = parsedDate?.time ?: 0L
                Log.d("UserAdapter", "timeInMillis: $timeInMillis")
                updated.text = DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            } catch (e: Exception) {
                Log.e("UserAdapter", "Failed to parse date: ${user.updated}", e)
                // Fallback text in case of error
                updated.text = user.updated
            }

            // Click listener
            itemView.setOnClickListener { onClick(user) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}
