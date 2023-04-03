package com.example.bookshelf

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.bookshelf.databinding.ActivityMainBinding
import com.example.bookshelf.ui.fragments.*

class MainActivity : AppCompatActivity(), Communicator {

    private lateinit var binding: ActivityMainBinding
    val myBooksFragment = MyBooks()
    val searchFragment = SearchBook()
    val friendsFragment = Friends()
    val friendFragment = Friend()
    val bookDetails= BookDetails()
    val readFragment = Read()
    val currentlyReadingFragment = CurrentlyReading()
    val wantToReadFragment = WantToRead()
    val bookReadDetails = BookReadDetails()
    val readingChallengeFragment = ReadingChallenge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeCurrentFragment(myBooksFragment)

        supportFragmentManager.addOnBackStackChangedListener {
            val stackHeight = supportFragmentManager.backStackEntryCount
            if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                supportActionBar!!.setHomeButtonEnabled(true)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportActionBar!!.setHomeButtonEnabled(false)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.myBooks -> {
                    clearStack()
                    makeCurrentFragment(myBooksFragment)
                }
                R.id.search -> {
                    clearStack()
                    makeCurrentFragment(searchFragment)
                }
                R.id.friends -> {
                    clearStack()
                    makeCurrentFragment(friendsFragment)
                }
            }
            true
        }
    }

    fun clearStack(){
        val count: Int = supportFragmentManager.getBackStackEntryCount()
        for (i in 0 until count) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, fragment)
            commit()
        }
    }

    fun setActionBarTitle(title: String?) {
        setTitle(title)
    }

    fun getFriendFragment(): Fragment{
        return friendFragment
    }

    fun getReadFragment(): Fragment{
        return readFragment
    }

    fun getCurrentlyReadingFragment(): Fragment{
        return currentlyReadingFragment
    }

    fun getWantToReadFragment(): Fragment{
        return wantToReadFragment
    }

    fun getReadingChallengeFragment(): Fragment{
        return readingChallengeFragment
    }

    fun getmyBooksFragment(): Fragment{
        return myBooksFragment
    }

    fun selectMyBooks(){
        binding.bottomNavigation.setSelectedItemId(R.id.myBooks)
    }

    override fun passSearchBook(
        title: String?,
        subTitle: String?,
        authors: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        pageCount: String?,
        thumbnail: String?,
        shelf: String?,
        bid: String?,
        stars: String?,
        readDate: String?
    ) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("authors", authors)
        bundle.putString("subTitle", subTitle)
        bundle.putString("publisher", publisher)
        bundle.putString("publishedDate", publishedDate)
        bundle.putString("description", description)
        bundle.putString("pageCount", pageCount)
        bundle.putString("thumbnail", thumbnail)
        bundle.putString("shelf", shelf)
        bundle.putString("bid", bid)
        bundle.putString("stars", stars)
        bundle.putString("readDate", readDate)

        bookDetails.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, bookDetails)
            addToBackStack(null)
            commit()
        }
    }

    override fun passReadBook(
        title: String?,
        subTitle: String?,
        authors: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        pageCount: String?,
        thumbnail: String?,
        shelf: String?,
        bid: String?,
        stars: String?,
        readDate: String?
    ) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("authors", authors)
        bundle.putString("subTitle", subTitle)
        bundle.putString("publisher", publisher)
        bundle.putString("publishedDate", publishedDate)
        bundle.putString("description", description)
        bundle.putString("pageCount", pageCount)
        bundle.putString("thumbnail", thumbnail)
        bundle.putString("shelf", shelf)
        bundle.putString("bid", bid)
        bundle.putString("stars", stars)
        bundle.putString("readDate", readDate)

        bookReadDetails.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, bookReadDetails)
            addToBackStack(null)
            commit()
        }
    }

    override fun passNickname(nickname: String?) {
        val bundle = Bundle()
        bundle.putString("nickname", nickname)
        friendFragment.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, friendFragment)
            addToBackStack(null)
            commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
