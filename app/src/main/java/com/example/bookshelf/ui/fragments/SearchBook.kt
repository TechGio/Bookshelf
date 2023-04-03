package com.example.bookshelf.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookshelf.*
import com.example.bookshelf.databinding.FragmentSearchBookBinding
import com.example.bookshelf.ui.book.BookInfo
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class SearchBook : Fragment() {

    private var mRequestQueue: RequestQueue? = null
    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    private var progressBar: ProgressBar? = null
    private var searchEdt: EditText? = null
    private var searchBtn: ImageButton? = null
    private lateinit var binding: FragmentSearchBookBinding
    private lateinit var searchBookRecyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var communicator: Communicator
    private lateinit var mAuth: FirebaseAuth
    private var shelf: String? = null
    private var uid: String? = null
    private var stars: String = "noStars"
    private var readDate: String = "noReadDate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookInfoArrayList = ArrayList()
        adapter = BookAdapter(this@SearchBook.requireContext(), bookInfoArrayList!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBookBinding.inflate(inflater, container, false)
        val view: View = binding.root
        communicator = activity as Communicator

        searchBookRecyclerView = binding.idRVBooks
        searchBookRecyclerView.layoutManager = LinearLayoutManager(this@SearchBook.requireContext(), RecyclerView.VERTICAL, false)
        searchBookRecyclerView.adapter = adapter
        (activity as MainActivity?)!!.setActionBarTitle("Search")

        progressBar = binding.idLoadingPB
        searchEdt = binding.idEdtSearchBooks
        searchBtn = binding.idBtnSearch

        searchBtn!!.setOnClickListener(View.OnClickListener {
            progressBar!!.visibility = View.VISIBLE

            for (i in 0 until bookInfoArrayList!!.size)
                bookInfoArrayList!!.removeLast()

            if (searchEdt!!.text.toString().isEmpty()) {
                searchEdt!!.error = "Please enter search query"
                return@OnClickListener
            }
            getBooksInfo(searchEdt!!.text.toString())
            adapter.notifyDataSetChanged()
        })

        adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                communicator.passSearchBook(
                    bookInfoArrayList!![position].getTitle(), bookInfoArrayList!![position].getSubtitle(), bookInfoArrayList!![position].getAuthors()!!.toString(),
                    bookInfoArrayList!![position].getPublisher(),  bookInfoArrayList!![position].getPublishedDate(),  bookInfoArrayList!![position].getDescription(),
                    bookInfoArrayList!![position].getPageCount(),  bookInfoArrayList!![position].getThumbnail(), "noShelf",
                    bookInfoArrayList!![position].getBid(), bookInfoArrayList!![position].getStars(), bookInfoArrayList!![position].getReadDate())
            }
        })
        adapter.notifyDataSetChanged()
        return view
    }

    private fun getBooksInfo(query: String) {
        mRequestQueue = Volley.newRequestQueue(this@SearchBook.requireContext())
        mRequestQueue!!.cache.clear()
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"
        val queue: RequestQueue = Volley.newRequestQueue(this@SearchBook.requireContext())

        val booksObjrequest = JsonObjectRequest(Request.Method.GET, url, null, object: Response.Listener<JSONObject?> {
            override fun onResponse(response: JSONObject?) {
                progressBar!!.visibility = View.GONE
                try {
                    val itemsArray = response?.getJSONArray("items")
                    for (i in 0 until itemsArray!!.length()) {
                        val itemsObj = itemsArray.getJSONObject(i)
                        val volumeObj = itemsObj.getJSONObject("volumeInfo")
                        val title = volumeObj.optString("title")
                        val subtitle = volumeObj.optString("subtitle")
                        val authorsArray = volumeObj.getJSONArray("authors")
                        val publisher = volumeObj.optString("publisher")
                        val publishedDate = volumeObj.optString("publishedDate")
                        val description = volumeObj.optString("description")
                        val pageCount = volumeObj.optInt("pageCount")
                        val imageLinks = volumeObj.optJSONObject("imageLinks")
                        val thumbnail = imageLinks?.optString("thumbnail")
                        val bid = itemsObj.optString("id")
                        val authorsArrayList: ArrayList<String> = ArrayList()
                        if (authorsArray.length() != 0) {
                            for (j in 0 until authorsArray.length()) {
                                authorsArrayList.add(authorsArray.optString(i))
                            }
                        }
                        val bookInfo = BookInfo(
                            title,
                            subtitle,
                            authorsArrayList.toString(),
                            publisher,
                            publishedDate,
                            description,
                            pageCount.toString(),
                            thumbnail,
                            bid,
                            shelf,
                            stars,
                            uid,
                            readDate
                        )
                        bookInfoArrayList!!.add(bookInfo)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Toast.makeText(this@SearchBook.requireContext(), "An error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        queue.add(booksObjrequest)
    }
}