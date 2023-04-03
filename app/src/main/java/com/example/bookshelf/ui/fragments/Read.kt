package com.example.bookshelf.ui.fragments

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
import com.example.bookshelf.R
import com.example.bookshelf.databinding.FragmentReadBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var binding: FragmentReadBinding
private lateinit var readRecyclerView: RecyclerView
private lateinit var adapter: BookAdapter
private var bookRead: ArrayList<BookInfo>? = null
private lateinit var mAuth: FirebaseAuth
private var uid: String? = null
private lateinit var communicator: Communicator
private lateinit var mDBRef: DatabaseReference

class Read : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookRead = ArrayList()
        adapter = BookAdapter(this@Read.requireContext(), bookRead!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReadBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()
        communicator = activity as Communicator
        binding.icon.visibility = View.GONE
        binding.noBooks.visibility = View.GONE
        (activity as MainActivity?)!!.setActionBarTitle("Read")
        val items = arrayOf("Sort By", "Title", "Author")
        val arrayList = ArrayAdapter<String>(this@Read.requireContext(),android.R.layout.simple_spinner_dropdown_item, items)
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortBy.adapter = arrayList
        binding.sortBy.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                orderBookList()
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        readRecyclerView = binding.readRecyclerView
        readRecyclerView.layoutManager = LinearLayoutManager(this@Read.requireContext(), RecyclerView.VERTICAL, false)
        readRecyclerView.adapter = adapter

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(mAuth.currentUser!!.uid).child("book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookRead?.clear()
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == "Read")
                        bookRead?.add(currentBook!!)
                }

                if(bookRead!!.size == 0){
                    binding.noBooks.visibility = View.VISIBLE
                    binding.icon.visibility = View.VISIBLE
                }

                adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        communicator.passReadBook(
                            bookRead!![position]?.getTitle(), bookRead!![position]?.getSubtitle(), bookRead!![position]?.getAuthors()!!.toString(),
                            bookRead!![position]?.getPublisher(),  bookRead!![position]?.getPublishedDate(),  bookRead!![position]?.getDescription(),
                            bookRead!![position]?.getPageCount(),  bookRead!![position]?.getThumbnail(), bookRead!![position]?.getShelf(),
                            bookRead!![position]?.getBid(), bookRead!![position]?.getStars(), bookRead!![position]?.getReadDate()
                        )
                    }
                })
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Read.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })

        return view
    }

    fun orderBookList(){
        var bookReadOld: ArrayList<BookInfo>? = ArrayList()
        for (i in 0 until bookRead!!.size)
            bookReadOld!!.add(bookRead!![i])
        for (i in 0 until bookRead!!.size)
            bookRead!!.removeLast()
        if(binding.sortBy.selectedItemPosition == 1)
            bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getTitle)))
        else if(binding.sortBy.selectedItemPosition == 2)
            bookReadOld = ArrayList(bookReadOld!!.sortedWith(compareBy(BookInfo::getAuthors)))
        for (i in 0 until bookReadOld!!.size)
            bookRead!!.add(bookReadOld!!.get(i))
    }
}