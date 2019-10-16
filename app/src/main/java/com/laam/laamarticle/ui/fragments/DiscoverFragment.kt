package com.laam.laamarticle.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.HomeRecyclerViewAdapter
import com.laam.laamarticle.adapters.TagRecyclerViewAdapter
import com.laam.laamarticle.models.Category
import com.laam.laamarticle.models.Post
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.SharedPrefHelper
import kotlinx.android.synthetic.main.fragment_discover.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiscoverFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        discover_main_recyclerview.setHasFixedSize(true)
        discover_tag_recyclerview.setHasFixedSize(true)

        showTagData()
        tagClicked(data_cat)

        with(discover_searchview) {
            setDismissOnTouchOutside(true)
            setOnSearchConfirmedListener { searchView, query ->
                search = if (query == "") {
                    " "
                } else {
                    query
                }
                tagClicked(data_cat)
                searchView.collapse(true)
            }

        }

        discover_swipe_refresh.setOnRefreshListener {
            showTagData()
            data_cat = Category(0, "All")
            search = ""
            tagClicked(data_cat)
            discover_searchview.inputQuery = ""
        }
    }

    private var data_cat: Category = Category(0, "")
    private var search: String = " "

    private fun showTagData() {
        discover_swipe_refresh.isRefreshing = true
        val service =
            ServiceBuilder.buildService(PostService::class.java).getCategory()

        service.enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful) {
                    val categoryList: MutableList<Category> = mutableListOf()
                    categoryList.add(Category(0, "All"))
                    categoryList.addAll(response.body()!!)

                    discover_tag_recyclerview.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    val adapter =
                        TagRecyclerViewAdapter(categoryList, activity!!.applicationContext)
                    discover_tag_recyclerview.adapter = adapter
                    adapter.setOnItemClickCallback(object :
                        TagRecyclerViewAdapter.OnItemClickCallback {
                        override fun onItemClicked(data: Category) {
                            tagClicked(data)
                            data_cat = data
                        }
                    })
                } else {
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
                discover_swipe_refresh.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(activity, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
                discover_swipe_refresh.isRefreshing = false
            }
        })
    }

    private fun tagClicked(data: Category) {
        val pref = SharedPrefHelper(activity!!.applicationContext)

        val service: Call<List<Post>> =
            ServiceBuilder.buildService(PostService::class.java)
                .getPostDiscover(pref.getAccount().id, data.id, search)

        service.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    discover_main_recyclerview.layoutManager = LinearLayoutManager(activity)
                    val adapter =
                        HomeRecyclerViewAdapter(response.body()!!, activity!!.applicationContext, activity!!.supportFragmentManager)
                    discover_main_recyclerview.adapter = adapter
                } else {
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(activity, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        fun newInstance(): DiscoverFragment {
            val fragment = DiscoverFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
