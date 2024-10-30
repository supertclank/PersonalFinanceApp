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
import api.data_class.GoalsRead
import com.example.personfinanceapp.R

class GoalsAdapter(private val listener: GoalsListener) :
    RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    private var goals: List<GoalsRead> = emptyList()
    private val expandedPositions = mutableSetOf<Int>()  // Tracks expanded positions

    interface GoalsListener {
        fun onEditGoal(goal: GoalsRead)
        fun onDeleteGoal(goal: GoalsRead)
        fun onGoalClick(goal: GoalsRead)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_item, parent, false)
        Log.d(TAG, "onCreateViewHolder: ViewHolder created")
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        Log.d(TAG, "onBindViewHolder: Binding goal at position $position: $goal")
        holder.bind(goal, position)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: Total goals count is ${goals.size}")
        return goals.size
    }

    fun updateGoals(newGoals: List<GoalsRead>) {
        this.goals = newGoals
        Log.d(TAG, "updateGoals: Goals list updated with ${newGoals.size} items")
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
                val position = adapterPosition
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position)
                    goalDetails.visibility = View.GONE
                } else {
                    expandedPositions.add(position)
                    goalDetails.visibility = View.VISIBLE
                }
                listener.onGoalClick(goals[position])
            }

            editButton.setOnClickListener {
                listener.onEditGoal(goals[adapterPosition])
            }

            deleteButton.setOnClickListener {
                listener.onDeleteGoal(goals[adapterPosition])
            }
        }

        fun bind(goal: GoalsRead, position: Int) {
            Log.d(TAG, "bind: Binding goal at position $position with data: $goal")
            goalTitle.text = goal.name
            goalSummary.text = "Target: ${goal.target_amount}, Current: ${goal.current_amount}"
            goalDescription.text = goal.description
            goalDetails.visibility = if (expandedPositions.contains(position)) View.VISIBLE else View.GONE
            Log.d(TAG, "bind: goalDetails visibility set to ${goalDetails.visibility}")
        }
    }
}
