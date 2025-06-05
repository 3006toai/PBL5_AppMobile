package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.HistoryItem

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewFruit)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.textViewName.text = historyItem.info.ten
        val imageUrl = "http://10.0.2.2:5000${historyItem.image_url}"
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .into(holder.imageView)
        val highestScore = historyItem.top3.maxByOrNull { prediction -> prediction.score }?.score ?: 0f
        holder.textViewScore.text = String.format("%.1f%%", highestScore)
    }

    override fun getItemCount(): Int = historyList.size
}