package tw.edu.teachermcyang.yuuzu_lib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tw.edu.teachermcyang.R

class DialogHelper(
    private val activity: Activity
) {

    // isLoading()
    private var isLoading = false
    @SuppressLint("InflateParams")
    private val loadingView = activity.layoutInflater.inflate(R.layout.custom_dialog_loading, null, false)
    private val loadingAnimation = loadingView.findViewById<LottieAnimationView>(R.id.loading_animation)
    private val loadingDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
        .setView(loadingView)
        .setCancelable(false)
        .setTitle("loading")
        .create()

    fun textInputDialog(title: String, customTextPositiveListener: CustomTextPositiveListener) {
        val textInputView = activity.layoutInflater.inflate(R.layout.custom_dialog_text, null, false)
        val inputText = textInputView.findViewById<TextInputEditText>(R.id.race_Input)
        val textInputDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(textInputView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton(R.string.alert_positive, null)
            .setNegativeButton(R.string.alert_negative) { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
                activity.finish()
            }
            .create()

        textInputDialog.setOnShowListener {
            val positiveButton = textInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                customTextPositiveListener.onPositiveTextClick(it, textInputDialog, inputText)
            }
        }

        textInputDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun inputDialog(title: String, hint: String, customTextListener: CustomTextListener) {
        val textInputView = activity.layoutInflater.inflate(R.layout.custom_dialog_text, null, false)
        val outline = textInputView.findViewById<TextInputLayout>(R.id.raceOutline)
        val inputText = textInputView.findViewById<TextInputEditText>(R.id.race_Input)
        val textInputDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(textInputView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton(R.string.alert_positive, null)
            .setNegativeButton(R.string.alert_negative, null)
            .create()

        outline.hint = hint

        textInputDialog.setOnShowListener {
            val positiveButton = textInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = textInputDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.setOnClickListener {
                customTextListener.onPositiveTextClick(it, textInputDialog, inputText)
            }
            negativeButton.setOnClickListener {
                customTextListener.onNegativeTextClick(it, textInputDialog)
            }
        }

        textInputDialog.show()
    }

    fun dropDownDialog(title: String, autoTextAdapter: ArrayAdapter<String>, customPositiveListener: CustomPositiveListener, onItemClickListener: OnItemClickListener) {
        val dropdownView = activity.layoutInflater.inflate(R.layout.custom_dialog_dropdown, null, false)
        val autoText = dropdownView.findViewById<AutoCompleteTextView>(R.id.customDropDown)
        val dropDownDialog = MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setView(dropdownView)
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton(R.string.alert_positive, null)
            .setNegativeButton(R.string.alert_negative) { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
                activity.finish()
            }
            .create()

        autoText.setAdapter(autoTextAdapter)
        autoText.setOnItemClickListener{ adapterView, view, i, l ->
            onItemClickListener.onItemClick(adapterView, view, i, l)
        }

        dropDownDialog.setOnShowListener {
            val positiveButton = dropDownDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener{
                customPositiveListener.onPositiveClick(it, dropDownDialog)
            }
        }

        dropDownDialog.show()
    }

    fun showDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_positive) { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .show()
    }

    fun showPositiveDialog(title: String, message: String, onPositiveListener: OnPositiveListener) {
        MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_positive) { dialogInterface: DialogInterface?, i: Int ->
                onPositiveListener.onPositiveClick(dialogInterface, i)
            }
            .setNegativeButton(R.string.alert_negative) { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .show()
    }

    fun showFullDialog(title: String, message: String, onDialogListener: OnDialogListener) {
        MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_positive) { dialogInterface: DialogInterface?, i: Int ->
                onDialogListener.onPositiveClick(dialogInterface, i)
            }
            .setNegativeButton(R.string.alert_negative) { dialogInterface: DialogInterface?, i: Int ->
                onDialogListener.onNegativeClick(dialogInterface, i)
            }
            .show()
    }

    fun showCustomButtonDialog(title: String, message: String, positiveText: String, negativeText: String, onDialogListener: OnDialogListener) {
        MaterialAlertDialogBuilder(activity, R.style.Style_CustomDialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveText) { dialogInterface: DialogInterface?, i: Int ->
                onDialogListener.onPositiveClick(dialogInterface, i)
            }
            .setNegativeButton(negativeText) { dialogInterface: DialogInterface?, i: Int ->
                onDialogListener.onNegativeClick(dialogInterface, i)
            }
            .show()
    }

    /**
     * @author Yuuzu
     * @param title: String = "title"
     * @param message: String = "message"
     * @param positiveText: String = "text"
     * @param negativeText: String = "text"
     * @param cancelable: Boolean = "true / false"
     * @param status: SweetAlertDialog.Type = "SweetAlertDialog.SUCCESS_TYPE" example...
     * @param sweetDialogListener: Object {}
     * <p>
    */
    fun sweetBtnDialog(title: String, message: String, positiveText: String, negativeText: String, cancelable: Boolean, status: Int, sweetDialogListener: SweetDialogListener) {
        SweetAlertDialog(activity, status).apply {
            setCancelable(cancelable)
            titleText = title
            contentText = message
            confirmText = positiveText
            cancelText = negativeText
            setConfirmClickListener {
                sweetDialogListener.onPositiveClick(it)
            }
            setCancelClickListener {
                sweetDialogListener.onNegativeClick(it)
            }
        }.show()
    }

    fun sweetDialog(title: String, status: Int, sweetDialogPositiveListener: SweetDialogPositiveListener?) {
        SweetAlertDialog(activity, status).apply {
            titleText = title
            confirmText = activity.getString(R.string.alert_positive)
            setCancelable(false)
            setConfirmClickListener {
                sweetDialogPositiveListener?.onPositiveClick(it)
                it.dismiss()
            }
        }.show()
    }

    fun loadingDialog() {
        isLoading = true
        loadingAnimation.setAnimation(R.raw.loading)
        loadingAnimation.playAnimation()
        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        isLoading = false
        loadingAnimation.cancelAnimation()
        loadingDialog.dismiss()
    }

    fun dialogIsLoading(): Boolean {
        return isLoading
    }

    interface OnPositiveListener {
        fun onPositiveClick(dialogInterface: DialogInterface?, i: Int)
    }

    interface OnDialogListener {
        fun onPositiveClick(dialog: DialogInterface?, which: Int)
        fun onNegativeClick(dialog: DialogInterface?, which: Int)
    }

    interface SweetDialogListener {
        fun onPositiveClick(dialog: SweetAlertDialog)
        fun onNegativeClick(dialog: SweetAlertDialog)
    }

    interface SweetDialogPositiveListener {
        fun onPositiveClick(dialog: SweetAlertDialog)
    }

    interface CustomTextPositiveListener {
        fun onPositiveTextClick(
            var1: View,
            dropDownDialog: androidx.appcompat.app.AlertDialog,
            inputText: TextInputEditText
        )
    }

    interface CustomTextListener {
        fun onPositiveTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
            inputText: TextInputEditText
        )

        fun onNegativeTextClick(
            var1: View,
            dialog: androidx.appcompat.app.AlertDialog,
        )
    }

    interface CustomPositiveListener {
        fun onPositiveClick(var1: View, dropDownDialog: androidx.appcompat.app.AlertDialog)
    }

    interface OnItemClickListener {
        fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long)
    }
}