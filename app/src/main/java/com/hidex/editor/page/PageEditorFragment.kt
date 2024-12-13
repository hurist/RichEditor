package com.hidex.editor.page

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hidex.editor.EditorMode
import com.hidex.editor.PageEditViewModel
import com.hidex.editor.R
import com.hidex.editor.adapter.FormatAdapter
import com.hidex.editor.base.BaseFragment
import com.hidex.editor.databinding.FragmentPageEditorBinding
import com.hidex.editor.extension.*
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import com.hidex.editor.format.ImageFormat
import com.hidex.editor.model.local.Page
import com.hidex.editor.page.dialogs.InsertIframeDialogFragment
import com.hidex.editor.page.dialogs.InsertImageUrlCaptionDialogFragment
import com.hidex.editor.page.option.InsertImageOptionsFragment
import com.hidex.editor.util.PermissionsHelper

/**
 * @author Sergey Petrov
 */
class PageEditorFragment : BaseFragment() {

    override val layoutRes: Int
        get() = R.layout.fragment_page_editor

    private val pageId: Long? = null
    private val mode: EditorMode = EditorMode.Edit
    private var title: String? = null
    private var authorName: String? = null
    private var authorUrl: String? = null
    private val viewModel by viewModels<PageEditViewModel>()

    private val formatAdapter: FormatAdapter by lazy {
        FormatAdapter(
            mode = mode,
            onFocusItemChanged = { format -> onFocusItemChanged(format) },
            onTextSelected = this::onTextSelected,
            onItemChanged = { onDraftChanged() },
            onPaste = { html -> viewModel.convertHtml(html) }
        )
    }

    private var formatAdapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private val addImageFromStorageDelegate by lazy { AddImageFromStorageDelegate(context) }
    private var _binding: FragmentPageEditorBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authorName = ""
        authorUrl = ""

