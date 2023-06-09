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
import com.example.bookshelf.databinding.FragmentWantToReadBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var binding: FragmentWantToReadBinding
private lateinit var wantToReadRecyclerView: RecyclerView
private lateinit var adapter: BookAdapter
private var bookWantToRead: ArrayList<BookInfo>? = null
private lateinit var mAuth: FirebaseAuth
private var uid: String? = null
private lateinit var communicator: Communicator
private lateinit var mDBRef: DatabaseReference

class WantToRead: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookWantToRead = ArrayList()
        adapter = BookAdapter(this@WantToRead.requireContext(), bookWantToRead!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWantToReadBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        communicator = activity as Communicator
        binding.icon.visibility = View.GONE
        binding.noBooks.visibility = View.GONE
        (activity as MainActivity?)!!.setActionBarTitle("Want To Read")
        val items = arrayOf("Sort By", "Title", "Author")
        val arrayList = ArrayAdapter<String>(this@WantToRead.requireContext(),
            R.layout.simple_spinner_dropdown_item, items)
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortBy.adapter = arrayList
        binding.sortBy.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var bookReadOld: ArrayList<BookInfo>? = ArrayList()
                for (i in 0 until bookWantToRead!!.size)
                    bookReadOld!!.add(bookWantToRead!![i])
                for (i in 0 until bookWantToRead!!.size)
                    bookWantToRead!!.removeLast()
                if(p2 == 1)
                    bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getTitle)))
                else if(p2 == 2)
                    bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getAuthors)))
                for (i in 0 until bookReadOld!!.size)
                    bookWantToRead!!.add(bookReadOld!!.get(i))

                adapter.notifyDataSetChanged()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        wantToReadRecyclerView = binding.wantToReadRecyclerView
        wantToReadRecyclerView.layoutManager = LinearLayoutManager(this@WantToRead.requireContext(), RecyclerView.VERTICAL, false)
        wantToReadRecyclerView.adapter = adapter

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookWantToRead?.clear()
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Want To Read")
                        bookWantToRead?.add(currentBook!!)
                }
                if(bookWantToRead!!.size == 0){
                    binding.noBooks.visibility = View.VISIBLE
                    binding.icon.visibility = View.VISIBLE
                }

                adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        communicator.passReadBook(
                            bookWantToRead!![position]?.getTitle(), bookWantToRead!![position]?.getSubtitle(), bookWantToRead!![position]?.getAuthors()!!.toString(),
                            bookWantToRead!![position]?.getPublisher(),  bookWantToRead!![position]?.getPublishedDate(),  bookWantToRead!![position]?.getDescription(),
                            bookWantToRead!![position]?.getPageCount(),  bookWantToRead!![position]?.getThumbnail(), bookWantToRead!![position]?.getShelf(),
                            bookWantToRead!![position]?.getBid(), bookWantToRead!![position]?.getStars(), "noReadDate"
                        )
                    }
                })
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@WantToRead.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        return view
    }
}