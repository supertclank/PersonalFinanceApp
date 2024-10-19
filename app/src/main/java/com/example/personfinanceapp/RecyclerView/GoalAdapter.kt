package com.example.personfinanceapp.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import api.data_class.GoalsRead
import com.example.personfinanceapp.R

class GoalAdapter(private val goalsList: List<GoalsRead>) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.goal_item, parent, false)
        return GoalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goalsList[position]
        holder.bind(goal)
    }

    override fun getItemCount(): Int {
        return goalsList.size
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val goalTitle: TextView = itemView.findViewById(R.id.goal_title)

        fun bind(goal: GoalsRead) {
            goalTitle.text = goal.name // Assuming `name` is a field in `GoalsRead`
        }
    }
}
