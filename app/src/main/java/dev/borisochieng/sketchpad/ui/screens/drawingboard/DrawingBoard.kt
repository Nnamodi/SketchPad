package dev.borisochieng.sketchpad.ui.screens.drawingboard

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.screens.dialog.NameSketchDialog
import dev.borisochieng.sketchpad.ui.screens.dialog.SavePromptDialog
import dev.borisochieng.sketchpad.ui.screens.dialog.Sizes
import dev.borisochieng.sketchpad.ui.screens.drawingboard.chat.ChatDialog
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.MovableTextBox
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.MoveableShapeBox
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.PaletteMenu
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.PaletteTopBar
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.ShapeOptions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.CanvasUiEvents
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.CanvasUiState
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.ShapeProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.SketchPadActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.TextProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.DrawMode
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.ExportOption
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.rememberDrawController
import dev.borisochieng.sketchpad.utils.SHAPE
import dev.borisochieng.sketchpad.utils.TEXT
import dev.borisochieng.sketchpad.utils.VOID_ID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID.randomUUID

@Composable
fun DrawingBoard(
    uiState: CanvasUiState, // passing the state in the way fixes state management issue
    exportSketch: (Bitmap) -> Unit,
    actions: (SketchPadActions) -> Unit,
    exportSketchAsPdf: (Bitmap) -> Unit,
    navigate: (Screens) -> Unit,
    onBroadCastUrl: (Uri) -> Unit,
    viewModel: SketchPadViewModel = koinViewModel(),
    boardId: String,
    userId: String,
    isFromCollabUrl: Boolean
) {
    val (userIsLoggedIn, _, _, _, sketch, collabUrl) = uiState
    val drawController = rememberDrawController()
    var drawMode by remember { mutableStateOf(DrawMode.Draw) }
    var exportOption by remember { mutableStateOf(ExportOption.PNG) }

    val absolutePaths = remember { mutableStateListOf<PathProperties>() }
    var paths by remember { mutableStateOf<List<PathProperties>>(emptyList()) }

    val absoluteTexts = remember { mutableStateListOf<TextProperties>() }
    var texts by remember { mutableStateOf<List<TextProperties>>(emptyList()) }
    val showNewTextBox = remember { mutableStateOf(false) }

    val absoluteShapes = remember { mutableStateListOf<ShapeProperties>() }
    var shapes by remember { mutableStateOf<List<ShapeProperties>>(emptyList()) }
    val newShapeBox = remember { mutableStateOf<ShapeOptions?>(null) }

    var pencilSize by remember { mutableFloatStateOf(Sizes.Small.strokeWidth) }
    var color by remember { mutableStateOf(Color.Black) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val chatVisible = remember { mutableStateOf(false) }
    val isExport = remember { mutableStateOf(false) }

    val openNameSketchDialog = rememberSaveable { mutableStateOf(false) }
    val openSavePromptDialog = rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val save: (String?) -> Unit = { name ->
        val action = if (name == null) {
            SketchPadActions.UpdateSketch(paths, texts)
        } else {
            openNameSketchDialog.value = false
            val newSketch = Sketch(
                name = name,
                pathList = paths,
//                shapeList = shapes,
                textList = texts
            )
            SketchPadActions.SaveSketch(newSketch)
        }
        actions(action)
        Toast.makeText(context, "Sketch saved", Toast.LENGTH_SHORT).show()
        navigate(Screens.Back)
    }

    val addWidgetToPaths: (String, String) -> Unit = { id, type ->
        val isShape = type == SHAPE
        val widgetPath = PathProperties(id = id, shapeMode = isShape, textMode = !isShape)
        paths += widgetPath
        absolutePaths.clear()
        absolutePaths.addAll(paths)
    }
    val removeWidgetFromPaths: (String) -> Unit = { id ->
        val existingWidgetPath = paths.first { it.id == id }
        paths -= existingWidgetPath
        absolutePaths -= existingWidgetPath
        // add it to undo/redo history
        absolutePaths.add(paths.size, existingWidgetPath)
    }

    val uiEvents by viewModel.uiEvents.collectAsState(initial = null)

    //listen for path changes
    LaunchedEffect(uiState.paths) {
        if (!userIsLoggedIn) return@LaunchedEffect
        //update local paths with collaborative paths
        absolutePaths.clear()
        paths = uiState.paths
        absolutePaths.addAll(paths)
    }

//    //update paths in db
//    LaunchedEffect(paths) {
//        if (!userIsLoggedIn) {
//            return@LaunchedEffect
//        } else if (uiState.sketchIsBackedUp) {
//            delay(300)
//            pathsBuffer.addAll(paths)
//            viewModel.updatePathInDb(paths = pathsBuffer, userId = userId, boardId = boardId)
//        }
//    }

    LaunchedEffect(uiEvents) {
        uiEvents?.let { event ->
            when (event) {
                is CanvasUiEvents.SnackBarEvent -> {
                    // Showing Snackbar with the message
                    snackbarHostState.showSnackbar(event.message)
                }
                // Handle other events if any
            }
        }
    }

    LaunchedEffect(Unit) { actions(SketchPadActions.CheckIfUserIsLoggedIn) }

    Scaffold(
        topBar = {
            PaletteTopBar(
                canSave = paths != sketch?.pathList,
                canUndo = paths.isNotEmpty(),
                canRedo = paths.size < absolutePaths.size,
                onSaveClicked = {
                    if (sketch == null) {
                        openNameSketchDialog.value = true
                    } else {
                        save(null)
                    }
                },
                unUndoClicked = {
                    when {
                        paths.last().shapeMode && shapes.isNotEmpty() -> {
                            shapes -= shapes.last()
                        }
                        paths.last().textMode && texts.isNotEmpty() -> {
                            texts -= texts.last()
                        }
                    }
                    paths -= paths.last()
                },
                unRedoClicked = {
                    val nextPath = absolutePaths[paths.size]
                    when {
                        nextPath.shapeMode && shapes.size != absoluteShapes.size -> {
                            shapes += absoluteShapes[shapes.size]
                        }
                        nextPath.textMode && texts.size != absoluteTexts.size -> {
                            texts += absoluteTexts[texts.size]
                        }
                    }
                    paths += nextPath
                },
                onExportClicked = {
                    drawController.saveBitmap()
                    isExport.value = false
                },
                onBroadCastUrl = {
                    if (userIsLoggedIn) {
                        sketch?.let {
                            if (!it.isBackedUp || collabUrl == null) {
                                scope.launch { snackbarHostState.showSnackbar("Sketch is not backed up yet") }
                                return@PaletteTopBar
                            }
                            //isCollabUrlShared = true
                            onBroadCastUrl(collabUrl)
                        }
                    } else {
                        scope.launch {
                            val action = snackbarHostState.showSnackbar(
                                message = "Sign up to avail collaborative feature",
                                actionLabel = "SIGN UP", duration = SnackbarDuration.Short
                            )
                            if (action != SnackbarResult.ActionPerformed) return@launch
                            navigate(Screens.OnBoardingScreen)
                        }
                    }
                },
                onExportClickedAsPdf = {
                    exportOption = ExportOption.PDF
                    drawController.saveBitmap()
                    isExport.value = false
                },
                menuExpanded = { value ->
                    isExport.value = value
                }

            )
        },
        bottomBar = {
            PaletteMenu(
                drawMode = drawMode,
                selectedColor = color,
                pencilSize = pencilSize,
                onColorChanged = { color = it },
                onShapePicked = { newShapeBox.value = it },
                onSizeChanged = { pencilSize = it },
                onDrawModeChanged = {
                    drawMode = it
                    if (it == DrawMode.Text) showNewTextBox.value = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        floatingActionButton = {
            if (userIsLoggedIn && isFromCollabUrl&& !isExport.value) {
                FloatingActionButton(
                    onClick = { chatVisible.value = true },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat"
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        LaunchedEffect(sketch) {
            if (sketch == null) return@LaunchedEffect
            // paths
            absolutePaths.clear(); paths = emptyList()
            absolutePaths.addAll(sketch.pathList)
            paths = sketch.pathList
            // shapes
//            absoluteShapes.clear(); shapes = emptyList()
//            absoluteShapes.addAll(sketch.shapeList)
//            shapes = sketch.shapeList
            // texts
            absoluteTexts.clear(); texts = emptyList()
            absoluteTexts.addAll(sketch.textList)
            texts = sketch.textList
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            val state = rememberTransformableState { zoomChange, panChange, _ ->
                if (drawMode != DrawMode.Touch) return@rememberTransformableState
                scale = (scale * zoomChange).coerceIn(1f, 5f)

                val extraWidth = (scale - 1) * constraints.maxWidth
                val extraHeight = (scale - 1) * constraints.maxHeight

                val maxX = extraWidth / 2
                val maxY = extraHeight / 2

                offset = Offset(
                    x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                    y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
                )
            }

            AndroidView(
                factory = {
                    ComposeView(context).apply {
                        setContent {
                            LaunchedEffect(drawController) {
                                drawController.trackBitmaps(
                                    this@apply, this,
                                    onCaptured = { imageBitmap, _ ->
                                        imageBitmap?.let { bitmap ->
                                            when (exportOption) {
                                                ExportOption.PNG -> {
                                                    exportSketch(bitmap.asAndroidBitmap())

                                                }
                                                ExportOption.PDF -> {
                                                    exportSketchAsPdf(bitmap.asAndroidBitmap())

                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .transformable(state)
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .pointerInput(drawMode) {
                                            if (drawMode !in setOf(DrawMode.Draw, DrawMode.Erase)) return@pointerInput
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val path = PathProperties(
                                                    id = randomUUID().toString(), //generate id for each new path
                                                    color = if (drawMode == DrawMode.Erase) Color.White else color,
                                                    start = change.position - dragAmount,
                                                    end = change.position,
                                                    strokeWidth = pencilSize
                                                )

                                                //update paths locally
                                                paths += path
                                                absolutePaths.clear()
                                                absolutePaths.addAll(paths)

                                                //send path updates to Firebase for collab
                                                if (isFromCollabUrl && userId != VOID_ID && boardId != VOID_ID) {
                                                    viewModel.updatePathInDb(
                                                        paths = paths,
                                                        userId = userId,
                                                        boardId = boardId
                                                    )
                                                }
                                            }
                                        }
                                ) {
                                    paths
                                        .filterNot { it.shapeMode || it.textMode }
                                        .forEach { path ->
                                            drawLine(
                                                color = path.color,
                                                start = path.start,
                                                end = path.end,
                                                strokeWidth = path.strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                }

                                texts.forEach { property ->
                                    MovableTextBox(
                                        properties = property,
                                        active = false,
                                        onRemove = { texts -= it; removeWidgetFromPaths(it.id) },
                                        onUpdate = { text ->
                                            removeWidgetFromPaths(text.id)
                                            val existingText = texts.first { it.id == text.id }
                                            texts -= existingText
                                            texts += text
                                            addWidgetToPaths(text.id, TEXT)
                                        }
                                    )
                                }
                                if (showNewTextBox.value) {
                                    MovableTextBox(
                                        active = true,
                                        onRemove = {
                                            showNewTextBox.value = false
                                            drawMode = DrawMode.Draw
                                        },
                                        onFinish = {
                                            texts += it
                                            absoluteTexts.clear()
                                            absoluteTexts.addAll(texts)
                                            addWidgetToPaths(it.id, TEXT)
                                            showNewTextBox.value = false
                                            drawMode = DrawMode.Draw
                                        }
                                    )
                                }

                                shapes.forEach { property ->
                                    MoveableShapeBox(
                                        properties = property,
                                        active = false,
                                        onRemove = { shapes -= it; removeWidgetFromPaths(it.id) },
                                        onUpdate = { shape ->
                                            removeWidgetFromPaths(shape.id)
                                            val existingShape = shapes.first { it.id == shape.id }
                                            shapes -= existingShape
                                            shapes += shape
                                            addWidgetToPaths(shape.id, SHAPE)
                                        }
                                    )
                                }
                                if (newShapeBox.value != null) {
                                    MoveableShapeBox(
                                        properties = ShapeProperties(shape = newShapeBox.value!!),
                                        active = true,
                                        onRemove = { newShapeBox.value = null },
                                        onFinish = {
                                            shapes += it
                                            absoluteShapes.clear()
                                            absoluteShapes.addAll(shapes)
                                            addWidgetToPaths(it.id, SHAPE)
                                            newShapeBox.value = null
                                        }
                                    )
                                }

                                LaunchedEffect(newShapeBox.value) {
                                    drawMode = if (newShapeBox.value == null) DrawMode.Draw else DrawMode.Shape
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (openNameSketchDialog.value) {
            NameSketchDialog(
                onNamed = { name -> save(name) },
                onDismiss = { openNameSketchDialog.value = false }
            )
        }

        if (openSavePromptDialog.value) {
            val sketchIsNew = sketch == null
            SavePromptDialog(
                sketchIsNew = sketchIsNew,
                onSave = {
                    if (sketchIsNew) {
                        openNameSketchDialog.value = true
                    } else {
                        save(null)
                    }
                },
                onDiscard = { navigate(Screens.Back) },
                onDismiss = { openSavePromptDialog.value = false }
            )
        }

        DisposableEffect(Unit) {
            onDispose { actions(SketchPadActions.SketchClosed) }
        }

        // onBackPress, if canvas has new lines drawn or text written, prompt user to save sketch or changes
        if (((paths.isNotEmpty() && paths != sketch?.pathList) ||
                    (texts.isNotEmpty() && texts != sketch?.textList)) &&
            !isFromCollabUrl
        ) {
            BackHandler { openSavePromptDialog.value = true }
        }
    }

    if (chatVisible.value) {
        ChatDialog(
            boardId = boardId,
            viewModel = viewModel,
            onCancel = { chatVisible.value = false },
        )
    }
}
