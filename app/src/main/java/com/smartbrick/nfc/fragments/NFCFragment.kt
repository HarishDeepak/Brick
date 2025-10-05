package com.smartbrick.nfc.fragments

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartbrick.nfc.NFCHandler
import com.smartbrick.nfc.databinding.FragmentNfcBinding
import com.smartbrick.nfc.models.NFCTag

class NFCFragment : Fragment() {

    private var _binding: FragmentNfcBinding? = null
    private val binding get() = _binding!!

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var isWriteMode = false
    private val registeredTags = mutableListOf<NFCTag>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNfcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNFC()
        setupViews()
        loadRegisteredTags()
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        val intent = Intent(requireActivity(), requireActivity().javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            requireContext(), 0, intent, PendingIntent.FLAG_MUTABLE
        )
    }

    private fun setupViews() {
        binding.btnRegisterTag.setOnClickListener {
            startTagRegistration()
        }

        binding.btnTestTag.setOnClickListener {
            startTagTest()
        }

        // Setup RecyclerView for registered tags
        binding.recyclerViewTags.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun startTagRegistration() {
        isWriteMode = true
        binding.statusText.text = "Hold your NFC tag near the phone to register it"
        binding.nfcIcon.text = "📡"

        Toast.makeText(requireContext(), "Ready to register NFC tag", Toast.LENGTH_SHORT).show()
    }

    private fun startTagTest() {
        isWriteMode = false
        binding.statusText.text = "Tap your registered NFC tag to test"
        binding.nfcIcon.text = "🏷️"
    }

    private fun loadRegisteredTags() {
        // Load from SharedPreferences or database
        registeredTags.clear()
        registeredTags.addAll(
            listOf(
                NFCTag("tag1", "Home Tag", "Living Room", "#2196F3", true),
                NFCTag("tag2", "Office Tag", "Desk", "#FF9800", false),
                NFCTag("tag3", "Study Tag", "Library", "#4CAF50", false)
            )
        )

        updateTagList()
    }

    private fun updateTagList() {
        val registeredCount = registeredTags.count { it.isRegistered }
        binding.registeredTagsCount.text = "$registeredCount tags registered"
    }

    fun handleNFCTag(tag: Tag) {
        if (isWriteMode) {
            // Register new tag
            val tagId = "tag_${System.currentTimeMillis()}"
            val success = NFCHandler.writeNFCTag(tag, tagId)

            if (success) {
                val newTag = NFCTag(
                    id = tagId,
                    name = "NFC Tag ${registeredTags.size + 1}",
                    location = "Unknown",
                    color = "#2196F3",
                    isRegistered = true
                )
                registeredTags.add(newTag)
                updateTagList()

                binding.statusText.text = "Tag registered successfully!"
                binding.nfcIcon.text = "✅"

                Toast.makeText(requireContext(), "NFC tag registered!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to register tag", Toast.LENGTH_SHORT).show()
            }

            isWriteMode = false
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            requireActivity(),
            pendingIntent,
            null,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
