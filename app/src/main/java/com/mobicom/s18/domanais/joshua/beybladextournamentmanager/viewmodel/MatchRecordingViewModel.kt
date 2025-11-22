package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Match Recording Screen.
 * Manages video upload operations for match recordings.
 */
class MatchRecordingViewModel(
    private val matchRepository: MatchRepository = MatchRepository()
) : ViewModel() {

    // Upload state
    private val _uploadState = MutableStateFlow<VideoUploadState>(VideoUploadState.Idle)
    val uploadState: StateFlow<VideoUploadState> = _uploadState.asStateFlow()

    /**
     * Upload a match video to Supabase Storage and save the download URL to Firestore.
     *
     * @param context The Android context needed to read the Uri
     * @param tournamentId The ID of the tournament
     * @param matchId The ID of the match
     * @param videoUri The local Uri of the video file to upload
     */
    fun uploadVideo(
        context: Context,
        tournamentId: String,
        matchId: String,
        videoUri: Uri
    ) {
        viewModelScope.launch {
            _uploadState.value = VideoUploadState.Loading

            val result = matchRepository.uploadMatchVideo(
                context = context,
                tournamentId = tournamentId,
                matchId = matchId,
                videoUri = videoUri
            )

            result.fold(
                onSuccess = { downloadUrl ->
                    _uploadState.value = VideoUploadState.Success(
                        message = "Video uploaded successfully!",
                        downloadUrl = downloadUrl
                    )
                },
                onFailure = { exception ->
                    _uploadState.value = VideoUploadState.Error(
                        exception.message ?: "Failed to upload video"
                    )
                }
            )
        }
    }

    /**
     * Reset upload state back to Idle.
     * Call this after showing success/error message or before starting a new upload.
     */
    fun resetUploadState() {
        _uploadState.value = VideoUploadState.Idle
    }
}

/**
 * UI state for video upload operations.
 */
sealed class VideoUploadState {
    object Idle : VideoUploadState()
    object Loading : VideoUploadState()
    data class Success(val message: String, val downloadUrl: String) : VideoUploadState()
    data class Error(val message: String) : VideoUploadState()
}

