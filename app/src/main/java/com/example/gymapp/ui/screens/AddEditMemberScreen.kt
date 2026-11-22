package com.example.gymapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.gymapp.data.Member
import com.example.gymapp.ui.components.MemberAvatar
import com.example.gymapp.util.DateUtils
import com.example.gymapp.util.PhotoStorageHelper
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.StatusPaid
import kotlinx.coroutines.launch
import java.io.File

private enum class ActiveDateField {
    JOIN_DATE,
    FEE_DUE_DATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberScreen(
    memberId: Int?,
    loadMember: suspend (Int) -> Member?,
    onSaveMember: (
        existingId: Int?,
        name: String,
        phone: String,
        photoPath: String?,
        previousPhotoPath: String?,
        joinDate: Long,
        feeAmount: Double,
        feeDueDate: Long,
        onSaved: () -> Unit
    ) -> Unit,
    onDeleteMember: (Member, () -> Unit) -> Unit,
    onMarkFeePaidImmediate: (Member) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = memberId != null && memberId > 0

    var loadedMember by remember { mutableStateOf<Member?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var joinDateMillis by remember { mutableLongStateOf(DateUtils.todayStartMillis()) }
    var feeDueDateMillis by remember { mutableLongStateOf(DateUtils.oneMonthFromTodayMillis()) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var originalPhotoPath by remember { mutableStateOf<String?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var feeError by remember { mutableStateOf<String?>(null) }

    var activeDateField by remember { mutableStateOf<ActiveDateField?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Holds temporary camera capture output file until TakePicture finishes
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(memberId) {
        if (memberId != null && memberId > 0) {
            val existing = loadMember(memberId)
            if (existing != null) {
                loadedMember = existing
                name = existing.name
                phone = existing.phone
                feeText = if (existing.feeAmount % 1.0 == 0.0) {
                    existing.feeAmount.toLong().toString()
                } else {
                    existing.feeAmount.toString()
                }
                joinDateMillis = existing.joinDate
                feeDueDateMillis = existing.feeDueDate
                photoPath = existing.photoPath
                originalPhotoPath = existing.photoPath
            }
        }
    }

    // Zero-permission Android Photo Picker for gallery selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = PhotoStorageHelper.copyUriToInternalStorage(context, uri)
            if (savedPath != null) {
                if (photoPath != null && photoPath != originalPhotoPath) {
                    PhotoStorageHelper.deletePhotoIfExists(photoPath)
                }
                photoPath = savedPath
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Could not save selected photo")
                }
            }
        }
    }

    // Camera TakePicture launcher using FileProvider URI
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val tempFile = pendingCameraFile
        pendingCameraFile = null
        if (success && tempFile != null) {
            val savedPath = PhotoStorageHelper.moveTempFileToInternalStorage(context, tempFile)
            if (savedPath != null) {
                if (photoPath != null && photoPath != originalPhotoPath) {
                    PhotoStorageHelper.deletePhotoIfExists(photoPath)
                }
                photoPath = savedPath
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to store captured photo")
                }
            }
        } else {
            tempFile?.delete()
        }
    }

    fun launchCamera() {
        val (uri, tempFile) = PhotoStorageHelper.createTempCameraImageUri(context)
        pendingCameraFile = tempFile
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            launchCamera()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to take a photo")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (isEditMode) R.string.edit_member else R.string.add_member
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isEditMode && loadedMember != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_member_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_member),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Preview + Picker Controls
                MemberAvatar(
                    name = name,
                    photoPath = photoPath,
                    size = 104.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.testTag("gallery_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.choose_from_gallery))
                    }

                    OutlinedButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.testTag("camera_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.take_photo))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text(stringResource(R.string.field_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = { Text(stringResource(R.string.field_phone)) },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { err -> { Text(err) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Fee Amount
                OutlinedTextField(
                    value = feeText,
                    onValueChange = {
                        feeText = it
                        feeError = null
                    },
                    label = { Text(stringResource(R.string.field_fee_amount)) },
                    isError = feeError != null,
                    supportingText = feeError?.let { err -> { Text(err) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_fee_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Joining Date Picker Field
                DatePickerField(
                    label = stringResource(R.string.field_join_date),
                    value = DateUtils.formatDate(joinDateMillis),
                    onClick = { activeDateField = ActiveDateField.JOIN_DATE },
                    modifier = Modifier.testTag("join_date_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fee Due Date Picker Field
                DatePickerField(
                    label = stringResource(R.string.field_fee_due_date),
                    value = DateUtils.formatDate(feeDueDateMillis),
                    onClick = { activeDateField = ActiveDateField.FEE_DUE_DATE },
                    modifier = Modifier.testTag("fee_due_date_field")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mark fee paid button (moves due date forward by 1 month)
                if (isEditMode && loadedMember != null) {
                    FilledTonalButton(
                        onClick = {
                            val nextDue = DateUtils.addOneMonth(feeDueDateMillis)
                            feeDueDateMillis = nextDue
                            loadedMember?.let { current ->
                                val updated = current.copy(feeDueDate = nextDue)
                                loadedMember = updated
                                onMarkFeePaidImmediate(current)
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Fee paid! Next due date: ${DateUtils.formatDate(nextDue)}"
                                )
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StatusPaid.copy(alpha = 0.14f),
                            contentColor = StatusPaid
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("mark_fee_paid_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.mark_fee_paid))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Save Button
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedPhone = phone.trim()
                        val parsedFee = feeText.trim().toDoubleOrNull()

                        var valid = true
                        if (trimmedName.isEmpty()) {
                            nameError = "Please enter member name"
                            valid = false
                        }
                        if (trimmedPhone.isEmpty()) {
                            phoneError = "Please enter phone number"
                            valid = false
                        }
                        if (parsedFee == null || parsedFee <= 0.0) {
                            feeError = "Enter a valid fee amount"
                            valid = false
                        }

                        if (valid && parsedFee != null) {
                            onSaveMember(
                                memberId,
                                trimmedName,
                                trimmedPhone,
                                photoPath,
                                originalPhotoPath,
                                joinDateMillis,
                                parsedFee,
                                feeDueDateMillis,
                                onNavigateBack
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralFlame
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_member_button")
                ) {
                    Text(text = stringResource(R.string.save_member))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Material 3 Date Picker Dialog
    activeDateField?.let { field ->
        val initialLocalMillis = when (field) {
            ActiveDateField.JOIN_DATE -> joinDateMillis
            ActiveDateField.FEE_DUE_DATE -> feeDueDateMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.localMillisToUtcPickerMillis(initialLocalMillis)
        )

        DatePickerDialog(
            onDismissRequest = { activeDateField = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val localMillis = DateUtils.utcPickerMillisToLocalMillis(utcMillis)
                            when (field) {
                                ActiveDateField.JOIN_DATE -> joinDateMillis = localMillis
                                ActiveDateField.FEE_DUE_DATE -> feeDueDateMillis = localMillis
                            }
                        }
                        activeDateField = null
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateField = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        val currentMember = loadedMember
        if (currentMember != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_confirm_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_confirm_message,
                            currentMember.name
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteMember(currentMember, onNavigateBack)
                        },
                        modifier = Modifier.testTag("confirm_delete_button")
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Select $label"
                )
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}
