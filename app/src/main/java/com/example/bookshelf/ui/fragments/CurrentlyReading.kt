package com.example.bookshelf.ui.fragments

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshelf.*
import com.example.bookshelf.databinding.FragmentCurrentlyReadingBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var binding: FragmentCurrentlyReadingBinding
private lateinit var currentlyReadingRecyclerView: RecyclerView
private lateinit var adapter: BookAdapter
private var bookCurrentlyReading: ArrayList<BookInfo>? = null
private lateinit var mAuth: FirebaseAuth
private var uid: String? = null
private lateinit var communicator: Communicator
private lateinit var mDBRef: DatabaseReference

class CurrentlyReading : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookCurrentlyReading = ArrayList()
        adapter = BookAdapter(this@CurrentlyReading.requireContext(), bookCurrentlyReading!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentlyReadingBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        communicator = activity as Communicator
        binding.icon.visibility = View.GONE
        binding.noBooks.visibility = View.GONE
        (activity as MainActivity?)!!.setActionBarTitle("Currently Reading")
        val items = arrayOf("Sort By", "Title", "Author")
        val arrayList = ArrayAdapter<String>(this@CurrentlyReading.requireContext(),
            R.layout.simple_spinner_dropdown_item, items)
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortBy.adapter = arrayList
        binding.sortBy.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var bookReadOld: ArrayList<BookInfo>? = ArrayList()
                for (i in 0 until bookCurrentlyReading!!.size)
                    bookReadOld!!.add(bookCurrentlyReading!![i])
                for (i in 0 until bookCurrentlyReading!!.size)
                    bookCurrentlyReading!!.removeLast()
                if(p2 == 1)
                    bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getTitle)))
                else if(p2 == 2)
                    bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getAuthors)))
                for (i in 0 until bookReadOld!!.size)
                    bookCurrentlyReading!!.add(bookReadOld!!.get(i))

                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        currentlyReadingRecyclerView = binding.currentlyReadingRecyclerView
        currentlyReadingRecyclerView.layoutManager = LinearLayoutManager(this@CurrentlyReading.requireContext(), RecyclerView.VERTICAL, false)
        currentlyReadingRecyclerView.adapter = adapter

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookCurrentlyReading?.clear()
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Currently Reading")
                        bookCurrentlyReading?.add(currentBook!!)
                }

                if(bookCurrentlyReading!!.size == 0){
                    binding.noBooks.visibility = View.VISIBLE
                    binding.icon.visibility = View.VISIBLE
                }

                adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        communicator.passReadBook(
                            bookCurrentlyReading!![position]?.getTitle(), bookCurrentlyReading!![position]?.getSubtitle(), bookCurrentlyReading!![position]?.getAuthors()!!.toString(),
                            bookCurrentlyReading!![position]?.getPublisher(),  bookCurrentlyReading!![position]?.getPublishedDate(),  bookCurrentlyReading!![position]?.getDescription(),
                            bookCurrentlyReading!![position]?.getPageCount(),  bookCurrentlyReading!![position]?.getThumbnail(), bookCurrentlyReading!![position]?.getShelf(),
                            bookCurrentlyReading!![position]?.getBid(), bookCurrentlyReading!![position]?.getStars(), "noReadDate"
                        )
                    }
                })
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CurrentlyReading.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        return view
    }
}