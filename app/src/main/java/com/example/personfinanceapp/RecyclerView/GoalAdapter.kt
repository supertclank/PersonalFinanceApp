package com.example.personfinanceapp.RecyclerView

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import api.data_class.GoalsRead // Assuming this is the correct import for Goal data class
import com.example.personfinanceapp.R

class GoalsAdapter(private val listener: GoalsListener) :
    RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {
    private var goals: List<GoalsRead> = emptyList()

    interface GoalsListener {
        fun onEditGoal(goal: GoalsRead)
        fun onDeleteGoal(goal: GoalsRead)
        fun onGoalClick(goal: GoalsRead)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_item, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]  // Use 'goals' instead of 'goalsList'
        Log.d(TAG, "onBindViewHolder: Binding goal at position $position: $goal")

        // Bind the data to your views here
        holder.bind(goal)
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<GoalsRead>) {
        this.goals = newGoals
        notifyDataSetChanged()
    }

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val goalTitle: TextView = itemView.findViewById(R.id.goal_title)
        private val goalSummary: TextView = itemView.findViewById(R.id.goal_summary)
        private val goalDetails: LinearLayout = itemView.findViewById(R.id.goal_details)
        private val goalDescription: TextView = itemView.findViewById(R.id.goal_description)
        private val editButton: Button = itemView.findViewById(R.id.edit_goal_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_goal_button)

        init {
            itemView.setOnClickListener {
                val isVisible = goalDetails.visibility == View.VISIBLE
                goalDetails.visibility = if (isVisible) View.GONE else View.VISIBLE
                listener.onGoalClick(goals[adapterPosition])
            }

            editButton.setOnClickListener {
                listener.onEditGoal(goals[adapterPosition])
            }

            deleteButton.setOnClickListener {
                listener.onDeleteGoal(goals[adapterPosition])
            }
        }

        fun bind(goal: GoalsRead) {
            goalTitle.text = goal.name
            goalSummary.text = "Target: ${goal.target_amount}, Current: ${goal.current_amount}"
            goalDescription.text = goal.description
            goalDetails.visibility = View.GONE // Ensure details are initially hidden
        }
    }
}