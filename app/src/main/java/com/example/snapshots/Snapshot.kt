package com.example.snapshots

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//@IgnoreExtraProperties
//data class Snapshot(
//    @get:Exclude var id: String = "", //que no exista en la bd de fb
//    var title: String = "",
//    var photoUrl: String = "",
//    var likeList: Map<String, Boolean>  = mutableMapOf()
//)
@IgnoreExtraProperties
data class Snapshot(@get:Exclude var id: String = "",
                    var ownerUid: String = "",
                    var title: String = "",
                    var photoUrl: String ="",
                    var likeList: Map<String, Boolean> = mutableMapOf())
