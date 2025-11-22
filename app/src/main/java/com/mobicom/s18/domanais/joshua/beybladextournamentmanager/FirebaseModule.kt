package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

object FirebaseModule {

    // Provides the FirebaseAuth instance
    val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    // Provides the FirebaseFirestore instance
    val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    // Provides the FirebaseStorage instance
    val storage: FirebaseStorage by lazy {
        Firebase.storage
    }

}