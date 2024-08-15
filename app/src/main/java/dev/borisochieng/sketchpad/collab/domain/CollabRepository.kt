package dev.borisochieng.sketchpad.collab.domain

import android.net.Uri
import dev.borisochieng.sketchpad.auth.data.FirebaseResponse
import dev.borisochieng.sketchpad.collab.data.models.BoardDetails
import dev.borisochieng.sketchpad.collab.data.models.DBPathProperties
import dev.borisochieng.sketchpad.collab.data.models.DBSketch
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.PathProperties
import kotlinx.coroutines.flow.Flow

interface CollabRepository {

    suspend fun createSketch(userId: String, sketch: DBSketch): FirebaseResponse<BoardDetails>

    suspend fun fetchExistingSketches(userId: String): FirebaseResponse<List<Sketch>>

    suspend fun listenForSketchChanges(userId: String, boardId: String): Flow<FirebaseResponse<List<PathProperties>>>

    suspend fun fetchSingleSketch(userId: String, boardId: String): FirebaseResponse<Sketch>

    suspend fun updatePathInDB(userId: String, boardId: String, paths: List<DBPathProperties>, pathIds: List<String>): FirebaseResponse<String>

    fun generateCollabUrl(userId: String, boardId: String): Uri



}