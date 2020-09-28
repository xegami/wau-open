package com.xegami.wau.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xegami.wau.android.R

class CommentRecyclerViewAdapter(
    private val context: Context,
    private val comments: MutableList<String>
) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_row_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvComment = itemView.findViewById<TextView>(R.id.tv_list_row_comment)

        fun bind(comment: String) {
            tvComment.text = comment
        }
    }

}