        viewModel.openPage(pageId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPageEditorBinding.bind(view)
        if (pageId != null) {
            //binding.constraintLayout.transitionName = getString(R.string.page_item_view_shared_element, pageId.toString())
        }

        binding.coordinatorLayout.applySystemWindowInsetsPadding(applyTop = true)
        binding.editorToolbar.root.applySystemWindowInsetsPadding(applyBottom = true)

        binding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = formatAdapter
            setItemViewCacheSize(100)
            itemAnimator = null
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    val lastItem = formatAdapter.items.lastOrNull()
                    if (lastItem == null || lastItem.html != Format.getEmptyHtml(lastItem.type) || lastItem.type == FormatType.HORIZONTAL_RULE) {
                        formatAdapter.addBlockFormatItem(formatAdapter.items.size + 1, Format(FormatType.PARAGRAPH))
                    } else {
                        formatAdapter.requestFocusForFormatItem(lastItem)
                    }
                    binding.recyclerView?.postDelayed({ activity?.showKeyboard() }, 50)
                    return super.onSingleTapUp(e)
                }
            })
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }

        val callBack = object : ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = 0 // ItemTouchHelper.START | ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = source.adapterPosition
                val toPosition = target.adapterPosition

                if (source.itemViewType != FormatAdapter.TYPE_HEADER && toPosition != 0) {
                    formatAdapter.moveBlockFormatItem(fromPosition, toPosition)
                    return true
                }

                return false
            }
        }

        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        formatAdapter.itemTouchHelper = itemTouchHelper
        if (formatAdapterDataObserver != null) {
            formatAdapter.unregisterAdapterDataObserver(requireNotNull(formatAdapterDataObserver))
        } else {
            formatAdapterDataObserver = createFormatAdapterDataObserver()
        }
        formatAdapter.registerAdapterDataObserver(requireNotNull(formatAdapterDataObserver))

        binding.doneImageView.setOnClickListener { doneOnClicked() }
        binding.closeImageView.setOnClickListener {
            Toast.makeText(context, "Close", Toast.LENGTH_SHORT).show()
            //findNavController().popBackStack()
        }

        binding.moreImageView.setOnClickListener {
            Toast.makeText(context, "More", Toast.LENGTH_SHORT).show()
            //presenter.onMoreClicked()
        }

        if (!title.isNullOrBlank()) {
            formatAdapter.pageTitle = title
        }

        setupEditorToolbar()
        setupMode(mode)
        setupAuthor()
        viewModel.pageLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            showPage(it.first, it.second)
        }
    }

    private fun onDraftChanged() {
        Toast.makeText(context, "Draft changed", Toast.LENGTH_SHORT).show()
        // TODO: implement onDraftChanged
        /*presenter.onDraftChanged(
            DraftFields(
                title = getPageTitle(),
                authorName = authorName,
                authorUrl = authorUrl,
                formats = formatAdapter.items
            )
        )*/
    }

    /*fun showMore(page: Page) {
        PageOptionsFragment.newInstance(
            mode = EditorMode.Edit,
            pageId = page.id,
            pagePath = page.path,
            draft = page.draft
        ).also {
            it.publishOption.onClick = { doneOnClicked() }
            it.onDraftDiscardedListener = { presenter.isDraftNeeded = false }
            it.onOnPostDeletedListener = { findNavController().popBackStack() }
        }.show(parentFragmentManager)
    }*/

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            authorName = savedInstanceState.getString(PAGE_AUTHOR_NAME)
            authorUrl = savedInstanceState.getString(PAGE_AUTHOR_URL)
            binding.recyclerView.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable(PAGE_ITEMS))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PAGE_AUTHOR_NAME, authorName)
        outState.putString(PAGE_AUTHOR_URL, authorUrl)
        outState.putParcelable(PAGE_ITEMS, binding.recyclerView.layoutManager?.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.hideKeyboard()
        //presenter.discardDraftPageIfNeeded(getPageTitle(), formatAdapter.items)
        // TODO: implement onDestroy, 清理草稿
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && data != null) {

            when (requestCode) {
                REQUEST_INSERT_IMAGE -> {
                    val images = addImageFromStorageDelegate.convertIntentDataToImageFormats(context, data)

                    images.forEach { image ->
                        addBlockFormatItem(image)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_INSERT_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage(requestCode)
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    fun showProgress(isVisible: Boolean) {
        if (isVisible) {
            showOverlay()
        } else {
            hideOverlay()
        }
    }

    fun showContentProgress(isVisible: Boolean) {
        if (isVisible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    fun onPageSaved() {
        //findNavController().popBackStack()
    }

    // TODO: 缓存加载成功
    fun showPage(page: Page, formats: List<Format>) {
        val state = binding.recyclerView.layoutManager?.onSaveInstanceState()
        with(formatAdapter) {
            pageTitle = page.title
            submitList(formats)
            focusedItem = null
        }
        // it's needed to prevent scroll to bottom https://stackoverflow.com/a/44053550/2271651
        binding.recyclerView.layoutManager?.onRestoreInstanceState(state)
    }

    private fun setupEditorToolbar() {
        binding.editorToolbar.apply {
            boldFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.STRONG) }

            italicFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.ITALIC) }

            underlineFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.UNDERLINE) }

            strikethroughFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.STRIKETHROUGH) }

            linkButton.setOnClickListener {
                linkButton.isChecked = !linkButton.isChecked
                formatAdapter.toggleFormat(FormatType.LINK)
            }

            paragraphButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.PARAGRAPH) }

            quoteFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.QUOTE) }

            insertLineButton.setOnClickListener { formatAdapter.addBlockFormatItem(Format(FormatType.HORIZONTAL_RULE)) }

            headingFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.HEADING) }

            subHeadingFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.SUB_HEADING) }

            unorderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.UNORDERED_LIST) }

            orderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.ORDERED_LIST) }

            insertImageButton.setOnClickListener {
                InsertImageOptionsFragment().apply {
                    fromGalleryOption.onClick = {
                        addImageFromStorageDelegate.showAlert(endAction = { chooseImage(REQUEST_INSERT_IMAGE) })
                    }
                    byUrlOption.onClick = { showImageUrlCaptionDialog(null) }
                }.show(parentFragmentManager)
            }

            insertIframeButton.setOnClickListener {
                InsertIframeDialogFragment().apply {
                    onAddClickListener = { format -> addBlockFormatItem(format) }
                }.show(parentFragmentManager)
            }

            moveUpButton.setOnClickListener {
                val focusedItemPosition = formatAdapter.getPositionForFocusedItem()
                val toPosition = focusedItemPosition - 1
                if (toPosition > 0) {
                    formatAdapter.moveBlockFormatItem(focusedItemPosition, toPosition)
                }
            }

            moveDownButton.setOnClickListener {
                val focusedItemPosition = formatAdapter.getPositionForFocusedItem()
                val toPosition = focusedItemPosition + 1
                if (toPosition > 0 && toPosition < formatAdapter.itemCount) {
                    formatAdapter.moveBlockFormatItem(focusedItemPosition, toPosition)
                }
            }
        }

        onTextSelected()
    }

    private fun doneOnClicked() {
        if (isInputValid()) {
            // TODO: 点击发布
            Toast.makeText(context, "Publish", Toast.LENGTH_SHORT).show()
            /*val builder = MaterialAlertDialogBuilder(context)
            builder.setMessage(R.string.do_you_want_publish)
            builder.setPositiveButton(getString(R.string.publish)) { _, _ ->
                presenter.publishPage(getPageTitle(), authorName, authorUrl, formatAdapter.items)
            }

            builder.setNegativeButton(getString(R.string.save_draft)) { dialog, _ ->
                presenter.savePageDraftIfNeeded(
                    draftFields = DraftFields(
                        title = getPageTitle(),
                        authorName = authorName,
                        authorUrl = authorUrl,
                        formats = formatAdapter.items
                    ),
                    force = true
                )
                dialog.dismiss()
            }
            builder.create().show()*/
        }
    }

    private fun isInputValid(): Boolean {
        val isValid: Boolean
        val pageHeaderViewHolder = formatAdapter.getPageHeaderViewHolder()
        if (pageHeaderViewHolder == null) {
            if (!formatAdapter.isPageTitleValid()) {
                formatAdapter.focusedItem = null
                binding.recyclerView.layoutManager?.scrollToPosition(0)
                binding.recyclerView.postDelayed({ formatAdapter.getPageHeaderViewHolder()?.isValid() }, 200)

                isValid = false
            } else {
                isValid = true
            }
        } else {
            isValid = pageHeaderViewHolder.isValid()
        }
        return isValid
    }

    private fun getPageTitle(): String {
        return formatAdapter.pageTitle.toString()
    }

    private fun setupMode(mode: EditorMode) {
        if (mode == EditorMode.Edit) {
            binding.moreImageView.visibility = View.VISIBLE
        } else {
            (binding.doneImageView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, resources.getDimensionPixelSize(R.dimen.margin_8), 0)
            binding.moreImageView.visibility = View.GONE
        }
    }

    private fun setupAuthor() {
        setupAuthorName(authorName)
        binding.authorLayout.setOnClickListener {
            // TODO: 作者点击
            Toast.makeText(context, "Author", Toast.LENGTH_SHORT).show()
            /*AuthorDialogFragment.newInstance(
                authorName = authorName,
                authorUrl = authorUrl,
                onAddClickListener = { authorName, authorUrl ->
                    this.authorName = authorName
                    this.authorUrl = authorUrl

                    setupAuthorName(authorName)
                }
            ).show(parentFragmentManager)*/
        }
    }

    private fun setupAuthorName(authorName: String?) {
        if (authorName.isNullOrBlank()) {
            binding.authorNameTextView.text = "add name"
        } else {
            binding.authorNameTextView.text = authorName
            binding.authorNameTextView.setTextColor(context.getColorFromAttr(R.attr.colorAccent))
        }
    }

    private fun chooseImage(requestCode: Int) {
        if (PermissionsHelper.checkPermissions(requireActivity(), requestCode, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            addImageFromStorageDelegate.startActivityForResult(context, requestCode)
        }
    }

    private fun showImageUrlCaptionDialog(url: String?) {
        InsertImageUrlCaptionDialogFragment.newInstance(url,
            onAddClickListener = { imageUrl, caption -> addBlockFormatItem(ImageFormat(imageUrl, caption)) }
        ).show(parentFragmentManager)
    }

    private fun addBlockFormatItem(format: Format) {
        formatAdapter.addBlockFormatItem(format)
    }

    private fun onTextSelected(selected: Boolean = false, format: Format? = null, formatTypes: List<FormatType> = emptyList()) {
        binding.editorToolbar.apply {
            boldFormatButton.isChecked = formatTypes.contains(FormatType.BOLD)
            italicFormatButton.isChecked = formatTypes.contains(FormatType.ITALIC)
            strikethroughFormatButton.isChecked = formatTypes.contains(FormatType.STRIKETHROUGH)
            underlineFormatButton.isChecked = formatTypes.contains(FormatType.UNDERLINE)
            linkButton.isChecked = formatTypes.contains(FormatType.LINK)

            boldFormatButton.setGone(!selected)
            italicFormatButton.setGone(!selected)
            strikethroughFormatButton.setGone(!selected)
            underlineFormatButton.setGone(!selected)
            linkButton.setGone(!selected)

            paragraphButton.setGone(selected)
            headingFormatButton.setGone(selected)
            subHeadingFormatButton.setGone(selected)
            quoteFormatButton.setGone(selected)
            unorderedListFormatButton.setGone(selected)
            orderedListFormatButton.setGone(selected)
            insertImageButton.setGone(selected)
            insertLineButton.setGone(selected)
            insertIframeButton.setGone(selected)
            moveUpButton.setGone(selected)
            moveDownButton.setGone(selected)

            if (format != null) {
                italicFormatButton.enable()
                boldFormatButton.enable()
                italicFormatButton.enable()
                strikethroughFormatButton.enable()
                underlineFormatButton.enable()

                when (format.type) {
                    FormatType.QUOTE, FormatType.ASIDE -> italicFormatButton.disable()
                    else -> {
                        Log.d("onTextSelected", "format.type: ${format.type} not handled")
                    }
                }
            }
        }
    }

    private fun onFocusItemChanged(format: Format?) {
        enableEditorToolbar(format != null || formatAdapter.items.isEmpty())
        binding.editorToolbar.apply {
            paragraphButton.isChecked = format?.type == FormatType.PARAGRAPH
            headingFormatButton.isChecked = format?.type == FormatType.HEADING
            subHeadingFormatButton.isChecked = format?.type == FormatType.SUB_HEADING
            quoteFormatButton.isChecked = format?.type == FormatType.QUOTE
            unorderedListFormatButton.isChecked = format?.type == FormatType.UNORDERED_LIST
            orderedListFormatButton.isChecked = format?.type == FormatType.ORDERED_LIST
        }
    }

    private fun enableEditorToolbar(isEnabled: Boolean) {
        binding.editorToolbar.apply {
            for (i in 0 until optionsLayout.childCount) {
                optionsLayout.getChildAt(i).run {
                    isClickable = isEnabled
                    isFocusable = isEnabled
                    if (isEnabled) {
                        enable()
                    } else {
                        disable()
                    }
                }
            }
        }
    }

    private fun createFormatAdapterDataObserver(): RecyclerView.AdapterDataObserver {
        return object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                onDraftChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                onChanged()
            }
        }
    }


    companion object {
        const val PAGE_AUTHOR_NAME = "PAGE_AUTHOR_NAME"
        const val PAGE_AUTHOR_URL = "PAGE_AUTHOR_URL"
        const val PAGE_ITEMS = "PAGE_ITEMS"

        private const val REQUEST_INSERT_IMAGE = 101
    }
}