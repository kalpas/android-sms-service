package kalpas.expensetracker.view.suggestions;

import kalpas.expensetracker.R;
import kalpas.expensetracker.core.Transaction;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link SuggestionsFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link SuggestionsFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class SuggestionsFragment extends Fragment {
    private static final String           ARG_TRANSACTION = "transaction";
    public static final String            TAG             = "kalpas.expensetracker.view.suggestions.SuggestionsFragment";

    // TODO: Rename and change types of parameters
    private Transaction                   transactionModel;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     * @param trx transaction to analyze
     * @return A new instance of fragment SuggestionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SuggestionsFragment newInstance(Transaction trx) {
        SuggestionsFragment fragment = new SuggestionsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTION, trx);
        fragment.setArguments(args);
        return fragment;
    }

    public SuggestionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionModel = (Transaction) getArguments().getSerializable(ARG_TRANSACTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // try { //FIXME
        // mListener = (OnTagsSelectedListener) activity;
        // } catch (ClassCastException e) {
        // throw new ClassCastException(activity.toString() +
        // " must implement OnTagsSelectedListener");
        // }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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
        public void onFragmentInteraction(Uri uri);
    }

}
