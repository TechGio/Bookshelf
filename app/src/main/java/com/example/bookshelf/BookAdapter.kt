package com.example.bookshelf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.ui.book.BookInfo
import com.squareup.picasso.Picasso

class BookAdapter(val context: Context, val bookInfoArrayList: ArrayList<BookInfo>): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {

        val currentBookInfo = bookInfoArrayList[position]

        if(currentBookInfo.getTitle() != "")
            holder.nameTV.text = currentBookInfo.getTitle()
        else
            holder.nameTV.visibility = View.GONE
        if(currentBookInfo.getPublisher() != "")
            holder.publisherTV.text = currentBookInfo.getPublisher()
        else
            holder.publisherTV.visibility = View.GONE
        if(currentBookInfo.getPageCount() != "")
            holder.pageCountTV.text = "No of Pages : " + currentBookInfo.getPageCount()
        else
            holder.pageCountTV.visibility = View.GONE
        if(currentBookInfo.getPublishedDate() != "")
            holder.dateTV.text = currentBookInfo.getPublishedDate()
        else
            holder.dateTV.visibility = View.GONE
        if(currentBookInfo.getAuthors() != "[]" && currentBookInfo.getAuthors() != "[, ]" && currentBookInfo.getAuthors() != "[, , ]" && currentBookInfo.getAuthors() != "[, , , ]" && currentBookInfo.getAuthors() != "[, , , , ]")
            holder.authorsTV.text = currentBookInfo.getAuthors().toString().replace("[", "")?.replace("]", "")
        else
            holder.authorsTV.visibility = View.GONE
        if(currentBookInfo.getShelf() != "Read" || currentBookInfo.getStars() == "noStars")
            holder.ratingBar.visibility = View.GONE
        else
            holder.ratingBar.rating = currentBookInfo.getStars()!!.toFloat()

        Picasso.get().load(currentBookInfo.getThumbnail()?.replace("http", "https")).placeholder(com.example.bookshelf.R.drawable.library).noFade().into(holder.bookIV)
    }

    override fun getItemCount(): Int {
        return bookInfoArrayList.size
    }

    class BookViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val nameTV = itemView.findViewById<TextView>(R.id.idTVBookTitle)
        val publisherTV = itemView.findViewById<TextView>(R.id.idTVpublisher)
        val pageCountTV = itemView.findViewById<TextView>(R.id.idTVPageCount)
        val dateTV = itemView.findViewById<TextView>(R.id.idTVDate)
        val bookIV = itemView.findViewById<ImageView>(R.id.idIVbook)
        val authorsTV = itemView.findViewById<TextView>(R.id.idTVAuthors)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBarItem)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}
