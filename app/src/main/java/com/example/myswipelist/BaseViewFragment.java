package com.example.myswipelist;

import android.app.Fragment;
import android.net.Uri;
import android.widget.Button;

abstract public class BaseViewFragment extends Fragment {
	
	
	
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		
		public void onFragmentInteraction(boolean isRight);
	}
	
	
	
}
