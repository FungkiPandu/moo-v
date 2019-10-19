package xyz.neopandu.moov.flow.main.favorite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener
import xyz.neopandu.moov.flow.search.SearchActivity
import xyz.neopandu.moov.flow.setting.PreferenceActivity

class FavoriteFragment : Fragment() {
    private var listener: OnListFragmentInteractionListener? = null

    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        toolbar = view.findViewById(R.id.fav_toolbar)
        viewPager = view.findViewById(R.id.fav_view_pager)
        tabLayout = view.findViewById(R.id.fav_tab_layout)

        viewPager.adapter = FavoritePagerAdapter(requireContext(), childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        setupToolbar()

        return view
    }

    private fun setupToolbar() {
        with(toolbar) {
            setTitle(R.string.title_favorite)
            setSubtitle(R.string.app_name)
            inflateMenu(R.menu.main_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_search -> {
                        startActivity(Intent(requireActivity(), SearchActivity::class.java))
                    }
                    R.id.action_preferences -> {
                        startActivity(Intent(requireActivity(), PreferenceActivity::class.java))
                    }
                }
                super.onOptionsItemSelected(it)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
