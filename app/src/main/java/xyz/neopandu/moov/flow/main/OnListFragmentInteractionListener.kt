package xyz.neopandu.moov.flow.main

import xyz.neopandu.moov.models.Movie


/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 *
 *
 * See the Android Training lesson
 * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
 * for more information.
 */
interface OnListFragmentInteractionListener {
    fun onListFragmentInteraction(item: Movie)
    fun toggleFavoriteButtonClicked(item: Movie)
}