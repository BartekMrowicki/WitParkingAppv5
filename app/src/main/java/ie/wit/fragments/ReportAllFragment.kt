package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.adapters.SubscriptionAdapter

import ie.wit.adapters.SubscriptionListener
import ie.wit.models.SubscriptionModel
import kotlinx.android.synthetic.main.fragment_report.view.*

class ReportAllFragment : ReportFragment(),
    SubscriptionListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_report, container, false)
        activity?.title = getString(R.string.menu_report_all)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))

        var query = FirebaseDatabase.getInstance()
            .reference.child("subscription")

        var options = FirebaseRecyclerOptions.Builder<SubscriptionModel>()
            .setQuery(query, SubscriptionModel::class.java)
            .setLifecycleOwner(this)
            .build()

        root.recyclerView.adapter = SubscriptionAdapter(options, this)

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReportAllFragment().apply {
                arguments = Bundle().apply { }
            }
    }
}
