package com.example.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseArray
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class HomeFragment : Fragment(), FragmentAux {
//    private val PATH_FB = "snapshots"
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mSnapshotsRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Mejoras
        setupFirebase()
        setupAdapter()
        //mine
//        val query = FirebaseDatabase.getInstance().reference.child(PATH_FB)
//        val options =
////            FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, Snapshot::class.java)
////                .build()
//            FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, SnapshotParser {
//                val snapshot = it.getValue(Snapshot::class.java)
//                snapshot!!.id = it.key!!
//                snapshot
//            }).build()
//        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options) {
//            private lateinit var mContext: Context
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
//                mContext = parent.context
//                val view =
//                    LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false)
//                return SnapshotHolder(view)
//            }
//
//            /*
//            *
//            ADAPTADOR
//            *
//             */
//            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
//                val snapshot = getItem(position)
//                with(holder) {
//                    setListener(snapshot)
//                    binding.tvTitle.text = snapshot.title
//                    binding.cbLike.text = snapshot.likeList.keys.size.toString()
//                    FirebaseAuth.getInstance().currentUser?.let {
//                        binding.cbLike.isChecked = snapshot.likeList.containsKey(it.uid)
//                    }
//                    Glide.with(mContext)
//                        .load(snapshot.photoUrl)
//                        .centerCrop()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(binding.imgPhoto)
//                }
//            }
//
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onDataChanged() { // cuando se carga la data borramos el progress bar
//                super.onDataChanged()
//                mBinding.progressBar.visibility = View.GONE
//                notifyDataSetChanged()
//            }
//
//            override fun onError(error: DatabaseError) { // Notificacion si hay error
//                super.onError(error)
//                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
//            }
//        }
        setupRecyclerView()
//        mLayoutManager = LinearLayoutManager(context)
//        mBinding.recyclerView.apply {
//            setHasFixedSize(true)
//            layoutManager = mLayoutManager
//            adapter = mFirebaseAdapter
//        }
    }

    private fun setupFirebase() {
        mSnapshotsRef =
            FirebaseDatabase.getInstance().reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    private fun setupAdapter() {
        val query = mSnapshotsRef

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query) {
            val snapshot = it.getValue(Snapshot::class.java)
            snapshot!!.id = it.key!!
            snapshot
        }.build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options) {
            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context

                val view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)

                with(holder) {
                    setListener(snapshot)

                    with(binding) {
                        tvTitle.text = snapshot.title
                        cbLike.text = snapshot.likeList.keys.size.toString()
                        cbLike.isChecked = snapshot.likeList
                            .containsKey(SnapshotsApplication.currentUser.uid)

                        Glide.with(mContext)
                            .load(snapshot.photoUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imgPhoto)
                        btnDelete.visibility = if (model.ownerUid == SnapshotsApplication.currentUser.uid){
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                //Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
                Snackbar.make(mBinding.root, error.message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupRecyclerView() {
        mLayoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.apply {
            setHasFixedSize(true) //Dimension fija
            layoutManager = mLayoutManager //aplicar linearLayoutManager
            adapter = mFirebaseAdapter //aplicar el adaptador
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }


    private fun deleteSnapshot(snapshot: Snapshot) {
//        SnapshotsApplication.currentUser = mFirebaseAuth?.currentUser
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                    val storageSnapshotsRef = FirebaseStorage.getInstance().reference
                        .child(SnapshotsApplication.PATH_SNAPSHOTS)
                        .child(SnapshotsApplication.currentUser.uid)
                        .child(snapshot.id)
                    storageSnapshotsRef.delete().addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            mSnapshotsRef.child(snapshot.id).removeValue()
                        } else {
                            Snackbar.make(
                                mBinding.root, getString(R.string.home_delete_photo_error),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .setNegativeButton(R.string.dialog_delete_cancel, null)
                .show()
        }
//        val databaseReference = FirebaseDatabase.getInstance().reference.child(PATH_FB)
//        databaseReference.child(snapshot.id).removeValue()
    }
    private fun setLike(snapshot: Snapshot, checked: Boolean) {
//        val databaseReference = FirebaseDatabase.getInstance().reference.child(PATH_FB)
//        if (checked) {
//            databaseReference.child(snapshot.id).child("likeList")
//                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(checked)
//        } else {
//            databaseReference.child(snapshot.id).child("likeList")
//                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
//        }
        val myUserRef = mSnapshotsRef.child(snapshot.id)
            .child(SnapshotsApplication.PROPERTY_LIKE_LIST)
            .child(SnapshotsApplication.currentUser.uid)

        if (checked) {
            myUserRef.setValue(checked)
        } else {
            myUserRef.setValue(null)
        }
    }
    /*
    * FragmentAux
    * */
    override fun refresh() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    //VIEWHOLDER
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSnapshotBinding.bind(view)
        fun setListener(snapshot: Snapshot) {
            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }
            binding.cbLike.setOnCheckedChangeListener { _, isChecked ->
                setLike(snapshot, isChecked)
            }
        }
    }


}