package xyz.neopandu.moov.flow.main.favorite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.neopandu.moov.R
import xyz.neopandu.moov.flow.main.OnListFragmentInteractionListener

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

        initToolbar()

        return view
    }

    private fun initToolbar() {
        toolbar.setSubtitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_change_settings -> {
                    val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(mIntent)
                }
            }
            super.onOptionsItemSelected(it)
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