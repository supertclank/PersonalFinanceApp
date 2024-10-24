import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personfinanceapp.R

class GoalsAdapter(
    private val goals: List<Goal>,
    private val listener: GoalsListener
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    interface GoalsListener {
        fun onEditGoal(goal: Goal)
        fun onDeleteGoal(goal: Goal)
        fun onGoalClick(goal: Goal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_item, parent, false) // Make sure to replace with your layout file
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.bind(goal)
    }

    override fun getItemCount(): Int = goals.size

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val goalTitle: TextView = itemView.findViewById(R.id.goal_title)
        private val goalSummary: TextView = itemView.findViewById(R.id.goal_summary)
        private val goalDetails: LinearLayout = itemView.findViewById(R.id.goal_details)
        private val goalDescription: TextView = itemView.findViewById(R.id.goal_description)
        private val editButton: Button = itemView.findViewById(R.id.edit_goal_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_goal_button)

        init {
            // Toggle visibility of goal details on item click
            itemView.setOnClickListener {
                goalDetails.visibility = if (goalDetails.visibility == View.GONE) {
                    View.VISIBLE // Show details
                } else {
                    View.GONE // Hide details
                }
                listener.onGoalClick(goals[adapterPosition]) // Notify listener of goal click
            }

            editButton.setOnClickListener {
                listener.onEditGoal(goals[adapterPosition]) // Notify listener to edit goal
            }

            deleteButton.setOnClickListener {
                listener.onDeleteGoal(goals[adapterPosition]) // Notify listener to delete goal
            }
        }

        fun bind(goal: Goal) {
            goalTitle.text = goal.name
            goalSummary.text = "Target: ${goal.targetAmount}, Current: ${goal.currentAmount}"
            goalDescription.text = goal.description
            goalDetails.visibility = View.GONE
        }
    }
}
